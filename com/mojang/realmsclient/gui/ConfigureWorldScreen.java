package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.io.IOException;
import java.util.Set;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class ConfigureWorldScreen extends RealmsScreen implements RealmsConfirmResultListener {
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
   private McoServer serverData;
   private volatile long serverId;
   private ConfigureWorldScreen.InvitedSelectionList invitedSelectionList;
   private int column1_x;
   private int column_width;
   private int column2_x;
   private static final int BUTTON_OPEN_ID = 0;
   private static final int BUTTON_CLOSE_ID = 1;
   private static final int BUTTON_UNINVITE_ID = 3;
   private static final int BUTTON_EDIT_ID = 5;
   private static final int BUTTON_SUBSCRIPTION_ID = 7;
   private static final int BUTTON_MINIGAME_ID = 8;
   private static final int BUTTON_BACK_ID = 10;
   private static final int BUTTON_ACTIVITY_ID = 11;
   private static final int BUTTON_EDIT_WORLD_ID = 12;
   private int selectedInvitedIndex = -1;
   private String selectedInvited;
   private RealmsButton editButton;
   private RealmsButton minigameButton;
   private RealmsButton subscriptionButton;
   private RealmsButton editWorldButton;
   private RealmsButton activityButton;
   private boolean stateChanged;
   private boolean openButtonHovered = false;
   private boolean closeButtonHovered = false;
   private volatile Set<String> ops;

   public ConfigureWorldScreen(RealmsScreen lastScreen, long serverId) {
      this.lastScreen = lastScreen;
      this.serverId = serverId;
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
         this.editButton = newButton(5, this.column2_x, this.row(6), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.edit"))
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
      this.buttonsAdd(newButton(10, this.column2_x + this.column_width / 2 + 2, this.row(12), this.column_width / 2 - 2, 20, getLocalizedString("gui.back")));
      this.invitedSelectionList = new ConfigureWorldScreen.InvitedSelectionList();
      this.editButton.active(false);
      this.editWorldButton.active(false);
      this.minigameButton.active(false);
      this.subscriptionButton.active(false);
      this.activityButton.active(false);
   }

   private void fetchOps() {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               ConfigureWorldScreen.this.ops = client.getOpsFor(ConfigureWorldScreen.this.serverId).ops;
            } catch (RealmsServiceException var3) {
               ConfigureWorldScreen.LOGGER.error("Couldn't fetch ops");
            } catch (Exception var4) {
               ConfigureWorldScreen.LOGGER.error("Couldn't parse response of fetching ops");
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
            Realms.setScreen(new EditOnlineWorldScreen(this, this.lastScreen, this.serverData.clone()));
         } else if (button.id() == 1) {
            String line2 = getLocalizedString("mco.configure.world.close.question.line1");
            String line3 = getLocalizedString("mco.configure.world.close.question.line2");
            Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 1));
         } else if (button.id() == 0) {
            this.openTheWorld();
         } else if (button.id() == 8) {
            if (this.serverData.worldType.equals(McoServer.WorldType.MINIGAME)) {
               Realms.setScreen(new ModifyMinigameWorldScreen(this, this.serverData));
            } else {
               Realms.setScreen(new StartMinigameWorldScreen(this, this.serverData));
            }
         } else if (button.id() == 7) {
            Realms.setScreen(new SubscriptionScreen(this, this.serverData));
         } else if (button.id() == 11) {
            Realms.setScreen(new ActivityScreen(this, this.serverData.id));
         } else if (button.id() == 12) {
            Realms.setScreen(new WorldManagementScreen(this, this.lastScreen, this.serverData.clone()));
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
               ConfigureWorldScreen.this.serverData = client.getOwnWorld(worldId);
               ConfigureWorldScreen.this.fetchOps();
               ConfigureWorldScreen.this.editButton.active(!ConfigureWorldScreen.this.serverData.expired);
               ConfigureWorldScreen.this.minigameButton.active(!ConfigureWorldScreen.this.serverData.expired);
               ConfigureWorldScreen.this.subscriptionButton.active(true);
               ConfigureWorldScreen.this.activityButton.active(true);
               ConfigureWorldScreen.this.editWorldButton.active(true);
               boolean isMinigame = ConfigureWorldScreen.this.serverData.worldType.equals(McoServer.WorldType.MINIGAME);
               if (isMinigame) {
                  ConfigureWorldScreen.this.minigameButton.msg(RealmsScreen.getLocalizedString("mco.configure.world.buttons.modifyMiniGame"));
                  ConfigureWorldScreen.this.editButton.active(false);
                  ConfigureWorldScreen.this.editWorldButton.active(false);
               }
            } catch (RealmsServiceException var3) {
               ConfigureWorldScreen.LOGGER.error("Couldn't get own world");
            } catch (IOException var4) {
               ConfigureWorldScreen.LOGGER.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   private void op() {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(this.selectedInvitedIndex)).getName();

      try {
         client.op(this.serverData.id, selectedInvite);
         this.fetchOps();
      } catch (RealmsServiceException var4) {
         LOGGER.error("Couldn't op the user");
      }

   }

   private void deop() {
      RealmsClient client = RealmsClient.createRealmsClient();
      String selectedInvite = ((PlayerInfo)this.serverData.players.get(this.selectedInvitedIndex)).getName();

      try {
         client.deop(this.serverData.id, selectedInvite);
         this.fetchOps();
      } catch (RealmsServiceException var4) {
         LOGGER.error("Couldn't deop the user");
      }

   }

   private void openTheWorld() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         Boolean openResult = client.open(this.serverData.id);
         if (openResult) {
            this.stateChanged = true;
            this.serverData.state = McoServer.State.OPEN;
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
            this.serverData.state = McoServer.State.CLOSED;
            this.init();
         }
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't close world");
      } catch (IOException var4) {
         LOGGER.error("Could not parse response closing world");
      }

   }

   private void uninvite() {
      if (this.selectedInvitedIndex >= 0 && this.selectedInvitedIndex < this.serverData.players.size()) {
         PlayerInfo playerInfo = (PlayerInfo)this.serverData.players.get(this.selectedInvitedIndex);
         this.selectedInvited = playerInfo.getUuid();
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

         Realms.setScreen(new ConfigureWorldScreen(this.lastScreen, this.serverData.id));
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
         Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 1));
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

      this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 17, 16777215);
      if (this.serverData != null && this.serverData.players != null) {
         this.drawString(
            getLocalizedString("mco.configure.world.invited") + " (" + this.serverData.players.size() + "/20)", this.column1_x, this.row(1), 10526880
         );
      } else {
         this.drawString(getLocalizedString("mco.configure.world.invited"), this.column1_x, this.row(1), 10526880);
      }

      super.render(xm, ym, a);
      if (this.serverData != null) {
         String name = this.serverData.getName();
         int nameWidth = this.fontWidth(name);
         if (this.serverData.state == McoServer.State.OPEN) {
            this.drawCenteredString(name, this.width() / 2, 30, 8388479);
         } else if (this.serverData.state == McoServer.State.CLOSED) {
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
      } else if (this.serverData.state == McoServer.State.CLOSED) {
         this.drawClose(x, y, xm, ym);
      } else if (this.serverData.state == McoServer.State.OPEN) {
         this.drawOpen(x, y, xm, ym);
      } else if (this.serverData.state == McoServer.State.ADMIN_LOCK) {
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

   private class InvitedSelectionList extends MovableScrolledSelectionList {
      public InvitedSelectionList() {
         super(
            ConfigureWorldScreen.this.column1_x,
            ConfigureWorldScreen.this.row(2),
            ConfigureWorldScreen.this.column_width + 10,
            ConfigureWorldScreen.this.row(13) - ConfigureWorldScreen.this.row(4),
            13,
            "+"
         );
      }

      @Override
      protected int getNumberOfItems() {
         return ConfigureWorldScreen.this.serverData == null ? 0 : ConfigureWorldScreen.this.serverData.players.size();
      }

      @Override
      protected void selectItem(int item, boolean doubleClick) {
         if (ConfigureWorldScreen.this.serverData != null) {
            if (item < ConfigureWorldScreen.this.serverData.players.size()) {
               ConfigureWorldScreen.this.selectedInvitedIndex = item;
            }
         }
      }

      @Override
      protected boolean isSelectedItem(int item) {
         return item == ConfigureWorldScreen.this.selectedInvitedIndex;
      }

      @Override
      protected int getMaxPosition() {
         return this.getNumberOfItems() * 13;
      }

      @Override
      protected void renderBackground() {
      }

      @Override
      protected void renderItem(int i, int x, int y, int h, Tezzelator t) {
         if (ConfigureWorldScreen.this.serverData != null) {
            if (i < ConfigureWorldScreen.this.serverData.players.size()) {
               this.renderInvitedItem(i, x, y, h, t);
            }

         }
      }

      @Override
      public void itemClicked(int x, int y, int xm, int ym) {
         int dy = 1;
         int my = y + dy;
         if (xm >= this.x1 - 21 && xm <= this.x1 - 12 && ym >= my && ym <= my + 9) {
            if (ConfigureWorldScreen.this.selectedInvitedIndex >= 0
               && ConfigureWorldScreen.this.selectedInvitedIndex < ConfigureWorldScreen.this.serverData.players.size()) {
               String selectedPlayer = ((PlayerInfo)ConfigureWorldScreen.this.serverData.players.get(ConfigureWorldScreen.this.selectedInvitedIndex)).getName();
               if (ConfigureWorldScreen.this.ops != null) {
                  if (ConfigureWorldScreen.this.ops.contains(selectedPlayer)) {
                     ConfigureWorldScreen.this.deop();
                  } else {
                     ConfigureWorldScreen.this.op();
                  }
               }
            }
         } else if (xm >= this.x1 - 33
            && xm <= this.x1 - 24
            && ym >= my
            && ym <= my + 9
            && ConfigureWorldScreen.this.selectedInvitedIndex >= 0
            && ConfigureWorldScreen.this.selectedInvitedIndex < ConfigureWorldScreen.this.serverData.players.size()) {
            ConfigureWorldScreen.this.uninvite();
         }

      }

      @Override
      protected void buttonClicked() {
         Realms.setScreen(new InviteScreen(ConfigureWorldScreen.this.lastScreen, ConfigureWorldScreen.this, ConfigureWorldScreen.this.serverData));
      }

      private void renderInvitedItem(int i, int x, int y, int h, Tezzelator t) {
         String invited = ((PlayerInfo)ConfigureWorldScreen.this.serverData.players.get(i)).getName();
         ConfigureWorldScreen.this.drawString(invited, x + 2 + 12, y + 1, 16777215);
         if (ConfigureWorldScreen.this.ops != null && ConfigureWorldScreen.this.ops.contains(invited)) {
            ConfigureWorldScreen.this.drawOpped(this.x1 - 20, y + 1, this.xm, this.ym);
         } else {
            ConfigureWorldScreen.this.drawNormal(this.x1 - 20, y + 1, this.xm, this.ym);
         }

         ConfigureWorldScreen.this.drawRemoveIcon(this.x1 - 32, y + 2, this.xm, this.ym);
         RealmsScreen.bindFace(invited);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 2, y + 1, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
         RealmsScreen.blit(x + 2, y + 1, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
      }
   }
}
