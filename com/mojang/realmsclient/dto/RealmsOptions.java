package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;

public class RealmsOptions {
   public Boolean pvp;
   public Boolean spawnAnimals;
   public Boolean spawnMonsters;
   public Boolean spawnNPCs;
   public Integer spawnProtection;
   public Boolean commandBlocks;
   private static boolean pvpDefault = true;
   private static boolean spawnAnimalsDefault = true;
   private static boolean spawnMonstersDefault = true;
   private static boolean spawnNPCsDefault = true;
   private static int spawnProtectionDefault = 0;
   private static boolean commandBlocksDefault = false;

   public RealmsOptions(Boolean pvp, Boolean spawnAnimals, Boolean spawnMonsters, Boolean spawnNPCs, Integer spawnProtection, Boolean commandBlocks) {
      this.pvp = pvp;
      this.spawnAnimals = spawnAnimals;
      this.spawnMonsters = spawnMonsters;
      this.spawnNPCs = spawnNPCs;
      this.spawnProtection = spawnProtection;
      this.commandBlocks = commandBlocks;
   }

   public static RealmsOptions getDefaults() {
      return new RealmsOptions(pvpDefault, spawnAnimalsDefault, spawnMonstersDefault, spawnNPCsDefault, spawnProtectionDefault, commandBlocksDefault);
   }

   public static RealmsOptions parse(JsonObject jsonObject) {
      return new RealmsOptions(
         JsonUtils.getBooleanOr("pvp", jsonObject, pvpDefault),
         JsonUtils.getBooleanOr("spawnAnimals", jsonObject, spawnAnimalsDefault),
         JsonUtils.getBooleanOr("spawnMonsters", jsonObject, spawnMonstersDefault),
         JsonUtils.getBooleanOr("spawnNPCs", jsonObject, spawnNPCsDefault),
         JsonUtils.getIntOr("spawnProtection", jsonObject, spawnProtectionDefault),
         JsonUtils.getBooleanOr("commandBlocks", jsonObject, commandBlocksDefault)
      );
   }

   public String toJson() {
      JsonObject jsonObject = new JsonObject();
      if (this.pvp != pvpDefault) {
         jsonObject.addProperty("pvp", this.pvp);
      }

      if (this.spawnAnimals != spawnAnimalsDefault) {
         jsonObject.addProperty("spawnAnimals", this.spawnAnimals);
      }

      if (this.spawnMonsters != spawnMonstersDefault) {
         jsonObject.addProperty("spawnMonsters", this.spawnMonsters);
      }

      if (this.spawnNPCs != spawnNPCsDefault) {
         jsonObject.addProperty("spawnNPCs", this.spawnNPCs);
      }

      if (this.spawnProtection != spawnProtectionDefault) {
         jsonObject.addProperty("spawnProtection", this.spawnProtection);
      }

      if (this.commandBlocks != commandBlocksDefault) {
         jsonObject.addProperty("commandBlocks", this.commandBlocks);
      }

      return jsonObject.toString();
   }
}
