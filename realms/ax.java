package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSliderButton;

public class ax extends RealmsScreen {
   private RealmsEditBox e;
   protected final ad a;
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
   String[] b;
   String[] c;
   String[][] d;
   private RealmsLabel B;
   private RealmsLabel C = null;

   public ax(ad configureWorldScreen, RealmsWorldOptions options, RealmsServer.c worldType, int activeSlot) {
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
         String warning;
         if (this.j.equals(RealmsServer.c.c)) {
            warning = getLocalizedString("mco.configure.world.edit.subscreen.adventuremap");
         } else if (this.j.equals(RealmsServer.c.e)) {
            warning = getLocalizedString("mco.configure.world.edit.subscreen.inspiration");
         } else {
            warning = getLocalizedString("mco.configure.world.edit.subscreen.experience");
         }

         this.C = new RealmsLabel(warning, this.width() / 2, 26, 16711680);
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
            ax.this.n = !ax.this.n;
            this.setMessage(ax.this.d());
         }
      });
      this.buttonsAdd(new RealmsButton(3, this.f, realms.u.a(3), this.g, 20, this.c()) {
         public void onPress() {
            ax.this.m = (ax.this.m + 1) % ax.this.c.length;
            this.setMessage(ax.this.c());
         }
      });
      this.buttonsAdd(this.v = new RealmsButton(5, this.h, realms.u.a(3), this.g, 20, this.e()) {
         public void onPress() {
            ax.this.p = !ax.this.p;
            this.setMessage(ax.this.e());
         }
      });
      this.buttonsAdd(new RealmsButton(2, this.f, realms.u.a(5), this.g, 20, this.b()) {
         public void onPress() {
            ax.this.l = (ax.this.l + 1) % ax.this.b.length;
            this.setMessage(ax.this.b());
            if (ax.this.j.equals(RealmsServer.c.a)) {
               ax.this.w.active(ax.this.l != 0);
               ax.this.w.setMessage(ax.this.f());
            }

         }
      });
      this.buttonsAdd(this.w = new RealmsButton(6, this.h, realms.u.a(5), this.g, 20, this.f()) {
         public void onPress() {
            ax.this.q = !ax.this.q;
            this.setMessage(ax.this.f());
         }
      });
      this.buttonsAdd(this.y = new ax.a(8, this.f, realms.u.a(7), this.g, this.r, 0.0F, 16.0F));
      this.buttonsAdd(this.x = new RealmsButton(7, this.h, realms.u.a(7), this.g, 20, this.g()) {
         public void onPress() {
            ax.this.o = !ax.this.o;
            this.setMessage(ax.this.g());
         }
      });
      this.buttonsAdd(this.A = new RealmsButton(10, this.f, realms.u.a(9), this.g, 20, this.i()) {
         public void onPress() {
            ax.this.t = !ax.this.t;
            this.setMessage(ax.this.i());
         }
      });
      this.buttonsAdd(this.z = new RealmsButton(9, this.h, realms.u.a(9), this.g, 20, this.h()) {
         public void onPress() {
            ax.this.s = !ax.this.s;
            this.setMessage(ax.this.h());
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
            ax.this.k();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.h, realms.u.a(13), this.g, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(ax.this.a);
         }
      });
      this.addWidget(this.e);
      this.addWidget(this.B = new RealmsLabel(getLocalizedString("mco.configure.world.buttons.options"), this.width() / 2, 17, 16777215));
      if (this.C != null) {
         this.addWidget(this.C);
      }

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
      this.B.render(this);
      if (this.C != null) {
         this.C.render(this);
      }

      this.e.render(xm, ym, a);
      super.render(xm, ym, a);
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
         if (ax.this.y.active()) {
            ax.this.r = (int)this.toValue(this.getValue());
         }
      }

      public String getMessage() {
         return RealmsScreen.getLocalizedString("mco.configure.world.spawnProtection")
            + ": "
            + (ax.this.r == 0 ? RealmsScreen.getLocalizedString("mco.configure.world.off") : ax.this.r);
      }
   }
}
