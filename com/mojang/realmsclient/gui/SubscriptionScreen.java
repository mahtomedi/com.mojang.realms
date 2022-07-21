package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class SubscriptionScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsScreen lastScreen;
   private final McoServer serverData;
   private final int BUTTON_BACK_ID = 0;
   private int daysLeft;
   private String startDate;
   private Subscription.SubscriptionType type;
   private final String baseUrl = "https://account.mojang.com";
   private final String path = "/buy/realms";
   private boolean onLink;

   public SubscriptionScreen(RealmsScreen lastScreen, McoServer serverData) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
   }

   public void tick() {
   }

   public void init() {
      this.getSubscription(this.serverData.id);
      Keyboard.enableRepeatEvents(true);
      this.buttonsAdd(newButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")));
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
         Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
      } catch (IOException var6) {
         LOGGER.error("Couldn't parse response subscribing");
      }

   }

   private String localPresentation(long cetTime) {
      Calendar cal = new GregorianCalendar(TimeZone.getDefault());
      cal.setTimeInMillis(cetTime);
      return SimpleDateFormat.getDateTimeInstance().format(cal.getTime());
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   private String getProfileUuid() {
      String accessToken = Realms.getSessionId();
      String[] tokens = accessToken.split(":");
      return tokens.length == 3 ? tokens[2] : "";
   }

   private void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var5) {
         LOGGER.error("Couldn't open link");
      }

   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      if (this.onLink) {
         String extensionUrl = "https://account.mojang.com/buy/realms?sid=" + this.serverData.remoteSubscriptionId + "&pid=" + this.getProfileUuid();
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(new StringSelection(extensionUrl), null);
         this.browseTo(extensionUrl);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.world.subscription.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.subscription.start"), this.width() / 2 - 100, 53, 10526880);
      this.drawString(this.startDate, this.width() / 2 - 100, 66, 16777215);
      if (this.type == Subscription.SubscriptionType.NORMAL) {
         this.drawString(getLocalizedString("mco.configure.world.subscription.daysleft"), this.width() / 2 - 100, 85, 10526880);
         this.drawString(this.getDaysLeft(), this.width() / 2 - 100, 98, 16777215);
      } else if (this.type == Subscription.SubscriptionType.RECURRING) {
         this.drawString(getLocalizedString("mco.configure.world.subscription.recurring.daysleft"), this.width() / 2 - 100, 85, 10526880);
         this.drawString(this.daysLeftPresentation(this.daysLeft), this.width() / 2 - 100, 98, 16777215);
      }

      this.drawString(getLocalizedString("mco.configure.world.subscription.extendHere"), this.width() / 2 - 100, 117, 10526880);
      String buyLink = "https://account.mojang.com/buy/realms";
      int linkColor = 3368635;
      int hoverColor = 7107012;
      int height = 130;
      int textWidth = this.fontWidth(buyLink);
      int x1 = this.width() / 2 - textWidth / 2 - 1;
      int y1 = height - 1;
      int x2 = x1 + textWidth + 1;
      int y2 = height + 1 + this.fontLineHeight();
      if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
         this.onLink = true;
         this.drawString("§n" + buyLink, this.width() / 2 - textWidth / 2, height, hoverColor);
      } else {
         this.onLink = false;
         this.drawString(buyLink, this.width() / 2 - textWidth / 2, height, linkColor);
      }

      super.render(xm, ym, a);
   }

   private String daysLeftPresentation(int daysLeft) {
      if (daysLeft == -1) {
         return "Expired";
      } else {
         return daysLeft > 1 ? daysLeft + " days" : getLocalizedString("mco.configure.world.subscription.less_than_a_day");
      }
   }

   private String getDaysLeft() {
      return this.daysLeft >= 0 ? String.valueOf(this.daysLeft) : "Expired";
   }
}
