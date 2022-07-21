package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsPendingInvitesScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_ACCEPT_ID = 1;
   private static final int BUTTON_REJECT_ID = 2;
   private static final String ACCEPT_ICON_LOCATION = "realms:textures/gui/realms/accept_icon.png";
   private static final String REJECT_ICON_LOCATION = "realms:textures/gui/realms/reject_icon.png";
   private final RealmsScreen lastScreen;
   private String toolTip;
   private boolean loaded;
   private RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
   private List<PendingInvite> pendingInvites = Lists.newArrayList();
   private RealmsLabel titleLabel;
   private int selectedInvite = -1;
   private RealmsButton acceptButton;
   private RealmsButton rejectButton;

   public RealmsPendingInvitesScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsPendingInvitesScreen.this.pendingInvites = client.pendingInvites().pendingInvites;

               for(PendingInvite invite : RealmsPendingInvitesScreen.this.pendingInvites) {
                  RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.addEntry(invite);
               }
            } catch (RealmsServiceException var7) {
               RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
            } finally {
               RealmsPendingInvitesScreen.this.loaded = true;
            }

         }
      }).start();
      this.buttonsAdd(
         this.acceptButton = new RealmsButton(1, this.width() / 2 - 174, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.accept")) {
            public void onPress() {
               RealmsPendingInvitesScreen.this.accept(RealmsPendingInvitesScreen.this.selectedInvite);
               RealmsPendingInvitesScreen.this.selectedInvite = -1;
               RealmsPendingInvitesScreen.this.updateButtonStates();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, this.height() - 32, 100, 20, getLocalizedString("gui.done")) {
         public void onPress() {
            Realms.setScreen(new RealmsMainScreen(RealmsPendingInvitesScreen.this.lastScreen));
         }
      });
      this.buttonsAdd(
         this.rejectButton = new RealmsButton(2, this.width() / 2 + 74, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.reject")) {
            public void onPress() {
               RealmsPendingInvitesScreen.this.reject(RealmsPendingInvitesScreen.this.selectedInvite);
               RealmsPendingInvitesScreen.this.selectedInvite = -1;
               RealmsPendingInvitesScreen.this.updateButtonStates();
            }
         }
      );
      this.titleLabel = new RealmsLabel(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
      this.addWidget(this.titleLabel);
      this.addWidget(this.pendingInvitationSelectionList);
      this.narrateLabels();
      this.updateButtonStates();
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
      this.pendingInvitationSelectionList.removeAtIndex(slot);
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
      this.pendingInvitationSelectionList.render(xm, ym, a);
      this.titleLabel.render(this);
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

   private void updateButtonStates() {
      this.acceptButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
      this.rejectButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
   }

   private boolean shouldAcceptAndRejectButtonBeVisible(int invite) {
      return invite != -1;
   }

   private class PendingInvitationSelectionList extends RealmsObjectSelectionList {
      public PendingInvitationSelectionList() {
         super(RealmsPendingInvitesScreen.this.width() + 50, RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 40, 36);
      }

      public void addEntry(PendingInvite pendingInvite) {
         this.addEntry(RealmsPendingInvitesScreen.this.new PendingInvitationSelectionListEntry(pendingInvite));
      }

      public void removeAtIndex(int index) {
         this.remove(index);
      }

      public int getItemCount() {
         return RealmsPendingInvitesScreen.this.pendingInvites.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public boolean isFocused() {
         return RealmsPendingInvitesScreen.this.isFocused(this);
      }

      public void renderBackground() {
         RealmsPendingInvitesScreen.this.renderBackground();
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               this.selectItem(slot, buttonNum, xm, ym);
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }

      protected void moveSelection(int dir) {
         int index = this.getSelectedIndex();
         int newIndex = RealmsMth.clamp(index + dir, 0, this.getItemCount() - 1);
         super.moveSelection(dir);
         this.selectItem(newIndex, 0, 0.0, 0.0);
         RealmsPendingInvitesScreen.this.updateButtonStates();
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(
               RealmsScreen.getLocalizedString(
                  "narrator.select", new Object[]{((PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(item)).worldName}
               )
            );
         }

         return this.selectInviteListItem(item, buttonNum, xMouse, yMouse);
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         int x = this.getScrollbarPosition() - 50;
         int y = clickSlotPos + 30 - this.getScroll();
         if (xm >= (double)x && xm <= (double)(x + 25) && ym >= (double)y && ym <= (double)(y + 15)) {
            RealmsPendingInvitesScreen.this.accept(slot);
         } else if (xm >= (double)(x + 41) && xm <= (double)(x + 41 + 15) && ym >= (double)y && ym <= (double)(y + 15)) {
            RealmsPendingInvitesScreen.this.reject(slot);
         }

      }

      public boolean selectInviteListItem(int item, int buttonNum, double xMouse, double yMouse) {
         if (buttonNum != 0) {
            return false;
         } else {
            RealmsPendingInvitesScreen.this.selectedInvite = item;
            RealmsPendingInvitesScreen.this.updateButtonStates();
            return true;
         }
      }
   }

   private class PendingInvitationSelectionListEntry extends RealmListEntry {
      final PendingInvite mPendingInvite;

      public PendingInvitationSelectionListEntry(PendingInvite pendingInvite) {
         this.mPendingInvite = pendingInvite;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderPendingInvitationItem(this.mPendingInvite, rowLeft, rowTop, mouseX, mouseY);
      }

      private void renderPendingInvitationItem(PendingInvite invite, int x, int y, int mouseX, int mouseY) {
         RealmsPendingInvitesScreen.this.drawString(invite.worldName, x + 2, y + 1, 16777215);
         RealmsPendingInvitesScreen.this.drawString(invite.worldOwnerName, x + 2, y + 12, 7105644);
         RealmsPendingInvitesScreen.this.drawString(
            RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - invite.date.getTime()), x + 2, y + 24, 7105644
         );
         int dx = 330;
         this.drawAccept(330, y + 5, mouseX, mouseY);
         this.drawReject(350, y + 5, mouseX, mouseY);
         RealmsTextureManager.withBoundFace(invite.worldOwnerUuid, () -> {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
         });
      }

      private void drawAccept(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < RealmsPendingInvitesScreen.this.height() - 40 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
         GlStateManager.popMatrix();
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
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
         GlStateManager.popMatrix();
         if (hovered) {
            RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.reject");
         }

      }
   }
}
