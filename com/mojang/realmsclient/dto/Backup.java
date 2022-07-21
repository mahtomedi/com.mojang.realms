package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
import realms.l;

@DontObfuscateOrShrink
public class Backup extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String backupId;
   public Date lastModifiedDate;
   public long size;
   private boolean uploadedVersion;
   public Map<String, String> metadata = new HashMap();
   public Map<String, String> changeList = new HashMap();

   public static Backup parse(JsonElement node) {
      JsonObject object = node.getAsJsonObject();
      Backup backup = new Backup();

      try {
         backup.backupId = bd.a("backupId", object, "");
         backup.lastModifiedDate = bd.a("lastModifiedDate", object);
         backup.size = bd.a("size", object, 0L);
         if (object.has("metadata")) {
            JsonObject metadataObject = object.getAsJsonObject("metadata");

            for(Entry<String, JsonElement> elem : metadataObject.entrySet()) {
               if (!((JsonElement)elem.getValue()).isJsonNull()) {
                  backup.metadata.put(format((String)elem.getKey()), ((JsonElement)elem.getValue()).getAsString());
               }
            }
         }
      } catch (Exception var7) {
         LOGGER.error("Could not parse Backup: " + var7.getMessage());
      }

      return backup;
   }

   private static String format(String key) {
      String[] worlds = key.split("_");
      StringBuilder sb = new StringBuilder();

      for(String world : worlds) {
         if (world != null && world.length() >= 1) {
            if ("of".equals(world)) {
               sb.append(world).append(" ");
            } else {
               char firstCharacter = Character.toUpperCase(world.charAt(0));
               sb.append(firstCharacter).append(world.substring(1, world.length())).append(" ");
            }
         }
      }

      return sb.toString();
   }

   public boolean isUploadedVersion() {
      return this.uploadedVersion;
   }

   public void setUploadedVersion(boolean uploadedVersion) {
      this.uploadedVersion = uploadedVersion;
   }
}
