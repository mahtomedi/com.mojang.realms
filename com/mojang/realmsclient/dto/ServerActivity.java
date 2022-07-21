package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class ServerActivity extends l {
   public String profileUuid;
   public long joinTime;
   public long leaveTime;

   public static ServerActivity parse(JsonObject element) {
      ServerActivity sa = new ServerActivity();

      try {
         sa.profileUuid = be.a("profileUuid", element, null);
         sa.joinTime = be.a("joinTime", element, Long.MIN_VALUE);
         sa.leaveTime = be.a("leaveTime", element, Long.MIN_VALUE);
      } catch (Exception var3) {
      }

      return sa;
   }
}
