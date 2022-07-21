package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsPlayerScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String OP_ICON_LOCATION = "realms:textures/gui/realms/op_icon.png";
   private static final String USER_ICON_LOCATION = "realms:textures/gui/realms/user_icon.png";
   private static final String CROSS_ICON_LOCATION = "realms:textures/gui/realms/cross_player_icon.png";
   private String toolTip;
   private final RealmsConfigureWorldScreen lastScreen;
   private final RealmsServer serverData;
   private RealmsPlayerScreen.InvitedSelectionList invitedSelectionList;
   private int column1_x;
   private int column_width;
   private int column2_x;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_INVITE_ID = 1;
   private static final int BUTTON_UNINVITE_ID = 2;
   private static final int BUTTON_ACTIVITY_ID = 3;
   private int selectedInvitedIndex = -1;
   private String selectedInvited;
   private boolean stateChanged;

   public RealmsPlayerScreen(RealmsConfigureWorldScreen lastScreen, RealmsServer serverData) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
   }

   public void tick() {
      super.tick();
   }

   public void init() {
      this.column1_x = this.width() / 2 - 160;
      this.column_width = 150;
      this.column2_x = this.width() / 2 + 12;
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(
         new RealmsButton(1, this.column2_x, RealmsConstants.row(1), this.column_width + 10, 20, getLocalizedString("mco.configure.world.buttons.invite")) {
            public void onPress() {
               Realms.setScreen(new RealmsInviteScreen(RealmsPlayerScreen.this.lastScreen, RealmsPlayerScreen.this, RealmsPlayerScreen.this.serverData));
            }
         }
      );
      RealmsButton activityFeedButton = new RealmsButton(
         3, this.column2_x, RealmsConstants.row(3), this.column_width + 10, 20, getLocalizedString("mco.configure.world.buttons.activity")
      ) {
         public void onPress() {
            Realms.setScreen(new RealmsActivityScreen(RealmsPlayerScreen.this, RealmsPlayerScreen.this.serverData));
         }
      };
      activityFeedButton.active(false);
      this.buttonsAdd(activityFeedButton);
      this.buttonsAdd(
         new RealmsButton(
            0, this.column2_x + this.column_width / 2 + 2, RealmsConstants.row(12), this.column_width / 2 + 10 - 2, 20, getLocalizedString("gui.back")
         ) {
            public void onPress() {
               RealmsPlayerScreen.this.backButtonClicked();
            }
         }
      );
      this.invitedSelectionList = new RealmsPlayerScreen.InvitedSelectionList();
      this.invitedSelectionList.setLeftPos(this.column1_x);
      this.addWidget(this.invitedSelectionList);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void backButtonClicked() {
      if (this.stateChanged) {
         Realms.setScreen(this.lastScreen.getNewScreen());
      } else {
         Realms.setScreen(this.lastScreen);
      }

   }

   private void op(int index) {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(index)).getUuid();

      try {
         this.updateOps(client.op(this.serverData.id, selectedInvite));
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't op the user");
      }

   }

   private void deop(int index) {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(index)).getUuid();

      try {
         this.updateOps(client.deop(this.serverData.id, selectedInvite));
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't deop the user");
      }

   }

   private void updateOps(Ops ops) {
      for(PlayerInfo playerInfo : this.serverData.players) {
         playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
      }

   }

   private void uninvite(int index) {
      if (index >= 0 && index < this.serverData.players.size()) {
         PlayerInfo playerInfo = (PlayerInfo)this.serverData.players.get(index);
         this.selectedInvited = playerInfo.getUuid();
         this.selectedInvitedIndex = index;
         RealmsConfirmScreen confirmScreen = new RealmsConfirmScreen(
            this, "Question", getLocalizedString("mco.configure.world.uninvite.question") + " '" + playerInfo.getName() + "' ?", 2
         );
         Realms.setScreen(confirmScreen);
      }

   }

   public void confirmResult(boolean result, int id) {
      if (id == 2) {
         if (result) {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               client.uninvite(this.serverData.id, this.selectedInvited);
            } catch (RealmsServiceException var5) {
               LOGGER.error("Couldn't uninvite user");
            }

            this.deleteFromInvitedList(this.selectedInvitedIndex);
         }

         this.stateChanged = true;
         Realms.setScreen(this);
      }

   }

   private void deleteFromInvitedList(int selectedInvitedIndex) {
      this.serverData.players.remove(selectedInvitedIndex);
   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      if (this.invitedSelectionList != null) {
         this.invitedSelectionList.render(xm, ym, a);
      }

      int bottomBorder = RealmsConstants.row(12) + 20;
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      Tezzelator t = Tezzelator.instance;
      bind("textures/gui/options_background.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
      t.vertex(0.0, (double)this.height(), 0.0).tex(0.0, (double)((float)(this.height() - bottomBorder) / 32.0F + 0.0F)).color(64, 64, 64, 255).endVertex();
      t.vertex((double)this.width(), (double)this.height(), 0.0)
         .tex((double)((float)this.width() / 32.0F), (double)((float)(this.height() - bottomBorder) / 32.0F + 0.0F))
         .color(64, 64, 64, 255)
         .endVertex();
      t.vertex((double)this.width(), (double)bottomBorder, 0.0).tex((double)((float)this.width() / 32.0F), 0.0).color(64, 64, 64, 255).endVertex();
      t.vertex(0.0, (double)bottomBorder, 0.0).tex(0.0, 0.0).color(64, 64, 64, 255).endVertex();
      t.end();
      this.drawCenteredString(getLocalizedString("mco.configure.world.players.title"), this.width() / 2, 17, 16777215);
      if (this.serverData != null && this.serverData.players != null) {
         this.drawString(
            getLocalizedString("mco.configure.world.invited") + " (" + this.serverData.players.size() + ")", this.column1_x, RealmsConstants.row(0), 10526880
         );
      } else {
         this.drawString(getLocalizedString("mco.configure.world.invited"), this.column1_x, RealmsConstants.row(0), 10526880);
      }

      super.render(xm, ym, a);
      if (this.serverData != null) {
         if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, xm, ym);
         }

      }
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

   private void drawRemoveIcon(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < RealmsConstants.row(12) + 20 && ym > RealmsConstants.row(1);
      bind("realms:textures/gui/realms/cross_player_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 7.0F : 0.0F, 8, 7, 8.0F, 14.0F);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.remove.tooltip");
      }

   }

   private void drawOpped(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < RealmsConstants.row(12) + 20 && ym > RealmsConstants.row(1);
      bind("realms:textures/gui/realms/op_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 8.0F : 0.0F, 8, 8, 8.0F, 16.0F);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.ops.tooltip");
      }

   }

   private void drawNormal(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < RealmsConstants.row(12) + 20 && ym > RealmsConstants.row(1);
      bind("realms:textures/gui/realms/user_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 8.0F : 0.0F, 8, 8, 8.0F, 16.0F);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.normal.tooltip");
      }

   }

   private class InvitedSelectionList extends RealmsClickableScrolledSelectionList {
      public InvitedSelectionList() {
         super(RealmsPlayerScreen.this.column_width + 10, RealmsConstants.row(12) + 20, RealmsConstants.row(1), RealmsConstants.row(12) + 20, 13);
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = RealmsPlayerScreen.this.column1_x;
            int x1 = RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width;
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

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot >= 0 && slot <= RealmsPlayerScreen.this.serverData.players.size() && RealmsPlayerScreen.this.toolTip != null) {
            if (!RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip"))
               && !RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.normal.tooltip"))) {
               if (RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip"))) {
                  RealmsPlayerScreen.this.uninvite(slot);
               }
            } else if (((PlayerInfo)RealmsPlayerScreen.this.serverData.players.get(slot)).isOperator()) {
               RealmsPlayerScreen.this.deop(slot);
            } else {
               RealmsPlayerScreen.this.op(slot);
            }

         }
      }

      public void renderBackground() {
         RealmsPlayerScreen.this.renderBackground();
      }

      public int getScrollbarPosition() {
         return RealmsPlayerScreen.this.column1_x + this.width() - 5;
      }

      public int getItemCount() {
         return RealmsPlayerScreen.this.serverData == null ? 1 : RealmsPlayerScreen.this.serverData.players.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 13;
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         if (RealmsPlayerScreen.this.serverData != null) {
            if (i < RealmsPlayerScreen.this.serverData.players.size()) {
               this.renderInvitedItem(i, x, y, h, mouseX, mouseY);
            }

         }
      }

      private void renderInvitedItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         PlayerInfo invited = (PlayerInfo)RealmsPlayerScreen.this.serverData.players.get(i);
         int inviteColor;
         if (!invited.getAccepted()) {
            inviteColor = 10526880;
         } else if (invited.getOnline()) {
            inviteColor = 8388479;
         } else {
            inviteColor = 16777215;
         }

         RealmsPlayerScreen.this.drawString(invited.getName(), RealmsPlayerScreen.this.column1_x + 3 + 12, y + 1, inviteColor);
         if (invited.isOperator()) {
            RealmsPlayerScreen.this.drawOpped(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, y + 1, mouseX, mouseY);
         } else {
            RealmsPlayerScreen.this.drawNormal(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, y + 1, mouseX, mouseY);
         }

         RealmsPlayerScreen.this.drawRemoveIcon(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 22, y + 2, mouseX, mouseY);
         RealmsPlayerScreen.this.drawString(
            RealmsScreen.getLocalizedString("mco.configure.world.activityfeed.disabled"), RealmsPlayerScreen.this.column2_x, RealmsConstants.row(5), 10526880
         );
         RealmsTextureManager.withBoundFace(invited.getUuid(), () -> {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, y + 1, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
            RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, y + 1, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
         });
      }
   }
}
