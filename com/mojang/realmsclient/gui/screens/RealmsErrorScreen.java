package com.mojang.realmsclient.gui.screens;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsErrorScreen extends RealmsScreen {
   private String title;
   private String message;
   private RealmsScreen lastScreen;

   public RealmsErrorScreen(String title, String message, RealmsScreen lastScreen) {
      this.title = title;
      this.message = message;
      this.lastScreen = lastScreen;
   }

   public void init() {
      super.init();
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, 140, getLocalizedString("gui.back")));
   }

   public void render(int xm, int ym, float a) {
      this.fillGradient(0, 0, this.width(), this.height(), -12574688, -11530224);
      this.drawCenteredString(this.title, this.width() / 2, 90, 16777215);
      this.drawCenteredString(this.message, this.width() / 2, 110, 16777215);
      super.render(xm, ym, a);
   }

   public void keyPressed(char eventCharacter, int eventKey) {
   }

   public void buttonClicked(RealmsButton button) {
      Realms.setScreen(this.lastScreen);
   }
}
