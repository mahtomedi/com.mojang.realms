package com.mojang.realmsclient.dto;

import net.minecraft.obfuscate.DontObfuscateOrShrink;
import realms.l;

@DontObfuscateOrShrink
public class RealmsServerPing extends l {
   public volatile String nrOfPlayers = "0";
   public volatile String playerList = "";
}
