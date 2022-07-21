package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSliderButton;
import org.lwjgl.input.Keyboard;

public class RealmsWorldSettingsSubScreen extends RealmsScreen {
   private static final int DONE_BUTTON_ID = 0;
   private static final int CANCEL_BUTTON = 1;
   private static final int DIFFICULTY_BUTTON_ID = 2;
   private static final int GAMEMODE_BUTTON_ID = 3;
   private static final int PVP_BUTTON_ID = 4;
   private static final int SPAWN_ANIMALS_BUTTON_ID = 5;
   private static final int SPAWN_MONSTERS_BUTTON_ID = 6;
   private static final int SPAWN_NPCS_BUTTON_ID = 7;
   private static final int SPAWN_PROTECTION_BUTTON_ID = 8;
   private static final int COMMANDBLOCKS_BUTTON_ID = 9;
   private static final int FORCE_GAME_MODE_ID = 10;
   protected final EditRealmsWorldScreen parent;
   private int column1_x;
   private int column_width;
   private int column2_x;
   private RealmsServer serverData;
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
   String[] difficulties;
   String[] gameModes;
   String[][] gameModeHints;

   public RealmsWorldSettingsSubScreen(EditRealmsWorldScreen parent, RealmsServer serverData) {
      this.parent = parent;
      this.serverData = serverData;
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            this.parent
               .saveServerData(
                  this.difficultyIndex,
                  this.gameModeIndex,
                  this.pvp,
                  this.spawnNPCs,
                  this.spawnAnimals,
                  this.spawnMonsters,
                  this.spawnProtection,
                  this.commandBlocks,
                  this.forceGameMode
               );
            this.parent.confirmResult(true, 0);
         } else if (button.id() == 1) {
            this.parent.confirmResult(false, 1);
         } else if (button.id() == 2) {
            this.difficultyIndex = (this.difficultyIndex + 1) % this.difficulties.length;
            button.msg(this.difficultyTitle());
            this.spawnMonstersButton.active(this.difficultyIndex != 0);
            this.spawnMonstersButton.msg(this.spawnMonstersTitle());
         } else if (button.id() == 3) {
            this.gameModeIndex = (this.gameModeIndex + 1) % this.gameModes.length;
            button.msg(this.gameModeTitle());
         } else if (button.id() == 4) {
            this.pvp = !this.pvp;
            button.msg(this.pvpTitle());
         } else if (button.id() == 5) {
            this.spawnAnimals = !this.spawnAnimals;
            button.msg(this.spawnAnimalsTitle());
         } else if (button.id() == 7) {
            this.spawnNPCs = !this.spawnNPCs;
            button.msg(this.spawnNPCsTitle());
         } else if (button.id() == 6) {
            this.spawnMonsters = !this.spawnMonsters;
            button.msg(this.spawnMonstersTitle());
         } else if (button.id() == 9) {
            this.commandBlocks = !this.commandBlocks;
            button.msg(this.commandBlocksTitle());
         } else if (button.id() == 10) {
            this.forceGameMode = !this.forceGameMode;
            button.msg(this.forceGameModeTitle());
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         this.parent.confirmResult(false, 1);
      }

   }

   public void init() {
      this.column1_x = this.width() / 2 - 122;
      this.column_width = 122;
      this.column2_x = this.width() / 2 + 10;
      this.createDifficultyAndGameMode();
      this.difficultyIndex = this.serverData.options.difficulty;
      this.gameModeIndex = this.serverData.options.gameMode;
      this.pvp = this.serverData.options.pvp;
      this.spawnAnimals = this.serverData.options.spawnAnimals;
      this.spawnMonsters = this.serverData.options.spawnMonsters;
      this.spawnNPCs = this.serverData.options.spawnNPCs;
      this.spawnProtection = this.serverData.options.spawnProtection;
      this.commandBlocks = this.serverData.options.commandBlocks;
      this.forceGameMode = this.serverData.options.forceGameMode;
      this.buttonsAdd(this.pvpButton = newButton(4, this.column1_x, this.height() / 4 + 0 - 20, this.column_width, 20, this.pvpTitle()));
      this.buttonsAdd(newButton(2, this.column1_x, this.height() / 4 + 24 - 20, this.column_width, 20, this.difficultyTitle()));
      this.buttonsAdd(
         this.spawnProtectionButton = new RealmsWorldSettingsSubScreen.SettingsSlider(
            8, this.column1_x, this.height() / 4 + 48 - 20, this.column_width, 17, this.spawnProtection, 0.0F, 16.0F
         )
      );
      this.buttonsAdd(this.forceGameModeButton = newButton(10, this.column1_x, this.height() / 4 + 72 - 20, this.column_width, 20, this.forceGameModeTitle()));
      this.buttonsAdd(newButton(3, this.column1_x, this.height() / 4 + 96 - 20, this.column_width, 20, this.gameModeTitle()));
      this.buttonsAdd(this.spawnAnimalsButton = newButton(5, this.column2_x, this.height() / 4 + 0 - 20, this.column_width, 20, this.spawnAnimalsTitle()));
      this.buttonsAdd(this.spawnMonstersButton = newButton(6, this.column2_x, this.height() / 4 + 24 - 20, this.column_width, 20, this.spawnMonstersTitle()));
      this.buttonsAdd(this.spawnNPCsButton = newButton(7, this.column2_x, this.height() / 4 + 48 - 20, this.column_width, 20, this.spawnNPCsTitle()));
      this.buttonsAdd(this.commandBlocksButton = newButton(9, this.column2_x, this.height() / 4 + 72 - 20, this.column_width, 20, this.commandBlocksTitle()));
      if (!this.serverData.worldType.equals(RealmsServer.WorldType.NORMAL)) {
         this.pvpButton.active(false);
         this.spawnAnimalsButton.active(false);
         this.spawnNPCsButton.active(false);
         this.spawnMonstersButton.active(false);
         this.spawnProtectionButton.active(false);
         this.commandBlocksButton.active(false);
         this.forceGameModeButton.active(false);
      }

      if (this.difficultyIndex == 0) {
         this.spawnMonstersButton.active(false);
      }

      this.buttonsAdd(newButton(0, this.column1_x, this.height() / 4 + 120 + 22, this.column_width, 20, getLocalizedString("mco.configure.world.buttons.done")));
      this.buttonsAdd(newButton(1, this.column2_x, this.height() / 4 + 120 + 22, this.column_width, 20, getLocalizedString("gui.cancel")));
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
      return getLocalizedString("mco.configure.world.pvp")
         + ": "
         + (this.pvp ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private String spawnAnimalsTitle() {
      return getLocalizedString("mco.configure.world.spawnAnimals")
         + ": "
         + (this.spawnAnimals ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private String spawnMonstersTitle() {
      return this.difficultyIndex == 0
         ? getLocalizedString("mco.configure.world.spawnMonsters") + ": " + getLocalizedString("mco.configure.world.off")
         : getLocalizedString("mco.configure.world.spawnMonsters")
            + ": "
            + (this.spawnMonsters ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private String spawnNPCsTitle() {
      return getLocalizedString("mco.configure.world.spawnNPCs")
         + ": "
         + (this.spawnNPCs ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private String commandBlocksTitle() {
      return getLocalizedString("mco.configure.world.commandBlocks")
         + ": "
         + (this.commandBlocks ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private String forceGameModeTitle() {
      return getLocalizedString("mco.configure.world.forceGameMode")
         + ": "
         + (this.forceGameMode ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.world.edit.subscreen.title"), this.width() / 2, 17, 16777215);
      this.renderHints();
      super.render(xm, ym, a);
   }

   public void renderHints() {
      this.drawString(this.gameModeHints[this.gameModeIndex][0], this.column1_x + 2, this.height() / 4 + 96 + 6, 10526880);
      this.drawString(this.gameModeHints[this.gameModeIndex][1], this.column1_x + 2, this.height() / 4 + 96 + 6 + this.fontLineHeight(), 10526880);
   }

   public void mouseReleased(int x, int y, int buttonNum) {
      this.spawnProtectionButton.released(x, y);
   }

   public void mouseDragged(int x, int y, int buttonNum, long delta) {
      this.spawnProtectionButton.clicked(x, y);
   }

   private class SettingsSlider extends RealmsSliderButton {
      public SettingsSlider(int id, int x, int y, int width, int steps, int currentValue, float minValue, float maxValue) {
         super(id, x, y, width, steps, currentValue, minValue, maxValue);
      }

      public String getMessage() {
         return RealmsScreen.getLocalizedString("mco.configure.world.spawnProtection")
            + ": "
            + (
               RealmsWorldSettingsSubScreen.this.spawnProtection == 0
                  ? RealmsScreen.getLocalizedString("mco.configure.world.off")
                  : RealmsWorldSettingsSubScreen.this.spawnProtection
            );
      }

      public void clicked(float value) {
         RealmsWorldSettingsSubScreen.this.spawnProtection = (int)value;
      }
   }
}
