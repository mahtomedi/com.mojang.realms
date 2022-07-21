package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

public class RealmsSettingsScreen extends RealmsScreen {
   private final RealmsConfigureWorldScreen configureWorldScreen;
   private final RealmsServer serverData;
   private static final int BUTTON_CANCEL_ID = 0;
   private static final int BUTTON_DONE_ID = 1;
   private static final int NAME_EDIT_BOX = 2;
   private static final int DESC_EDIT_BOX = 3;
   private static final int BUTTON_OPEN_CLOSE_ID = 5;
   private final int COMPONENT_WIDTH = 212;
   private RealmsButton doneButton;
   private RealmsEditBox descEdit;
   private RealmsEditBox nameEdit;

   public RealmsSettingsScreen(RealmsConfigureWorldScreen configureWorldScreen, RealmsServer serverData) {
      this.configureWorldScreen = configureWorldScreen;
      this.serverData = serverData;
   }

   public void tick() {
      this.nameEdit.tick();
      this.descEdit.tick();
      this.doneButton.active(this.nameEdit.getValue() != null && !this.nameEdit.getValue().trim().isEmpty());
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      int center = this.width() / 2 - 106;
      this.buttonsAdd(
         this.doneButton = new RealmsButton(1, center - 2, RealmsConstants.row(12), 106, 20, getLocalizedString("mco.configure.world.buttons.done")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSettingsScreen.this.save();
            }
         }
      );
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 2, RealmsConstants.row(12), 106, 20, getLocalizedString("gui.cancel")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsSettingsScreen.this.configureWorldScreen);
         }
      });
      this.buttonsAdd(
         new RealmsButton(
            5,
            this.width() / 2 - 53,
            RealmsConstants.row(0),
            106,
            20,
            getLocalizedString(this.serverData.state.equals(RealmsServer.State.OPEN) ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open")
         ) {
            public void onClick(double mouseX, double mouseY) {
               if (RealmsSettingsScreen.this.serverData.state.equals(RealmsServer.State.OPEN)) {
                  String line2 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line1");
                  String line3 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line2");
                  Realms.setScreen(new RealmsLongConfirmationScreen(RealmsSettingsScreen.this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 5));
               } else {
                  RealmsSettingsScreen.this.configureWorldScreen.openTheWorld(false, RealmsSettingsScreen.this);
               }
   
            }
         }
      );
      this.nameEdit = this.newEditBox(2, center, RealmsConstants.row(4), 212, 20);
      this.nameEdit.setMaxLength(32);
      if (this.serverData.getName() != null) {
         this.nameEdit.setValue(this.serverData.getName());
      }

      this.addWidget(this.nameEdit);
      this.focusOn(this.nameEdit);
      this.descEdit = this.newEditBox(3, center, RealmsConstants.row(8), 212, 20);
      this.descEdit.setMaxLength(32);
      if (this.serverData.getDescription() != null) {
         this.descEdit.setValue(this.serverData.getDescription());
      }

      this.addWidget(this.descEdit);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public void confirmResult(boolean result, int id) {
      switch(id) {
         case 5:
            if (result) {
               this.configureWorldScreen.closeTheWorld(this);
            } else {
               Realms.setScreen(this);
            }
      }
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.configureWorldScreen);
            return true;
         case 257:
         case 335:
            this.save();
            return true;
         case 258:
            this.focusNext();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.world.settings.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 106, RealmsConstants.row(3), 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 106, RealmsConstants.row(7), 10526880);
      this.nameEdit.render(xm, ym, a);
      this.descEdit.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   public void save() {
      this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
   }
}
