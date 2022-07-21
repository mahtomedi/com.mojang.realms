package com.mojang.realmsclient.client;

import com.mojang.realmsclient.dto.UploadInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUpload {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String UPLOAD_PATH = "/upload";
   private static final String PORT = "8080";
   private volatile boolean cancelled = false;
   private volatile boolean finished = false;
   private HttpPost request;
   private int statusCode = -1;
   private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();

   public void upload(
      final File file,
      final long worldId,
      final UploadInfo uploadInfo,
      final String sessionId,
      final String username,
      final String clientVersion,
      final UploadStatus uploadStatus
   ) {
      (new Thread() {
            public void run() {
               FileUpload.this.request = new HttpPost("http://" + uploadInfo.getUploadEndpoint() + ":" + "8080" + "/upload" + "/" + worldId);
               CloseableHttpClient client = null;
   
               try {
                  client = HttpClientBuilder.create().setDefaultRequestConfig(FileUpload.this.requestConfig).build();
                  FileUpload.this.request
                     .setHeader("Cookie", "sid=" + sessionId + ";token=" + uploadInfo.getToken() + ";user=" + username + ";version=" + clientVersion);
                  uploadStatus.totalBytes = file.length();
                  FileUpload.CustomInputStreamEntity entity = new FileUpload.CustomInputStreamEntity(new FileInputStream(file), file.length(), uploadStatus);
                  entity.setContentType("application/octet-stream");
                  FileUpload.this.request.setEntity(entity);
                  HttpResponse response = client.execute(FileUpload.this.request);
                  int statusCode = response.getStatusLine().getStatusCode();
                  if (statusCode == 401) {
                     FileUpload.LOGGER.debug("Realms server returned 401: " + response.getFirstHeader("WWW-Authenticate"));
                  }
   
                  FileUpload.this.statusCode = statusCode;
               } catch (Exception var13) {
                  FileUpload.LOGGER.debug("Caught exception while uploading: " + var13.getMessage());
               } finally {
                  FileUpload.this.request.releaseConnection();
                  FileUpload.this.finished = true;
                  if (client != null) {
                     try {
                        client.close();
                     } catch (IOException var12) {
                        FileUpload.LOGGER.debug("Failed to close Realms upload client");
                     }
                  }
   
               }
   
            }
         })
         .start();
   }

   public void cancel() {
      this.cancelled = true;
      if (this.request != null) {
         this.request.abort();
      }

   }

   public boolean isFinished() {
      return this.finished;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   private static class CustomInputStreamEntity extends InputStreamEntity {
      private final long length;
      private final InputStream content;
      private final UploadStatus uploadStatus;

      public CustomInputStreamEntity(InputStream instream, long length, UploadStatus uploadStatus) {
         super(instream);
         this.content = instream;
         this.length = length;
         this.uploadStatus = uploadStatus;
      }

      public void writeTo(OutputStream outstream) throws IOException {
         Args.notNull(outstream, "Output stream");
         InputStream instream = this.content;

         try {
            byte[] buffer = new byte[4096];
            int l;
            if (this.length < 0L) {
               while((l = instream.read(buffer)) != -1) {
                  outstream.write(buffer, 0, l);
                  UploadStatus var12 = this.uploadStatus;
                  var12.bytesWritten = var12.bytesWritten + (long)l;
               }
            } else {
               long remaining = this.length;

               while(remaining > 0L) {
                  l = instream.read(buffer, 0, (int)Math.min(4096L, remaining));
                  if (l == -1) {
                     break;
                  }

                  outstream.write(buffer, 0, l);
                  UploadStatus var7 = this.uploadStatus;
                  var7.bytesWritten = var7.bytesWritten + (long)l;
                  remaining -= (long)l;
                  outstream.flush();
               }
            }
         } finally {
            instream.close();
         }

      }
   }
}
