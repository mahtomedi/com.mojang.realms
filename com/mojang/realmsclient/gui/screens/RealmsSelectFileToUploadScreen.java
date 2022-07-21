package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsConstants;
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

public class RealmsSelectFileToUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int CANCEL_BUTTON = 1;
   private static final int UPLOAD_BUTTON = 2;
   private final RealmsResetWorldScreen lastScreen;
   private final long worldId;
   private final int slotId;
   private RealmsButton uploadButton;
   private final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private List<RealmsLevelSummary> levelList = new ArrayList();
   private int selectedWorld = -1;
   private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
   private String worldLang;
   private String conversionLang;
   private final String[] gameModesLang = new String[4];

   public RealmsSelectFileToUploadScreen(long worldId, int slotId, RealmsResetWorldScreen lastScreen) {
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
      this.setKeyboardHandlerSendRepeatsToGui(true);

      try {
         this.loadLevelList();
      } catch (Exception var2) {
         LOGGER.error("Couldn't load level list", var2);
         Realms.setScreen(new RealmsGenericErrorScreen("Unable to load worlds", var2.getMessage(), this.lastScreen));
         return;
      }

      this.worldLang = getLocalizedString("selectWorld.world");
      this.conversionLang = getLocalizedString("selectWorld.conversion");
      this.gameModesLang[Realms.survivalId()] = getLocalizedString("gameMode.survival");
      this.gameModesLang[Realms.creativeId()] = getLocalizedString("gameMode.creative");
      this.gameModesLang[Realms.adventureId()] = getLocalizedString("gameMode.adventure");
      this.gameModesLang[Realms.spectatorId()] = getLocalizedString("gameMode.spectator");
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 6, this.height() - 32, 153, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsSelectFileToUploadScreen.this.lastScreen);
         }
      });
      this.buttonsAdd(
         this.uploadButton = new RealmsButton(2, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.upload.button.name")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSelectFileToUploadScreen.this.upload();
            }
         }
      );
      this.uploadButton.active(this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size());
      this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();
      this.addWidget(this.worldSelectionList);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   private void upload() {
      if (this.selectedWorld != -1 && !((RealmsLevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
         RealmsLevelSummary selectedLevel = (RealmsLevelSummary)this.levelList.get(this.selectedWorld);
         Realms.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, selectedLevel));
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.worldSelectionList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 13, 16777215);
      this.drawCenteredString(getLocalizedString("mco.upload.select.world.subtitle"), this.width() / 2, RealmsConstants.row(-1), 10526880);
      if (this.levelList.size() == 0) {
         this.drawCenteredString(getLocalizedString("mco.upload.select.world.none"), this.width() / 2, this.height() / 2 - 20, 16777215);
      }

      super.render(xm, ym, a);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void tick() {
      super.tick();
   }

   private class WorldSelectionList extends RealmsScrolledSelectionList {
      public WorldSelectionList() {
         super(
            RealmsSelectFileToUploadScreen.this.width(),
            RealmsSelectFileToUploadScreen.this.height(),
            RealmsConstants.row(0),
            RealmsSelectFileToUploadScreen.this.height() - 40,
            36
         );
      }

      public int getItemCount() {
         return RealmsSelectFileToUploadScreen.this.levelList.size();
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         RealmsSelectFileToUploadScreen.this.selectedWorld = item;
         RealmsSelectFileToUploadScreen.this.uploadButton
            .active(
               RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
                  && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
                  && !((RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore()
            );
         return true;
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
