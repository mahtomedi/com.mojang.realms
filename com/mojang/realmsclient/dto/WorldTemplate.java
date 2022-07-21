package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
import realms.l;

@DontObfuscateOrShrink
public class WorldTemplate extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String id;
   public String name;
   public String version;
   public String author;
   public String link;
   public String image;
   public String trailer;
   public String recommendedPlayers;
   public WorldTemplate.a type;

   public static WorldTemplate parse(JsonObject node) {
      WorldTemplate template = new WorldTemplate();

      try {
         template.id = bd.a("id", node, "");
         template.name = bd.a("name", node, "");
         template.version = bd.a("version", node, "");
         template.author = bd.a("author", node, "");
         template.link = bd.a("link", node, "");
         template.image = bd.a("image", node, null);
         template.trailer = bd.a("trailer", node, "");
         template.recommendedPlayers = bd.a("recommendedPlayers", node, "");
         template.type = WorldTemplate.a.valueOf(bd.a("type", node, WorldTemplate.a.a.name()));
      } catch (Exception var3) {
         LOGGER.error("Could not parse WorldTemplate: " + var3.getMessage());
      }

      return template;
   }

   public static enum a {
      a,
      b,
      c,
      d,
      e;
   }
}
