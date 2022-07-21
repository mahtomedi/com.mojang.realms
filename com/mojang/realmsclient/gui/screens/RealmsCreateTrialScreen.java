package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

public class RealmsCreateTrialScreen extends RealmsScreen {
   private final RealmsMainScreen lastScreen;
   private RealmsEditBox nameBox;
   private RealmsEditBox descriptionBox;
   private static final int CREATE_BUTTON = 0;
   private static final int CANCEL_BUTTON = 1;
   private static final int NAME_BOX_ID = 3;
   private static final int DESCRIPTION_BOX_ID = 4;
   private boolean initialized;
   private RealmsButton createButton;

   public RealmsCreateTrialScreen(RealmsMainScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.tick();
         this.createButton.active(this.valid());
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.tick();
      }

   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      if (!this.initialized) {
         this.initialized = true;
         this.nameBox = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20);
         this.focusOn(this.nameBox);
         this.descriptionBox = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20);
      }

      this.buttonsAdd(
         this.createButton = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsCreateTrialScreen.this.createWorld();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsCreateTrialScreen.this.lastScreen);
         }
      });
      this.createButton.active(this.valid());
      this.addWidget(this.nameBox);
      this.addWidget(this.descriptionBox);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean charTyped(char character, int mods) {
      this.createButton.active(this.valid());
      return false;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.lastScreen);
            return true;
         case 257:
         case 335:
            this.createWorld();
            return true;
         case 258:
            this.focusNext();
            return true;
         default:
            this.createButton.active(this.valid());
            return false;
      }
   }

   private void createWorld() {
      if (this.valid()) {
         RealmsTasks.TrialCreationTask trialCreationTask = new RealmsTasks.TrialCreationTask(
            this.nameBox.getValue(), this.descriptionBox.getValue(), this.lastScreen
         );
         RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, trialCreationTask);
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean valid() {
      return this.nameBox != null && this.nameBox.getValue() != null && !this.nameBox.getValue().trim().isEmpty();
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.trial.title"), this.width() / 2, 11, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 10526880);
      if (this.nameBox != null) {
         this.nameBox.render(xm, ym, a);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render(xm, ym, a);
      }

      super.render(xm, ym, a);
   }
}
