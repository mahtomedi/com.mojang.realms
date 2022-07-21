package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

public class aq extends RealmsScreen {
   private final ar b;
   private RealmsLabel c;
   private RealmsEditBox d;
   private Boolean e = true;
   private Integer f = 0;
   String[] a;
   private final int g = 0;
   private final int h = 1;
   private final int i = 4;
   private RealmsButton j;
   private RealmsButton k;
   private RealmsButton l;
   private String m = getLocalizedString("mco.backup.button.reset");

   public aq(ar lastScreen) {
      this.b = lastScreen;
   }

   public aq(ar lastScreen, String buttonTitle) {
      this(lastScreen);
      this.m = buttonTitle;
   }

   public void tick() {
      this.d.tick();
      super.tick();
   }

   public void init() {
      this.a = new String[]{
         getLocalizedString("generator.default"),
         getLocalizedString("generator.flat"),
         getLocalizedString("generator.largeBiomes"),
         getLocalizedString("generator.amplified")
      };
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 8, u.a(12), 97, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(aq.this.b);
         }
      });
      this.buttonsAdd(this.j = new RealmsButton(1, this.width() / 2 - 102, u.a(12), 97, 20, this.m) {
         public void onPress() {
            aq.this.a();
         }
      });
      this.d = this.newEditBox(4, this.width() / 2 - 100, u.a(2), 200, 20, getLocalizedString("mco.reset.world.seed"));
      this.d.setMaxLength(32);
      this.d.setValue("");
      this.addWidget(this.d);
      this.focusOn(this.d);
      this.buttonsAdd(this.k = new RealmsButton(2, this.width() / 2 - 102, u.a(4), 205, 20, this.b()) {
         public void onPress() {
            aq.this.f = (aq.this.f + 1) % aq.this.a.length;
            this.setMessage(aq.this.b());
         }
      });
      this.buttonsAdd(this.l = new RealmsButton(3, this.width() / 2 - 102, u.a(6) - 2, 205, 20, this.c()) {
         public void onPress() {
            aq.this.e = !aq.this.e;
            this.setMessage(aq.this.c());
         }
      });
      this.c = new RealmsLabel(getLocalizedString("mco.reset.world.generate"), this.width() / 2, 17, 16777215);
      this.addWidget(this.c);
      this.narrateLabels();
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.b);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void a() {
      this.b.a(new ar.c(this.d.getValue(), this.f, this.e));
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.c.render(this);
      this.drawString(getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, u.a(1), 10526880);
      this.d.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   private String b() {
      String levelType = getLocalizedString("selectWorld.mapType");
      return levelType + " " + this.a[this.f];
   }

   private String c() {
      return getLocalizedString("selectWorld.mapFeatures") + " " + getLocalizedString(this.e ? "mco.configure.world.on" : "mco.configure.world.off");
   }
}
