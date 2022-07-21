package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsState;
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

public class BuyRealmsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsScreen lastScreen;
   private static int BACK_BUTTON_ID = 111;
   private volatile RealmsState realmsStatus;
   private boolean onLink = false;

   public BuyRealmsScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void tick() {
      super.tick();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      int buttonLength = 212;
      this.buttonsAdd(newButton(BACK_BUTTON_ID, this.width() / 2 - buttonLength / 2, 180, buttonLength, 20, getLocalizedString("gui.back")));
      this.fetchMessage();
   }

   private void fetchMessage() {
      final RealmsClient client = RealmsClient.createRealmsClient();
      (new Thread("Realms-stat-message") {
         public void run() {
            try {
               BuyRealmsScreen.this.realmsStatus = client.fetchRealmsState();
            } catch (RealmsServiceException var2) {
               BuyRealmsScreen.LOGGER.error("Could not get stat");
            }

         }
      }).start();
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == BACK_BUTTON_ID) {
            Realms.setScreen(this.lastScreen);
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      if (this.onLink) {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(new StringSelection(this.realmsStatus.getBuyLink()), null);
         this.browseTo(this.realmsStatus.getBuyLink());
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
      this.drawCenteredString(getLocalizedString("mco.buy.realms.title"), this.width() / 2, 11, 16777215);
      if (this.realmsStatus != null) {
         String[] lines = this.realmsStatus.getStatusMessage().split("\n");
         int height = 52;

         for(String line : lines) {
            this.drawCenteredString(line, this.width() / 2, height, 10526880);
            height += 18;
         }

         if (this.realmsStatus.getBuyLink() != null) {
            String buyLink = this.realmsStatus.getBuyLink();
            int linkColor = 3368635;
            int hoverColor = 7107012;
            height += 18;
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
         }

         super.render(xm, ym, a);
      }
   }
}
