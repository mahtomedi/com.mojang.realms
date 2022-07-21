package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;

public class RealmsNotificationsScreen extends RealmsScreen {
   private static final String INVITE_ICON_LOCATION = "realms:textures/gui/realms/invite_icon.png";
   private static final String TRIAL_ICON_LOCATION = "realms:textures/gui/realms/trial_icon.png";
   private static final String NEWS_ICON_LOCATION = "realms:textures/gui/realms/news_notification_mainscreen.png";
   private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
   private volatile int numberOfPendingInvites;
   private static boolean checkedMcoAvailability;
   private static boolean trialAvailable;
   private static boolean validClient;
   private static boolean hasUnreadNews;
   private static final List<RealmsDataFetcher.Task> tasks = Arrays.asList(
      RealmsDataFetcher.Task.PENDING_INVITE, RealmsDataFetcher.Task.TRIAL_AVAILABLE, RealmsDataFetcher.Task.UNREAD_NEWS
   );

   public RealmsNotificationsScreen(RealmsScreen lastScreen) {
   }

   public void init() {
      this.checkIfMcoEnabled();
      this.setKeyboardHandlerSendRepeatsToGui(true);
   }

   public void tick() {
      if ((!Realms.getRealmsNotificationsEnabled() || !Realms.inTitleScreen() || !validClient) && !realmsDataFetcher.isStopped()) {
         realmsDataFetcher.stop();
      } else if (validClient && Realms.getRealmsNotificationsEnabled()) {
         realmsDataFetcher.initWithSpecificTaskList(tasks);
         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE)) {
            trialAvailable = realmsDataFetcher.isTrialAvailable();
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
            hasUnreadNews = realmsDataFetcher.hasUnreadNews();
         }

         realmsDataFetcher.markClean();
      }
   }

   private void checkIfMcoEnabled() {
      if (!checkedMcoAvailability) {
         checkedMcoAvailability = true;
         (new Thread("Realms Notification Availability checker #1") {
            public void run() {
               RealmsClient client = RealmsClient.createRealmsClient();

               try {
                  RealmsClient.CompatibleVersionResponse versionResponse = client.clientCompatible();
                  if (!versionResponse.equals(RealmsClient.CompatibleVersionResponse.COMPATIBLE)) {
                     return;
                  }
               } catch (RealmsServiceException var3) {
                  if (var3.httpResultCode != 401) {
                     RealmsNotificationsScreen.checkedMcoAvailability = false;
                  }

                  return;
               } catch (IOException var4) {
                  RealmsNotificationsScreen.checkedMcoAvailability = false;
                  return;
               }

               RealmsNotificationsScreen.validClient = true;
            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      if (validClient) {
         this.drawIcons(xm, ym);
      }

      super.render(xm, ym, a);
   }

   public boolean mouseClicked(double xm, double ym, int button) {
      return super.mouseClicked(xm, ym, button);
   }

   private void drawIcons(int xm, int ym) {
      int pendingInvitesCount = this.numberOfPendingInvites;
      int spacing = 24;
      int topPos = this.height() / 4 + 48;
      int baseX = this.width() / 2 + 80;
      int baseY = topPos + 48 + 2;
      int iconOffset = 0;
      if (hasUnreadNews) {
         RealmsScreen.bind("realms:textures/gui/realms/news_notification_mainscreen.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.4F, 0.4F, 0.4F);
         RealmsScreen.blit((int)((double)(baseX + 2 - iconOffset) * 2.5), (int)((double)baseY * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
         GlStateManager.popMatrix();
         iconOffset += 14;
      }

      if (pendingInvitesCount != 0) {
         RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(baseX - iconOffset, baseY - 6, 0.0F, 0.0F, 15, 25, 31, 25);
         GlStateManager.popMatrix();
         iconOffset += 16;
      }

      if (trialAvailable) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         int ySprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            ySprite = 8;
         }

         RealmsScreen.blit(baseX + 4 - iconOffset, baseY + 4, 0.0F, (float)ySprite, 8, 8, 8, 16);
         GlStateManager.popMatrix();
      }

   }

   public void removed() {
      realmsDataFetcher.stop();
   }
}
