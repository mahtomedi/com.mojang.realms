package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class WorldDownload extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String downloadLink;
   public String resourcePackUrl;
   public String resourcePackHash;

   public static WorldDownload parse(String json) {
      JsonParser jsonParser = new JsonParser();
      JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
      WorldDownload worldDownload = new WorldDownload();

      try {
         worldDownload.downloadLink = be.a("downloadLink", jsonObject, "");
         worldDownload.resourcePackUrl = be.a("resourcePackUrl", jsonObject, "");
         worldDownload.resourcePackHash = be.a("resourcePackHash", jsonObject, "");
      } catch (Exception var5) {
         LOGGER.error("Could not parse WorldDownload: " + var5.getMessage());
      }

      return worldDownload;
   }
}
