package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.Backup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Keyboard;

public class BackupInfoScreen extends RealmsScreen {
   private final RealmsScreen lastScreen;
   private final int BUTTON_BACK_ID = 0;
   private final Backup backup;
   private List<String> keys = new ArrayList();
   private BackupInfoScreen.BackupInfoList backupInfoList;
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
      if (backup.changeList != null) {
         for(Entry<String, String> entry : backup.changeList.entrySet()) {
            this.keys.add(entry.getKey());
         }
      }

   }

   public void tick() {
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsAdd(newButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 24, getLocalizedString("gui.back")));
      this.backupInfoList = new BackupInfoScreen.BackupInfoList();
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString("Changes from last backup", this.width() / 2, 10, 16777215);
      this.backupInfoList.render(xm, ym, a);
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

   private class BackupInfoList extends MovableScrolledSelectionList {
      public BackupInfoList() {
         super(0, 30, BackupInfoScreen.this.width(), BackupInfoScreen.this.height() - 84, 30);
      }

      @Override
      protected int getNumberOfItems() {
         return BackupInfoScreen.this.backup.changeList.size();
      }

      @Override
      protected void selectItem(int item, boolean doubleClick) {
      }

      @Override
      protected boolean isSelectedItem(int item) {
         return false;
      }

      @Override
      protected int getMaxPosition() {
         return this.getNumberOfItems() * 30;
      }

      @Override
      protected void renderBackground() {
      }

      @Override
      protected void renderItem(int i, int x, int y, int h, Tezzelator t) {
         String key = (String)BackupInfoScreen.this.keys.get(i);
         BackupInfoScreen.this.drawString(key, BackupInfoScreen.this.width() / 2 - 40, y, 16777215);
         String metadataValue = (String)BackupInfoScreen.this.backup.changeList.get(key);
         BackupInfoScreen.this.drawString(
            BackupInfoScreen.this.checkForSpecificMetadata(key, metadataValue), BackupInfoScreen.this.width() / 2 - 40, y + 12, 10526880
         );
      }
   }
}
