package com.mojang.realmsclient.dto;

import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.l;

@DontObfuscateOrShrink
public class RealmsDescriptionDto extends l {
   public String name;
   public String description;

   public RealmsDescriptionDto(String name, String description) {
      this.name = name;
      this.description = description;
   }
}
