package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsTermsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BUTTON_AGREE_ID = 1;
   private static final int BUTTON_DISAGREE_ID = 2;
   private final RealmsScreen lastScreen;
   private final RealmsMainScreen mainScreen;
   private final RealmsServer realmsServer;
   private RealmsButton agreeButton;
   private boolean onLink;
   private final String realmsToSUrl = "https://minecraft.net/realms/terms";

   public RealmsTermsScreen(RealmsScreen lastScreen, RealmsMainScreen mainScreen, RealmsServer realmsServer) {
      this.lastScreen = lastScreen;
      this.mainScreen = mainScreen;
      this.realmsServer = realmsServer;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      int column1X = this.width() / 4;
      int columnWidth = this.width() / 4 - 2;
      int column2X = this.width() / 2 + 4;
      this.buttonsAdd(
         this.agreeButton = new RealmsButton(1, column1X, RealmsConstants.row(12), columnWidth, 20, getLocalizedString("mco.terms.buttons.agree")) {
            public void onPress() {
               RealmsTermsScreen.this.agreedToTos();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(2, column2X, RealmsConstants.row(12), columnWidth, 20, getLocalizedString("mco.terms.buttons.disagree")) {
         public void onPress() {
            Realms.setScreen(RealmsTermsScreen.this.lastScreen);
         }
      });
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

   private void agreedToTos() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         client.agreeToTos();
         RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
            this.lastScreen, new RealmsTasks.RealmsGetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())
         );
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't agree to TOS");
      }

   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.onLink) {
         RealmsBridge.setClipboard("https://minecraft.net/realms/terms");
         RealmsUtil.browseTo("https://minecraft.net/realms/terms");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.terms.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.terms.sentence.1"), this.width() / 2 - 120, RealmsConstants.row(5), 16777215);
      int firstPartWidth = this.fontWidth(getLocalizedString("mco.terms.sentence.1"));
      int x1 = this.width() / 2 - 121 + firstPartWidth;
      int y1 = RealmsConstants.row(5);
      int x2 = x1 + this.fontWidth("mco.terms.sentence.2") + 1;
      int y2 = y1 + 1 + this.fontLineHeight();
      if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
         this.onLink = true;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, RealmsConstants.row(5), 7107012);
      } else {
         this.onLink = false;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, RealmsConstants.row(5), 3368635);
      }

      super.render(xm, ym, a);
   }
}
