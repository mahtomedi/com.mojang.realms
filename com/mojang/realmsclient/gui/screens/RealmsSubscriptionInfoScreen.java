package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsSubscriptionInfoScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsScreen lastScreen;
   private final RealmsServer serverData;
   private final RealmsScreen mainScreen;
   private final int BUTTON_BACK_ID = 0;
   private final int BUTTON_DELETE_ID = 1;
   private final int BUTTON_SUBSCRIPTION_ID = 2;
   private final String subscriptionTitle;
   private final String subscriptionStartLabelText;
   private final String timeLeftLabelText;
   private final String daysLeftLabelText;
   private int daysLeft;
   private String startDate;
   private Subscription.SubscriptionType type;
   private final String PURCHASE_LINK = "https://account.mojang.com/buy/realms";

   public RealmsSubscriptionInfoScreen(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen mainScreen) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
      this.mainScreen = mainScreen;
      this.subscriptionTitle = getLocalizedString("mco.configure.world.subscription.title");
      this.subscriptionStartLabelText = getLocalizedString("mco.configure.world.subscription.start");
      this.timeLeftLabelText = getLocalizedString("mco.configure.world.subscription.timeleft");
      this.daysLeftLabelText = getLocalizedString("mco.configure.world.subscription.recurring.daysleft");
   }

   public void init() {
      this.getSubscription(this.serverData.id);
      Realms.narrateNow(
         new String[]{
            this.subscriptionTitle, this.subscriptionStartLabelText, this.startDate, this.timeLeftLabelText, this.daysLeftPresentation(this.daysLeft)
         }
      );
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(
         new RealmsButton(2, this.width() / 2 - 100, RealmsConstants.row(6), getLocalizedString("mco.configure.world.subscription.extend")) {
            public void onPress() {
               String extensionUrl = "https://account.mojang.com/buy/realms?sid="
                  + RealmsSubscriptionInfoScreen.this.serverData.remoteSubscriptionId
                  + "&pid="
                  + Realms.getUUID();
               RealmsBridge.setClipboard(extensionUrl);
               RealmsUtil.browseTo(extensionUrl);
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(RealmsSubscriptionInfoScreen.this.lastScreen);
         }
      });
      if (this.serverData.expired) {
         this.buttonsAdd(
            new RealmsButton(1, this.width() / 2 - 100, RealmsConstants.row(10), getLocalizedString("mco.configure.world.delete.button")) {
               public void onPress() {
                  String line2 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line1");
                  String line3 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line2");
                  Realms.setScreen(
                     new RealmsLongConfirmationScreen(RealmsSubscriptionInfoScreen.this, RealmsLongConfirmationScreen.Type.Warning, line2, line3, true, 1)
                  );
               }
            }
         );
      }

   }

   private void getSubscription(long worldId) {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         Subscription subscription = client.subscriptionFor(worldId);
         this.daysLeft = subscription.daysLeft;
         this.startDate = this.localPresentation(subscription.startDate);
         this.type = subscription.type;
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't get subscription");
         Realms.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
      } catch (IOException var6) {
         LOGGER.error("Couldn't parse response subscribing");
      }

   }

   public void confirmResult(boolean result, int id) {
      if (id == 1 && result) {
         (new Thread("Realms-delete-realm") {
            public void run() {
               try {
                  RealmsClient client = RealmsClient.createRealmsClient();
                  client.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
               } catch (RealmsServiceException var2) {
                  RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
                  RealmsSubscriptionInfoScreen.LOGGER.error(var2);
               } catch (IOException var3) {
                  RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
                  var3.printStackTrace();
               }

               Realms.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen);
            }
         }).start();
      }

      Realms.setScreen(this);
   }

   private String localPresentation(long cetTime) {
      Calendar cal = new GregorianCalendar(TimeZone.getDefault());
      cal.setTimeInMillis(cetTime);
      return DateFormat.getDateTimeInstance().format(cal.getTime());
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      int center = this.width() / 2 - 100;
      this.drawCenteredString(this.subscriptionTitle, this.width() / 2, 17, 16777215);
      this.drawString(this.subscriptionStartLabelText, center, RealmsConstants.row(0), 10526880);
      this.drawString(this.startDate, center, RealmsConstants.row(1), 16777215);
      if (this.type == Subscription.SubscriptionType.NORMAL) {
         this.drawString(this.timeLeftLabelText, center, RealmsConstants.row(3), 10526880);
      } else if (this.type == Subscription.SubscriptionType.RECURRING) {
         this.drawString(this.daysLeftLabelText, center, RealmsConstants.row(3), 10526880);
      }

      this.drawString(this.daysLeftPresentation(this.daysLeft), center, RealmsConstants.row(4), 16777215);
      super.render(xm, ym, a);
   }

   private String daysLeftPresentation(int daysLeft) {
      if (daysLeft == -1 && this.serverData.expired) {
         return getLocalizedString("mco.configure.world.subscription.expired");
      } else if (daysLeft <= 1) {
         return getLocalizedString("mco.configure.world.subscription.less_than_a_day");
      } else {
         int months = daysLeft / 30;
         int days = daysLeft % 30;
         StringBuilder sb = new StringBuilder();
         if (months > 0) {
            sb.append(months).append(" ");
            if (months == 1) {
               sb.append(getLocalizedString("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
            } else {
               sb.append(getLocalizedString("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
            }
         }

         if (days > 0) {
            if (sb.length() > 0) {
               sb.append(", ");
            }

            sb.append(days).append(" ");
            if (days == 1) {
               sb.append(getLocalizedString("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
            } else {
               sb.append(getLocalizedString("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
            }
         }

         return sb.toString();
      }
   }
}
