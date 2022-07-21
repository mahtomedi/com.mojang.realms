package com.mojang.realmsclient.dto;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.l;

@DontObfuscateOrShrink
public class PingResult extends l {
   public List<RegionPingResult> pingResults = new ArrayList();
   public List<Long> worldIds = new ArrayList();
}
