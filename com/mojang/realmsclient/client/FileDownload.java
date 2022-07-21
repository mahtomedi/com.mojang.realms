package com.mojang.realmsclient.client;

import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileDownload {
   private static final Logger LOGGER = LogManager.getLogger();
   private volatile boolean cancelled = false;
   private volatile boolean finished = false;
   private volatile boolean error = false;
   private volatile boolean extracting = false;
   private volatile File tempFile;
   private volatile HttpGet request;
   private Thread currentThread;
   private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{
      "CON",
      "COM",
      "PRN",
      "AUX",
      "CLOCK$",
      "NUL",
      "COM1",
      "COM2",
      "COM3",
      "COM4",
      "COM5",
      "COM6",
      "COM7",
      "COM8",
      "COM9",
      "LPT1",
      "LPT2",
      "LPT3",
      "LPT4",
      "LPT5",
      "LPT6",
      "LPT7",
      "LPT8",
      "LPT9"
   };

   public long contentLength(String downloadLink) {
      CloseableHttpClient client = null;
      HttpGet httpGet = null;

      long var5;
      try {
         httpGet = new HttpGet(downloadLink);
         client = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse response = client.execute(httpGet);
         return Long.parseLong(response.getFirstHeader("Content-Length").getValue());
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpGet != null) {
            httpGet.releaseConnection();
         }

         if (client != null) {
            try {
               client.close();
            } catch (IOException var15) {
               LOGGER.error("Could not close http client", var15);
            }
         }

      }

      return var5;
   }

   public void download(
      final String downloadLink,
      final String worldName,
      final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus,
      final RealmsAnvilLevelStorageSource levelStorageSource
   ) {
      if (this.currentThread == null) {
         this.currentThread = new Thread() {
            public void run() {
               CloseableHttpClient client = null;

               try {
                  FileDownload.this.tempFile = File.createTempFile("backup", ".tar.gz");
                  FileDownload.this.request = new HttpGet(downloadLink);
                  client = HttpClientBuilder.create().setDefaultRequestConfig(FileDownload.this.requestConfig).build();
                  HttpResponse response = client.execute(FileDownload.this.request);
                  downloadStatus.totalBytes = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
                  if (response.getStatusLine().getStatusCode() == 200) {
                     OutputStream os = new FileOutputStream(FileDownload.this.tempFile);
                     FileDownload.ProgressListener progressListener = FileDownload.this.new ProgressListener(
                        worldName.trim(), FileDownload.this.tempFile, levelStorageSource, downloadStatus
                     );
                     FileDownload.DownloadCountingOutputStream dcount = FileDownload.this.new DownloadCountingOutputStream(os);
                     dcount.setListener(progressListener);
                     IOUtils.copy(response.getEntity().getContent(), dcount);
                     return;
                  }

                  FileDownload.this.error = true;
                  FileDownload.this.request.abort();
               } catch (Exception var15) {
                  FileDownload.LOGGER.error("Caught exception while downloading: " + var15.getMessage());
                  FileDownload.this.error = true;
                  return;
               } finally {
                  FileDownload.this.request.releaseConnection();
                  if (FileDownload.this.tempFile != null) {
                     FileDownload.this.tempFile.delete();
                  }

                  if (client != null) {
                     try {
                        client.close();
                     } catch (IOException var14) {
                        FileDownload.LOGGER.error("Failed to close Realms download client");
                     }
                  }

               }

            }
         };
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.request != null) {
         this.request.abort();
      }

      if (this.tempFile != null) {
         this.tempFile.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String folder) {
      folder = folder.replaceAll("[\\./\"]", "_");

      for(String invalidName : INVALID_FILE_NAMES) {
         if (folder.equalsIgnoreCase(invalidName)) {
            folder = "_" + folder + "_";
         }
      }

      return folder;
   }

   private void untarGzipArchive(String name, File file, RealmsAnvilLevelStorageSource levelStorageSource) throws IOException {
      Pattern namePattern = Pattern.compile(".*-([0-9]+)$");
      int number = 1;

      for(char replacer : RealmsSharedConstants.ILLEGAL_FILE_CHARACTERS) {
         name = name.replace(replacer, '_');
      }

      if (StringUtils.isEmpty(name)) {
         name = "Realm";
      }

      name = findAvailableFolderName(name);

      try {
         for(RealmsLevelSummary summary : levelStorageSource.getLevelList()) {
            if (summary.getLevelId().toLowerCase().startsWith(name.toLowerCase())) {
               Matcher matcher = namePattern.matcher(summary.getLevelId());
               if (matcher.matches()) {
                  if (Integer.valueOf(matcher.group(1)) > number) {
                     number = Integer.valueOf(matcher.group(1));
                  }
               } else {
                  ++number;
               }
            }
         }
      } catch (Exception var21) {
         this.error = true;
         return;
      }

      String finalName;
      if (levelStorageSource.isNewLevelIdAcceptable(name) && number <= 1) {
         finalName = name;
      } else {
         finalName = name + (number == 1 ? "" : "-" + number);
         if (!levelStorageSource.isNewLevelIdAcceptable(finalName)) {
            boolean foundName = false;

            while(!foundName) {
               ++number;
               finalName = name + (number == 1 ? "" : "-" + number);
               if (levelStorageSource.isNewLevelIdAcceptable(finalName)) {
                  foundName = true;
               }
            }
         }
      }

      TarArchiveInputStream tarIn = null;
      File saves = new File(Realms.getGameDirectoryPath(), "saves");

      try {
         saves.mkdir();
         tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));

         for(TarArchiveEntry tarEntry = tarIn.getNextTarEntry(); tarEntry != null; tarEntry = tarIn.getNextTarEntry()) {
            File destPath = new File(saves, tarEntry.getName().replace("world", finalName));
            if (tarEntry.isDirectory()) {
               destPath.mkdirs();
            } else {
               destPath.createNewFile();
               byte[] btoRead = new byte[1024];
               BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
               int len = 0;

               while((len = tarIn.read(btoRead)) != -1) {
                  bout.write(btoRead, 0, len);
               }

               bout.close();
               Object var31 = null;
            }
         }
      } catch (Exception var19) {
         this.error = true;
      } finally {
         if (tarIn != null) {
            tarIn.close();
         }

         if (file != null) {
            file.delete();
         }

         levelStorageSource.renameLevel(finalName, finalName.trim());
         this.finished = true;
      }

   }

   private class DownloadCountingOutputStream extends CountingOutputStream {
      private ActionListener listener = null;

      public DownloadCountingOutputStream(OutputStream out) {
         super(out);
      }

      public void setListener(ActionListener listener) {
         this.listener = listener;
      }

      protected void afterWrite(int n) throws IOException {
         super.afterWrite(n);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, null));
         }

      }
   }

   private class ProgressListener implements ActionListener {
      private volatile String worldName;
      private volatile File tempFile;
      private volatile RealmsAnvilLevelStorageSource levelStorageSource;
      private volatile RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

      private ProgressListener(
         String worldName, File tempFile, RealmsAnvilLevelStorageSource levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus
      ) {
         this.worldName = worldName;
         this.tempFile = tempFile;
         this.levelStorageSource = levelStorageSource;
         this.downloadStatus = downloadStatus;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var3) {
               FileDownload.this.error = true;
            }
         }

      }
   }
}
