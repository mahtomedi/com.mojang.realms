package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConfirmResultListener;
import java.io.IOException;
import java.util.Set;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsConfigureWorldScreen extends RealmsScreen implements RealmsConfirmResultListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String TOGGLE_ON_ICON_LOCATION = "realms:textures/gui/realms/toggle_on_icon.png";
   private static final String TOGGLE_OFF_ICON_LOCATION = "realms:textures/gui/realms/toggle_off_icon.png";
   private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
   private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
   private static final String OP_ICON_LOCATION = "realms:textures/gui/realms/op_icon.png";
   private static final String USER_ICON_LOCATION = "realms:textures/gui/realms/user_icon.png";
   private static final String CROSS_ICON_LOCATION = "realms:textures/gui/realms/cross_icon.png";
   private String toolTip;
   private final RealmsScreen lastScreen;
   private RealmsServer serverData;
   private volatile long serverId;
   private RealmsConfigureWorldScreen.InvitedSelectionList invitedSelectionList;
   private int column1_x;
   private int column_width;
   private int column2_x;
   private static final int BUTTON_OPEN_ID = 0;
   private static final int BUTTON_CLOSE_ID = 1;
   private static final int BUTTON_UNINVITE_ID = 3;
   private static final int BUTTON_SETTING_ID = 5;
   private static final int BUTTON_SUBSCRIPTION_ID = 7;
   private static final int BUTTON_MINIGAME_ID = 8;
   private static final int BUTTON_BACK_ID = 10;
   private static final int BUTTON_ACTIVITY_ID = 11;
   private static final int BUTTON_EDIT_WORLD_ID = 12;
   private static final int BUTTON_INVITE_ID = 13;
   private int selectedInvitedIndex = -1;
   private String selectedInvited;
   private RealmsButton settingsButton;
   private RealmsButton minigameButton;
   private RealmsButton subscriptionButton;
   private RealmsButton editWorldButton;
   private RealmsButton activityButton;
   private RealmsButton inviteButton;
   private boolean stateChanged;
   private boolean openButtonHovered = false;
   private boolean closeButtonHovered = false;
   private volatile Set<String> ops;

   public RealmsConfigureWorldScreen(RealmsScreen lastScreen, long serverId) {
      this.lastScreen = lastScreen;
      this.serverId = serverId;
   }

   public void mouseEvent() {
      super.mouseEvent();
      if (this.invitedSelectionList != null) {
         this.invitedSelectionList.mouseEvent();
      }

   }

   public void tick() {
      super.tick();
   }

   public void init() {
      this.getOwnWorld(this.serverId);
      this.column1_x = this.width() / 2 - 160;
      this.column_width = 150;
      this.column2_x = this.width() / 2 + 12;
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(
         this.settingsButton = newButton(5, this.column2_x, this.row(6), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.edit"))
      );
      this.buttonsAdd(
         this.editWorldButton = newButton(12, this.column2_x, this.row(4), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.editworld"))
      );
      this.buttonsAdd(
         this.minigameButton = newButton(8, this.column2_x, this.row(2), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.startMiniGame"))
      );
      this.buttonsAdd(
         this.subscriptionButton = newButton(
            7, this.column2_x, this.row(8), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.subscription")
         )
      );
      this.buttonsAdd(
         this.activityButton = newButton(
            11, this.column1_x, this.row(12), this.column_width + 10, 20, getLocalizedString("mco.configure.world.buttons.activity")
         )
      );
      this.buttonsAdd(this.inviteButton = newButton(13, this.column1_x, this.row(10), this.column_width + 10, 20, "+"));
      this.buttonsAdd(newButton(10, this.column2_x + this.column_width / 2 + 2, this.row(12), this.column_width / 2 - 2, 20, getLocalizedString("gui.back")));
      this.invitedSelectionList = new RealmsConfigureWorldScreen.InvitedSelectionList();
      this.invitedSelectionList.setLeftPos(this.column1_x);
      this.settingsButton.active(false);
      this.editWorldButton.active(false);
      this.minigameButton.active(false);
      this.subscriptionButton.active(false);
      this.activityButton.active(false);
      this.inviteButton.active(false);
   }

   private void fetchOps() {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsConfigureWorldScreen.this.ops = client.getOpsFor(RealmsConfigureWorldScreen.this.serverId).ops;
            } catch (RealmsServiceException var3) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't fetch ops");
            } catch (Exception var4) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't parse response of fetching ops");
            }

         }
      }).start();
   }

   private int row(int i) {
      return 40 + i * 13;
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 10) {
            this.backButtonClicked();
         } else if (button.id() == 5) {
            Realms.setScreen(new RealmsEditRealmsWorldScreen(this, this.lastScreen, this.serverData.clone()));
         } else if (button.id() == 1) {
            String line2 = getLocalizedString("mco.configure.world.close.question.line1");
            String line3 = getLocalizedString("mco.configure.world.close.question.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 1));
         } else if (button.id() == 0) {
            this.openTheWorld();
         } else if (button.id() == 8) {
            if (this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
               Realms.setScreen(new RealmsModifyMinigameWorldScreen(this, this.serverData));
            } else {
               Realms.setScreen(new RealmsStartMinigameWorldScreen(this, this.serverData));
            }
         } else if (button.id() == 7) {
            Realms.setScreen(new RealmsSubscriptionScreen(this, this.serverData));
         } else if (button.id() == 11) {
            Realms.setScreen(new RealmsActivityScreen(this, this.serverData.id));
         } else if (button.id() == 12) {
            Realms.setScreen(new RealmsWorldManagementScreen(this, this.lastScreen, this.serverData.clone()));
         } else if (button.id() == 13) {
            Realms.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData));
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         this.backButtonClicked();
      }

   }

   private void backButtonClicked() {
      if (this.stateChanged) {
         ((RealmsMainScreen)this.lastScreen).removeSelection();
      }

      Realms.setScreen(this.lastScreen);
   }

   private void getOwnWorld(final long worldId) {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsConfigureWorldScreen.this.serverData = client.getOwnWorld(worldId);
               RealmsConfigureWorldScreen.this.fetchOps();
               RealmsConfigureWorldScreen.this.settingsButton.active(!RealmsConfigureWorldScreen.this.serverData.expired);
               RealmsConfigureWorldScreen.this.minigameButton.active(!RealmsConfigureWorldScreen.this.serverData.expired);
               RealmsConfigureWorldScreen.this.subscriptionButton.active(true);
               RealmsConfigureWorldScreen.this.activityButton.active(true);
               RealmsConfigureWorldScreen.this.editWorldButton.active(true);
               boolean isMinigame = RealmsConfigureWorldScreen.this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
               if (isMinigame) {
                  RealmsConfigureWorldScreen.this.minigameButton.msg(RealmsScreen.getLocalizedString("mco.configure.world.buttons.modifyMiniGame"));
                  RealmsConfigureWorldScreen.this.settingsButton.active(false);
                  RealmsConfigureWorldScreen.this.editWorldButton.active(false);
               }
            } catch (RealmsServiceException var3) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't get own world");
            } catch (IOException var4) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   private void op(int index) {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(index)).getName();

      try {
         client.op(this.serverData.id, selectedInvite);
         this.fetchOps();
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't op the user");
      }

   }

   private void deop(int index) {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(index)).getName();

      try {
         client.deop(this.serverData.id, selectedInvite);
         this.fetchOps();
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't deop the user");
      }

   }

   private void openTheWorld() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         Boolean openResult = client.open(this.serverData.id);
         if (openResult) {
            this.stateChanged = true;
            this.serverData.state = RealmsServer.State.OPEN;
            this.init();
         }
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't open world");
      } catch (IOException var4) {
         LOGGER.error("Could not parse response opening world");
      }

   }

   private void closeTheWorld() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         boolean closeResult = client.close(this.serverData.id);
         if (closeResult) {
            this.stateChanged = true;
            this.serverData.state = RealmsServer.State.CLOSED;
            this.init();
         }
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't close world");
      } catch (IOException var4) {
         LOGGER.error("Could not parse response closing world");
      }

   }

   private void uninvite(int index) {
      if (index >= 0 && index < this.serverData.players.size()) {
         PlayerInfo playerInfo = (PlayerInfo)this.serverData.players.get(index);
         this.selectedInvited = playerInfo.getUuid();
         this.selectedInvitedIndex = index;
         RealmsConfirmScreen confirmScreen = new RealmsConfirmScreen(
            this, "Question", getLocalizedString("mco.configure.world.uninvite.question") + " '" + playerInfo.getName() + "'", 3
         );
         Realms.setScreen(confirmScreen);
      }

   }

   @Override
   public void confirmResult(boolean result, int id) {
      if (id == 3) {
         if (result) {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               client.uninvite(this.serverData.id, this.selectedInvited);
            } catch (RealmsServiceException var5) {
               LOGGER.error("Couldn't uninvite user");
            }

            this.deleteFromInvitedList(this.selectedInvitedIndex);
         }

         Realms.setScreen(new RealmsConfigureWorldScreen(this.lastScreen, this.serverData.id));
      } else if (id == 1) {
         if (result) {
            this.closeTheWorld();
         }

         Realms.setScreen(this);
      }

   }

   private void deleteFromInvitedList(int selectedInvitedIndex) {
      this.serverData.players.remove(selectedInvitedIndex);
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      if (this.closeButtonHovered && !this.serverData.expired) {
         this.openTheWorld();
      } else if (this.openButtonHovered && !this.serverData.expired) {
         String line2 = getLocalizedString("mco.configure.world.close.question.line1");
         String line3 = getLocalizedString("mco.configure.world.close.question.line2");
         Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 1));
      }

      super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.closeButtonHovered = false;
      this.openButtonHovered = false;
      this.renderBackground();
      if (this.invitedSelectionList != null) {
         this.invitedSelectionList.render(xm, ym, a);
      }

      GL11.glDisable(2896);
      GL11.glDisable(2912);
      Tezzelator t = Tezzelator.instance;
      bind("textures/gui/options_background.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(4210752);
      t.vertexUV(0.0, (double)this.height(), 0.0, 0.0, (double)((float)(this.height() - this.row(10)) / s + 0.0F));
      t.vertexUV(
         (double)this.width(), (double)this.height(), 0.0, (double)((float)this.width() / s), (double)((float)(this.height() - this.row(10)) / s + 0.0F)
      );
      t.vertexUV((double)this.width(), (double)this.row(10), 0.0, (double)((float)this.width() / s), 0.0);
      t.vertexUV(0.0, (double)this.row(10), 0.0, 0.0, 0.0);
      t.end();
      this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 17, 16777215);
      if (this.serverData != null && this.serverData.players != null) {
         this.drawString(
            getLocalizedString("mco.configure.world.invited") + " (" + this.serverData.players.size() + "/20)", this.column1_x, this.row(1), 10526880
         );
         this.inviteButton.active(this.serverData.players.size() < 20);
      } else {
         this.drawString(getLocalizedString("mco.configure.world.invited"), this.column1_x, this.row(1), 10526880);
         this.inviteButton.active(false);
      }

      super.render(xm, ym, a);
      if (this.serverData != null) {
         String name = this.serverData.getName();
         int nameWidth = this.fontWidth(name);
         if (this.serverData.state == RealmsServer.State.OPEN) {
            this.drawCenteredString(name, this.width() / 2, 30, 8388479);
         } else if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.drawCenteredString(name, this.width() / 2, 30, 13421772);
         } else {
            this.drawCenteredString(name, this.width() / 2, 30, 8388479);
         }

         int statusX = this.width() / 2 - nameWidth / 2 - 13;
         this.drawServerStatus(statusX, 30, xm, ym);
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
         this.fontDrawShadow(msg, rx, ry, -1);
      }
   }

   private void drawServerStatus(int x, int y, int xm, int ym) {
      if (this.serverData.expired) {
         this.drawExpired(x, y, xm, ym);
      } else if (this.serverData.state == RealmsServer.State.CLOSED) {
         this.drawClose(x, y, xm, ym);
      } else if (this.serverData.state == RealmsServer.State.OPEN) {
         this.drawOpen(x, y, xm, ym);
      } else if (this.serverData.state == RealmsServer.State.ADMIN_LOCK) {
         this.drawLocked(x, y, xm, ym);
      }

   }

   private void drawRemoveIcon(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/cross_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 8, 7, 8.0F, 7.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.remove.tooltip");
      }

   }

   private void drawOpped(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/op_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 8, 8, 8.0F, 8.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.ops.tooltip");
      }

   }

   private void drawNormal(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/user_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 8, 8, 8.0F, 8.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.configure.world.invites.normal.tooltip");
      }

   }

   private void drawExpired(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/expired_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void drawOpen(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/toggle_on_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      int xx = x - 12;
      RealmsScreen.blit(xx * 2, y * 2, 0.0F, 0.0F, 32, 16, 32.0F, 16.0F);
      GL11.glPopMatrix();
      if (xm >= xx && xm <= xx + 16 && ym >= y && ym <= y + 8) {
         this.toolTip = getLocalizedString("mco.selectServer.closeserver");
         this.openButtonHovered = true;
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/toggle_off_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      int xx = x - 12;
      RealmsScreen.blit(xx * 2, y * 2, 0.0F, 0.0F, 32, 16, 32.0F, 16.0F);
      GL11.glPopMatrix();
      if (xm >= xx && xm <= xx + 16 && ym >= y && ym <= y + 8) {
         this.toolTip = getLocalizedString("mco.selectServer.openserver");
         this.closeButtonHovered = true;
      }

   }

   private void drawLocked(int x, int y, int xm, int ym) {
      bind("realms:textures/gui/realms/off_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.locked");
      }

   }

   private class InvitedSelectionList extends RealmsClickableScrolledSelectionList {
      public InvitedSelectionList() {
         super(
            RealmsConfigureWorldScreen.this.column_width + 10,
            RealmsConfigureWorldScreen.this.row(10),
            RealmsConfigureWorldScreen.this.row(2),
            RealmsConfigureWorldScreen.this.row(10),
            13
         );
      }

      public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
         if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
            int x0 = RealmsConfigureWorldScreen.this.column1_x;
            int x1 = RealmsConfigureWorldScreen.this.column1_x + RealmsConfigureWorldScreen.this.column_width;
            int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
            int slot = clickSlotPos / itemHeight;
            if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
            }
         }

      }

      public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
         int removex = RealmsConfigureWorldScreen.this.column1_x + RealmsConfigureWorldScreen.this.column_width - 22;
         int removey = clickSlotPos + 70 - this.getScroll();
         int mx = removex + 10;
         int my = removey - 3;
         if (xm >= mx && xm <= mx + 9 && ym >= my && ym <= my + 9) {
            if (slot >= 0 && slot < RealmsConfigureWorldScreen.this.serverData.players.size()) {
               String selectedPlayer = ((PlayerInfo)RealmsConfigureWorldScreen.this.serverData.players.get(slot)).getName();
               if (RealmsConfigureWorldScreen.this.ops != null) {
                  if (RealmsConfigureWorldScreen.this.ops.contains(selectedPlayer)) {
                     RealmsConfigureWorldScreen.this.deop(slot);
                  } else {
                     RealmsConfigureWorldScreen.this.op(slot);
                  }
               }
            }
         } else if (xm >= removex
            && xm <= removex + 9
            && ym >= removey
            && ym <= removey + 9
            && slot >= 0
            && slot < RealmsConfigureWorldScreen.this.serverData.players.size()) {
            RealmsConfigureWorldScreen.this.uninvite(slot);
         }

      }

      public void renderBackground() {
         RealmsConfigureWorldScreen.this.renderBackground();
      }

      public int getScrollbarPosition() {
         return RealmsConfigureWorldScreen.this.column1_x + this.width() - 5;
      }

      public int getItemCount() {
         return RealmsConfigureWorldScreen.this.serverData == null ? 1 : RealmsConfigureWorldScreen.this.serverData.players.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 13;
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         if (RealmsConfigureWorldScreen.this.serverData != null) {
            if (i < RealmsConfigureWorldScreen.this.serverData.players.size()) {
               this.renderInvitedItem(i, x, y, h);
            }

         }
      }

      private void renderInvitedItem(int i, int x, int y, int h) {
         PlayerInfo invited = (PlayerInfo)RealmsConfigureWorldScreen.this.serverData.players.get(i);
         RealmsConfigureWorldScreen.this.drawString(invited.getName(), RealmsConfigureWorldScreen.this.column1_x + 3 + 12, y + 1, 16777215);
         if (RealmsConfigureWorldScreen.this.ops != null && RealmsConfigureWorldScreen.this.ops.contains(invited.getName())) {
            RealmsConfigureWorldScreen.this.drawOpped(
               RealmsConfigureWorldScreen.this.column1_x + RealmsConfigureWorldScreen.this.column_width - 10, y + 1, this.xm(), this.ym()
            );
         } else {
            RealmsConfigureWorldScreen.this.drawNormal(
               RealmsConfigureWorldScreen.this.column1_x + RealmsConfigureWorldScreen.this.column_width - 10, y + 1, this.xm(), this.ym()
            );
         }

         RealmsConfigureWorldScreen.this.drawRemoveIcon(
            RealmsConfigureWorldScreen.this.column1_x + RealmsConfigureWorldScreen.this.column_width - 22, y + 2, this.xm(), this.ym()
         );
         RealmsScreen.bindFace(invited.getUuid(), invited.getName());
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(RealmsConfigureWorldScreen.this.column1_x + 2 + 2, y + 1, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
         RealmsScreen.blit(RealmsConfigureWorldScreen.this.column1_x + 2 + 2, y + 1, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
      }
   }
}
