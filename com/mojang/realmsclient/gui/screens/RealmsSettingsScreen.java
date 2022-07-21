package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class RealmsSettingsScreen extends RealmsScreen {
   private RealmsConfigureWorldScreen configureWorldScreen;
   private RealmsServer serverData;
   private static final int BUTTON_CANCEL_ID = 0;
   private static final int BUTTON_DONE_ID = 1;
   private static final int NAME_EDIT_BOX = 2;
   private static final int DESC_EDIT_BOX = 3;
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
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      int center = this.width() / 2 - 106;
      this.buttonsAdd(this.doneButton = newButton(1, center, RealmsConstants.row(12), 106, 20, getLocalizedString("mco.configure.world.buttons.done")));
      this.buttonsAdd(newButton(0, this.width() / 2 + 4, RealmsConstants.row(12), 106, 20, getLocalizedString("gui.cancel")));
      this.nameEdit = this.newEditBox(2, center, RealmsConstants.row(1), 212, 20);
      this.nameEdit.setFocus(true);
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setValue(this.serverData.getName());
      this.descEdit = this.newEditBox(3, center, RealmsConstants.row(5), 212, 20);
      this.descEdit.setMaxLength(32);
      this.descEdit.setValue(this.serverData.getDescription());
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         switch(button.id()) {
            case 0:
               Realms.setScreen(this.configureWorldScreen);
               break;
            case 1:
               this.save();
               break;
            default:
               return;
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      this.nameEdit.keyPressed(ch, eventKey);
      this.descEdit.keyPressed(ch, eventKey);
      switch(eventKey) {
         case 1:
            Realms.setScreen(this.configureWorldScreen);
         default:
            return;
         case 15:
            this.nameEdit.setFocus(!this.nameEdit.isFocused());
            this.descEdit.setFocus(!this.descEdit.isFocused());
            break;
         case 28:
         case 156:
            this.save();
      }

      this.doneButton.active(this.nameEdit.getValue() != null && !this.nameEdit.getValue().trim().equals(""));
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.descEdit.mouseClicked(x, y, buttonNum);
      this.nameEdit.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.configure.world.settings.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 106, RealmsConstants.row(0), 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 106, RealmsConstants.row(4), 10526880);
      this.nameEdit.render();
      this.descEdit.render();
      super.render(xm, ym, a);
   }

   public void save() {
      this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
   }
}
