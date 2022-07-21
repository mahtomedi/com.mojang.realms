package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class RealmsPendingInvitesScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BUTTON_BACK_ID = 0;
   private static final String ACCEPT_ICON_LOCATION = "realms:textures/gui/realms/accept_icon.png";
   private static final String REJECT_ICON_LOCATION = "realms:textures/gui/realms/reject_icon.png";
   private final RealmsScreen lastScreen;
   private String toolTip;
   private boolean loaded;
   private RealmsPendingInvitesScreen.PendingInvitationList pendingList;
   private List<PendingInvite> pendingInvites = Lists.newArrayList();

   public RealmsPendingInvitesScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.pendingList = new RealmsPendingInvitesScreen.PendingInvitationList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsPendingInvitesScreen.this.pendingInvites = client.pendingInvites().pendingInvites;
            } catch (RealmsServiceException var6) {
               RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
            } finally {
               RealmsPendingInvitesScreen.this.loaded = true;
            }

         }
      }).start();
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 75, this.height() - 32, 153, 20, getLocalizedString("gui.done")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(new RealmsMainScreen(RealmsPendingInvitesScreen.this.lastScreen));
         }
      });
      this.addWidget(this.pendingList);
      this.focusOn(this.pendingList);
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(new RealmsMainScreen(this.lastScreen));
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void updateList(int slot) {
      this.pendingInvites.remove(slot);
   }

   private void reject(final int slot) {
      if (slot < this.pendingInvites.size()) {
         (new Thread("Realms-reject-invitation") {
            public void run() {
               try {
                  RealmsClient client = RealmsClient.createRealmsClient();
                  client.rejectInvitation(((PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(slot)).invitationId);
                  RealmsPendingInvitesScreen.this.updateList(slot);
               } catch (RealmsServiceException var2) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
               }

            }
         }).start();
      }

   }

   private void accept(final int slot) {
      if (slot < this.pendingInvites.size()) {
         (new Thread("Realms-accept-invitation") {
            public void run() {
               try {
                  RealmsClient client = RealmsClient.createRealmsClient();
                  client.acceptInvitation(((PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(slot)).invitationId);
                  RealmsPendingInvitesScreen.this.updateList(slot);
               } catch (RealmsServiceException var2) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
               }

            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.pendingList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

      if (this.pendingInvites.size() == 0 && this.loaded) {
         this.drawCenteredString(getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 16777215);
      }

      super.render(xm, ym, a);
   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private class PendingInvitationList extends RealmsClickableScrolledSelectionList {
      public PendingInvitationList() {
         super(RealmsPendingInvitesScreen.this.width() + 50, RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 40, 36);
      }

      public int getItemCount() {
         return RealmsPendingInvitesScreen.this.pendingInvites.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         RealmsPendingInvitesScreen.this.renderBackground();
      }

      public void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = this.getScrollbarPosition() - 290;
         int x1 = this.getScrollbarPosition() - 10;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3553);
         t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
         t.vertex((double)x0, (double)(y + h + 2), 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y + h + 2), 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y - 2), 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x0, (double)(y - 2), 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y + h + 1), 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y + h + 1), 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y - 1), 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y - 1), 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.end();
         GL11.glEnable(3553);
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         if (i < RealmsPendingInvitesScreen.this.pendingInvites.size()) {
            this.renderPendingInvitationItem(i, x, y, h, mouseX, mouseY);
         }

      }

      private void renderPendingInvitationItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         PendingInvite invite = (PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(i);
         RealmsPendingInvitesScreen.this.drawString(invite.worldName, x + 2, y + 1, 16777215);
         RealmsPendingInvitesScreen.this.drawString(invite.worldOwnerName, x + 2, y + 12, 7105644);
         RealmsPendingInvitesScreen.this.drawString(
            RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - invite.date.getTime()), x + 2, y + 24, 7105644
         );
         int dx = this.getScrollbarPosition() - 50;
         this.drawAccept(dx, y, mouseX, mouseY);
         this.drawReject(dx + 20, y, mouseX, mouseY);
         RealmsTextureManager.withBoundFace(invite.worldOwnerUuid, () -> {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
            RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
         });
      }

      private void drawAccept(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < RealmsPendingInvitesScreen.this.height() - 40 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37.0F, 18.0F);
         GL11.glPopMatrix();
         if (hovered) {
            RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.accept");
         }

      }

      private void drawReject(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < RealmsPendingInvitesScreen.this.height() - 40 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/reject_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37.0F, 18.0F);
         GL11.glPopMatrix();
         if (hovered) {
            RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.reject");
         }

      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         int x = this.getScrollbarPosition() - 50;
         int y = clickSlotPos + 30 - this.getScroll();
         if (xm >= (double)x && xm <= (double)(x + 15) && ym >= (double)y && ym <= (double)(y + 15)) {
            RealmsPendingInvitesScreen.this.accept(slot);
         } else if (xm >= (double)(x + 20) && xm <= (double)(x + 20 + 15) && ym >= (double)y && ym <= (double)(y + 15)) {
            RealmsPendingInvitesScreen.this.reject(slot);
         }

      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + (int)this.yo() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }
   }
}
