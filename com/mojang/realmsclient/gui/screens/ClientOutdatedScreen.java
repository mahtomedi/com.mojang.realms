package com.mojang.realmsclient.gui.screens;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class ClientOutdatedScreen extends RealmsScreen {
   private static final int BUTTON_BACK_ID = 0;
   private final RealmsScreen lastScreen;

   public ClientOutdatedScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void init() {
      this.buttonsClear();
      this.buttonsAdd(newButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 12, "Back"));
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      String title = getLocalizedString("mco.client.outdated.title");
      String msg = getLocalizedString("mco.client.outdated.msg");
      this.drawCenteredString(title, this.width() / 2, this.height() / 2 - 50, 16711680);
      this.drawCenteredString(msg, this.width() / 2, this.height() / 2 - 30, 16777215);
      super.render(xm, ym, a);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.id() == 0) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 28 || eventKey == 156 || eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }
}
