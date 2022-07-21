package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
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
   private RealmsLabel titleLabel;
   private RealmsLabel subtitleLabel;

   public RealmsSelectFileToUploadScreen(long worldId, int slotId, RealmsResetWorldScreen lastScreen) {
      this.lastScreen = lastScreen;
      this.worldId = worldId;
      this.slotId = slotId;
   }

   private void loadLevelList() throws Exception {
      RealmsAnvilLevelStorageSource levelSource = this.getLevelStorageSource();
      this.levelList = levelSource.getLevelList();
      Collections.sort(this.levelList);

      for(RealmsLevelSummary summary : this.levelList) {
         this.worldSelectionList.addEntry(summary);
      }

   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

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
      this.addWidget(this.worldSelectionList);
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 6, this.height() - 32, 153, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(RealmsSelectFileToUploadScreen.this.lastScreen);
         }
      });
      this.buttonsAdd(
         this.uploadButton = new RealmsButton(2, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.upload.button.name")) {
            public void onPress() {
               RealmsSelectFileToUploadScreen.this.upload();
            }
         }
      );
      this.uploadButton.active(this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size());
      this.addWidget(this.titleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 13, 16777215));
      this.addWidget(
         this.subtitleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.subtitle"), this.width() / 2, RealmsConstants.row(-1), 10526880)
      );
      this.narrateLabels();
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
      this.titleLabel.render(this);
      this.subtitleLabel.render(this);
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

   private class WorldListEntry extends RealmListEntry {
      final RealmsLevelSummary levelSummary;

      public WorldListEntry(RealmsLevelSummary levelSummary) {
         this.levelSummary = levelSummary;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderItem(this.levelSummary, index, rowLeft, rowTop, rowHeight, Tezzelator.instance, mouseX, mouseY);
      }

      public boolean mouseClicked(double x, double y, int buttonNum) {
         RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
         return true;
      }

      protected void renderItem(RealmsLevelSummary levelSummary, int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
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

   private class WorldSelectionList extends RealmsObjectSelectionList {
      public WorldSelectionList() {
         super(
            RealmsSelectFileToUploadScreen.this.width(),
            RealmsSelectFileToUploadScreen.this.height(),
            RealmsConstants.row(0),
            RealmsSelectFileToUploadScreen.this.height() - 40,
            36
         );
      }

      public void addEntry(RealmsLevelSummary levelSummary) {
         this.addEntry(RealmsSelectFileToUploadScreen.this.new WorldListEntry(levelSummary));
      }

      public int getItemCount() {
         return RealmsSelectFileToUploadScreen.this.levelList.size();
      }

      public int getMaxPosition() {
         return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
      }

      public boolean isFocused() {
         return RealmsSelectFileToUploadScreen.this.isFocused(this);
      }

      public void renderBackground() {
         RealmsSelectFileToUploadScreen.this.renderBackground();
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         this.selectItem(item);
         return true;
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(
               RealmsScreen.getLocalizedString(
                  "narrator.select", new Object[]{((RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(item)).getLevelName()}
               )
            );
         }

         RealmsSelectFileToUploadScreen.this.selectedWorld = item;
         RealmsSelectFileToUploadScreen.this.uploadButton
            .active(
               RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
                  && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
                  && !((RealmsLevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore()
            );
      }

      public boolean isSelectedItem(int item) {
         return item == RealmsSelectFileToUploadScreen.this.selectedWorld;
      }
   }
}
