package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.l;

@DontObfuscateOrShrink
public class RealmsServerPlayerLists extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public List<RealmsServerPlayerList> servers;

   public static RealmsServerPlayerLists parse(String json) {
      RealmsServerPlayerLists list = new RealmsServerPlayerLists();
      list.servers = new ArrayList();

      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(json).getAsJsonObject();
         if (object.get("lists").isJsonArray()) {
            JsonArray jsonArray = object.get("lists").getAsJsonArray();
            Iterator<JsonElement> it = jsonArray.iterator();

            while(it.hasNext()) {
               list.servers.add(RealmsServerPlayerList.parse(((JsonElement)it.next()).getAsJsonObject()));
            }
         }
      } catch (Exception var6) {
         LOGGER.error("Could not parse RealmsServerPlayerLists: " + var6.getMessage());
      }

      return list;
   }
}
