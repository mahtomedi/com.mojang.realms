package com.mojang.realmsclient.dto;

import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.l;

@DontObfuscateOrShrink
public class RealmsWorldResetDto extends l {
   private final String seed;
   private final long worldTemplateId;
   private final int levelType;
   private final boolean generateStructures;

   public RealmsWorldResetDto(String seed, long worldTemplateId, int levelType, boolean generateStructures) {
      this.seed = seed;
      this.worldTemplateId = worldTemplateId;
      this.levelType = levelType;
      this.generateStructures = generateStructures;
   }
}
