package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class ServerActivityList extends l {
   public long periodInMillis;
   public List<ServerActivity> serverActivities = new ArrayList();

   public static ServerActivityList parse(String json) {
      ServerActivityList activityList = new ServerActivityList();
      JsonParser parser = new JsonParser();

      try {
         JsonElement jsonElement = parser.parse(json);
         JsonObject object = jsonElement.getAsJsonObject();
         activityList.periodInMillis = be.a("periodInMillis", object, -1L);
         JsonElement activityArray = object.get("playerActivityDto");
         if (activityArray != null && activityArray.isJsonArray()) {
            for(JsonElement element : activityArray.getAsJsonArray()) {
               ServerActivity sa = ServerActivity.parse(element.getAsJsonObject());
               activityList.serverActivities.add(sa);
            }
         }
      } catch (Exception var10) {
      }

      return activityList;
   }
}
