package realms;

import net.minecraft.realms.AbstractRealmsButton;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class ae extends RealmsScreen {
   protected RealmsScreen a;
   protected String b;
   private final String f;
   protected String c;
   protected String d;
   protected int e;
   private int g;

   public ae(RealmsScreen parent, String title1, String title2, int id) {
      this.a = parent;
      this.b = title1;
      this.f = title2;
      this.e = id;
      this.c = getLocalizedString("gui.yes");
      this.d = getLocalizedString("gui.no");
   }

   public ae(RealmsScreen parent, String title1, String title2, String yesButton, String noButton, int id) {
      this.a = parent;
      this.b = title1;
      this.f = title2;
      this.c = yesButton;
      this.d = noButton;
      this.e = id;
   }

   public void init() {
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, u.a(9), 100, 20, this.c) {
         public void onPress() {
            ae.this.a.confirmResult(true, ae.this.e);
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, u.a(9), 100, 20, this.d) {
         public void onPress() {
            ae.this.a.confirmResult(false, ae.this.e);
         }
      });
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.b, this.width() / 2, u.a(3), 16777215);
      this.drawCenteredString(this.f, this.width() / 2, u.a(5), 16777215);
      super.render(xm, ym, a);
   }

   public void a(int delay) {
      this.g = delay;

      for(AbstractRealmsButton<?> button : this.buttons()) {
         button.active(false);
      }

   }

   public void tick() {
      super.tick();
      if (--this.g == 0) {
         for(AbstractRealmsButton<?> button : this.buttons()) {
            button.active(true);
         }
      }

   }
}
