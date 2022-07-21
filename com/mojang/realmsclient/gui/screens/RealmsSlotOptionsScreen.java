package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSliderButton;

public class RealmsSlotOptionsScreen extends RealmsScreen {
   private static final int BUTTON_CANCEL_ID = 0;
   private static final int BUTTON_DONE_ID = 1;
   private static final int BUTTON_DIFFICULTY_ID = 2;
   private static final int BUTTON_GAMEMODE_ID = 3;
   private static final int BUTTON_PVP_ID = 4;
   private static final int BUTTON_SPAWN_ANIMALS_ID = 5;
   private static final int BUTTON_SPAWN_MONSTERS_ID = 6;
   private static final int BUTTON_SPAWN_NPCS_ID = 7;
   private static final int BUTTON_SPAWN_PROTECTION_ID = 8;
   private static final int BUTTON_COMMANDBLOCKS_ID = 9;
   private static final int BUTTON_FORCE_GAMEMODE_ID = 10;
   private static final int NAME_EDIT_BOX = 11;
   private RealmsEditBox nameEdit;
   protected final RealmsConfigureWorldScreen parent;
   private int column1_x;
   private int column_width;
   private int column2_x;
   private final RealmsWorldOptions options;
   private final RealmsServer.WorldType worldType;
   private final int activeSlot;
   private int difficultyIndex;
   private int gameModeIndex;
   private Boolean pvp;
   private Boolean spawnNPCs;
   private Boolean spawnAnimals;
   private Boolean spawnMonsters;
   private Integer spawnProtection;
   private Boolean commandBlocks;
   private Boolean forceGameMode;
   private RealmsButton pvpButton;
   private RealmsButton spawnAnimalsButton;
   private RealmsButton spawnMonstersButton;
   private RealmsButton spawnNPCsButton;
   private RealmsSliderButton spawnProtectionButton;
   private RealmsButton commandBlocksButton;
   private RealmsButton forceGameModeButton;
   private boolean notNormal;
   String[] difficulties;
   String[] gameModes;
   String[][] gameModeHints;

   public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen configureWorldScreen, RealmsWorldOptions options, RealmsServer.WorldType worldType, int activeSlot) {
      this.parent = configureWorldScreen;
      this.options = options;
      this.worldType = worldType;
      this.activeSlot = activeSlot;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public void tick() {
      this.nameEdit.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.parent);
            return true;
         case 257:
         case 335:
            this.saveSettings();
            return true;
         case 258:
            this.focusNext();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void init() {
      this.column_width = 170;
      this.column1_x = this.width() / 2 - this.column_width * 2 / 2;
      this.column2_x = this.width() / 2 + 10;
      this.createDifficultyAndGameMode();
      this.difficultyIndex = this.options.difficulty;
      this.gameModeIndex = this.options.gameMode;
      if (this.worldType.equals(RealmsServer.WorldType.NORMAL)) {
         this.pvp = this.options.pvp;
         this.spawnProtection = this.options.spawnProtection;
         this.forceGameMode = this.options.forceGameMode;
         this.spawnAnimals = this.options.spawnAnimals;
         this.spawnMonsters = this.options.spawnMonsters;
         this.spawnNPCs = this.options.spawnNPCs;
         this.commandBlocks = this.options.commandBlocks;
      } else {
         this.notNormal = true;
         this.pvp = true;
         this.spawnProtection = 0;
         this.forceGameMode = false;
         this.spawnAnimals = true;
         this.spawnMonsters = true;
         this.spawnNPCs = true;
         this.commandBlocks = true;
      }

      this.nameEdit = this.newEditBox(11, this.column1_x + 2, RealmsConstants.row(1), this.column_width - 4, 20);
      this.nameEdit.setMaxLength(10);
      this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
      this.focusOn(this.nameEdit);
      this.buttonsAdd(this.pvpButton = new RealmsButton(4, this.column2_x, RealmsConstants.row(1), this.column_width, 20, this.pvpTitle()) {
         public void onClick(double mouseX, double mouseY) {
            RealmsSlotOptionsScreen.this.pvp = !RealmsSlotOptionsScreen.this.pvp;
            this.msg(RealmsSlotOptionsScreen.this.pvpTitle());
         }
      });
      this.buttonsAdd(new RealmsButton(3, this.column1_x, RealmsConstants.row(3), this.column_width, 20, this.gameModeTitle()) {
         public void onClick(double mouseX, double mouseY) {
            RealmsSlotOptionsScreen.this.gameModeIndex = (RealmsSlotOptionsScreen.this.gameModeIndex + 1) % RealmsSlotOptionsScreen.this.gameModes.length;
            this.msg(RealmsSlotOptionsScreen.this.gameModeTitle());
         }
      });
      this.buttonsAdd(this.spawnAnimalsButton = new RealmsButton(5, this.column2_x, RealmsConstants.row(3), this.column_width, 20, this.spawnAnimalsTitle()) {
         public void onClick(double mouseX, double mouseY) {
            RealmsSlotOptionsScreen.this.spawnAnimals = !RealmsSlotOptionsScreen.this.spawnAnimals;
            this.msg(RealmsSlotOptionsScreen.this.spawnAnimalsTitle());
         }
      });
      this.buttonsAdd(
         new RealmsButton(2, this.column1_x, RealmsConstants.row(5), this.column_width, 20, this.difficultyTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSlotOptionsScreen.this.difficultyIndex = (RealmsSlotOptionsScreen.this.difficultyIndex + 1)
                  % RealmsSlotOptionsScreen.this.difficulties.length;
               this.msg(RealmsSlotOptionsScreen.this.difficultyTitle());
               if (RealmsSlotOptionsScreen.this.worldType.equals(RealmsServer.WorldType.NORMAL)) {
                  RealmsSlotOptionsScreen.this.spawnMonstersButton.active(RealmsSlotOptionsScreen.this.difficultyIndex != 0);
                  RealmsSlotOptionsScreen.this.spawnMonstersButton.msg(RealmsSlotOptionsScreen.this.spawnMonstersTitle());
               }
   
            }
         }
      );
      this.buttonsAdd(
         this.spawnMonstersButton = new RealmsButton(6, this.column2_x, RealmsConstants.row(5), this.column_width, 20, this.spawnMonstersTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSlotOptionsScreen.this.spawnMonsters = !RealmsSlotOptionsScreen.this.spawnMonsters;
               this.msg(RealmsSlotOptionsScreen.this.spawnMonstersTitle());
            }
         }
      );
      this.buttonsAdd(
         this.spawnProtectionButton = new RealmsSlotOptionsScreen.SettingsSlider(
            8, this.column1_x, RealmsConstants.row(7), this.column_width, 17, this.spawnProtection, 0.0F, 16.0F
         )
      );
      this.buttonsAdd(this.spawnNPCsButton = new RealmsButton(7, this.column2_x, RealmsConstants.row(7), this.column_width, 20, this.spawnNPCsTitle()) {
         public void onClick(double mouseX, double mouseY) {
            RealmsSlotOptionsScreen.this.spawnNPCs = !RealmsSlotOptionsScreen.this.spawnNPCs;
            this.msg(RealmsSlotOptionsScreen.this.spawnNPCsTitle());
         }
      });
      this.buttonsAdd(
         this.forceGameModeButton = new RealmsButton(10, this.column1_x, RealmsConstants.row(9), this.column_width, 20, this.forceGameModeTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSlotOptionsScreen.this.forceGameMode = !RealmsSlotOptionsScreen.this.forceGameMode;
               this.msg(RealmsSlotOptionsScreen.this.forceGameModeTitle());
            }
         }
      );
      this.buttonsAdd(
         this.commandBlocksButton = new RealmsButton(9, this.column2_x, RealmsConstants.row(9), this.column_width, 20, this.commandBlocksTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSlotOptionsScreen.this.commandBlocks = !RealmsSlotOptionsScreen.this.commandBlocks;
               this.msg(RealmsSlotOptionsScreen.this.commandBlocksTitle());
            }
         }
      );
      if (!this.worldType.equals(RealmsServer.WorldType.NORMAL)) {
         this.pvpButton.active(false);
         this.spawnAnimalsButton.active(false);
         this.spawnNPCsButton.active(false);
         this.spawnMonstersButton.active(false);
         this.spawnProtectionButton.active(false);
         this.commandBlocksButton.active(false);
         this.spawnProtectionButton.active(false);
         this.forceGameModeButton.active(false);
      }

      if (this.difficultyIndex == 0) {
         this.spawnMonstersButton.active(false);
      }

      this.buttonsAdd(
         new RealmsButton(1, this.column1_x, RealmsConstants.row(13), this.column_width, 20, getLocalizedString("mco.configure.world.buttons.done")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSlotOptionsScreen.this.saveSettings();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.column2_x, RealmsConstants.row(13), this.column_width, 20, getLocalizedString("gui.cancel")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsSlotOptionsScreen.this.parent);
         }
      });
      this.addWidget(this.nameEdit);
   }

   private void createDifficultyAndGameMode() {
      this.difficulties = new String[]{
         getLocalizedString("options.difficulty.peaceful"),
         getLocalizedString("options.difficulty.easy"),
         getLocalizedString("options.difficulty.normal"),
         getLocalizedString("options.difficulty.hard")
      };
      this.gameModes = new String[]{
         getLocalizedString("selectWorld.gameMode.survival"),
         getLocalizedString("selectWorld.gameMode.creative"),
         getLocalizedString("selectWorld.gameMode.adventure")
      };
      this.gameModeHints = new String[][]{
         {getLocalizedString("selectWorld.gameMode.survival.line1"), getLocalizedString("selectWorld.gameMode.survival.line2")},
         {getLocalizedString("selectWorld.gameMode.creative.line1"), getLocalizedString("selectWorld.gameMode.creative.line2")},
         {getLocalizedString("selectWorld.gameMode.adventure.line1"), getLocalizedString("selectWorld.gameMode.adventure.line2")}
      };
   }

   private String difficultyTitle() {
      String difficulty = getLocalizedString("options.difficulty");
      return difficulty + ": " + this.difficulties[this.difficultyIndex];
   }

   private String gameModeTitle() {
      String gameMode = getLocalizedString("selectWorld.gameMode");
      return gameMode + ": " + this.gameModes[this.gameModeIndex];
   }

   private String pvpTitle() {
      return getLocalizedString("mco.configure.world.pvp") + ": " + getLocalizedString(this.pvp ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String spawnAnimalsTitle() {
      return getLocalizedString("mco.configure.world.spawnAnimals")
         + ": "
         + getLocalizedString(this.spawnAnimals ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String spawnMonstersTitle() {
      return this.difficultyIndex == 0
         ? getLocalizedString("mco.configure.world.spawnMonsters") + ": " + getLocalizedString("mco.configure.world.off")
         : getLocalizedString("mco.configure.world.spawnMonsters")
            + ": "
            + getLocalizedString(this.spawnMonsters ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String spawnNPCsTitle() {
      return getLocalizedString("mco.configure.world.spawnNPCs")
         + ": "
         + getLocalizedString(this.spawnNPCs ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String commandBlocksTitle() {
      return getLocalizedString("mco.configure.world.commandBlocks")
         + ": "
         + getLocalizedString(this.commandBlocks ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   private String forceGameModeTitle() {
      return getLocalizedString("mco.configure.world.forceGameMode")
         + ": "
         + getLocalizedString(this.forceGameMode ? "mco.configure.world.on" : "mco.configure.world.off");
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      String slotName = getLocalizedString("mco.configure.world.edit.slot.name");
      this.drawString(slotName, this.column1_x + this.column_width / 2 - this.fontWidth(slotName) / 2, RealmsConstants.row(0) - 5, 16777215);
      this.drawCenteredString(getLocalizedString("mco.configure.world.buttons.options"), this.width() / 2, 17, 16777215);
      if (this.notNormal) {
         if (this.worldType.equals(RealmsServer.WorldType.ADVENTUREMAP)) {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.adventuremap"), this.width() / 2, 26, 16711680);
         } else if (this.worldType.equals(RealmsServer.WorldType.INSPIRATION)) {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.inspiration"), this.width() / 2, 26, 16711680);
         } else {
            this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.experience"), this.width() / 2, 26, 16711680);
         }
      }

      this.nameEdit.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   public boolean mouseReleased(double x, double y, int buttonNum) {
      if (!this.spawnProtectionButton.active()) {
         return super.mouseReleased(x, y, buttonNum);
      } else {
         this.spawnProtectionButton.onRelease(x, y);
         return true;
      }
   }

   public boolean mouseDragged(double x, double y, int buttonNum, double dx, double dy) {
      if (!this.spawnProtectionButton.active()) {
         return super.mouseDragged(x, y, buttonNum, dx, dy);
      } else {
         if (x < (double)(this.column1_x + this.spawnProtectionButton.getWidth())
            && x > (double)this.column1_x
            && y < (double)(this.spawnProtectionButton.y() + 20)
            && y > (double)this.spawnProtectionButton.y()) {
            this.spawnProtectionButton.onClick(x, y);
         }

         return true;
      }
   }

   private String getSlotName() {
      return this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot)) ? "" : this.nameEdit.getValue();
   }

   private void saveSettings() {
      if (!this.worldType.equals(RealmsServer.WorldType.ADVENTUREMAP)
         && !this.worldType.equals(RealmsServer.WorldType.EXPERIENCE)
         && !this.worldType.equals(RealmsServer.WorldType.INSPIRATION)) {
         this.parent
            .saveSlotSettings(
               new RealmsWorldOptions(
                  this.pvp,
                  this.spawnAnimals,
                  this.spawnMonsters,
                  this.spawnNPCs,
                  this.spawnProtection,
                  this.commandBlocks,
                  this.difficultyIndex,
                  this.gameModeIndex,
                  this.forceGameMode,
                  this.getSlotName()
               )
            );
      } else {
         this.parent
            .saveSlotSettings(
               new RealmsWorldOptions(
                  this.options.pvp,
                  this.options.spawnAnimals,
                  this.options.spawnMonsters,
                  this.options.spawnNPCs,
                  this.options.spawnProtection,
                  this.options.commandBlocks,
                  this.difficultyIndex,
                  this.gameModeIndex,
                  this.options.forceGameMode,
                  this.getSlotName()
               )
            );
      }

   }

   private class SettingsSlider extends RealmsSliderButton {
      public SettingsSlider(int id, int x, int y, int width, int steps, int currentValue, float minValue, float maxValue) {
         super(id, x, y, width, steps, currentValue, (double)minValue, (double)maxValue);
      }

      public String getMessage() {
         return RealmsScreen.getLocalizedString("mco.configure.world.spawnProtection")
            + ": "
            + (
               RealmsSlotOptionsScreen.this.spawnProtection == 0
                  ? RealmsScreen.getLocalizedString("mco.configure.world.off")
                  : RealmsSlotOptionsScreen.this.spawnProtection
            );
      }

      public void clicked(double value) {
         if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active()) {
            RealmsSlotOptionsScreen.this.spawnProtection = (int)value;
         }
      }
   }
}
