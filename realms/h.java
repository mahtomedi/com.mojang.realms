package realms;

import java.net.Proxy;

public class h {
   private static Proxy a;

   public static Proxy a() {
      return a;
   }

   public static void a(Proxy proxy) {
      if (a == null) {
         a = proxy;
      }

   }
}
