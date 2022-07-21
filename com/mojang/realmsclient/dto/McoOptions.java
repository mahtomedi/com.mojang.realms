package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;

public class McoOptions {
   public Boolean pvp;
   public Boolean spawnAnimals;
   public Boolean spawnMonsters;
   public Boolean spawnNPCs;
   public Integer spawnProtection;
   private static boolean pvpDefault = true;
   private static boolean spawnAnimalsDefault = true;
   private static boolean spawnMonstersDefault = true;
   private static boolean spawnNPCsDefault = true;
   private static int spawnProtectionDefault = 0;

   public McoOptions(Boolean pvp, Boolean spawnAnimals, Boolean spawnMonsters, Boolean spawnNPCs, Integer spawnProtection) {
      this.pvp = pvp;
      this.spawnAnimals = spawnAnimals;
      this.spawnMonsters = spawnMonsters;
      this.spawnNPCs = spawnNPCs;
      this.spawnProtection = spawnProtection;
   }

   public static McoOptions getDefaults() {
      return new McoOptions(pvpDefault, spawnAnimalsDefault, spawnMonstersDefault, spawnNPCsDefault, spawnProtectionDefault);
   }

   public static McoOptions parse(JsonObject jsonObject) {
      return new McoOptions(
         JsonUtils.getBooleanOr("pvp", jsonObject, pvpDefault),
         JsonUtils.getBooleanOr("spawnAnimals", jsonObject, spawnAnimalsDefault),
         JsonUtils.getBooleanOr("spawnMonsters", jsonObject, spawnMonstersDefault),
         JsonUtils.getBooleanOr("spawnNPCs", jsonObject, spawnNPCsDefault),
         JsonUtils.getIntOr("spawnProtection", jsonObject, spawnProtectionDefault)
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

      return jsonObject.toString();
   }
}
