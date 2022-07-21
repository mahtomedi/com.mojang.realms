package realms;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.PendingInvite;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class an extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private String c;
   private boolean d;
   private an.a e;
   private List<PendingInvite> f = Lists.newArrayList();
   private RealmsLabel g;
   private int h = -1;
   private RealmsButton i;
   private RealmsButton j;

   public an(RealmsScreen lastScreen) {
      this.b = lastScreen;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.e = new an.a();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            g client = realms.g.a();

            try {
               an.this.f = client.k().pendingInvites;

               for(PendingInvite invite : an.this.f) {
                  an.this.e.a(invite);
               }
            } catch (o var7) {
               an.a.error("Couldn't list invites");
            } finally {
               an.this.d = true;
            }

         }
      }).start();
      this.buttonsAdd(this.i = new RealmsButton(1, this.width() / 2 - 174, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.accept")) {
         public void onPress() {
            an.this.c(an.this.h);
            an.this.h = -1;
            an.this.b();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, this.height() - 32, 100, 20, getLocalizedString("gui.done")) {
         public void onPress() {
            Realms.setScreen(new realms.b(an.this.b));
         }
      });
      this.buttonsAdd(this.j = new RealmsButton(2, this.width() / 2 + 74, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.reject")) {
         public void onPress() {
            an.this.b(an.this.h);
            an.this.h = -1;
            an.this.b();
         }
      });
      this.g = new RealmsLabel(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
      this.addWidget(this.g);
      this.addWidget(this.e);
      this.narrateLabels();
      this.b();
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(new realms.b(this.b));
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void a(int slot) {
      this.f.remove(slot);
      this.e.a(slot);
   }

   private void b(final int slot) {
      if (slot < this.f.size()) {
         (new Thread("Realms-reject-invitation") {
            public void run() {
               try {
                  g client = realms.g.a();
                  client.b(((PendingInvite)an.this.f.get(slot)).invitationId);
                  an.this.a(slot);
               } catch (o var2) {
                  an.a.error("Couldn't reject invite");
               }

            }
         }).start();
      }

   }

   private void c(final int slot) {
      if (slot < this.f.size()) {
         (new Thread("Realms-accept-invitation") {
            public void run() {
               try {
                  g client = realms.g.a();
                  client.a(((PendingInvite)an.this.f.get(slot)).invitationId);
                  an.this.a(slot);
               } catch (o var2) {
                  an.a.error("Couldn't accept invite");
               }

            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      this.c = null;
      this.renderBackground();
      this.e.render(xm, ym, a);
      this.g.render(this);
      if (this.c != null) {
         this.a(this.c, xm, ym);
      }

      if (this.f.size() == 0 && this.d) {
         this.drawCenteredString(getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 16777215);
      }

      super.render(xm, ym, a);
   }

   protected void a(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private void b() {
      this.i.setVisible(this.d(this.h));
      this.j.setVisible(this.d(this.h));
   }

   private boolean d(int invite) {
      return invite != -1;
   }

   class a extends RealmsObjectSelectionList {
      public a() {
         super(an.this.width() + 50, an.this.height(), 32, an.this.height() - 40, 36);
      }

      public void a(PendingInvite pendingInvite) {
         this.addEntry(an.this.new b(pendingInvite));
      }

      public void a(int index) {
         this.remove(index);
      }

      public int getItemCount() {
         return an.this.f.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public boolean isFocused() {
         return an.this.isFocused(this);
      }

      public void renderBackground() {
         an.this.renderBackground();
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               this.selectItem(slot);
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((PendingInvite)an.this.f.get(item)).worldName}));
         }

         this.b(item);
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         int x = this.getScrollbarPosition() - 50;
         int y = clickSlotPos + 30 - this.getScroll();
         if (xm >= (double)x && xm <= (double)(x + 25) && ym >= (double)y && ym <= (double)(y + 15)) {
            an.this.c(slot);
         } else if (xm >= (double)(x + 41) && xm <= (double)(x + 41 + 15) && ym >= (double)y && ym <= (double)(y + 15)) {
            an.this.b(slot);
         }

      }

      public void b(int item) {
         an.this.h = item;
         an.this.b();
      }
   }

   class b extends RealmListEntry {
      final PendingInvite a;

      public b(PendingInvite pendingInvite) {
         this.a = pendingInvite;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft, rowTop, mouseX, mouseY);
      }

      private void a(PendingInvite invite, int x, int y, int mouseX, int mouseY) {
         an.this.drawString(invite.worldName, x + 2, y + 1, 16777215);
         an.this.drawString(invite.worldOwnerName, x + 2, y + 12, 7105644);
         an.this.drawString(bj.a(System.currentTimeMillis() - invite.date.getTime()), x + 2, y + 24, 7105644);
         int dx = 330;
         this.a(330, y + 5, mouseX, mouseY);
         this.b(350, y + 5, mouseX, mouseY);
         bi.a(invite.worldOwnerUuid, (Runnable)(() -> {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
         }));
      }

      private void a(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < an.this.height() - 40 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
         GlStateManager.popMatrix();
         if (hovered) {
            an.this.c = RealmsScreen.getLocalizedString("mco.invites.button.accept");
         }

      }

      private void b(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < an.this.height() - 40 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/reject_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
         GlStateManager.popMatrix();
         if (hovered) {
            an.this.c = RealmsScreen.getLocalizedString("mco.invites.button.reject");
         }

      }
   }
}
