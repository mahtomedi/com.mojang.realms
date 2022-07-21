package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ar extends at<WorldTemplate> {
   private static final Logger b = LogManager.getLogger();
   private final RealmsScreen c;
   private final RealmsServer d;
   private final RealmsScreen e;
   private RealmsLabel f;
   private RealmsLabel g;
   private String h = getLocalizedString("mco.reset.world.title");
   private String i = getLocalizedString("mco.reset.world.warning");
   private String j = getLocalizedString("gui.cancel");
   private int k = 16711680;
   private final int l = 0;
   private final int m = 100;
   private WorldTemplatePaginatedList n = null;
   private WorldTemplatePaginatedList o = null;
   private WorldTemplatePaginatedList p = null;
   private WorldTemplatePaginatedList q = null;
   public int a = -1;
   private ar.b r = ar.b.a;
   private ar.c s = null;
   private WorldTemplate t = null;
   private String u = null;
   private int v = -1;

   public ar(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen) {
      this.c = lastScreen;
      this.d = serverData;
      this.e = returnScreen;
   }

   public ar(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen, String title, String subtitle, int subtitleColor, String buttonTitle) {
      this(lastScreen, serverData, returnScreen);
      this.h = title;
      this.i = subtitle;
      this.k = subtitleColor;
      this.j = buttonTitle;
   }

   public void a(int confirmationId) {
      this.v = confirmationId;
   }

   public void b(int slot) {
      this.a = slot;
   }

   public void a(String title) {
      this.u = title;
   }

   public void init() {
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 40, realms.u.a(14) - 10, 80, 20, this.j) {
         public void onPress() {
            Realms.setScreen(ar.this.c);
         }
      });
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            g client = realms.g.a();

            try {
               WorldTemplatePaginatedList templates = client.a(1, 10, RealmsServer.c.a);
               WorldTemplatePaginatedList adventuremaps = client.a(1, 10, RealmsServer.c.c);
               WorldTemplatePaginatedList experiences = client.a(1, 10, RealmsServer.c.d);
               WorldTemplatePaginatedList inspirations = client.a(1, 10, RealmsServer.c.e);
               Realms.execute(() -> {
                  ar.this.n = templates;
                  ar.this.o = adventuremaps;
                  ar.this.p = experiences;
                  ar.this.q = inspirations;
               });
            } catch (o var6) {
               ar.b.error("Couldn't fetch templates in reset world", var6);
            }

         }
      }).start();
      this.addWidget(this.f = new RealmsLabel(this.h, this.width() / 2, 7, 16777215));
      this.addWidget(this.g = new RealmsLabel(this.i, this.width() / 2, 22, this.k));
      this.buttonsAdd(
         new ar.a(this.c(1), realms.u.a(0) + 10, getLocalizedString("mco.reset.world.generate"), -1L, "realms:textures/gui/realms/new_world.png", ar.b.b) {
            public void onPress() {
               Realms.setScreen(new aq(ar.this, ar.this.h));
            }
         }
      );
      this.buttonsAdd(
         new ar.a(this.c(2), realms.u.a(0) + 10, getLocalizedString("mco.reset.world.upload"), -1L, "realms:textures/gui/realms/upload.png", ar.b.c) {
            public void onPress() {
               Realms.setScreen(new au(ar.this.d.id, ar.this.a != -1 ? ar.this.a : ar.this.d.activeSlot, ar.this));
            }
         }
      );
      this.buttonsAdd(
         new ar.a(this.c(3), realms.u.a(0) + 10, getLocalizedString("mco.reset.world.template"), -1L, "realms:textures/gui/realms/survival_spawn.png", ar.b.e) {
            public void onPress() {
               av templateScreen = new av(ar.this, RealmsServer.c.a, ar.this.n);
               templateScreen.a(RealmsScreen.getLocalizedString("mco.reset.world.template"));
               Realms.setScreen(templateScreen);
            }
         }
      );
      this.buttonsAdd(
         new ar.a(this.c(1), realms.u.a(6) + 20, getLocalizedString("mco.reset.world.adventure"), -1L, "realms:textures/gui/realms/adventure.png", ar.b.d) {
            public void onPress() {
               av screen = new av(ar.this, RealmsServer.c.c, ar.this.o);
               screen.a(RealmsScreen.getLocalizedString("mco.reset.world.adventure"));
               Realms.setScreen(screen);
            }
         }
      );
      this.buttonsAdd(
         new ar.a(this.c(2), realms.u.a(6) + 20, getLocalizedString("mco.reset.world.experience"), -1L, "realms:textures/gui/realms/experience.png", ar.b.f) {
            public void onPress() {
               av experienceScreen = new av(ar.this, RealmsServer.c.d, ar.this.p);
               experienceScreen.a(RealmsScreen.getLocalizedString("mco.reset.world.experience"));
               Realms.setScreen(experienceScreen);
            }
         }
      );
      this.buttonsAdd(
         new ar.a(this.c(3), realms.u.a(6) + 20, getLocalizedString("mco.reset.world.inspiration"), -1L, "realms:textures/gui/realms/inspiration.png", ar.b.g) {
            public void onPress() {
               av inspirationScreen = new av(ar.this, RealmsServer.c.e, ar.this.q);
               inspirationScreen.a(RealmsScreen.getLocalizedString("mco.reset.world.inspiration"));
               Realms.setScreen(inspirationScreen);
            }
         }
      );
      this.narrateLabels();
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.c);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      return super.mouseClicked(x, y, buttonNum);
   }

   private int c(int i) {
      return this.width() / 2 - 130 + (i - 1) * 100;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.f.render(this);
      this.g.render(this);
      super.render(xm, ym, a);
   }

   private void a(int x, int y, String text, long imageId, String image, ar.b resetType, boolean hoveredOrFocused, boolean hovered) {
      if (imageId == -1L) {
         bind(image);
      } else {
         bj.a(String.valueOf(imageId), image);
      }

      if (hoveredOrFocused) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RealmsScreen.blit(x + 2, y + 14, 0.0F, 0.0F, 56, 56, 56, 56);
      bind("realms:textures/gui/realms/slot_frame.png");
      if (hoveredOrFocused) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RealmsScreen.blit(x, y + 12, 0.0F, 0.0F, 60, 60, 60, 60);
      this.drawCenteredString(text, x + 30, y, hoveredOrFocused ? 10526880 : 16777215);
   }

   void a(WorldTemplate worldTemplate) {
      if (worldTemplate != null) {
         if (this.a == -1) {
            this.b(worldTemplate);
         } else {
            switch(worldTemplate.type) {
               case a:
                  this.r = ar.b.e;
                  break;
               case c:
                  this.r = ar.b.d;
                  break;
               case d:
                  this.r = ar.b.f;
                  break;
               case e:
                  this.r = ar.b.g;
            }

            this.t = worldTemplate;
            this.b();
         }
      }

   }

   private void b() {
      this.a((RealmsScreen)this);
   }

   public void a(RealmsScreen screen) {
      bi.i switchSlotTask = new bi.i(this.d.id, this.a, screen, 100);
      al longRunningMcoTaskScreen = new al(this.c, switchSlotTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void confirmResult(boolean result, int id) {
      if (id == 100 && result) {
         switch(this.r) {
            case d:
            case e:
            case f:
            case g:
               if (this.t != null) {
                  this.b(this.t);
               }
               break;
            case b:
               if (this.s != null) {
                  this.b(this.s);
               }
               break;
            default:
               return;
         }

      } else {
         if (result) {
            Realms.setScreen(this.e);
            if (this.v != -1) {
               this.e.confirmResult(true, this.v);
            }
         }

      }
   }

   public void b(WorldTemplate template) {
      bi.f resettingWorldTask = new bi.f(this.d.id, this.e, template);
      if (this.u != null) {
         resettingWorldTask.c(this.u);
      }

      if (this.v != -1) {
         resettingWorldTask.a(this.v);
      }

      al longRunningMcoTaskScreen = new al(this.c, resettingWorldTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void a(ar.c resetWorldInfo) {
      if (this.a == -1) {
         this.b(resetWorldInfo);
      } else {
         this.r = ar.b.b;
         this.s = resetWorldInfo;
         this.b();
      }

   }

   private void b(ar.c resetWorldInfo) {
      bi.f resettingWorldTask = new bi.f(this.d.id, this.e, resetWorldInfo.a, resetWorldInfo.b, resetWorldInfo.c);
      if (this.u != null) {
         resettingWorldTask.c(this.u);
      }

      if (this.v != -1) {
         resettingWorldTask.a(this.v);
      }

      al longRunningMcoTaskScreen = new al(this.c, resettingWorldTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   abstract class a extends RealmsButton {
      private final long a;
      private final String c;
      private final ar.b d;

      public a(int x, int y, String text, long imageId, String image, ar.b resetType) {
         super(100 + resetType.ordinal(), x, y, 60, 72, text);
         this.a = imageId;
         this.c = image;
         this.d = resetType;
      }

      public void tick() {
         super.tick();
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         ar.this.a(
            this.x(),
            this.y(),
            this.getProxy().getMessage(),
            this.a,
            this.c,
            this.d,
            this.getProxy().isHovered(),
            this.getProxy().isMouseOver((double)mouseX, (double)mouseY)
         );
      }
   }

   static enum b {
      a,
      b,
      c,
      d,
      e,
      f,
      g;
   }

   public static class c {
      String a;
      int b;
      boolean c;

      public c(String seed, int levelType, boolean generateStructures) {
         this.a = seed;
         this.b = levelType;
         this.c = generateStructures;
      }
   }
}
