package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
import realms.l;

@DontObfuscateOrShrink
public class RealmsServerAddress extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String address;
   public String resourcePackUrl;
   public String resourcePackHash;

   public static RealmsServerAddress parse(String json) {
      JsonParser parser = new JsonParser();
      RealmsServerAddress serverAddress = new RealmsServerAddress();

      try {
         JsonObject object = parser.parse(json).getAsJsonObject();
         serverAddress.address = bd.a("address", object, null);
         serverAddress.resourcePackUrl = bd.a("resourcePackUrl", object, null);
         serverAddress.resourcePackHash = bd.a("resourcePackHash", object, null);
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsServerAddress: " + var4.getMessage());
      }

      return serverAddress;
   }
}
