package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
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
         serverAddress.address = be.a("address", object, null);
         serverAddress.resourcePackUrl = be.a("resourcePackUrl", object, null);
         serverAddress.resourcePackHash = be.a("resourcePackHash", object, null);
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsServerAddress: " + var4.getMessage());
      }

      return serverAddress;
   }
}
