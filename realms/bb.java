package realms;

public class bb {
   public final int a;
   public final String b;

   public bb(int statusCode, String errorMessage) {
      this.a = statusCode;
      this.b = errorMessage;
   }

   public static class a {
      private int a = -1;
      private String b = null;

      public bb.a a(int statusCode) {
         this.a = statusCode;
         return this;
      }

      public bb.a a(String errorMessage) {
         this.b = errorMessage;
         return this;
      }

      public bb a() {
         return new bb(this.a, this.b);
      }
   }
}
