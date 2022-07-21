package com.mojang.realmsclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;

public class JsonUtils {
   public static String getStringOr(String key, JsonObject node, String defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsString();
      } else {
         return defaultValue;
      }
   }

   public static int getIntOr(String key, JsonObject node, int defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsInt();
      } else {
         return defaultValue;
      }
   }

   public static long getLongOr(String key, JsonObject node, long defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsLong();
      } else {
         return defaultValue;
      }
   }

   public static boolean getBooleanOr(String key, JsonObject node, boolean defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsBoolean();
      } else {
         return defaultValue;
      }
   }

   public static Date getDateOr(String key, JsonObject node) {
      JsonElement element = node.get(key);
      return element != null ? new Date(Long.parseLong(element.getAsString())) : new Date();
   }
}
