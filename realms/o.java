package realms;

import net.minecraft.realms.RealmsScreen;

public class o extends Exception {
   public final int a;
   public final String b;
   public final int c;
   public final String d;

   public o(int httpResultCode, String httpResponseText, i error) {
      super(httpResponseText);
      this.a = httpResultCode;
      this.b = httpResponseText;
      this.c = error.b();
      this.d = error.a();
   }

   public o(int httpResultCode, String httpResponseText, int errorCode, String errorMsg) {
      super(httpResponseText);
      this.a = httpResultCode;
      this.b = httpResponseText;
      this.c = errorCode;
      this.d = errorMsg;
   }

   public String toString() {
      if (this.c == -1) {
         return "Realms (" + this.a + ") " + this.b;
      } else {
         String translationKey = "mco.errorMessage." + this.c;
         String translated = RealmsScreen.getLocalizedString(translationKey);
         return (translated.equals(translationKey) ? this.d : translated) + " - " + this.c;
      }
   }
}
