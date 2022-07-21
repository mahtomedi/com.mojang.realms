package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsClientOutdatedScreen extends RealmsScreen {
   private static final int BUTTON_BACK_ID = 0;
   private final RealmsScreen lastScreen;
   private final boolean outdated;

   public RealmsClientOutdatedScreen(RealmsScreen lastScreen, boolean outdated) {
      this.lastScreen = lastScreen;
      this.outdated = outdated;
   }

   public void init() {
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), "Back") {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsClientOutdatedScreen.this.lastScreen);
         }
      });
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      String title = getLocalizedString(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
      this.drawCenteredString(title, this.width() / 2, RealmsConstants.row(3), 16711680);
      int lines = this.outdated ? 2 : 3;

      for(int i = 0; i < lines; ++i) {
         String message = getLocalizedString((this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (i + 1));
         this.drawCenteredString(message, this.width() / 2, RealmsConstants.row(5) + i * 12, 16777215);
      }

      super.render(xm, ym, a);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey != 257 && eventKey != 335 && eventKey != 256) {
         return super.keyPressed(eventKey, scancode, mods);
      } else {
         Realms.setScreen(this.lastScreen);
         return true;
      }
   }
}
