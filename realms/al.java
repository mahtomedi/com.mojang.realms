package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;

public class al extends RealmsScreen {
   private static final v a = new v();
   private volatile int b;
   private static boolean c;
   private static boolean d;
   private static boolean e;
   private static boolean f;
   private static final List<v.d> g = Arrays.asList(v.d.b, v.d.c, v.d.e);

   public al(RealmsScreen lastScreen) {
   }

   public void init() {
      this.a();
      this.setKeyboardHandlerSendRepeatsToGui(true);
   }

   public void tick() {
      if ((!Realms.getRealmsNotificationsEnabled() || !Realms.inTitleScreen() || !e) && !a.a()) {
         a.k();
      } else if (e && Realms.getRealmsNotificationsEnabled()) {
         a.a(g);
         if (a.a(v.d.b)) {
            this.b = a.f();
         }

         if (a.a(v.d.c)) {
            d = a.g();
         }

         if (a.a(v.d.e)) {
            f = a.i();
         }

         a.c();
      }
   }

   private void a() {
      if (!c) {
         c = true;
         (new Thread("Realms Notification Availability checker #1") {
            public void run() {
               g client = realms.g.a();

               try {
                  g.a versionResponse = client.i();
                  if (!versionResponse.equals(g.a.a)) {
                     return;
                  }
               } catch (o var3) {
                  if (var3.a != 401) {
                     al.c = false;
                  }

                  return;
               } catch (IOException var4) {
                  al.c = false;
                  return;
               }

               al.e = true;
            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      if (e) {
         this.a(xm, ym);
      }

      super.render(xm, ym, a);
   }

   public boolean mouseClicked(double xm, double ym, int button) {
      return super.mouseClicked(xm, ym, button);
   }

   private void a(int xm, int ym) {
      int pendingInvitesCount = this.b;
      int spacing = 24;
      int topPos = this.height() / 4 + 48;
      int baseX = this.width() / 2 + 80;
      int baseY = topPos + 48 + 2;
      int iconOffset = 0;
      if (f) {
         RealmsScreen.bind("realms:textures/gui/realms/news_notification_mainscreen.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.4F, 0.4F, 0.4F);
         RealmsScreen.blit((int)((double)(baseX + 2 - iconOffset) * 2.5), (int)((double)baseY * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
         GlStateManager.popMatrix();
         iconOffset += 14;
      }

      if (pendingInvitesCount != 0) {
         RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(baseX - iconOffset, baseY - 6, 0.0F, 0.0F, 15, 25, 31, 25);
         GlStateManager.popMatrix();
         iconOffset += 16;
      }

      if (d) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         int ySprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            ySprite = 8;
         }

         RealmsScreen.blit(baseX + 4 - iconOffset, baseY + 4, 0.0F, (float)ySprite, 8, 8, 8, 16);
         GlStateManager.popMatrix();
      }

   }

   public void removed() {
      a.k();
   }
}
