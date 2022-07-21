package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsSelectFileToUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int CANCEL_BUTTON = 1;
   private static final int UPLOAD_BUTTON = 2;
   private final RealmsScreen lastScreen;
   private final RealmsScreen configureScreen;
   private final long worldId;
   private int slotId;
   private RealmsButton uploadButton;
   private final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private List<RealmsLevelSummary> levelList = new ArrayList();
   private int selectedWorld = -1;
   private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
   private String worldLang;
   private String conversionLang;
   private String[] gameModesLang = new String[4];
   private String errorMessage = null;

   public RealmsSelectFileToUploadScreen(long worldId, int slotId, RealmsScreen configureScreen, RealmsScreen lastScreen) {
      this.configureScreen = configureScreen;
      this.lastScreen = lastScreen;
      this.worldId = worldId;
      this.slotId = slotId;
   }

   private void loadLevelList() throws Exception {
      RealmsAnvilLevelStorageSource levelSource = this.getLevelStorageSource();
      this.levelList = levelSource.getLevelList();
      Collections.sort(this.levelList);
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();

      try {
         this.loadLevelList();
      } catch (Exception var3) {
         LOGGER.error("Couldn't load level list", var3);
         Realms.setScreen(new RealmsErrorScreen("Unable to load worlds", var3.getMessage(), this.lastScreen));
         return;
      }

      this.worldLang = getLocalizedString("selectWorld.world");
      this.conversionLang = getLocalizedString("selectWorld.conversion");
      this.gameModesLang[Realms.survivalId()] = getLocalizedString("gameMode.survival");
      this.gameModesLang[Realms.creativeId()] = getLocalizedString("gameMode.creative");
      this.gameModesLang[Realms.adventureId()] = getLocalizedString("gameMode.adventure");
      this.gameModesLang[Realms.spectatorId()] = getLocalizedString("gameMode.spectator");
      int x1 = (this.width() / 2 - 170) / 2;
      int x2 = x1 + this.width() / 2;
      this.buttonsAdd(newButton(1, this.width() / 2 + 6, this.height() - 48, 153, 20, getLocalizedString("gui.cancel")));
      this.buttonsAdd(this.uploadButton = newButton(2, this.width() / 2 - 154, this.height() - 48, 153, 20, getLocalizedString("mco.upload.button.name")));
      this.uploadButton.active(this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size());
      this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 2) {
            this.upload();
         }

      }
   }

   private void upload() {
      if (this.selectedWorld != -1 && !((RealmsLevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
         RealmsLevelSummary selectedLevel = (RealmsLevelSummary)this.levelList.get(this.selectedWorld);
         Realms.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.configureScreen, selectedLevel));
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.worldSelectionList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 9, 16777215);
      if (this.errorMessage != null) {
         this.drawCenteredString(this.errorMessage, this.width() / 2, this.uploadButton.y() - 13, 16711680);
      }

      super.render(xm, ym, a);
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void mouseEvent() {
      super.mouseEvent();
      this.worldSelectionList.mouseEvent();
   }

   public void tick() {
      super.tick();
   }

   private class WorldSelectionList extends RealmsScrolledSelectionList {
      public WorldSelectionList() {
         super(
            RealmsSelectFileToUploadScreen.this.width(),
            RealmsSelectFileToUploadScreen.this.height(),
            32,
            RealmsSelectFileToUploadScreen.this.height() - 64,
            36
         );
      }

      public int getItemCount() {
         return RealmsSelectFileToUploadScreen.this.levelList.size();
      }

      public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
         RealmsSelectFileToUploadScreen.this.selectedWorld = item;
         RealmsSelectFileToUploadScreen.this.uploadButton
            .active(
               RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
                  && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
                  && !((RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore()
            );
         RealmsSelectFileToUploadScreen.this.errorMessage = null;
         if (doubleClick) {
            RealmsSelectFileToUploadScreen.this.upload();
         }

      }

      public boolean isSelectedItem(int item) {
         return item == RealmsSelectFileToUploadScreen.this.selectedWorld;
      }

      public int getMaxPosition() {
         return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
      }

      public void renderBackground() {
         RealmsSelectFileToUploadScreen.this.renderBackground();
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         RealmsLevelSummary levelSummary = (RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(i);
         String name = levelSummary.getLevelName();
         if (name == null || name.isEmpty()) {
            name = RealmsSelectFileToUploadScreen.this.worldLang + " " + (i + 1);
         }

         String id = levelSummary.getLevelId();
         id = id + " (" + RealmsSelectFileToUploadScreen.this.DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
         id = id + ")";
         String info = "";
         if (levelSummary.isRequiresConversion()) {
            info = RealmsSelectFileToUploadScreen.this.conversionLang + " " + info;
         } else {
            info = RealmsSelectFileToUploadScreen.this.gameModesLang[levelSummary.getGameMode()];
            if (levelSummary.isHardcore()) {
               info = ChatFormatting.DARK_RED + RealmsScreen.getLocalizedString("mco.upload.hardcore") + ChatFormatting.RESET;
            }

            if (levelSummary.hasCheats()) {
               info = info + ", " + RealmsScreen.getLocalizedString("selectWorld.cheats");
            }
         }

         RealmsSelectFileToUploadScreen.this.drawString(name, x + 2, y + 1, 16777215);
         RealmsSelectFileToUploadScreen.this.drawString(id, x + 2, y + 12, 8421504);
         RealmsSelectFileToUploadScreen.this.drawString(info, x + 2, y + 12 + 10, 8421504);
      }
   }
}
