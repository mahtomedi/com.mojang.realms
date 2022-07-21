package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
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
         template.id = be.a("id", node, "");
         template.name = be.a("name", node, "");
         template.version = be.a("version", node, "");
         template.author = be.a("author", node, "");
         template.link = be.a("link", node, "");
         template.image = be.a("image", node, null);
         template.trailer = be.a("trailer", node, "");
         template.recommendedPlayers = be.a("recommendedPlayers", node, "");
         template.type = WorldTemplate.a.valueOf(be.a("type", node, WorldTemplate.a.a.name()));
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
