package realms;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.UploadInfo;
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

public class d {
   private static final Logger a = LogManager.getLogger();
   private final File b;
   private final long c;
   private final int d;
   private final UploadInfo e;
   private final String f;
   private final String g;
   private final String h;
   private final k i;
   private AtomicBoolean j = new AtomicBoolean(false);
   private CompletableFuture<bb> k;
   private final RequestConfig l = RequestConfig.custom()
      .setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L))
      .setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L))
      .build();

   public d(File file, long worldId, int slotId, UploadInfo uploadInfo, String sessionId, String username, String clientVersion, k uploadStatus) {
      this.b = file;
      this.c = worldId;
      this.d = slotId;
      this.e = uploadInfo;
      this.f = sessionId;
      this.g = username;
      this.h = clientVersion;
      this.i = uploadStatus;
   }

   public void a(Consumer<bb> callback) {
      if (this.k == null) {
         this.k = CompletableFuture.supplyAsync(() -> this.a(0));
         this.k.thenAccept(callback);
      }
   }

   public void a() {
      this.j.set(true);
      if (this.k != null) {
         this.k.cancel(false);
         this.k = null;
      }

   }

   private bb a(int currentAttempt) {
      bb.a uploadResultBuilder = new bb.a();
      if (this.j.get()) {
         return uploadResultBuilder.a();
      } else {
         this.i.b = this.b.length();
         HttpPost request = new HttpPost("http://" + this.e.getUploadEndpoint() + ":" + this.e.getPort() + "/upload" + "/" + this.c + "/" + this.d);
         CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(this.l).build();

         bb var8;
         try {
            this.a(request);
            HttpResponse response = client.execute(request);
            long retryDelaySeconds = this.a(response);
            if (!this.a(retryDelaySeconds, currentAttempt)) {
               this.a(response, uploadResultBuilder);
               return uploadResultBuilder.a();
            }

            var8 = this.b(retryDelaySeconds, currentAttempt);
         } catch (Exception var12) {
            if (!this.j.get()) {
               a.error("Caught exception while uploading: ", var12);
            }

            return uploadResultBuilder.a();
         } finally {
            this.a(request, client);
         }

         return var8;
      }
   }

   private void a(HttpPost request, CloseableHttpClient client) {
      request.releaseConnection();
      if (client != null) {
         try {
            client.close();
         } catch (IOException var4) {
            a.error("Failed to close Realms upload client");
         }
      }

   }

   private void a(HttpPost request) throws FileNotFoundException {
      String realmsVersion = bd.a();
      if (realmsVersion != null) {
         request.setHeader(
            "Cookie", "sid=" + this.f + ";token=" + this.e.getToken() + ";user=" + this.g + ";version=" + this.h + ";realms_version=" + realmsVersion
         );
      } else {
         request.setHeader("Cookie", "sid=" + this.f + ";token=" + this.e.getToken() + ";user=" + this.g + ";version=" + this.h);
      }

      d.a entity = new d.a(new FileInputStream(this.b), this.b.length(), this.i);
      entity.setContentType("application/octet-stream");
      request.setEntity(entity);
   }

   private void a(HttpResponse response, bb.a uploadResultBuilder) throws IOException {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 401) {
         a.debug("Realms server returned 401: " + response.getFirstHeader("WWW-Authenticate"));
      }

      uploadResultBuilder.a(statusCode);
      if (response.getEntity() != null) {
         String json = EntityUtils.toString(response.getEntity(), "UTF-8");
         if (json != null) {
            try {
               JsonParser parser = new JsonParser();
               JsonElement errorMsgElement = parser.parse(json).getAsJsonObject().get("errorMsg");
               Optional<String> errorMessage = Optional.ofNullable(errorMsgElement).map(JsonElement::getAsString);
               uploadResultBuilder.a((String)errorMessage.orElse(null));
            } catch (Exception var8) {
            }
         }
      }

   }

   private boolean a(long retryDelaySeconds, int currentAttempt) {
      return retryDelaySeconds > 0L && currentAttempt + 1 < 5;
   }

   private bb b(long retryDelaySeconds, int currentAttempt) throws InterruptedException {
      Thread.sleep(Duration.ofSeconds(retryDelaySeconds).toMillis());
      return this.a(currentAttempt + 1);
   }

   private long a(HttpResponse response) {
      return Optional.ofNullable(response.getFirstHeader("Retry-After")).map(Header::getValue).map(Long::valueOf).orElse(0L);
   }

   public boolean b() {
      return this.k.isDone() || this.k.isCancelled();
   }

   static class a extends InputStreamEntity {
      private final long a;
      private final InputStream b;
      private final k c;

      public a(InputStream content, long length, k uploadStatus) {
         super(content);
         this.b = content;
         this.a = length;
         this.c = uploadStatus;
      }

      public void writeTo(OutputStream outstream) throws IOException {
         Args.notNull(outstream, "Output stream");
         InputStream instream = this.b;

         try {
            byte[] buffer = new byte[4096];
            int l;
            if (this.a < 0L) {
               while((l = instream.read(buffer)) != -1) {
                  outstream.write(buffer, 0, l);
                  k var12 = this.c;
                  var12.a = var12.a + (long)l;
               }
            } else {
               long remaining = this.a;

               while(remaining > 0L) {
                  l = instream.read(buffer, 0, (int)Math.min(4096L, remaining));
                  if (l == -1) {
                     break;
                  }

                  outstream.write(buffer, 0, l);
                  k var7 = this.c;
                  var7.a = var7.a + (long)l;
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
