package com.mojang.realmsclient.gui.screens;

import java.net.URI;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsParentalConsentScreen extends RealmsScreen {
   private final RealmsScreen nextScreen;
   private static final int OK_BUTTON_ID = 10;
   private static final int BACK_BUTTON_ID = 5;
   private String line1;
   private String line2;

   public RealmsParentalConsentScreen(RealmsScreen nextScreen) {
      this.nextScreen = nextScreen;
   }

   public void init() {
      this.buttonsClear();
      this.line1 = "You need parental consent to access Minecraft Realms.";
      this.line2 = "This is not currently set up on your account.";
      this.buttonsAdd(newButton(10, this.width() / 2 - 100, this.height() - 65, 200, 20, "Set up parental consent"));
      this.buttonsAdd(newButton(5, this.width() / 2 - 100, this.height() - 42, 200, 20, "Back"));
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.id() == 10) {
         this.browseTo("https://accounts.mojang.com/account/requestConsent/" + Realms.getUUID());
      } else if (button.id() == 5) {
         Realms.setScreen(this.nextScreen);
      }

   }

   private void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var5) {
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.line1, this.width() / 2, 80, 16777215);
      this.drawCenteredString(this.line2, this.width() / 2, 100, 16777215);
      super.render(xm, ym, a);
   }
}
