package realms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

public abstract class j<T extends j<T>> {
   protected HttpURLConnection a;
   private boolean c;
   protected String b;

   public j(String url, int connectTimeout, int readTimeout) {
      try {
         this.b = url;
         Proxy proxy = h.a();
         if (proxy != null) {
            this.a = (HttpURLConnection)new URL(url).openConnection(proxy);
         } else {
            this.a = (HttpURLConnection)new URL(url).openConnection();
         }

         this.a.setConnectTimeout(connectTimeout);
         this.a.setReadTimeout(readTimeout);
      } catch (MalformedURLException var5) {
         throw new n(var5.getMessage(), var5);
      } catch (IOException var6) {
         throw new n(var6.getMessage(), var6);
      }
   }

   public void a(String key, String value) {
      a(this.a, key, value);
   }

   public static void a(HttpURLConnection connection, String key, String value) {
      String cookie = connection.getRequestProperty("Cookie");
      if (cookie == null) {
         connection.setRequestProperty("Cookie", key + "=" + value);
      } else {
         connection.setRequestProperty("Cookie", cookie + ";" + key + "=" + value);
      }

   }

   public T b(String name, String value) {
      this.a.addRequestProperty(name, value);
      return (T)this;
   }

   public int a() {
      return a(this.a);
   }

   public static int a(HttpURLConnection connection) {
      String pauseTime = connection.getHeaderField("Retry-After");

      try {
         return Integer.valueOf(pauseTime);
      } catch (Exception var3) {
         return 5;
      }
   }

   public int b() {
      try {
         this.d();
         return this.a.getResponseCode();
      } catch (Exception var2) {
         throw new n(var2.getMessage(), var2);
      }
   }

   public String c() {
      try {
         this.d();
         String result = null;
         if (this.b() >= 400) {
            result = this.a(this.a.getErrorStream());
         } else {
            result = this.a(this.a.getInputStream());
         }

         this.f();
         return result;
      } catch (IOException var2) {
         throw new n(var2.getMessage(), var2);
      }
   }

   private String a(InputStream in) throws IOException {
      if (in == null) {
         return "";
      } else {
         InputStreamReader streamReader = new InputStreamReader(in, "UTF-8");
         StringBuilder sb = new StringBuilder();

         for(int x = streamReader.read(); x != -1; x = streamReader.read()) {
            sb.append((char)x);
         }

         return sb.toString();
      }
   }

   private void f() {
      byte[] bytes = new byte[1024];

      try {
         int count = 0;
         InputStream in = this.a.getInputStream();

         while(in.read(bytes) > 0) {
         }

         in.close();
         return;
      } catch (Exception var10) {
         try {
            InputStream errorStream = this.a.getErrorStream();
            int ret = 0;
            if (errorStream != null) {
               while(errorStream.read(bytes) > 0) {
               }

               errorStream.close();
               return;
            }
         } catch (IOException var9) {
            return;
         }
      } finally {
         if (this.a != null) {
            this.a.disconnect();
         }

      }

   }

   protected T d() {
      if (this.c) {
         return (T)this;
      } else {
         T t = this.e();
         this.c = true;
         return t;
      }
   }

   protected abstract T e();

   public static j<?> a(String url) {
      return new j.b(url, 5000, 60000);
   }

   public static j<?> a(String url, int connectTimeoutMillis, int readTimeoutMillis) {
      return new j.b(url, connectTimeoutMillis, readTimeoutMillis);
   }

   public static j<?> c(String uri, String content) {
      return new j.c(uri, content, 5000, 60000);
   }

   public static j<?> a(String uri, String content, int connectTimeoutMillis, int readTimeoutMillis) {
      return new j.c(uri, content, connectTimeoutMillis, readTimeoutMillis);
   }

   public static j<?> b(String url) {
      return new j.a(url, 5000, 60000);
   }

   public static j<?> d(String url, String content) {
      return new j.d(url, content, 5000, 60000);
   }

   public static j<?> b(String url, String content, int connectTimeoutMillis, int readTimeoutMillis) {
      return new j.d(url, content, connectTimeoutMillis, readTimeoutMillis);
   }

   public String c(String header) {
      return a(this.a, header);
   }

   public static String a(HttpURLConnection connection, String header) {
      try {
         return connection.getHeaderField(header);
      } catch (Exception var3) {
         return "";
      }
   }

   public static class a extends j<j.a> {
      public a(String uri, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
      }

      public j.a f() {
         try {
            this.a.setDoOutput(true);
            this.a.setRequestMethod("DELETE");
            this.a.connect();
            return this;
         } catch (Exception var2) {
            throw new n(var2.getMessage(), var2);
         }
      }
   }

   public static class b extends j<j.b> {
      public b(String uri, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
      }

      public j.b f() {
         try {
            this.a.setDoInput(true);
            this.a.setDoOutput(true);
            this.a.setUseCaches(false);
            this.a.setRequestMethod("GET");
            return this;
         } catch (Exception var2) {
            throw new n(var2.getMessage(), var2);
         }
      }
   }

   public static class c extends j<j.c> {
      private final String c;

      public c(String uri, String content, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
         this.c = content;
      }

      public j.c f() {
         try {
            if (this.c != null) {
               this.a.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.a.setDoInput(true);
            this.a.setDoOutput(true);
            this.a.setUseCaches(false);
            this.a.setRequestMethod("POST");
            OutputStream out = this.a.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(this.c);
            writer.close();
            out.flush();
            return this;
         } catch (Exception var3) {
            throw new n(var3.getMessage(), var3);
         }
      }
   }

   public static class d extends j<j.d> {
      private final String c;

      public d(String uri, String content, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
         this.c = content;
      }

      public j.d f() {
         try {
            if (this.c != null) {
               this.a.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.a.setDoOutput(true);
            this.a.setDoInput(true);
            this.a.setRequestMethod("PUT");
            OutputStream out = this.a.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(this.c);
            writer.close();
            out.flush();
            return this;
         } catch (Exception var3) {
            throw new n(var3.getMessage(), var3);
         }
      }
   }
}
