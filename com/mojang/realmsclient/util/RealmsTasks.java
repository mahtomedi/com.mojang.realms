package com.mojang.realmsclient.util;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsTasks {
   private static final Logger LOGGER = LogManager.getLogger();

   public static class ResettingWorldTask extends LongRunningTask {
      private final String seed;
      private final WorldTemplate worldTemplate;
      private final int levelType;
      private final boolean generateStructures;
      private final long serverId;
      private final RealmsScreen lastScreen;

      public ResettingWorldTask(long serverId, RealmsScreen lastScreen, WorldTemplate worldTemplate) {
         this.seed = null;
         this.worldTemplate = worldTemplate;
         this.levelType = -1;
         this.generateStructures = true;
         this.serverId = serverId;
         this.lastScreen = lastScreen;
      }

      public ResettingWorldTask(long serverId, RealmsScreen lastScreen, String seed, int levelType, boolean generateStructures) {
         this.seed = seed;
         this.worldTemplate = null;
         this.levelType = levelType;
         this.generateStructures = generateStructures;
         this.serverId = serverId;
         this.lastScreen = lastScreen;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");
         this.setTitle(title);
         int i = 0;

         while(i < 6) {
            try {
               if (this.aborted()) {
                  return;
               }

               if (this.worldTemplate != null) {
                  client.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
               } else {
                  client.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
               }

               if (this.aborted()) {
                  return;
               }

               Realms.setScreen(new RealmsConfigureWorldScreen(this.lastScreen, this.serverId));
               return;
            } catch (RetryCallException var5) {
               if (this.aborted()) {
                  return;
               }

               this.pause(var5.delaySeconds);
               ++i;
            } catch (RealmsServiceException var6) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Couldn't reset world");
               this.error(var6.toString());
               return;
            } catch (Exception var7) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Couldn't reset world");
               this.error(var7.toString());
               return;
            }
         }

      }

      private void pause(int pauseTimeSeconds) {
         try {
            Thread.sleep((long)(pauseTimeSeconds * 1000));
         } catch (InterruptedException var3) {
            RealmsTasks.LOGGER.error(var3);
         }

      }
   }

   public static class SwitchMinigameTask extends LongRunningTask {
      private final long worldId;
      private final WorldTemplate worldTemplate;
      private final RealmsConfigureWorldScreen lastScreen;

      public SwitchMinigameTask(long worldId, WorldTemplate worldTemplate, RealmsConfigureWorldScreen lastScreen) {
         this.worldId = worldId;
         this.worldTemplate = worldTemplate;
         this.lastScreen = lastScreen;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.starting.screen.title");
         this.setTitle(title);

         try {
            if (this.aborted()) {
               return;
            }

            Boolean result = client.putIntoMinigameMode(this.worldId, this.worldTemplate.id);
            if (this.aborted()) {
               return;
            }

            Realms.setScreen(this.lastScreen);
         } catch (RealmsServiceException var4) {
            if (this.aborted()) {
               return;
            }

            RealmsTasks.LOGGER.error("Couldn't start mini game!");
            this.error(var4.toString());
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            RealmsTasks.LOGGER.error("Couldn't start mini game!");
            this.error(var5.toString());
         }

      }
   }

   public static class SwitchSlotTask extends LongRunningTask {
      private final long worldId;
      private final int slot;
      private final RealmsConfigureWorldScreen lastScreen;

      public SwitchSlotTask(long worldId, int slot, RealmsConfigureWorldScreen lastScreen) {
         this.worldId = worldId;
         this.slot = slot;
         this.lastScreen = lastScreen;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.slot.screen.title");
         this.setTitle(title);

         try {
            if (this.aborted()) {
               return;
            }

            Boolean result = client.switchSlot(this.worldId, this.slot);
            if (!result) {
               this.error("Couldn't switch slot");
               return;
            }

            if (this.aborted()) {
               return;
            }

            Realms.setScreen(this.lastScreen);
         } catch (RealmsServiceException var4) {
            if (this.aborted()) {
               return;
            }

            RealmsTasks.LOGGER.error("Couldn't switch slot!");
            this.error(var4.toString());
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            RealmsTasks.LOGGER.error("Couldn't switch slot!");
            this.error(var5.toString());
         }

      }
   }
}
