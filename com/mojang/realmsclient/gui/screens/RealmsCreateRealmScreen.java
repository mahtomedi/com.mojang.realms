package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.RealmsTasks;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

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
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(
         this.createButton = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")) {
            public void onPress() {
               RealmsCreateRealmScreen.this.createWorld();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(RealmsCreateRealmScreen.this.lastScreen);
         }
      });
      this.createButton.active(false);
      this.nameBox = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20);
      this.addWidget(this.nameBox);
      this.focusOn(this.nameBox);
      this.descriptionBox = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20);
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
         default:
            this.createButton.active(this.valid());
            return false;
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

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.selectServer.create"), this.width() / 2, 11, 16777215);
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
