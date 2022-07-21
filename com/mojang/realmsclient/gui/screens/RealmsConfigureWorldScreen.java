package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.RealmsHideableButton;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConfigureWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String ON_ICON_LOCATION = "realms:textures/gui/realms/on_icon.png";
   private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
   private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
   private static final String EXPIRES_SOON_ICON_LOCATION = "realms:textures/gui/realms/expires_soon_icon.png";
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private static final String EMPTY_FRAME_LOCATION = "realms:textures/gui/realms/empty_frame.png";
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
   private RealmsHideableButton optionsButton;
   private RealmsHideableButton backupButton;
   private RealmsHideableButton resetWorldButton;
   private RealmsHideableButton switchMinigameButton;
   private boolean stateChanged;
   private int hoveredSlot = -1;
   private int animTick;
   private int clicks;
   private boolean hoveredActiveSlot;

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
            public void onClick(double mouseX, double mouseY) {
               Realms.setScreen(new RealmsPlayerScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData));
            }
         }
      );
      this.buttonsAdd(
         this.settingsButton = new RealmsButton(
            3, this.centerButton(1, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.settings")
         ) {
            public void onClick(double mouseX, double mouseY) {
               Realms.setScreen(new RealmsSettingsScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone()));
            }
         }
      );
      this.buttonsAdd(
         this.subscriptionButton = new RealmsButton(
            4, this.centerButton(2, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.subscription")
         ) {
            public void onClick(double mouseX, double mouseY) {
               Realms.setScreen(
                  new RealmsSubscriptionInfoScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.lastScreen
                  )
               );
            }
         }
      );
      this.buttonsAdd(
         this.optionsButton = new RealmsHideableButton(
            5, this.leftButton(0), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.options")
         ) {
            @Override
            public void clicked(double mx, double my) {
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
         this.backupButton = new RealmsHideableButton(
            6, this.leftButton(1), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.backup")
         ) {
            @Override
            public void clicked(double mx, double my) {
               Realms.setScreen(
                  new RealmsBackupScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.serverData.activeSlot
                  )
               );
            }
         }
      );
      this.buttonsAdd(
         this.resetWorldButton = new RealmsHideableButton(
            7, this.leftButton(2), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.resetworld")
         ) {
            @Override
            public void clicked(double mx, double my) {
               Realms.setScreen(
                  new RealmsResetWorldScreen(
                     RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.getNewScreen()
                  )
               );
            }
         }
      );
      this.buttonsAdd(
         this.switchMinigameButton = new RealmsHideableButton(
            8, this.leftButton(0), RealmsConstants.row(13) - 5, 100, 20, getLocalizedString("mco.configure.world.buttons.switchminigame")
         ) {
            @Override
            public void clicked(double mx, double my) {
               RealmsSelectWorldTemplateScreen minigameScreen = new RealmsSelectWorldTemplateScreen(
                  RealmsConfigureWorldScreen.this, null, RealmsServer.WorldType.MINIGAME
               );
               minigameScreen.setTitle(RealmsScreen.getLocalizedString("mco.template.title.minigame"));
               Realms.setScreen(minigameScreen);
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
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

   private int leftButton(int i) {
      return this.left_x + i * 95;
   }

   private int centerButton(int i, int total) {
      return this.width() / 2 - (total * 105 - 5) / 2 + i * 105;
   }

   public void tick() {
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.hoveredActiveSlot = false;
      this.hoveredSlot = -1;
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

         for(Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
            if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
               this.drawSlotFrame(
                  this.frame(entry.getKey()),
                  RealmsConstants.row(5) + 5,
                  xm,
                  ym,
                  this.serverData.activeSlot == entry.getKey() && !this.isMinigame(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  ((RealmsWorldOptions)entry.getValue()).templateId,
                  ((RealmsWorldOptions)entry.getValue()).templateImage,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            } else {
               this.drawSlotFrame(
                  this.frame(entry.getKey()),
                  RealmsConstants.row(5) + 5,
                  xm,
                  ym,
                  this.serverData.activeSlot == entry.getKey() && !this.isMinigame(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  -1L,
                  null,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            }
         }

         this.drawSlotFrame(this.frame(4), RealmsConstants.row(5) + 5, xm, ym, this.isMinigame(), "Minigame", 4, -1L, null, false);
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

   public void confirmResult(boolean result, int id) {
      switch(id) {
         case 9:
            if (result) {
               this.switchSlot();
            } else {
               Realms.setScreen(this);
            }
            break;
         case 10:
            if (result) {
               RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(
                  this,
                  this.serverData,
                  this.getNewScreen(),
                  getLocalizedString("mco.configure.world.switch.slot"),
                  getLocalizedString("mco.configure.world.switch.slot.subtitle"),
                  10526880,
                  getLocalizedString("gui.cancel")
               );
               resetWorldScreen.setSlot(this.hoveredSlot);
               resetWorldScreen.setResetTitle(getLocalizedString("mco.create.world.reset.title"));
               Realms.setScreen(resetWorldScreen);
            } else {
               Realms.setScreen(this);
            }
            break;
         case 11:
            Realms.setScreen(this);
      }

   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (buttonNum == 0) {
         this.clicks += 7;
         if (this.hoveredSlot != -1) {
            if (this.hoveredSlot < 4) {
               String line2 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
               String line3 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
               if (((RealmsWorldOptions)this.serverData.slots.get(this.hoveredSlot)).empty) {
                  Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 10));
               } else {
                  Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 9));
               }
            } else if (!this.isMinigame() && !this.serverData.expired) {
               RealmsSelectWorldTemplateScreen screen = new RealmsSelectWorldTemplateScreen(this, null, RealmsServer.WorldType.MINIGAME);
               screen.setTitle(getLocalizedString("mco.template.title.minigame"));
               screen.setWarning(getLocalizedString("mco.minigame.world.info.line1") + "\\n" + getLocalizedString("mco.minigame.world.info.line2"));
               Realms.setScreen(screen);
            }

            return true;
         } else if (this.clicks >= 10
            && this.hoveredActiveSlot
            && (this.serverData.state == RealmsServer.State.OPEN || this.serverData.state == RealmsServer.State.CLOSED)) {
            if (this.serverData.state == RealmsServer.State.OPEN) {
               this.lastScreen.play(this.serverData, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
            } else {
               this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
            }

            return true;
         } else {
            return super.mouseClicked(x, y, buttonNum);
         }
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
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
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
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
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20.0F, 28.0F);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20.0F, 28.0F);
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
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.toolTip = getLocalizedString("mco.selectServer.open");
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27) {
         this.toolTip = getLocalizedString("mco.selectServer.closed");
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
   }

   private void drawSlotFrame(int x, int y, int xm, int ym, boolean active, String text, int i, long imageId, String image, boolean empty) {
      label97:
      if (xm >= x && xm <= x + 80 && ym >= y && ym <= y + 80) {
         if (this.isMinigame()) {
            if (i == 4) {
               break label97;
            }
         } else if (this.serverData.activeSlot == i) {
            break label97;
         }

         if (i != 4 || !this.serverData.expired) {
            this.hoveredSlot = i;
            this.toolTip = getLocalizedString(i == 4 ? "mco.configure.world.slot.tooltip.minigame" : "mco.configure.world.slot.tooltip");
         }
      }

      label83:
      if (xm >= x && xm <= x + 80 && ym >= y && ym <= y + 80) {
         if (this.isMinigame()) {
            if (i != 4) {
               break label83;
            }
         } else if (this.serverData.activeSlot != i) {
            break label83;
         }

         if (!this.serverData.expired && (this.serverData.state == RealmsServer.State.OPEN || this.serverData.state == RealmsServer.State.CLOSED)) {
            this.hoveredActiveSlot = true;
            this.toolTip = getLocalizedString("mco.configure.world.slot.tooltip.active");
         }
      }

      if (empty) {
         bind("realms:textures/gui/realms/empty_frame.png");
      } else if (image != null && imageId != -1L) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (i == 1) {
         bind("textures/gui/title/background/panorama_0.png");
      } else if (i == 2) {
         bind("textures/gui/title/background/panorama_2.png");
      } else if (i == 3) {
         bind("textures/gui/title/background/panorama_3.png");
      } else {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
      }

      if (!active) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else if (active) {
         float c = 0.9F + 0.1F * RealmsMth.cos((float)this.animTick * 0.2F);
         GlStateManager.color4f(c, c, c, 1.0F);
      }

      RealmsScreen.blit(x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74.0F, 74.0F);
      bind("realms:textures/gui/realms/slot_frame.png");
      if (this.hoveredSlot == i) {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else if (!active) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RealmsScreen.blit(x, y, 0.0F, 0.0F, 80, 80, 80.0F, 80.0F);
      this.drawCenteredString(text, x + 40, y + 66, 16777215);
   }

   private void hideRegularButtons() {
      this.hide(this.optionsButton);
      this.hide(this.backupButton);
      this.hide(this.resetWorldButton);
   }

   private void hide(RealmsHideableButton button) {
      button.setVisible(false);
      this.removeButton(button);
   }

   private void showRegularButtons() {
      this.show(this.optionsButton);
      this.show(this.backupButton);
      this.show(this.resetWorldButton);
   }

   private void show(RealmsHideableButton button) {
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

   private void switchSlot() {
      RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, this.hoveredSlot, this.getNewScreen(), 11);
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
