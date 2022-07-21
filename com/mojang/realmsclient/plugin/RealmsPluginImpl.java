package com.mojang.realmsclient.plugin;

import com.mojang.datafixers.util.Either;
import javax.annotation.Nonnull;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import net.minecraft.realms.pluginapi.LoadedRealmsPlugin;
import net.minecraft.realms.pluginapi.RealmsPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bb;
import realms.bc;

@DontObfuscateOrShrink
public class RealmsPluginImpl implements RealmsPlugin {
   private static final Logger LOGGER = LogManager.getLogger();

   public Either<LoadedRealmsPlugin, String> tryLoad(@Nonnull String minecraftVersionString) {
      String builtForMinecraftVersion = bc.b();
      LOGGER.info("Built for minecraft version {}", builtForMinecraftVersion);
      return builtForMinecraftVersion != null && !builtForMinecraftVersion.equals(minecraftVersionString)
         ? Either.right(
            String.format(
               "Realms incompatible. Built for Minecraft version %s, trying to load in %s. Note that the realms plugin isn't forwards compatible any longer.",
               builtForMinecraftVersion,
               minecraftVersionString
            )
         )
         : Either.left(new bb());
   }
}
