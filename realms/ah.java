package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class ah extends RealmsScreen {
   private final RealmsScreen a;
   private String b;
   private String c;

   public ah(o realmsServiceException, RealmsScreen nextScreen) {
      this.a = nextScreen;
      this.a(realmsServiceException);
   }

   public ah(String message, RealmsScreen nextScreen) {
      this.a = nextScreen;
      this.a(message);
   }

   public ah(String title, String message, RealmsScreen nextScreen) {
      this.a = nextScreen;
      this.a(title, message);
   }

   private void a(o realmsServiceException) {
      if (realmsServiceException.c == -1) {
         this.b = "An error occurred (" + realmsServiceException.a + "):";
         this.c = realmsServiceException.b;
      } else {
         this.b = "Realms (" + realmsServiceException.c + "):";
         String translationKey = "mco.errorMessage." + realmsServiceException.c;
         String translated = getLocalizedString(translationKey);
         this.c = translated.equals(translationKey) ? realmsServiceException.d : translated;
      }

   }

   private void a(String message) {
      this.b = "An error occurred: ";
      this.c = message;
   }

   private void a(String title, String message) {
      this.b = title;
      this.c = message;
   }

   public void init() {
      Realms.narrateNow(this.b + ": " + this.c);
      this.buttonsAdd(new RealmsButton(10, this.width() / 2 - 100, this.height() - 52, 200, 20, "Ok") {
         public void onPress() {
            Realms.setScreen(ah.this.a);
         }
      });
   }

   public void tick() {
      super.tick();
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.b, this.width() / 2, 80, 16777215);
      this.drawCenteredString(this.c, this.width() / 2, 100, 16711680);
      super.render(xm, ym, a);
   }
}
