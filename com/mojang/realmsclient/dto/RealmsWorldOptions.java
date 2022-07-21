package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import net.minecraft.realms.RealmsScreen;
import realms.bd;
import realms.l;

@DontObfuscateOrShrink
public class RealmsWorldOptions extends l {
   public Boolean pvp;
   public Boolean spawnAnimals;
   public Boolean spawnMonsters;
   public Boolean spawnNPCs;
   public Integer spawnProtection;
   public Boolean commandBlocks;
   public Boolean forceGameMode;
   public Integer difficulty;
   public Integer gameMode;
   public String slotName;
   public long templateId;
   public String templateImage;
   public boolean adventureMap;
   public boolean empty;
   private static final boolean forceGameModeDefault = false;
   private static final boolean pvpDefault = true;
   private static final boolean spawnAnimalsDefault = true;
   private static final boolean spawnMonstersDefault = true;
   private static final boolean spawnNPCsDefault = true;
   private static final int spawnProtectionDefault = 0;
   private static final boolean commandBlocksDefault = false;
   private static final int difficultyDefault = 2;
   private static final int gameModeDefault = 0;
   private static final String slotNameDefault = "";
   private static final long templateIdDefault = -1L;
   private static final String templateImageDefault = null;
   private static final boolean adventureMapDefault = false;

   public RealmsWorldOptions(
      Boolean pvp,
      Boolean spawnAnimals,
      Boolean spawnMonsters,
      Boolean spawnNPCs,
      Integer spawnProtection,
      Boolean commandBlocks,
      Integer difficulty,
      Integer gameMode,
      Boolean forceGameMode,
      String slotName
   ) {
      this.pvp = pvp;
      this.spawnAnimals = spawnAnimals;
      this.spawnMonsters = spawnMonsters;
      this.spawnNPCs = spawnNPCs;
      this.spawnProtection = spawnProtection;
      this.commandBlocks = commandBlocks;
      this.difficulty = difficulty;
      this.gameMode = gameMode;
      this.forceGameMode = forceGameMode;
      this.slotName = slotName;
   }

   public static RealmsWorldOptions getDefaults() {
      return new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
   }

   public static RealmsWorldOptions getEmptyDefaults() {
      RealmsWorldOptions options = new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
      options.setEmpty(true);
      return options;
   }

   public void setEmpty(boolean empty) {
      this.empty = empty;
   }

   public static RealmsWorldOptions parse(JsonObject jsonObject) {
      RealmsWorldOptions newOptions = new RealmsWorldOptions(
         bd.a("pvp", jsonObject, true),
         bd.a("spawnAnimals", jsonObject, true),
         bd.a("spawnMonsters", jsonObject, true),
         bd.a("spawnNPCs", jsonObject, true),
         bd.a("spawnProtection", jsonObject, 0),
         bd.a("commandBlocks", jsonObject, false),
         bd.a("difficulty", jsonObject, 2),
         bd.a("gameMode", jsonObject, 0),
         bd.a("forceGameMode", jsonObject, false),
         bd.a("slotName", jsonObject, "")
      );
      newOptions.templateId = bd.a("worldTemplateId", jsonObject, -1L);
      newOptions.templateImage = bd.a("worldTemplateImage", jsonObject, templateImageDefault);
      newOptions.adventureMap = bd.a("adventureMap", jsonObject, false);
      return newOptions;
   }

   public String getSlotName(int i) {
      if (this.slotName != null && !this.slotName.isEmpty()) {
         return this.slotName;
      } else {
         return this.empty ? RealmsScreen.getLocalizedString("mco.configure.world.slot.empty") : this.getDefaultSlotName(i);
      }
   }

   public String getDefaultSlotName(int i) {
      return RealmsScreen.getLocalizedString("mco.configure.world.slot", new Object[]{i});
   }

   public String toJson() {
      JsonObject jsonObject = new JsonObject();
      if (!this.pvp) {
         jsonObject.addProperty("pvp", this.pvp);
      }

      if (!this.spawnAnimals) {
         jsonObject.addProperty("spawnAnimals", this.spawnAnimals);
      }

      if (!this.spawnMonsters) {
         jsonObject.addProperty("spawnMonsters", this.spawnMonsters);
      }

      if (!this.spawnNPCs) {
         jsonObject.addProperty("spawnNPCs", this.spawnNPCs);
      }

      if (this.spawnProtection != 0) {
         jsonObject.addProperty("spawnProtection", this.spawnProtection);
      }

      if (this.commandBlocks) {
         jsonObject.addProperty("commandBlocks", this.commandBlocks);
      }

      if (this.difficulty != 2) {
         jsonObject.addProperty("difficulty", this.difficulty);
      }

      if (this.gameMode != 0) {
         jsonObject.addProperty("gameMode", this.gameMode);
      }

      if (this.forceGameMode) {
         jsonObject.addProperty("forceGameMode", this.forceGameMode);
      }

      if (this.slotName != null && !this.slotName.equals("")) {
         jsonObject.addProperty("slotName", this.slotName);
      }

      return jsonObject.toString();
   }

   public RealmsWorldOptions clone() {
      return new RealmsWorldOptions(
         this.pvp,
         this.spawnAnimals,
         this.spawnMonsters,
         this.spawnNPCs,
         this.spawnProtection,
         this.commandBlocks,
         this.difficulty,
         this.gameMode,
         this.forceGameMode,
         this.slotName
      );
   }
}
