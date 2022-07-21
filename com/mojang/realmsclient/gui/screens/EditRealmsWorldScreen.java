package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConfirmResultListener;
import java.io.UnsupportedEncodingException;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class EditRealmsWorldScreen extends RealmsScreen implements RealmsConfirmResultListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsScreen configureWorldScreen;
   private RealmsScreen onlineScreen;
   private RealmsEditBox descEdit;
   private RealmsEditBox nameEdit;
   private RealmsServer serverData;
   private static final int DONE_BUTTON_ID = 0;
   private static final int CANCEL_BUTTON = 1;
   private static final int MORE_SETTINGS_BUTTON_ID = 3;
   private static final int NAME_EDIT_BOX = 4;
   private static final int DESC_EDIT_BOX = 5;
   private RealmsButton doneButton;
   private RealmsButton moreSettingsButton;

   public EditRealmsWorldScreen(RealmsScreen configureWorldScreen, RealmsScreen onlineScreen, RealmsServer serverData) {
      this.configureWorldScreen = configureWorldScreen;
      this.onlineScreen = onlineScreen;
      this.serverData = serverData;
   }

   public void tick() {
      this.nameEdit.tick();
      this.descEdit.tick();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(
         this.moreSettingsButton = newButton(
            3, this.width() / 2 - 106, this.height() / 4 + 120 - 3, 216, 20, getLocalizedString("mco.configure.world.buttons.moreoptions")
         )
      );
      this.buttonsAdd(
         this.doneButton = newButton(0, this.width() / 2 - 106, this.height() / 4 + 120 + 22, 106, 20, getLocalizedString("mco.configure.world.buttons.done"))
      );
      this.buttonsAdd(newButton(1, this.width() / 2 + 4, this.height() / 4 + 120 + 22, 106, 20, getLocalizedString("gui.cancel")));
      this.nameEdit = this.newEditBox(4, this.width() / 2 - 106, 56, 212, 20);
      this.nameEdit.setFocus(true);
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setValue(this.serverData.getName());
      this.descEdit = this.newEditBox(5, this.width() / 2 - 106, 96, 212, 20);
      this.descEdit.setMaxLength(32);
      this.descEdit.setValue(this.serverData.getMotd());
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            Realms.setScreen(this.configureWorldScreen);
         } else if (button.id() == 0) {
            this.update();
         } else if (button.id() == 3) {
            this.saveServerData();
            Realms.setScreen(new RealmsWorldSettingsSubScreen(this, this.serverData));
         }

      }
   }

   private void saveServerData() {
      this.serverData.setName(this.nameEdit.getValue());
      this.serverData.setMotd(this.descEdit.getValue());
   }

   public void keyPressed(char ch, int eventKey) {
      this.nameEdit.keyPressed(ch, eventKey);
      this.descEdit.keyPressed(ch, eventKey);
      if (eventKey == 15) {
         this.nameEdit.setFocus(!this.nameEdit.isFocused());
         this.descEdit.setFocus(!this.descEdit.isFocused());
      }

      if (eventKey == 28 || eventKey == 156) {
         this.update();
      }

      if (eventKey == 1) {
         Realms.setScreen(this.configureWorldScreen);
      }

      this.doneButton.active(this.nameEdit.getValue() != null && !this.nameEdit.getValue().trim().equals(""));
   }

   private void update() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         String desc = this.descEdit.getValue() != null && !this.descEdit.getValue().trim().equals("") ? this.descEdit.getValue() : null;
         client.update(this.serverData.id, this.nameEdit.getValue(), desc, this.serverData.difficulty, this.serverData.gameMode, this.serverData.options);
         this.saveServerData();
         Realms.setScreen(new ConfigureWorldScreen(this.onlineScreen, this.serverData.id));
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't edit world");
         Realms.setScreen(new RealmsGenericErrorScreen(var3, this));
      } catch (UnsupportedEncodingException var4) {
         LOGGER.error("Couldn't edit world");
      }

   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.descEdit.mouseClicked(x, y, buttonNum);
      this.nameEdit.mouseClicked(x, y, buttonNum);
   }

   @Override
   public void confirmResult(boolean result, int id) {
      Realms.setScreen(this);
   }

   public void saveServerData(
      int difficuly, int gameMode, boolean pvp, boolean spawnNPCs, boolean spawnAnimals, boolean spawnMonsters, int spawnProtection, boolean commandBlocks
   ) {
      this.serverData.difficulty = difficuly;
      this.serverData.gameMode = gameMode;
      this.serverData.options.pvp = pvp;
      this.serverData.options.spawnNPCs = spawnNPCs;
      this.serverData.options.spawnAnimals = spawnAnimals;
      this.serverData.options.spawnMonsters = spawnMonsters;
      this.serverData.options.spawnProtection = spawnProtection;
      this.serverData.options.commandBlocks = commandBlocks;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.world.edit.title"), this.width() / 2, 17, 16777215);
      String nameString = getLocalizedString("mco.configure.world.name");
      String descriptionString = getLocalizedString("mco.configure.world.description");
      this.drawString(nameString, this.width() / 2 - 106, 43, 10526880);
      this.drawString(descriptionString, this.width() / 2 - 106, 84, 10526880);
      this.nameEdit.render();
      this.descEdit.render();
      super.render(xm, ym, a);
   }
}
