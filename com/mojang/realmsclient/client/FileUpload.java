package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.RealmsVersion;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.gui.screens.UploadResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUpload {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int MAX_RETRIES = 5;
   private static final String UPLOAD_PATH = "/upload";
   private final File file;
   private final long worldId;
   private final int slotId;
   private final UploadInfo uploadInfo;
   private final String sessionId;
   private final String username;
   private final String clientVersion;
   private final UploadStatus uploadStatus;
   private AtomicBoolean cancelled = new AtomicBoolean(false);
   private CompletableFuture<UploadResult> uploadTask;
   private final RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L))
      .setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L))
      .build();

   public FileUpload(
      File file, long worldId, int slotId, UploadInfo uploadInfo, String sessionId, String username, String clientVersion, UploadStatus uploadStatus
   ) {
      this.file = file;
      this.worldId = worldId;
      this.slotId = slotId;
      this.uploadInfo = uploadInfo;
      this.sessionId = sessionId;
      this.username = username;
      this.clientVersion = clientVersion;
      this.uploadStatus = uploadStatus;
   }

   public void upload(Consumer<UploadResult> callback) {
      if (this.uploadTask == null) {
         this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0));
         this.uploadTask.thenAccept(callback);
      }
   }

   public void cancel() {
      this.cancelled.set(true);
      if (this.uploadTask != null) {
         this.uploadTask.cancel(false);
         this.uploadTask = null;
      }

   }

   private UploadResult requestUpload(int currentAttempt) {
      UploadResult.Builder uploadResultBuilder = new UploadResult.Builder();
      if (this.cancelled.get()) {
         return uploadResultBuilder.build();
      } else {
         this.uploadStatus.totalBytes = this.file.length();
         HttpPost request = new HttpPost(
            "http://" + this.uploadInfo.getUploadEndpoint() + ":" + this.uploadInfo.getPort() + "/upload" + "/" + this.worldId + "/" + this.slotId
         );
         CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();

         UploadResult var8;
         try {
            this.setupRequest(request);
            HttpResponse response = client.execute(request);
            long retryDelaySeconds = this.getRetryDelaySeconds(response);
            if (!this.shouldRetry(retryDelaySeconds, currentAttempt)) {
               this.handleResponse(response, uploadResultBuilder);
               return uploadResultBuilder.build();
            }

            var8 = this.retryUploadAfter(retryDelaySeconds, currentAttempt);
         } catch (Exception var12) {
            if (!this.cancelled.get()) {
               LOGGER.error("Caught exception while uploading: ", var12);
            }

            return uploadResultBuilder.build();
         } finally {
            this.cleanup(request, client);
         }

         return var8;
      }
   }

   private void cleanup(HttpPost request, CloseableHttpClient client) {
      request.releaseConnection();
      if (client != null) {
         try {
            client.close();
         } catch (IOException var4) {
            LOGGER.error("Failed to close Realms upload client");
         }
      }

   }

   private void setupRequest(HttpPost request) throws FileNotFoundException {
      String realmsVersion = RealmsVersion.getVersion();
      if (realmsVersion != null) {
         request.setHeader(
            "Cookie",
            "sid="
               + this.sessionId
               + ";token="
               + this.uploadInfo.getToken()
               + ";user="
               + this.username
               + ";version="
               + this.clientVersion
               + ";realms_version="
               + realmsVersion
         );
      } else {
         request.setHeader(
            "Cookie", "sid=" + this.sessionId + ";token=" + this.uploadInfo.getToken() + ";user=" + this.username + ";version=" + this.clientVersion
         );
      }

      FileUpload.CustomInputStreamEntity entity = new FileUpload.CustomInputStreamEntity(new FileInputStream(this.file), this.file.length(), this.uploadStatus);
      entity.setContentType("application/octet-stream");
      request.setEntity(entity);
   }

   private void handleResponse(HttpResponse response, UploadResult.Builder uploadResultBuilder) throws IOException {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 401) {
         LOGGER.debug("Realms server returned 401: " + response.getFirstHeader("WWW-Authenticate"));
      }

      uploadResultBuilder.withStatusCode(statusCode);
      if (response.getEntity() != null) {
         String json = EntityUtils.toString(response.getEntity(), "UTF-8");
         if (json != null) {
            try {
               JsonParser parser = new JsonParser();
               JsonElement errorMsgElement = parser.parse(json).getAsJsonObject().get("errorMsg");
               Optional<String> errorMessage = Optional.ofNullable(errorMsgElement).map(JsonElement::getAsString);
               uploadResultBuilder.withErrorMessage((String)errorMessage.orElse(null));
            } catch (Exception var8) {
            }
         }
      }

   }

   private boolean shouldRetry(long retryDelaySeconds, int currentAttempt) {
      return retryDelaySeconds > 0L && currentAttempt + 1 < 5;
   }

   private UploadResult retryUploadAfter(long retryDelaySeconds, int currentAttempt) throws InterruptedException {
      Thread.sleep(Duration.ofSeconds(retryDelaySeconds).toMillis());
      return this.requestUpload(currentAttempt + 1);
   }

   private long getRetryDelaySeconds(HttpResponse response) {
      return Optional.ofNullable(response.getFirstHeader("Retry-After")).map(Header::getValue).map(Long::valueOf).orElse(0L);
   }

   public boolean isFinished() {
      return this.uploadTask.isDone() || this.uploadTask.isCancelled();
   }

   private static class CustomInputStreamEntity extends InputStreamEntity {
      private final long length;
      private final InputStream content;
      private final UploadStatus uploadStatus;

      public CustomInputStreamEntity(InputStream content, long length, UploadStatus uploadStatus) {
         super(content);
         this.content = content;
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
