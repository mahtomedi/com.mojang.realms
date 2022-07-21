package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class ab extends RealmsScreen {
   private final RealmsScreen a;
   private final boolean b;

   public ab(RealmsScreen lastScreen, boolean outdated) {
      this.a = lastScreen;
      this.b = outdated;
   }

   public void init() {
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, u.a(12), "Back") {
         public void onPress() {
            Realms.setScreen(ab.this.a);
         }
      });
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      String title = getLocalizedString(this.b ? "mco.client.outdated.title" : "mco.client.incompatible.title");
      this.drawCenteredString(title, this.width() / 2, u.a(3), 16711680);
      int lines = this.b ? 2 : 3;

      for(int i = 0; i < lines; ++i) {
         String message = getLocalizedString((this.b ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (i + 1));
         this.drawCenteredString(message, this.width() / 2, u.a(5) + i * 12, 16777215);
      }

      super.render(xm, ym, a);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey != 257 && eventKey != 335 && eventKey != 256) {
         return super.keyPressed(eventKey, scancode, mods);
      } else {
         Realms.setScreen(this.a);
         return true;
      }
   }
}
