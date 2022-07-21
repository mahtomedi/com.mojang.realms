package com.mojang.realmsclient.util;

import java.util.HashMap;
import java.util.Map;

public class UploadTokenCache {
   private static Map<Long, String> tokenCache = new HashMap();

   public static String get(long worldId) {
      return (String)tokenCache.get(worldId);
   }

   public static void invalidate(long world) {
      tokenCache.remove(world);
   }

   public static void put(long wid, String token) {
      tokenCache.put(wid, token);
   }
}
