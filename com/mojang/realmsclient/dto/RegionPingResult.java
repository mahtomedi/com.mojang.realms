package com.mojang.realmsclient.dto;

import java.util.Locale;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.l;

@DontObfuscateOrShrink
public class RegionPingResult extends l {
   private final String regionName;
   private final int ping;

   public RegionPingResult(String regionName, int ping) {
      this.regionName = regionName;
      this.ping = ping;
   }

   public int ping() {
      return this.ping;
   }

   @Override
   public String toString() {
      return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, (float)this.ping);
   }
}
