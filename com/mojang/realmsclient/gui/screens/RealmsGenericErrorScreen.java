package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsGenericErrorScreen extends RealmsScreen {
   private final RealmsScreen nextScreen;
   private static final int OK_BUTTON_ID = 10;
   private String line1;
   private String line2;

   public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, RealmsScreen nextScreen) {
      this.nextScreen = nextScreen;
      this.errorMessage(realmsServiceException);
   }

   public RealmsGenericErrorScreen(String message, RealmsScreen nextScreen) {
      this.nextScreen = nextScreen;
      this.errorMessage(message);
   }

   private void errorMessage(RealmsServiceException realmsServiceException) {
      if (realmsServiceException.errorCode != -1) {
         this.line1 = "Realms (" + realmsServiceException.errorCode + "):";
         this.line2 = getLocalizedString("mco.errorMessage." + realmsServiceException.errorCode);
      } else {
         this.line1 = "An error occurred (" + realmsServiceException.httpResultCode + "):";
         this.line2 = realmsServiceException.httpResponseContent;
      }

   }

   private void errorMessage(String message) {
      this.line1 = "An error occurred: ";
      this.line2 = message;
   }

   public void init() {
      this.buttonsClear();
      this.buttonsAdd(newButton(10, this.width() / 2 - 100, this.height() - 52, 200, 20, "Ok"));
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.id() == 10) {
         Realms.setScreen(this.nextScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.line1, this.width() / 2, 80, 16777215);
      this.drawCenteredString(this.line2, this.width() / 2, 100, 16711680);
      super.render(xm, ym, a);
   }
}
