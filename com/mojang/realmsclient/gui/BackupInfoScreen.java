package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.Backup;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class BackupInfoScreen extends RealmsScreen {
   private final RealmsScreen lastScreen;
   private final int BUTTON_BACK_ID = 0;
   private final Backup backup;
   String[] difficulties = new String[]{
      getLocalizedString("options.difficulty.peaceful"),
      getLocalizedString("options.difficulty.easy"),
      getLocalizedString("options.difficulty.normal"),
      getLocalizedString("options.difficulty.hard")
   };
   String[] gameModes = new String[]{
      getLocalizedString("selectWorld.gameMode.survival"),
      getLocalizedString("selectWorld.gameMode.creative"),
      getLocalizedString("selectWorld.gameMode.adventure")
   };

   public BackupInfoScreen(RealmsScreen lastScreen, Backup backup) {
      this.lastScreen = lastScreen;
      this.backup = backup;
   }

   public void tick() {
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsAdd(newButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")));
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
         }

      }
   }

   protected void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      int y = 17;
      this.drawCenteredString("Changes from last backup", this.width() / 2, 10, 16777215);
      y += 30;

      for(String key : this.backup.changeList.keySet()) {
         this.drawString(key, this.width() / 2 - 100, y, 16777215);
         y += 12;
         String metadataValue = (String)this.backup.changeList.get(key);
         this.drawString(this.checkForSpecificMetadata(key, metadataValue), this.width() / 2 - 100, y, 10526880);
         y += 14;
      }

      super.render(xm, ym, a);
   }

   private String checkForSpecificMetadata(String key, String value) {
      String k = key.toLowerCase();
      if (k.contains("game") && k.contains("mode")) {
         return this.gameModeMetadata(value);
      } else {
         return k.contains("game") && k.contains("difficulty") ? this.gameDifficultyMetadata(value) : value;
      }
   }

   private String gameDifficultyMetadata(String value) {
      try {
         return this.difficulties[Integer.parseInt(value)];
      } catch (Exception var3) {
         return "UNKNOWN";
      }
   }

   private String gameModeMetadata(String value) {
      try {
         return this.gameModes[Integer.parseInt(value)];
      } catch (Exception var3) {
         return "UNKNOWN";
      }
   }
}
