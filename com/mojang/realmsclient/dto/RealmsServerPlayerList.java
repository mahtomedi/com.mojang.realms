package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class RealmsServerPlayerList extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final JsonParser jsonParser = new JsonParser();
   public long serverId;
   public List<String> players;

   public static RealmsServerPlayerList parse(JsonObject node) {
      RealmsServerPlayerList playerList = new RealmsServerPlayerList();

      try {
         playerList.serverId = be.a("serverId", node, -1L);
         String playerListString = be.a("playerList", node, null);
         if (playerListString != null) {
            JsonElement element = jsonParser.parse(playerListString);
            if (element.isJsonArray()) {
               playerList.players = parsePlayers(element.getAsJsonArray());
            } else {
               playerList.players = new ArrayList();
            }
         } else {
            playerList.players = new ArrayList();
         }
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsServerPlayerList: " + var4.getMessage());
      }

      return playerList;
   }

   private static List<String> parsePlayers(JsonArray jsonArray) {
      ArrayList<String> players = new ArrayList();

      for(JsonElement aJsonArray : jsonArray) {
         try {
            players.add(aJsonArray.getAsString());
         } catch (Exception var5) {
         }
      }

      return players;
   }
}
