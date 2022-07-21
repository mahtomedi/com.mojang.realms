package realms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;

public class bj {
   private static final YggdrasilAuthenticationService b = new YggdrasilAuthenticationService(Realms.getProxy(), UUID.randomUUID().toString());
   private static final MinecraftSessionService c = b.createMinecraftSessionService();
   public static LoadingCache<String, GameProfile> a = CacheBuilder.newBuilder()
      .expireAfterWrite(60L, TimeUnit.MINUTES)
      .build(new CacheLoader<String, GameProfile>() {
         public GameProfile a(String uuid) throws Exception {
            GameProfile profile = bj.c.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(uuid), null), false);
            if (profile == null) {
               throw new Exception("Couldn't get profile");
            } else {
               return profile;
            }
         }
      });

   public static String a(String uuid) throws Exception {
      GameProfile gameProfile = (GameProfile)a.get(uuid);
      return gameProfile.getName();
   }

   public static Map<Type, MinecraftProfileTexture> b(String uuid) {
      try {
         GameProfile gameProfile = (GameProfile)a.get(uuid);
         return c.getTextures(gameProfile, false);
      } catch (Exception var2) {
         return new HashMap();
      }
   }

   public static void c(String uri) {
      Realms.openUri(uri);
   }

   public static String a(Long timeDiff) {
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
}
