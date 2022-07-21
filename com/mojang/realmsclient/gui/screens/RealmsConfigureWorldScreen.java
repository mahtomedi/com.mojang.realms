package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.RealmsTasks;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConfigureWorldScreen extends RealmsScreenWithCallback<WorldTemplate> implements RealmsWorldSlotButton.Listener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String ON_ICON_LOCATION = "realms:textures/gui/realms/on_icon.png";
   private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
   private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
   private static final String EXPIRES_SOON_ICON_LOCATION = "realms:textures/gui/realms/expires_soon_icon.png";
   private String toolTip;
   private final RealmsMainScreen lastScreen;
   private RealmsServer serverData;
   private final long serverId;
   private int left_x;
   private int right_x;
   private final int default_button_width = 80;
   private final int default_button_offset = 5;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_PLAYERS_ID = 2;
   private static final int BUTTON_SETTINGS_ID = 3;
   private static final int BUTTON_SUBSCRIPTION_ID = 4;
   private static final int BUTTON_OPTIONS_ID = 5;
   private static final int BUTTON_BACKUP_ID = 6;
   private static final int BUTTON_RESET_WORLD_ID = 7;
   private static final int BUTTON_SWITCH_MINIGAME_ID = 8;
   private static final int SWITCH_SLOT_ID = 9;
   private static final int SWITCH_SLOT_ID_EMPTY = 10;
   private static final int SWITCH_SLOT_ID_RESULT = 11;
   private RealmsButton playersButton;
   private RealmsButton settingsButton;
   private RealmsButton subscriptionButton;
   private RealmsButton optionsButton;
   private RealmsButton backupButton;
   private RealmsButton resetWorldButton;
   private RealmsButton switchMinigameButton;
   private boolean stateChanged;
   private int animTick;
   private int clicks;

   public RealmsConfigureWorldScreen(RealmsMainScreen lastScreen, long serverId) {
      this.lastScreen = lastScreen;
      this.serverId = serverId;
   }

   public void init() {
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      }

      this.left_x = this.width() / 2 - 187;
      this.right_x = this.width() / 2 + 190;
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(
         this.playersButton = new RealmsButton(
            2, this.centerButton(0, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.players")
         ) {
            public void onPress() {
               Realms.setScreen(new RealmsPlayerScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData));
            }
         }
      );
      this.buttonsAdd(
         this.settingsButton = new RealmsButton(
            3, this.centerButton(1, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.settings")
         ) {
            public void onPress() {
               Realms.setScreen(new RealmsSettingsScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone()));
            }
         }
      );
      this.buttonsAdd(
         this.subscriptionButton = new RealmsButton(
            4, this.centerButton(2, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.subscription")
         ) {
            public void onPress() {
               Realms.setScreen(
                  new RealmsSubscriptionInfoScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.lastScreen
                  )
               );
            }
         }
      );

      for(int i = 1; i < 5; ++i) {
         this.addSlotButton(i);
      }

      this.buttonsAdd(
         this.switchMinigameButton = new RealmsButton(
            8, this.leftButton(0), RealmsConstants.row(13) - 5, 100, 20, getLocalizedString("mco.configure.world.buttons.switchminigame")
         ) {
            public void onPress() {
               RealmsSelectWorldTemplateScreen minigameScreen = new RealmsSelectWorldTemplateScreen(
                  RealmsConfigureWorldScreen.this, null, RealmsServer.WorldType.MINIGAME
               );
               minigameScreen.setTitle(RealmsScreen.getLocalizedString("mco.template.title.minigame"));
               Realms.setScreen(minigameScreen);
            }
         }
      );
      this.buttonsAdd(
         this.optionsButton = new RealmsButton(
            5, this.leftButton(0), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.options")
         ) {
            public void onPress() {
               Realms.setScreen(
                  new RealmsSlotOptionsScreen(
                     RealmsConfigureWorldScreen.this,
                     ((RealmsWorldOptions)RealmsConfigureWorldScreen.this.serverData.slots.get(RealmsConfigureWorldScreen.this.serverData.activeSlot)).clone(),
                     RealmsConfigureWorldScreen.this.serverData.worldType,
                     RealmsConfigureWorldScreen.this.serverData.activeSlot
                  )
               );
            }
         }
      );
      this.buttonsAdd(
         this.backupButton = new RealmsButton(6, this.leftButton(1), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.backup")) {
            public void onPress() {
               Realms.setScreen(
                  new RealmsBackupScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.serverData.activeSlot
                  )
               );
            }
         }
      );
      this.buttonsAdd(
         this.resetWorldButton = new RealmsButton(
            7, this.leftButton(2), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.resetworld")
         ) {
            public void onPress() {
               Realms.setScreen(
                  new RealmsResetWorldScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.getNewScreen()
                  )
               );
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            RealmsConfigureWorldScreen.this.backButtonClicked();
         }
      });
      this.backupButton.active(true);
      if (this.serverData == null) {
         this.hideMinigameButtons();
         this.hideRegularButtons();
         this.playersButton.active(false);
         this.settingsButton.active(false);
         this.subscriptionButton.active(false);
      } else {
         this.disableButtons();
         if (this.isMinigame()) {
            this.hideRegularButtons();
         } else {
            this.hideMinigameButtons();
         }
      }

   }

   private void addSlotButton(int i) {
      int x = this.frame(i);
      int y = RealmsConstants.row(5) + 5;
      int buttonId = 100 + i;
      RealmsWorldSlotButton worldSlotButton = new RealmsWorldSlotButton(x, y, 80, 80, () -> this.serverData, s -> this.toolTip = s, buttonId, i, this);
      this.getProxy().buttonsAdd(worldSlotButton);
   }

   private int leftButton(int i) {
      return this.left_x + i * 95;
   }

   private int centerButton(int i, int total) {
      return this.width() / 2 - (total * 105 - 5) / 2 + i * 105;
   }

   public void tick() {
      this.tickButtons();
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.worlds.title"), this.width() / 2, RealmsConstants.row(4), 16777215);
      super.render(xm, ym, a);
      if (this.serverData == null) {
         this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 17, 16777215);
      } else {
         String name = this.serverData.getName();
         int nameWidth = this.fontWidth(name);
         int nameColor = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
         int titleWidth = this.fontWidth(getLocalizedString("mco.configure.world.title"));
         this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 12, 16777215);
         this.drawCenteredString(name, this.width() / 2, 24, nameColor);
         int statusX = Math.min(this.centerButton(2, 3) + 80 - 11, this.width() / 2 + nameWidth / 2 + titleWidth / 2 + 10);
         this.drawServerStatus(statusX, 7, xm, ym);
         if (this.isMinigame()) {
            this.drawString(
               getLocalizedString("mco.configure.current.minigame") + ": " + this.serverData.getMinigameName(),
               this.left_x + 80 + 20 + 10,
               RealmsConstants.row(13),
               16777215
            );
         }

         if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, xm, ym);
         }

      }
   }

   private int frame(int i) {
      return this.left_x + (i - 1) * 98;
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
         this.lastScreen.removeSelection();
      }

      Realms.setScreen(this.lastScreen);
   }

   private void fetchServerData(final long worldId) {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsConfigureWorldScreen.this.serverData = client.getOwnWorld(worldId);
               RealmsConfigureWorldScreen.this.disableButtons();
               if (RealmsConfigureWorldScreen.this.isMinigame()) {
                  RealmsConfigureWorldScreen.this.showMinigameButtons();
               } else {
                  RealmsConfigureWorldScreen.this.showRegularButtons();
               }
            } catch (RealmsServiceException var3) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't get own world");
               Realms.setScreen(new RealmsGenericErrorScreen(var3.getMessage(), RealmsConfigureWorldScreen.this.lastScreen));
            } catch (IOException var4) {
               RealmsConfigureWorldScreen.LOGGER.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   private void disableButtons() {
      this.playersButton.active(!this.serverData.expired);
      this.settingsButton.active(!this.serverData.expired);
      this.subscriptionButton.active(true);
      this.switchMinigameButton.active(!this.serverData.expired);
      this.optionsButton.active(!this.serverData.expired);
      this.resetWorldButton.active(!this.serverData.expired);
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      return super.mouseClicked(x, y, buttonNum);
   }

   private void joinRealm(RealmsServer serverData) {
      if (this.serverData.state == RealmsServer.State.OPEN) {
         this.lastScreen.play(serverData, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      } else {
         this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      }

   }

   @Override
   public void onSlotClick(int slotIndex, @Nonnull RealmsWorldSlotButton.Action action, boolean minigame, boolean empty) {
      switch(action) {
         case NOTHING:
            break;
         case JOIN:
            this.joinRealm(this.serverData);
            break;
         case SWITCH_SLOT:
            if (minigame) {
               this.switchToMinigame();
            } else if (empty) {
               this.switchToEmptySlot(slotIndex, this.serverData);
            } else {
               this.switchToFullSlot(slotIndex, this.serverData);
            }
            break;
         default:
            throw new IllegalStateException("Unknown action " + action);
      }

   }

   private void switchToMinigame() {
      RealmsSelectWorldTemplateScreen screen = new RealmsSelectWorldTemplateScreen(this, null, RealmsServer.WorldType.MINIGAME);
      screen.setTitle(getLocalizedString("mco.template.title.minigame"));
      screen.setWarning(getLocalizedString("mco.minigame.world.info.line1") + "\\n" + getLocalizedString("mco.minigame.world.info.line2"));
      Realms.setScreen(screen);
   }

   private void switchToFullSlot(int selectedSlot, RealmsServer serverData) {
      String line2 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
      String line3 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
      Realms.setScreen(new RealmsLongConfirmationScreen((result, id) -> {
         if (result) {
            this.switchSlot(serverData.id, selectedSlot);
         } else {
            Realms.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 9));
   }

   private void switchToEmptySlot(int selectedSlot, RealmsServer serverData) {
      String line2 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
      String line3 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
      Realms.setScreen(
         new RealmsLongConfirmationScreen(
            (result, id) -> {
               if (result) {
                  RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(
                     this,
                     serverData,
                     this.getNewScreen(),
                     getLocalizedString("mco.configure.world.switch.slot"),
                     getLocalizedString("mco.configure.world.switch.slot.subtitle"),
                     10526880,
                     getLocalizedString("gui.cancel")
                  );
                  resetWorldScreen.setSlot(selectedSlot);
                  resetWorldScreen.setResetTitle(getLocalizedString("mco.create.world.reset.title"));
                  Realms.setScreen(resetWorldScreen);
               } else {
                  Realms.setScreen(this);
               }
      
            },
            RealmsLongConfirmationScreen.Type.Info,
            line2,
            line3,
            true,
            10
         )
      );
   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         if (rx + width + 3 > this.right_x) {
            rx = rx - width - 20;
         }

         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private void drawServerStatus(int x, int y, int xm, int ym) {
      if (this.serverData.expired) {
         this.drawExpired(x, y, xm, ym);
      } else if (this.serverData.state == RealmsServer.State.CLOSED) {
         this.drawClose(x, y, xm, ym);
      } else if (this.serverData.state == RealmsServer.State.OPEN) {
         if (this.serverData.daysLeft < 7) {
            this.drawExpiring(x, y, xm, ym, this.serverData.daysLeft);
         } else {
            this.drawOpen(x, y, xm, ym);
         }
      }

   }

   private void drawExpired(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.toolTip = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void drawExpiring(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      if (this.animTick % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         if (daysLeft <= 0) {
            this.toolTip = getLocalizedString("mco.selectServer.expires.soon");
         } else if (daysLeft == 1) {
            this.toolTip = getLocalizedString("mco.selectServer.expires.day");
         } else {
            this.toolTip = getLocalizedString("mco.selectServer.expires.days", new Object[]{daysLeft});
         }
      }

   }

   private void drawOpen(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.toolTip = getLocalizedString("mco.selectServer.open");
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.toolTip = getLocalizedString("mco.selectServer.closed");
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
   }

   private void hideRegularButtons() {
      this.hide(this.optionsButton);
      this.hide(this.backupButton);
      this.hide(this.resetWorldButton);
   }

   private void hide(RealmsButton button) {
      button.setVisible(false);
      this.removeButton(button);
   }

   private void showRegularButtons() {
      this.show(this.optionsButton);
      this.show(this.backupButton);
      this.show(this.resetWorldButton);
   }

   private void show(RealmsButton button) {
      button.setVisible(true);
      this.buttonsAdd(button);
   }

   private void hideMinigameButtons() {
      this.hide(this.switchMinigameButton);
   }

   private void showMinigameButtons() {
      this.show(this.switchMinigameButton);
   }

   public void saveSlotSettings(RealmsWorldOptions options) {
      RealmsWorldOptions oldOptions = (RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot);
      options.templateId = oldOptions.templateId;
      options.templateImage = oldOptions.templateImage;
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         client.updateSlot(this.serverData.id, this.serverData.activeSlot, options);
         this.serverData.slots.put(this.serverData.activeSlot, options);
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't save slot settings");
         Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
         return;
      } catch (UnsupportedEncodingException var6) {
         LOGGER.error("Couldn't save slot settings");
      }

      Realms.setScreen(this);
   }

   public void saveSettings(String name, String desc) {
      String description = desc != null && !desc.trim().isEmpty() ? desc : null;
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         client.update(this.serverData.id, name, description);
         this.serverData.setName(name);
         this.serverData.setDescription(description);
      } catch (RealmsServiceException var6) {
         LOGGER.error("Couldn't save settings");
         Realms.setScreen(new RealmsGenericErrorScreen(var6, this));
         return;
      } catch (UnsupportedEncodingException var7) {
         LOGGER.error("Couldn't save settings");
      }

      Realms.setScreen(this);
   }

   public void openTheWorld(boolean join, RealmsScreen screenInCaseOfCancel) {
      RealmsTasks.OpenServerTask openServerTask = new RealmsTasks.OpenServerTask(this.serverData, this, this.lastScreen, join);
      RealmsLongRunningMcoTaskScreen openWorldLongRunningTaskScreen = new RealmsLongRunningMcoTaskScreen(screenInCaseOfCancel, openServerTask);
      openWorldLongRunningTaskScreen.start();
      Realms.setScreen(openWorldLongRunningTaskScreen);
   }

   public void closeTheWorld(RealmsScreen screenInCaseOfCancel) {
      RealmsTasks.CloseServerTask closeServerTask = new RealmsTasks.CloseServerTask(this.serverData, this);
      RealmsLongRunningMcoTaskScreen closeWorldLongRunningTaskScreen = new RealmsLongRunningMcoTaskScreen(screenInCaseOfCancel, closeServerTask);
      closeWorldLongRunningTaskScreen.start();
      Realms.setScreen(closeWorldLongRunningTaskScreen);
   }

   public void stateChanged() {
      this.stateChanged = true;
   }

   void callback(WorldTemplate worldTemplate) {
      if (worldTemplate != null) {
         if (WorldTemplate.WorldTemplateType.MINIGAME.equals(worldTemplate.type)) {
            this.switchMinigame(worldTemplate);
         }

      }
   }

   private void switchSlot(long worldId, int selectedSlot) {
      RealmsConfigureWorldScreen newScreen = this.getNewScreen();
      RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(worldId, selectedSlot, (result, id) -> Realms.setScreen(newScreen), 11);
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   private void switchMinigame(WorldTemplate selectedWorldTemplate) {
      RealmsTasks.SwitchMinigameTask startMinigameTask = new RealmsTasks.SwitchMinigameTask(this.serverData.id, selectedWorldTemplate, this.getNewScreen());
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, startMinigameTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public RealmsConfigureWorldScreen getNewScreen() {
      return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
   }
}
