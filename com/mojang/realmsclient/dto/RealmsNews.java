package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
import realms.l;

@DontObfuscateOrShrink
public class RealmsNews extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String newsLink;

   public static RealmsNews parse(String json) {
      RealmsNews news = new RealmsNews();

      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(json).getAsJsonObject();
         news.newsLink = bd.a("newsLink", object, null);
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsNews: " + var4.getMessage());
      }

      return news;
   }
}
