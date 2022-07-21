package com.mojang.realmsclient.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsConnect;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsTasks {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int NUMBER_OF_RETRIES = 25;

   private static void pause(int seconds) {
      try {
         Thread.sleep((long)(seconds * 1000));
      } catch (InterruptedException var2) {
         LOGGER.error("", var2);
      }

   }

   public static class CloseServerTask extends LongRunningTask {
      private final RealmsServer serverData;
      private final RealmsConfigureWorldScreen configureScreen;

      public CloseServerTask(RealmsServer realmsServer, RealmsConfigureWorldScreen configureWorldScreen) {
         this.serverData = realmsServer;
         this.configureScreen = configureWorldScreen;
      }

      public void run() {
         this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.closing"));
         RealmsClient client = RealmsClient.createRealmsClient();

         for(int i = 0; i < 25; ++i) {
            if (this.aborted()) {
               return;
            }

            try {
               boolean closeResult = client.close(this.serverData.id);
               if (closeResult) {
                  this.configureScreen.stateChanged();
                  this.serverData.state = RealmsServer.State.CLOSED;
                  Realms.setScreen(this.configureScreen);
                  break;
               }
            } catch (RetryCallException var4) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.pause(var4.delaySeconds);
            } catch (Exception var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Failed to close server", var5);
               this.error("Failed to close the server");
            }
         }

      }
   }

   public static class OpenServerTask extends LongRunningTask {
      private final RealmsServer serverData;
      private final RealmsScreen returnScreen;
      private final boolean join;
      private final RealmsScreen mainScreen;

      public OpenServerTask(RealmsServer realmsServer, RealmsScreen returnScreen, RealmsScreen mainScreen, boolean join) {
         this.serverData = realmsServer;
         this.returnScreen = returnScreen;
         this.join = join;
         this.mainScreen = mainScreen;
      }

      public void run() {
         this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.opening"));
         RealmsClient client = RealmsClient.createRealmsClient();

         for(int i = 0; i < 25; ++i) {
            if (this.aborted()) {
               return;
            }

            try {
               boolean openResult = client.open(this.serverData.id);
               if (openResult) {
                  if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                     ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                  }

                  this.serverData.state = RealmsServer.State.OPEN;
                  if (this.join) {
                     ((RealmsMainScreen)this.mainScreen).play(this.serverData);
                  } else {
                     Realms.setScreen(this.returnScreen);
                  }
                  break;
               }
            } catch (RetryCallException var4) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.pause(var4.delaySeconds);
            } catch (Exception var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Failed to open server", var5);
               this.error("Failed to open the server");
            }
         }

      }
   }

   public static class RealmsConnectTask extends LongRunningTask {
      private final RealmsConnect realmsConnect;
      private final RealmsServerAddress a;

      public RealmsConnectTask(RealmsScreen lastScreen, RealmsServerAddress address) {
         this.a = address;
         this.realmsConnect = new RealmsConnect(lastScreen);
      }

      public void run() {
         this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
         net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(this.a.address);
         this.realmsConnect.connect(address.getHost(), address.getPort());
      }

      @Override
      public void abortTask() {
         this.realmsConnect.abort();
         Realms.clearResourcePack();
      }

      @Override
      public void tick() {
         this.realmsConnect.tick();
      }
   }

   public static class RealmsGetServerDetailsTask extends LongRunningTask {
      private final RealmsServer server;
      private final RealmsScreen lastScreen;

      public RealmsGetServerDetailsTask(RealmsScreen lastScreen, RealmsServer server) {
         this.lastScreen = lastScreen;
         this.server = server;
      }

      public void run() {
         this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
         RealmsClient client = RealmsClient.createRealmsClient();
         boolean addressRetrieved = false;
         boolean hasError = false;
         int sleepTime = 5;
         final RealmsServerAddress address = null;
         boolean tosNotAccepted = false;

         for(int i = 0; i < 20 && !this.aborted(); ++i) {
            try {
               address = client.join(this.server.id);
               addressRetrieved = true;
            } catch (RetryCallException var10) {
               sleepTime = var10.delaySeconds;
            } catch (RealmsServiceException var11) {
               if (var11.errorCode == 6002) {
                  tosNotAccepted = true;
               } else {
                  hasError = true;
                  this.error(var11.toString());
                  RealmsTasks.LOGGER.error("Couldn't connect to world", var11);
               }
               break;
            } catch (IOException var12) {
               RealmsTasks.LOGGER.error("Couldn't parse response connecting to world", var12);
            } catch (Exception var13) {
               hasError = true;
               RealmsTasks.LOGGER.error("Couldn't connect to world", var13);
               this.error(var13.getLocalizedMessage());
               break;
            }

            if (addressRetrieved) {
               break;
            }

            this.sleep(sleepTime);
         }

         if (tosNotAccepted) {
            Realms.setScreen(new RealmsTermsScreen(this.lastScreen, this.server));
         } else if (!this.aborted() && !hasError) {
            if (addressRetrieved) {
               if (this.server.resourcePackUrl != null && this.server.resourcePackHash != null) {
                  try {
                     Futures.addCallback(
                        Realms.downloadResourcePack(this.server.resourcePackUrl, this.server.resourcePackHash),
                        new FutureCallback<Object>() {
                           public void onSuccess(@Nullable Object result) {
                              RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
                                 RealmsGetServerDetailsTask.this.lastScreen,
                                 new RealmsTasks.RealmsConnectTask(RealmsGetServerDetailsTask.this.lastScreen, address)
                              );
                              longRunningMcoTaskScreen.start();
                              Realms.setScreen(longRunningMcoTaskScreen);
                           }
   
                           public void onFailure(Throwable t) {
                              Realms.clearResourcePack();
                              RealmsTasks.LOGGER.error(t);
                              RealmsGetServerDetailsTask.this.error("Failed to download resource pack!");
                           }
                        }
                     );
                  } catch (Exception var9) {
                     Realms.clearResourcePack();
                     RealmsTasks.LOGGER.error(var9);
                     this.error("Failed to download resource pack!");
                  }
               } else {
                  RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
                     this.lastScreen, new RealmsTasks.RealmsConnectTask(this.lastScreen, address)
                  );
                  longRunningMcoTaskScreen.start();
                  Realms.setScreen(longRunningMcoTaskScreen);
               }
            } else {
               this.error(RealmsScreen.getLocalizedString("mco.errorMessage.connectionFailure"));
            }
         }

      }

      private void sleep(int sleepTimeSeconds) {
         try {
            Thread.sleep((long)(sleepTimeSeconds * 1000));
         } catch (InterruptedException var3) {
            RealmsTasks.LOGGER.warn(var3.getLocalizedMessage());
         }

      }
   }

   public static class ResettingWorldTask extends LongRunningTask {
      private final String seed;
      private final WorldTemplate worldTemplate;
      private final int levelType;
      private final boolean generateStructures;
      private final long serverId;
      private final RealmsScreen lastScreen;
      private int confirmationId = -1;
      private String title = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");

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

      public void setConfirmationId(int confirmationId) {
         this.confirmationId = confirmationId;
      }

      public void setResetTitle(String title) {
         this.title = title;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         this.setTitle(this.title);
         int i = 0;

         while(i < 25) {
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

               if (this.confirmationId != -1) {
                  this.lastScreen.confirmResult(true, this.confirmationId);
               } else {
                  Realms.setScreen(this.lastScreen);
               }

               return;
            } catch (RetryCallException var4) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.pause(var4.delaySeconds);
               ++i;
            } catch (Exception var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Couldn't reset world");
               this.error(var5.toString());
               return;
            }
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

         for(int i = 0; i < 25; ++i) {
            try {
               if (this.aborted()) {
                  return;
               }

               if (client.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
                  Realms.setScreen(this.lastScreen);
                  break;
               }
            } catch (RetryCallException var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.pause(var5.delaySeconds);
            } catch (Exception var6) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Couldn't start mini game!");
               this.error(var6.toString());
            }
         }

      }
   }

   public static class SwitchSlotTask extends LongRunningTask {
      private final long worldId;
      private final int slot;
      private final RealmsScreen lastScreen;
      private final int confirmId;

      public SwitchSlotTask(long worldId, int slot, RealmsScreen lastScreen, int confirmId) {
         this.worldId = worldId;
         this.slot = slot;
         this.lastScreen = lastScreen;
         this.confirmId = confirmId;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.slot.screen.title");
         this.setTitle(title);

         for(int i = 0; i < 25; ++i) {
            try {
               if (this.aborted()) {
                  return;
               }

               if (client.switchSlot(this.worldId, this.slot)) {
                  this.lastScreen.confirmResult(true, this.confirmId);
                  break;
               }
            } catch (RetryCallException var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.pause(var5.delaySeconds);
            } catch (Exception var6) {
               if (this.aborted()) {
                  return;
               }

               RealmsTasks.LOGGER.error("Couldn't switch world!");
               this.error(var6.toString());
            }
         }

      }
   }

   public static class TrialCreationTask extends LongRunningTask {
      private final String name;
      private final String motd;
      private final RealmsMainScreen lastScreen;

      public TrialCreationTask(String name, String motd, RealmsMainScreen lastScreen) {
         this.name = name;
         this.motd = motd;
         this.lastScreen = lastScreen;
      }

      public void run() {
         String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
         this.setTitle(title);
         RealmsClient client = RealmsClient.createRealmsClient();

         try {
            RealmsServer server = client.createTrial(this.name, this.motd);
            if (server != null) {
               this.lastScreen.setCreatedTrial(true);
               this.lastScreen.closePopup();
               RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(
                  this.lastScreen,
                  server,
                  this.lastScreen.newScreen(),
                  RealmsScreen.getLocalizedString("mco.selectServer.create"),
                  RealmsScreen.getLocalizedString("mco.create.world.subtitle"),
                  10526880,
                  RealmsScreen.getLocalizedString("mco.create.world.skip")
               );
               resetWorldScreen.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
               Realms.setScreen(resetWorldScreen);
            } else {
               this.error(RealmsScreen.getLocalizedString("mco.trial.unavailable"));
            }
         } catch (RealmsServiceException var5) {
            RealmsTasks.LOGGER.error("Couldn't create trial");
            this.error(var5.toString());
         } catch (UnsupportedEncodingException var6) {
            RealmsTasks.LOGGER.error("Couldn't create trial");
            this.error(var6.getLocalizedMessage());
         } catch (IOException var7) {
            RealmsTasks.LOGGER.error("Could not parse response creating trial");
            this.error(var7.getLocalizedMessage());
         } catch (Exception var8) {
            RealmsTasks.LOGGER.error("Could not create trial");
            this.error(var8.getLocalizedMessage());
         }

      }
   }

   public static class WorldCreationTask extends LongRunningTask {
      private final String name;
      private final String motd;
      private final long worldId;
      private final RealmsScreen lastScreen;

      public WorldCreationTask(long worldId, String name, String motd, RealmsScreen lastScreen) {
         this.worldId = worldId;
         this.name = name;
         this.motd = motd;
         this.lastScreen = lastScreen;
      }

      public void run() {
         String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
         this.setTitle(title);
         RealmsClient client = RealmsClient.createRealmsClient();

         try {
            client.initializeWorld(this.worldId, this.name, this.motd);
            Realms.setScreen(this.lastScreen);
         } catch (RealmsServiceException var4) {
            RealmsTasks.LOGGER.error("Couldn't create world");
            this.error(var4.toString());
         } catch (UnsupportedEncodingException var5) {
            RealmsTasks.LOGGER.error("Couldn't create world");
            this.error(var5.getLocalizedMessage());
         } catch (IOException var6) {
            RealmsTasks.LOGGER.error("Could not parse response creating world");
            this.error(var6.getLocalizedMessage());
         } catch (Exception var7) {
            RealmsTasks.LOGGER.error("Could not create world");
            this.error(var7.getLocalizedMessage());
         }

      }
   }
}
