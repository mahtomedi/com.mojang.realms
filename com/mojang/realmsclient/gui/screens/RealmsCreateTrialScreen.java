package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class RealmsCreateTrialScreen extends RealmsScreen {
   private RealmsMainScreen lastScreen;
   private RealmsEditBox nameBox;
   private RealmsEditBox descriptionBox;
   private static int CREATE_BUTTON = 0;
   private static int CANCEL_BUTTON = 1;
   private static int NAME_BOX_ID = 3;
   private static int DESCRIPTION_BOX_ID = 4;
   private boolean initialized = false;
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
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      if (!this.initialized) {
         this.initialized = true;
         this.nameBox = this.newEditBox(NAME_BOX_ID, this.width() / 2 - 100, 65, 200, 20);
         this.nameBox.setFocus(true);
         this.descriptionBox = this.newEditBox(DESCRIPTION_BOX_ID, this.width() / 2 - 100, 115, 200, 20);
      }

      this.buttonsAdd(
         this.createButton = newButton(CREATE_BUTTON, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world"))
      );
      this.buttonsAdd(newButton(CANCEL_BUTTON, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")));
      this.createButton.active(this.valid());
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == CANCEL_BUTTON) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == CREATE_BUTTON) {
            this.createWorld();
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (this.nameBox != null) {
         this.nameBox.keyPressed(ch, eventKey);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.keyPressed(ch, eventKey);
      }

      switch(eventKey) {
         case 1:
            Realms.setScreen(this.lastScreen);
            break;
         case 15:
            if (this.nameBox != null) {
               this.nameBox.setFocus(!this.nameBox.isFocused());
            }

            if (this.descriptionBox != null) {
               this.descriptionBox.setFocus(!this.descriptionBox.isFocused());
            }
            break;
         case 28:
         case 156:
            this.buttonClicked(this.createButton);
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
      return this.nameBox != null && this.nameBox.getValue() != null && !this.nameBox.getValue().trim().equals("");
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      if (this.nameBox != null) {
         this.nameBox.mouseClicked(x, y, buttonNum);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.mouseClicked(x, y, buttonNum);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.trial.title"), this.width() / 2, 11, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 10526880);
      if (this.nameBox != null) {
         this.nameBox.render();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render();
      }

      super.render(xm, ym, a);
   }
}