package realms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;

public class bd {
   public static String a(String key, JsonObject node, String defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsString();
      } else {
         return defaultValue;
      }
   }

   public static int a(String key, JsonObject node, int defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsInt();
      } else {
         return defaultValue;
      }
   }

   public static long a(String key, JsonObject node, long defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsLong();
      } else {
         return defaultValue;
      }
   }

   public static boolean a(String key, JsonObject node, boolean defaultValue) {
      JsonElement element = node.get(key);
      if (element != null) {
         return element.isJsonNull() ? defaultValue : element.getAsBoolean();
      } else {
         return defaultValue;
      }
   }

   public static Date a(String key, JsonObject node) {
      JsonElement element = node.get(key);
      return element != null ? new Date(Long.parseLong(element.getAsString())) : new Date();
   }
}
