package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.io.IOException;
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
      RealmsServerAddress a = null;
      boolean tosNotAccepted = false;

      for(int i = 0; i < 10 && !this.aborted(); ++i) {
         try {
            a = client.join(this.data.id);
            addressRetrieved = true;
         } catch (RetryCallException var9) {
            sleepTime = var9.delaySeconds;
         } catch (RealmsServiceException var10) {
            if (var10.errorCode == 6002) {
               tosNotAccepted = true;
            } else {
               hasError = true;
               this.error(var10.toString());
               LOGGER.error("Couldn't connect to world", var10);
            }
            break;
         } catch (IOException var11) {
            LOGGER.error("Couldn't parse response connecting to world", var11);
         } catch (Exception var12) {
            hasError = true;
            LOGGER.error("Couldn't connect to world", var12);
            this.error(var12.getLocalizedMessage());
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
            net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(a.address);
            this.realmsConnect.connect(address.getHost(), address.getPort());
         } else {
            Realms.setScreen(this.onlineScreen);
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
