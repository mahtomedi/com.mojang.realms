package realms;

public class ba {
   public final int a;
   public final String b;

   public ba(int statusCode, String errorMessage) {
      this.a = statusCode;
      this.b = errorMessage;
   }

   public static class a {
      private int a = -1;
      private String b = null;

      public ba.a a(int statusCode) {
         this.a = statusCode;
         return this;
      }

      public ba.a a(String errorMessage) {
         this.b = errorMessage;
         return this;
      }

      public ba a() {
         return new ba(this.a, this.b);
      }
   }
}
