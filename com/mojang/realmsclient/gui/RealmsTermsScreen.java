package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsTermsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int AGREE_BUTTON_ID = 1;
   private static final int DISAGREE_BUTTON_ID = 2;
   private final RealmsScreen lastScreen;
   private final McoServer mcoServer;
   private RealmsButton agreeButton;
   private boolean onLink = false;
   private String realmsToSUrl = "https://minecraft.net/realms/terms";

   public RealmsTermsScreen(RealmsScreen lastScreen, McoServer mcoServer) {
      this.lastScreen = lastScreen;
      this.mcoServer = mcoServer;
   }

   public void tick() {
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      int column1_x = this.width() / 4;
      int column_width = this.width() / 4 - 2;
      int column2_x = this.width() / 2 + 4;
      this.buttonsAdd(this.agreeButton = newButton(1, column1_x, this.height() / 5 + 96 + 22, column_width, 20, getLocalizedString("mco.terms.buttons.agree")));
      this.buttonsAdd(newButton(2, column2_x, this.height() / 5 + 96 + 22, column_width, 20, getLocalizedString("mco.terms.buttons.disagree")));
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 2) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 1) {
            this.agreedToTos();
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   private void agreedToTos() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         client.agreeToTos();
         LongRunningMcoTaskScreen longRunningMcoTaskScreen = new LongRunningMcoTaskScreen(
            this.lastScreen, new OnlineConnectTask(this.lastScreen, this.mcoServer)
         );
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't agree to TOS");
      }

   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      if (this.onLink) {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(new StringSelection(this.realmsToSUrl), null);
         this.browseTo(this.realmsToSUrl);
      }

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

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.terms.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.terms.sentence.1"), this.width() / 2 - 120, 87, 16777215);
      int firstPartWidth = this.fontWidth(getLocalizedString("mco.terms.sentence.1"));
      int linkColor = 3368635;
      int hoverColor = 7107012;
      int x1 = this.width() / 2 - 121 + firstPartWidth;
      int y1 = 86;
      int x2 = x1 + this.fontWidth("mco.terms.sentence.2") + 1;
      int y2 = 87 + this.fontLineHeight();
      if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
         this.onLink = true;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, 87, hoverColor);
      } else {
         this.onLink = false;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, 87, linkColor);
      }

      super.render(xm, ym, a);
   }
}
