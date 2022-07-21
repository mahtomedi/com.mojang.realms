package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsConfirmResultListener;
import net.minecraft.realms.RealmsScreen;

public class ak extends RealmsScreen {
   private final ak.a e;
   private final String f;
   private final String g;
   protected final RealmsConfirmResultListener a;
   protected final String b;
   protected final String c;
   private final String h;
   protected final int d;
   private final boolean i;

   public ak(RealmsConfirmResultListener listener, ak.a type, String line2, String line3, boolean yesNoQuestion, int id) {
      this.a = listener;
      this.d = id;
      this.e = type;
      this.f = line2;
      this.g = line3;
      this.i = yesNoQuestion;
      this.b = getLocalizedString("gui.yes");
      this.c = getLocalizedString("gui.no");
      this.h = getLocalizedString("mco.gui.ok");
   }

   public void init() {
      Realms.narrateNow(new String[]{this.e.d, this.f, this.g});
      if (this.i) {
         this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, u.a(8), 100, 20, this.b) {
            public void onPress() {
               ak.this.a.confirmResult(true, ak.this.d);
            }
         });
         this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, u.a(8), 100, 20, this.c) {
            public void onPress() {
               ak.this.a.confirmResult(false, ak.this.d);
            }
         });
      } else {
         this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, u.a(8), 100, 20, this.h) {
            public void onPress() {
               ak.this.a.confirmResult(true, ak.this.d);
            }
         });
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.a.confirmResult(false, this.d);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.e.d, this.width() / 2, u.a(2), this.e.c);
      this.drawCenteredString(this.f, this.width() / 2, u.a(4), 16777215);
      this.drawCenteredString(this.g, this.width() / 2, u.a(6), 16777215);
      super.render(xm, ym, a);
   }

   public static enum a {
      a("Warning!", 16711680),
      b("Info!", 8226750);

      public final int c;
      public final String d;

      private a(String text, int colorCode) {
         this.d = text;
         this.c = colorCode;
      }
   }
}
