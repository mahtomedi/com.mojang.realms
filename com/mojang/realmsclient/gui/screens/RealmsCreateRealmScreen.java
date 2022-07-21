package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.RealmsTasks;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class RealmsCreateRealmScreen extends RealmsScreen {
   private final RealmsServer server;
   private final RealmsMainScreen lastScreen;
   private RealmsEditBox nameBox;
   private RealmsEditBox descriptionBox;
   private static final int CREATE_BUTTON = 0;
   private static final int CANCEL_BUTTON = 1;
   private static final int NAME_BOX_ID = 3;
   private static final int DESCRIPTION_BOX_ID = 4;
   private RealmsButton createButton;

   public RealmsCreateRealmScreen(RealmsServer server, RealmsMainScreen lastScreen) {
      this.server = server;
      this.lastScreen = lastScreen;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.tick();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.tick();
      }

   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(this.createButton = newButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")));
      this.buttonsAdd(newButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")));
      this.createButton.active(false);
      this.nameBox = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20);
      this.nameBox.setFocus(true);
      this.descriptionBox = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20);
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 0) {
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

      this.createButton.active(this.valid());
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
         RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(
            this.lastScreen,
            this.server,
            this.lastScreen.newScreen(),
            getLocalizedString("mco.selectServer.create"),
            getLocalizedString("mco.create.world.subtitle"),
            10526880,
            getLocalizedString("mco.create.world.skip")
         );
         resetWorldScreen.setResetTitle(getLocalizedString("mco.create.world.reset.title"));
         RealmsTasks.WorldCreationTask worldCreationTask = new RealmsTasks.WorldCreationTask(
            this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), resetWorldScreen
         );
         RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, worldCreationTask);
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean valid() {
      return this.nameBox.getValue() != null && !this.nameBox.getValue().trim().isEmpty();
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
      this.drawCenteredString(getLocalizedString("mco.selectServer.create"), this.width() / 2, 11, 16777215);
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
