package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
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
         worldDownload.downloadLink = bd.a("downloadLink", jsonObject, "");
         worldDownload.resourcePackUrl = bd.a("resourcePackUrl", jsonObject, "");
         worldDownload.resourcePackHash = bd.a("resourcePackHash", jsonObject, "");
      } catch (Exception var5) {
         LOGGER.error("Could not parse WorldDownload: " + var5.getMessage());
      }

      return worldDownload;
   }
}
