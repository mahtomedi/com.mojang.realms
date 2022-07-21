package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsUtil {
   public static LoadingCache<String, String> nameCache = CacheBuilder.newBuilder()
      .expireAfterWrite(60L, TimeUnit.MINUTES)
      .build(new CacheLoader<String, String>() {
         public String load(String uuid) throws Exception {
            String name = Realms.uuidToName(uuid);
            if (name == null) {
               throw new Exception("Couldn't get username");
            } else {
               return name;
            }
         }
      });
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int MINUTES = 60;
   private static final int HOURS = 3600;
   private static final int DAYS = 86400;

   public static void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var4) {
         LOGGER.error("Couldn't open link");
      }

   }

   public static String convertToAgePresentation(Long timeDiff) {
      if (timeDiff < 0L) {
         return "right now";
      } else {
         long timeDiffInSeconds = timeDiff / 1000L;
         if (timeDiffInSeconds < 60L) {
            return (timeDiffInSeconds == 1L ? "1 second" : timeDiffInSeconds + " seconds") + " ago";
         } else if (timeDiffInSeconds < 3600L) {
            long minutes = timeDiffInSeconds / 60L;
            return (minutes == 1L ? "1 minute" : minutes + " minutes") + " ago";
         } else if (timeDiffInSeconds < 86400L) {
            long hours = timeDiffInSeconds / 3600L;
            return (hours == 1L ? "1 hour" : hours + " hours") + " ago";
         } else {
            long days = timeDiffInSeconds / 86400L;
            return (days == 1L ? "1 day" : days + " days") + " ago";
         }
      }
   }

   public static boolean slotIsBrokenByUpdate(RealmsWorldOptions slot) {
      return slot.adventureMap && "1.8.9".equals(slot.minecraftVersion);
   }

   public static boolean mapIsBrokenByUpdate(RealmsServer serverData) {
      return serverData.minecraftVersion != null && "1.8.9".equals(serverData.minecraftVersion) && !serverData.worldType.equals(RealmsServer.WorldType.NORMAL);
   }
}
