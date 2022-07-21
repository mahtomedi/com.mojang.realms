package com.mojang.realmsclient.gui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsConnect;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConnectTask extends LongRunningTask {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsConnect realmsConnect;
   private final RealmsServer data;
   private final RealmsScreen onlineScreen;

   public RealmsConnectTask(RealmsScreen onlineScreen, RealmsServer server) {
      this.onlineScreen = onlineScreen;
      this.realmsConnect = new RealmsConnect(onlineScreen);
      this.data = server;
   }

   public void run() {
      this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
      RealmsClient client = RealmsClient.createRealmsClient();
      boolean addressRetrieved = false;
      boolean hasError = false;
      int sleepTime = 5;
      final RealmsServerAddress a = null;
      boolean tosNotAccepted = false;

      for(int i = 0; i < 20 && !this.aborted(); ++i) {
         try {
            a = client.join(this.data.id);
            addressRetrieved = true;
         } catch (RetryCallException var10) {
            sleepTime = var10.delaySeconds;
         } catch (RealmsServiceException var11) {
            if (var11.errorCode == 6002) {
               tosNotAccepted = true;
            } else {
               hasError = true;
               this.error(var11.toString());
               LOGGER.error("Couldn't connect to world", var11);
            }
            break;
         } catch (IOException var12) {
            LOGGER.error("Couldn't parse response connecting to world", var12);
         } catch (Exception var13) {
            hasError = true;
            LOGGER.error("Couldn't connect to world", var13);
            this.error(var13.getLocalizedMessage());
         }

         if (addressRetrieved) {
            break;
         }

         this.sleep(sleepTime);
      }

      if (tosNotAccepted) {
         Realms.setScreen(new RealmsTermsScreen(this.onlineScreen, this.data));
      } else if (!this.aborted() && !hasError) {
         if (addressRetrieved) {
            if (this.data.resourcePackUrl != null && this.data.resourcePackHash != null) {
               try {
                  Futures.addCallback(Realms.downloadResourcePack(this.data.resourcePackUrl, this.data.resourcePackHash), new FutureCallback<Object>() {
                     public void onSuccess(@Nullable Object result) {
                        net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(a.address);
                        RealmsConnectTask.this.realmsConnect.connect(address.getHost(), address.getPort());
                     }

                     public void onFailure(Throwable t) {
                        RealmsConnectTask.LOGGER.error(t);
                        RealmsConnectTask.this.error("Failed to download resource pack!");
                     }
                  });
               } catch (Exception var9) {
                  Realms.clearResourcePack();
                  LOGGER.error(var9);
                  this.error("Failed to download resource pack!");
               }
            } else {
               net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(a.address);
               this.realmsConnect.connect(address.getHost(), address.getPort());
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
         LOGGER.warn(var3.getLocalizedMessage());
      }

   }

   @Override
   public void abortTask() {
      this.realmsConnect.abort();
   }

   @Override
   public void tick() {
      this.realmsConnect.tick();
   }
}
