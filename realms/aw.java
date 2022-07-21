package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSliderButton;

public class aw extends RealmsScreen {
   private RealmsEditBox e;
   protected final ac a;
   private int f;
   private int g;
   private int h;
   private final RealmsWorldOptions i;
   private final RealmsServer.c j;
   private final int k;
   private int l;
   private int m;
   private Boolean n;
   private Boolean o;
   private Boolean p;
   private Boolean q;
   private Integer r;
   private Boolean s;
   private Boolean t;
   private RealmsButton u;
   private RealmsButton v;
   private RealmsButton w;
   private RealmsButton x;
   private RealmsSliderButton y;
   private RealmsButton z;
   private RealmsButton A;
   private boolean B;
   String[] b;
   String[] c;
   String[][] d;
   private RealmsLabel C;

   public aw(ac configureWorldScreen, RealmsWorldOptions options, RealmsServer.c worldType, int activeSlot) {
      this.a = configureWorldScreen;
      this.i = options;
      this.j = worldType;
      this.k = activeSlot;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public void tick() {
      this.e.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.a);
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void init() {
      this.g = 170;
      this.f = this.width() / 2 - this.g * 2 / 2;
      this.h = this.width() / 2 + 10;
      this.a();
      this.l = this.i.difficulty;
      this.m = this.i.gameMode;
      if (this.j.equals(RealmsServer.c.a)) {
         this.n = this.i.pvp;
         this.r = this.i.spawnProtection;
         this.t = this.i.forceGameMode;
         this.p = this.i.spawnAnimals;
         this.q = this.i.spawnMonsters;
         this.o = this.i.spawnNPCs;
         this.s = this.i.commandBlocks;
      } else {
         this.B = true;
         this.n = true;
         this.r = 0;
         this.t = false;
         this.p = true;
         this.q = true;
         this.o = true;
         this.s = true;
      }

      this.e = this.newEditBox(11, this.f + 2, realms.u.a(1), this.g - 4, 20, getLocalizedString("mco.configure.world.edit.slot.name"));
      this.e.setMaxLength(10);
      this.e.setValue(this.i.getSlotName(this.k));
      this.focusOn(this.e);
      this.buttonsAdd(this.u = new RealmsButton(4, this.h, realms.u.a(1), this.g, 20, this.d()) {
         public void onPress() {
            aw.this.n = !aw.this.n;
            this.setMessage(aw.this.d());
         }
      });
      this.buttonsAdd(new RealmsButton(3, this.f, realms.u.a(3), this.g, 20, this.c()) {
         public void onPress() {
            aw.this.m = (aw.this.m + 1) % aw.this.c.length;
            this.setMessage(aw.this.c());
         }
      });
      this.buttonsAdd(this.v = new RealmsButton(5, this.h, realms.u.a(3), this.g, 20, this.e()) {
         public void onPress() {
            aw.this.p = !aw.this.p;
            this.setMessage(aw.this.e());
         }
      });
      this.buttonsAdd(new RealmsButton(2, this.f, realms.u.a(5), this.g, 20, this.b()) {
         public void onPress() {
            aw.this.l = (aw.this.l + 1) % aw.this.b.length;
            this.setMessage(aw.this.b());
            if (aw.this.j.equals(RealmsServer.c.a)) {
               aw.this.w.active(aw.this.l != 0);
               aw.this.w.setMessage(aw.this.f());
            }

         }
      });
      this.buttonsAdd(this.w = new RealmsButton(6, this.h, realms.u.a(5), this.g, 20, this.f()) {
         public void onPress() {
            aw.this.q = !aw.this.q;
            this.setMessage(aw.this.f());
         }
      });
      this.buttonsAdd(this.y = new aw.a(8, this.f, realms.u.a(7), this.g, this.r, 0.0F, 16.0F));
      this.buttonsAdd(this.x = new RealmsButton(7, this.h, realms.u.a(7), this.g, 20, this.g()) {
         public void onPress() {
            aw.this.o = !aw.this.o;
            this.setMessage(aw.this.g());
         }
      });
      this.buttonsAdd(this.A = new RealmsButton(10, this.f, realms.u.a(9), this.g, 20, this.i()) {
         public void onPress() {
            aw.this.t = !aw.this.t;
            this.setMessage(aw.this.i());
         }
      });
      this.buttonsAdd(this.z = new RealmsButton(9, this.h, realms.u.a(9), this.g, 20, this.h()) {
         public void onPress() {
            aw.this.s = !aw.this.s;
            this.setMessage(aw.this.h());
         }
      });
      if (!this.j.equals(RealmsServer.c.a)) {
         this.u.active(false);
         this.v.active(false);
         this.x.active(false);
         this.w.active(false);
         this.y.active(false);
         this.z.active(false);
         this.y.active(false);
         this.A.active(false);
      }

      if (this.l == 0) {
         this.w.active(false);
      }

      this.buttonsAdd(new RealmsButton(1, this.f, realms.u.a(13), this.g, 20, getLocalizedString("mco.configure.world.buttons.done")) {
         public void onPress() {
            aw.this.k();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.h, realms.u.a(13), this.g, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(aw.this.a);
         }
      });
      this.addWidget(this.e);
      this.addWidget(this.C = new RealmsLabel(getLocalizedString("mco.configure.world.buttons.options"), this.width() / 2, 17, 16777215));
      this.narrateLabels();
   }

   private void a() {
      this.b = new String[]{
         getLocalizedString("options.difficulty.peaceful"),
         getLocalizedString("options.difficulty.easy"),
         getLocalizedString("options.difficulty.normal"),
         getLocalizedString("options.difficulty.hard")
      };
      this.c = new String[]{
         getLocalizedString("selectWorld.gameMode.survival"),
         getLocalizedString("selectWorld.gameMode.creative"),
         getLocalizedString("selectWorld.gameMode.adventure")
      };
      this.d = new String[][]{
         {getLocalizedString("selectWorld.gameMode.survival.line1"), getLocalizedString("selectWorld.gameMode.survival.line2")},
         {getLocalizedString("selectWorld.gameMode.creative.line1"), getLocalizedString("selectWorld.gameMode.creative.line2")},
         {getLocalizedString("selectWorld.gameMode.adventure.line1"), getLocalizedString("selectWorld.gameMode.adventure.line2")}
      };
   }

   private String b() {
      String difficulty = getLocalizedString("options.difficulty");
      return difficulty + ": " + this.b[this.l];
   }

   private String c() {
      String gameMode = getLocalizedString("selectWorld.gameMode");
      return gameMode + ": " + this.c[this.m];
   }

   private String d() {
      return getLocalizedString("mco.configure.world.pvp") + ": " + getLocalizedString(this.n ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String e() {
      return getLocalizedString("mco.configure.world.spawnAnimals") + ": " + getLocalizedString(this.p ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String f() {
      return this.l == 0
         ? getLocalizedString("mco.configure.world.spawnMonsters") + ": " + getLocalizedString("mco.configure.world.off")
         : getLocalizedString("mco.configure.world.spawnMonsters") + ": " + getLocalizedString(this.q ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String g() {
      return getLocalizedString("mco.configure.world.spawnNPCs") + ": " + getLocalizedString(this.o ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String h() {
      return getLocalizedString("mco.configure.world.commandBlocks") + ": " + getLocalizedString(this.s ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String i() {
      return getLocalizedString("mco.configure.world.forceGameMode") + ": " + getLocalizedString(this.t ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      String slotName = getLocalizedString("mco.configure.world.edit.slot.name");
      this.drawString(slotName, this.f + this.g / 2 - this.fontWidth(slotName) / 2, realms.u.a(0) - 5, 16777215);
      this.C.render(this);
      if (this.B) {
         if (this.j.equals(RealmsServer.c.c)) {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.adventuremap"), this.width() / 2, 26, 16711680);
         } else if (this.j.equals(RealmsServer.c.e)) {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.inspiration"), this.width() / 2, 26, 16711680);
         } else {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.experience"), this.width() / 2, 26, 16711680);
         }
      }

      this.e.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   public boolean mouseReleased(double x, double y, int buttonNum) {
      if (!this.y.active()) {
         return super.mouseReleased(x, y, buttonNum);
      } else {
         this.y.onRelease(x, y);
         return true;
      }
   }

   public boolean mouseDragged(double x, double y, int buttonNum, double dx, double dy) {
      if (!this.y.active()) {
         return super.mouseDragged(x, y, buttonNum, dx, dy);
      } else {
         if (x < (double)(this.f + this.y.getWidth()) && x > (double)this.f && y < (double)(this.y.y() + 20) && y > (double)this.y.y()) {
            this.y.onClick(x, y);
         }

         return true;
      }
   }

   private String j() {
      return this.e.getValue().equals(this.i.getDefaultSlotName(this.k)) ? "" : this.e.getValue();
   }

   private void k() {
      if (!this.j.equals(RealmsServer.c.c) && !this.j.equals(RealmsServer.c.d) && !this.j.equals(RealmsServer.c.e)) {
         this.a.a(new RealmsWorldOptions(this.n, this.p, this.q, this.o, this.r, this.s, this.l, this.m, this.t, this.j()));
      } else {
         this.a
            .a(
               new RealmsWorldOptions(
                  this.i.pvp,
                  this.i.spawnAnimals,
                  this.i.spawnMonsters,
                  this.i.spawnNPCs,
                  this.i.spawnProtection,
                  this.i.commandBlocks,
                  this.l,
                  this.m,
                  this.i.forceGameMode,
                  this.j()
               )
            );
      }

   }

   class a extends RealmsSliderButton {
      public a(int id, int x, int y, int width, int currentValue, float minValue, float maxValue) {
         super(id, x, y, width, currentValue, (double)minValue, (double)maxValue);
      }

      public void applyValue() {
         if (aw.this.y.active()) {
            aw.this.r = (int)this.toValue(this.getValue());
         }
      }

      public String getMessage() {
         return RealmsScreen.getLocalizedString("mco.configure.world.spawnProtection")
            + ": "
            + (aw.this.r == 0 ? RealmsScreen.getLocalizedString("mco.configure.world.off") : aw.this.r);
      }
   }
}
