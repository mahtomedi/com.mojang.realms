package realms;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.realmsclient.dto.WorldDownload;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
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

public class c {
   private static final Logger a = LogManager.getLogger();
   private volatile boolean b;
   private volatile boolean c;
   private volatile boolean d;
   private volatile boolean e;
   private volatile File f;
   private volatile File g;
   private volatile HttpGet h;
   private Thread i;
   private final RequestConfig j = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] k = new String[]{
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

   public long a(String downloadLink) {
      CloseableHttpClient client = null;
      HttpGet httpGet = null;

      long var5;
      try {
         httpGet = new HttpGet(downloadLink);
         client = HttpClientBuilder.create().setDefaultRequestConfig(this.j).build();
         CloseableHttpResponse response = client.execute(httpGet);
         return Long.parseLong(response.getFirstHeader("Content-Length").getValue());
      } catch (Throwable var16) {
         a.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpGet != null) {
            httpGet.releaseConnection();
         }

         if (client != null) {
            try {
               client.close();
            } catch (IOException var15) {
               a.error("Could not close http client", var15);
            }
         }

      }

      return var5;
   }

   public void a(final WorldDownload worldDownload, final String worldName, final ag.a downloadStatus, final RealmsAnvilLevelStorageSource levelStorageSource) {
      if (this.i == null) {
         this.i = new Thread() {
            public void run() {
               CloseableHttpClient client = null;

               try {
                  c.this.f = File.createTempFile("backup", ".tar.gz");
                  c.this.h = new HttpGet(worldDownload.downloadLink);
                  client = HttpClientBuilder.create().setDefaultRequestConfig(c.this.j).build();
                  HttpResponse response = client.execute(c.this.h);
                  downloadStatus.b = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
                  if (response.getStatusLine().getStatusCode() == 200) {
                     OutputStream os = new FileOutputStream(c.this.f);
                     realms.c.b progressListener = c.this.new b(worldName.trim(), c.this.f, levelStorageSource, downloadStatus, worldDownload);
                     realms.c.a dcount = c.this.new a(os);
                     dcount.a(progressListener);
                     IOUtils.copy(response.getEntity().getContent(), dcount);
                     return;
                  }

                  c.this.d = true;
                  c.this.h.abort();
               } catch (Exception var89) {
                  realms.c.a.error("Caught exception while downloading: " + var89.getMessage());
                  c.this.d = true;
                  return;
               } finally {
                  c.this.h.releaseConnection();
                  if (c.this.f != null) {
                     c.this.f.delete();
                  }

                  if (!c.this.d) {
                     if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
                        try {
                           c.this.f = File.createTempFile("resources", ".tar.gz");
                           c.this.h = new HttpGet(worldDownload.resourcePackUrl);
                           HttpResponse response = client.execute(c.this.h);
                           downloadStatus.b = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
                           if (response.getStatusLine().getStatusCode() != 200) {
                              c.this.d = true;
                              c.this.h.abort();
                              return;
                           }

                           OutputStream os = new FileOutputStream(c.this.f);
                           realms.c.c progressListener = c.this.new c(c.this.f, downloadStatus, worldDownload);
                           realms.c.a dcount = c.this.new a(os);
                           dcount.a(progressListener);
                           IOUtils.copy(response.getEntity().getContent(), dcount);
                        } catch (Exception var87) {
                           realms.c.a.error("Caught exception while downloading: " + var87.getMessage());
                           c.this.d = true;
                        } finally {
                           c.this.h.releaseConnection();
                           if (c.this.f != null) {
                              c.this.f.delete();
                           }

                        }
                     } else {
                        c.this.c = true;
                     }
                  }

                  if (client != null) {
                     try {
                        client.close();
                     } catch (IOException var86) {
                        realms.c.a.error("Failed to close Realms download client");
                     }
                  }

               }

            }
         };
         this.i.setUncaughtExceptionHandler(new m(a));
         this.i.start();
      }
   }

   public void a() {
      if (this.h != null) {
         this.h.abort();
      }

      if (this.f != null) {
         this.f.delete();
      }

      this.b = true;
   }

   public boolean b() {
      return this.c;
   }

   public boolean c() {
      return this.d;
   }

   public boolean d() {
      return this.e;
   }

   public static String b(String folder) {
      folder = folder.replaceAll("[\\./\"]", "_");

      for(String invalidName : k) {
         if (folder.equalsIgnoreCase(invalidName)) {
            folder = "_" + folder + "_";
         }
      }

      return folder;
   }

   private void a(String name, File file, RealmsAnvilLevelStorageSource levelStorageSource) throws IOException {
      Pattern namePattern = Pattern.compile(".*-([0-9]+)$");
      int number = 1;

      for(char replacer : RealmsSharedConstants.ILLEGAL_FILE_CHARACTERS) {
         name = name.replace(replacer, '_');
      }

      if (StringUtils.isEmpty(name)) {
         name = "Realm";
      }

      name = b(name);

      try {
         for(RealmsLevelSummary summary : levelStorageSource.getLevelList()) {
            if (summary.getLevelId().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) {
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
      } catch (Exception var22) {
         a.error("Error getting level list", var22);
         this.d = true;
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
               Object var32 = null;
            }
         }
      } catch (Exception var20) {
         a.error("Error extracting world", var20);
         this.d = true;
      } finally {
         if (tarIn != null) {
            tarIn.close();
         }

         if (file != null) {
            file.delete();
         }

         levelStorageSource.renameLevel(finalName, finalName.trim());
         File dataFile = new File(saves, finalName + File.separator + "level.dat");
         Realms.deletePlayerTag(dataFile);
         this.g = new File(saves, finalName + File.separator + "resources.zip");
      }

   }

   class a extends CountingOutputStream {
      private ActionListener b;

      public a(OutputStream out) {
         super(out);
      }

      public void a(ActionListener listener) {
         this.b = listener;
      }

      protected void afterWrite(int n) throws IOException {
         super.afterWrite(n);
         if (this.b != null) {
            this.b.actionPerformed(new ActionEvent(this, 0, null));
         }

      }
   }

   class b implements ActionListener {
      private final String b;
      private final File c;
      private final RealmsAnvilLevelStorageSource d;
      private final ag.a e;
      private final WorldDownload f;

      private b(String worldName, File tempFile, RealmsAnvilLevelStorageSource levelStorageSource, ag.a downloadStatus, WorldDownload worldDownload) {
         this.b = worldName;
         this.c = tempFile;
         this.d = levelStorageSource;
         this.e = downloadStatus;
         this.f = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.e.a = ((realms.c.a)e.getSource()).getByteCount();
         if (this.e.a >= this.e.b && !c.this.b && !c.this.d) {
            try {
               c.this.e = true;
               c.this.a(this.b, this.c, this.d);
            } catch (IOException var3) {
               realms.c.a.error("Error extracting archive", var3);
               c.this.d = true;
            }
         }

      }
   }

   class c implements ActionListener {
      private final File b;
      private final ag.a c;
      private final WorldDownload d;

      private c(File tempFile, ag.a downloadStatus, WorldDownload worldDownload) {
         this.b = tempFile;
         this.c = downloadStatus;
         this.d = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.c.a = ((realms.c.a)e.getSource()).getByteCount();
         if (this.c.a >= this.c.b && !c.this.b) {
            try {
               String actualHash = Hashing.sha1().hashBytes(Files.toByteArray(this.b)).toString();
               if (actualHash.equals(this.d.resourcePackHash)) {
                  FileUtils.copyFile(this.b, c.this.g);
                  c.this.c = true;
               } else {
                  realms.c.a.error("Resourcepack had wrong hash (expected " + this.d.resourcePackHash + ", found " + actualHash + "). Deleting it.");
                  FileUtils.deleteQuietly(this.b);
                  c.this.d = true;
               }
            } catch (IOException var3) {
               realms.c.a.error("Error copying resourcepack file", var3.getMessage());
               c.this.d = true;
            }
         }

      }
   }
}
