package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class InviteScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsEditBox profileName;
   private RealmsServer serverData;
   private final RealmsScreen onlineScreen;
   private final ConfigureWorldScreen configureScreen;
   private final int BUTTON_INVITE = 0;
   private final int BUTTON_CANCEL = 1;
   private final int PROFILENAME_EDIT_BOX = 2;
   private String defaultErrorMsg = "Could not invite the provided name";
   private String errorMsg;
   private boolean showError;
   private RealmsButton inviteButton;

   public InviteScreen(RealmsScreen onlineScreen, ConfigureWorldScreen configureScreen, RealmsServer serverData) {
      this.onlineScreen = onlineScreen;
      this.configureScreen = configureScreen;
      this.serverData = serverData;
   }

   public void tick() {
      this.profileName.tick();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(
         this.inviteButton = newButton(0, this.width() / 2 - 100, this.height() / 4 + 96 + 12, getLocalizedString("mco.configure.world.buttons.invite"))
      );
      this.buttonsAdd(newButton(1, this.width() / 2 - 100, this.height() / 4 + 120 + 12, getLocalizedString("gui.cancel")));
      this.profileName = this.newEditBox(2, this.width() / 2 - 100, 66, 200, 20);
      this.profileName.setFocus(true);
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            Realms.setScreen(this.configureScreen);
         } else if (button.id() == 0) {
            RealmsClient client = RealmsClient.createRealmsClient();
            if (this.profileName.getValue() == null || this.profileName.getValue().isEmpty()) {
               return;
            }

            try {
               RealmsServer realmsServer = client.invite(this.serverData.id, this.profileName.getValue());
               if (realmsServer != null) {
                  this.serverData.players = realmsServer.players;
                  Realms.setScreen(new ConfigureWorldScreen(this.onlineScreen, this.serverData.id));
               } else {
                  this.showError(this.defaultErrorMsg);
               }
            } catch (Exception var4) {
               LOGGER.error("Couldn't invite user");
               this.showError(this.defaultErrorMsg);
            }
         }

      }
   }

   private void showError(String errorMsg) {
      this.showError = true;
      this.errorMsg = errorMsg;
   }

   public void keyPressed(char ch, int eventKey) {
      this.profileName.keyPressed(ch, eventKey);
      if (eventKey == 15) {
         if (this.profileName.isFocused()) {
            this.profileName.setFocus(false);
         } else {
            this.profileName.setFocus(true);
         }
      }

      if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.inviteButton);
      }

      if (eventKey == 1) {
         Realms.setScreen(this.configureScreen);
      }

   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.profileName.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawString(getLocalizedString("mco.configure.world.invite.profile.name"), this.width() / 2 - 100, 53, 10526880);
      if (this.showError) {
         this.drawCenteredString(this.errorMsg, this.width() / 2, 100, 16711680);
      }

      this.profileName.render();
      super.render(xm, ym, a);
   }
}
