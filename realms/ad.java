package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ad extends at<WorldTemplate> implements w.b {
   private static final Logger a = LogManager.getLogger();
   private String b;
   private final b c;
   private RealmsServer d;
   private final long e;
   private int f;
   private int g;
   private final int h = 80;
   private final int i = 5;
   private RealmsButton j;
   private RealmsButton k;
   private RealmsButton l;
   private RealmsButton m;
   private RealmsButton n;
   private RealmsButton o;
   private RealmsButton p;
   private boolean q;
   private int r;
   private int s;

   public ad(b lastScreen, long serverId) {
      this.c = lastScreen;
      this.e = serverId;
   }

   public void init() {
      if (this.d == null) {
         this.a(this.e);
      }

      this.f = this.width() / 2 - 187;
      this.g = this.width() / 2 + 190;
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(this.j = new RealmsButton(2, this.a(0, 3), u.a(0), 100, 20, getLocalizedString("mco.configure.world.buttons.players")) {
         public void onPress() {
            Realms.setScreen(new ap(ad.this, ad.this.d));
         }
      });
      this.buttonsAdd(this.k = new RealmsButton(3, this.a(1, 3), u.a(0), 100, 20, getLocalizedString("mco.configure.world.buttons.settings")) {
         public void onPress() {
            Realms.setScreen(new aw(ad.this, ad.this.d.clone()));
         }
      });
      this.buttonsAdd(this.l = new RealmsButton(4, this.a(2, 3), u.a(0), 100, 20, getLocalizedString("mco.configure.world.buttons.subscription")) {
         public void onPress() {
            Realms.setScreen(new ay(ad.this, ad.this.d.clone(), ad.this.c));
         }
      });

      for(int i = 1; i < 5; ++i) {
         this.a(i);
      }

      this.buttonsAdd(this.p = new RealmsButton(8, this.b(0), u.a(13) - 5, 100, 20, getLocalizedString("mco.configure.world.buttons.switchminigame")) {
         public void onPress() {
            av minigameScreen = new av(ad.this, RealmsServer.c.b);
            minigameScreen.a(RealmsScreen.getLocalizedString("mco.template.title.minigame"));
            Realms.setScreen(minigameScreen);
         }
      });
      this.buttonsAdd(
         this.m = new RealmsButton(5, this.b(0), u.a(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.options")) {
            public void onPress() {
               Realms.setScreen(
                  new ax(ad.this, ((RealmsWorldOptions)ad.this.d.slots.get(ad.this.d.activeSlot)).clone(), ad.this.d.worldType, ad.this.d.activeSlot)
               );
            }
         }
      );
      this.buttonsAdd(this.n = new RealmsButton(6, this.b(1), u.a(13) - 5, 90, 20, getLocalizedString("mco.configure.world.backup")) {
         public void onPress() {
            Realms.setScreen(new aa(ad.this, ad.this.d.clone(), ad.this.d.activeSlot));
         }
      });
      this.buttonsAdd(this.o = new RealmsButton(7, this.b(2), u.a(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.resetworld")) {
         public void onPress() {
            Realms.setScreen(new ar(ad.this, ad.this.d.clone(), ad.this.b()));
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.g - 80 + 8, u.a(13) - 5, 70, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            ad.this.d();
         }
      });
      this.n.active(true);
      if (this.d == null) {
         this.j();
         this.h();
         this.j.active(false);
         this.k.active(false);
         this.l.active(false);
      } else {
         this.e();
         if (this.g()) {
            this.h();
         } else {
            this.j();
         }
      }

   }

   private void a(int i) {
      int x = this.c(i);
      int y = u.a(5) + 5;
      int buttonId = 100 + i;
      w worldSlotButton = new w(x, y, 80, 80, () -> this.d, s -> this.b = s, buttonId, i, this);
      this.getProxy().buttonsAdd(worldSlotButton);
   }

   private int b(int i) {
      return this.f + i * 95;
   }

   private int a(int i, int total) {
      return this.width() / 2 - (total * 105 - 5) / 2 + i * 105;
   }

   public void tick() {
      this.tickButtons();
      ++this.r;
      --this.s;
      if (this.s < 0) {
         this.s = 0;
      }

   }

   public void render(int xm, int ym, float a) {
      this.b = null;
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.worlds.title"), this.width() / 2, u.a(4), 16777215);
      super.render(xm, ym, a);
      if (this.d == null) {
         this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 17, 16777215);
      } else {
         String name = this.d.getName();
         int nameWidth = this.fontWidth(name);
         int nameColor = this.d.state == RealmsServer.b.a ? 10526880 : 8388479;
         int titleWidth = this.fontWidth(getLocalizedString("mco.configure.world.title"));
         this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 12, 16777215);
         this.drawCenteredString(name, this.width() / 2, 24, nameColor);
         int statusX = Math.min(this.a(2, 3) + 80 - 11, this.width() / 2 + nameWidth / 2 + titleWidth / 2 + 10);
         this.a(statusX, 7, xm, ym);
         if (this.g()) {
            this.drawString(getLocalizedString("mco.configure.current.minigame") + ": " + this.d.getMinigameName(), this.f + 80 + 20 + 10, u.a(13), 16777215);
         }

         if (this.b != null) {
            this.a(this.b, xm, ym);
         }

      }
   }

   private int c(int i) {
      return this.f + (i - 1) * 98;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.d();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void d() {
      if (this.q) {
         this.c.e();
      }

      Realms.setScreen(this.c);
   }

   private void a(final long worldId) {
      (new Thread() {
         public void run() {
            g client = realms.g.a();

            try {
               ad.this.d = client.a(worldId);
               ad.this.e();
               if (ad.this.g()) {
                  ad.this.k();
               } else {
                  ad.this.i();
               }
            } catch (o var3) {
               ad.a.error("Couldn't get own world");
               Realms.setScreen(new ai(var3.getMessage(), ad.this.c));
            } catch (IOException var4) {
               ad.a.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   private void e() {
      this.j.active(!this.d.expired);
      this.k.active(!this.d.expired);
      this.l.active(true);
      this.p.active(!this.d.expired);
      this.m.active(!this.d.expired);
      this.o.active(!this.d.expired);
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      return super.mouseClicked(x, y, buttonNum);
   }

   private void a(RealmsServer serverData) {
      if (this.d.state == RealmsServer.b.b) {
         this.c.a(serverData, new ad(this.c.f(), this.e));
      } else {
         this.a(true, new ad(this.c.f(), this.e));
      }

   }

   @Override
   public void a(int slotIndex, @Nonnull w.a action, boolean minigame, boolean empty) {
      switch(action) {
         case a:
            break;
         case c:
            this.a(this.d);
            break;
         case b:
            if (minigame) {
               this.f();
            } else if (empty) {
               this.b(slotIndex, this.d);
            } else {
               this.a(slotIndex, this.d);
            }
            break;
         default:
            throw new IllegalStateException("Unknown action " + action);
      }

   }

   private void f() {
      av screen = new av(this, RealmsServer.c.b);
      screen.a(getLocalizedString("mco.template.title.minigame"));
      screen.b(getLocalizedString("mco.minigame.world.info.line1") + "\\n" + getLocalizedString("mco.minigame.world.info.line2"));
      Realms.setScreen(screen);
   }

   private void a(int selectedSlot, RealmsServer serverData) {
      String line2 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
      String line3 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
      Realms.setScreen(new ak((result, id) -> {
         if (result) {
            this.a(serverData.id, selectedSlot);
         } else {
            Realms.setScreen(this);
         }

      }, ak.a.b, line2, line3, true, 9));
   }

   private void b(int selectedSlot, RealmsServer serverData) {
      String line2 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
      String line3 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
      Realms.setScreen(
         new ak(
            (result, id) -> {
               if (result) {
                  ar resetWorldScreen = new ar(
                     this,
                     serverData,
                     this.b(),
                     getLocalizedString("mco.configure.world.switch.slot"),
                     getLocalizedString("mco.configure.world.switch.slot.subtitle"),
                     10526880,
                     getLocalizedString("gui.cancel")
                  );
                  resetWorldScreen.b(selectedSlot);
                  resetWorldScreen.a(getLocalizedString("mco.create.world.reset.title"));
                  Realms.setScreen(resetWorldScreen);
               } else {
                  Realms.setScreen(this);
               }
      
            },
            ak.a.b,
            line2,
            line3,
            true,
            10
         )
      );
   }

   protected void a(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         if (rx + width + 3 > this.g) {
            rx = rx - width - 20;
         }

         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private void a(int x, int y, int xm, int ym) {
      if (this.d.expired) {
         this.b(x, y, xm, ym);
      } else if (this.d.state == RealmsServer.b.a) {
         this.d(x, y, xm, ym);
      } else if (this.d.state == RealmsServer.b.b) {
         if (this.d.daysLeft < 7) {
            this.a(x, y, xm, ym, this.d.daysLeft);
         } else {
            this.c(x, y, xm, ym);
         }
      }

   }

   private void b(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.b = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void a(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      if (this.r % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         if (daysLeft <= 0) {
            this.b = getLocalizedString("mco.selectServer.expires.soon");
         } else if (daysLeft == 1) {
            this.b = getLocalizedString("mco.selectServer.expires.day");
         } else {
            this.b = getLocalizedString("mco.selectServer.expires.days", new Object[]{daysLeft});
         }
      }

   }

   private void c(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.b = getLocalizedString("mco.selectServer.open");
      }

   }

   private void d(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.b = getLocalizedString("mco.selectServer.closed");
      }

   }

   private boolean g() {
      return this.d != null && this.d.worldType.equals(RealmsServer.c.b);
   }

   private void h() {
      this.a(this.m);
      this.a(this.n);
      this.a(this.o);
   }

   private void a(RealmsButton button) {
      button.setVisible(false);
      this.removeButton(button);
   }

   private void i() {
      this.b(this.m);
      this.b(this.n);
      this.b(this.o);
   }

   private void b(RealmsButton button) {
      button.setVisible(true);
      this.buttonsAdd(button);
   }

   private void j() {
      this.a(this.p);
   }

   private void k() {
      this.b(this.p);
   }

   public void a(RealmsWorldOptions options) {
      RealmsWorldOptions oldOptions = (RealmsWorldOptions)this.d.slots.get(this.d.activeSlot);
      options.templateId = oldOptions.templateId;
      options.templateImage = oldOptions.templateImage;
      g client = realms.g.a();

      try {
         client.a(this.d.id, this.d.activeSlot, options);
         this.d.slots.put(this.d.activeSlot, options);
      } catch (o var5) {
         a.error("Couldn't save slot settings");
         Realms.setScreen(new ai(var5, this));
         return;
      } catch (UnsupportedEncodingException var6) {
         a.error("Couldn't save slot settings");
      }

      Realms.setScreen(this);
   }

   public void a(String name, String desc) {
      String description = desc != null && !desc.trim().isEmpty() ? desc : null;
      g client = realms.g.a();

      try {
         client.b(this.d.id, name, description);
         this.d.setName(name);
         this.d.setDescription(description);
      } catch (o var6) {
         a.error("Couldn't save settings");
         Realms.setScreen(new ai(var6, this));
         return;
      } catch (UnsupportedEncodingException var7) {
         a.error("Couldn't save settings");
      }

      Realms.setScreen(this);
   }

   public void a(boolean join, RealmsScreen screenInCaseOfCancel) {
      bi.c openServerTask = new bi.c(this.d, this, this.c, join);
      al openWorldLongRunningTaskScreen = new al(screenInCaseOfCancel, openServerTask);
      openWorldLongRunningTaskScreen.a();
      Realms.setScreen(openWorldLongRunningTaskScreen);
   }

   public void a(RealmsScreen screenInCaseOfCancel) {
      bi.a closeServerTask = new bi.a(this.d, this);
      al closeWorldLongRunningTaskScreen = new al(screenInCaseOfCancel, closeServerTask);
      closeWorldLongRunningTaskScreen.a();
      Realms.setScreen(closeWorldLongRunningTaskScreen);
   }

   public void a() {
      this.q = true;
   }

   void a(WorldTemplate worldTemplate) {
      if (worldTemplate != null) {
         if (WorldTemplate.a.b.equals(worldTemplate.type)) {
            this.b(worldTemplate);
         }

      }
   }

   private void a(long worldId, int selectedSlot) {
      ad newScreen = this.b();
      bi.i switchSlotTask = new bi.i(worldId, selectedSlot, (result, id) -> Realms.setScreen(newScreen), 11);
      al longRunningMcoTaskScreen = new al(this.c, switchSlotTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   private void b(WorldTemplate selectedWorldTemplate) {
      bi.h startMinigameTask = new bi.h(this.d.id, selectedWorldTemplate, this.b());
      al longRunningMcoTaskScreen = new al(this.c, startMinigameTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public ad b() {
      return new ad(this.c, this.e);
   }
}
