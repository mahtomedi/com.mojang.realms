package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.LongRunningTask;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class ModifyMinigameWorldScreen extends ScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsScreen lastScreen;
   private RealmsServer serverData;
   private final int SWITCH_BUTTON_ID = 1;
   private final int END_BUTTON_ID = 2;
   private final int CANCEL_BUTTON = 3;

   public ModifyMinigameWorldScreen(RealmsScreen lastScreen, RealmsServer serverData) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
   }

   public void tick() {
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(newButton(1, this.width() / 2 - 100, 116, 200, 20, getLocalizedString("mco.minigame.world.changeButton")));
      this.buttonsAdd(newButton(2, this.width() / 2 - 100, 70, 200, 20, getLocalizedString("mco.minigame.world.stopButton")));
      this.buttonsAdd(newButton(3, this.width() / 2 - 45, this.height() / 4 + 120 + 12, 97, 20, getLocalizedString("gui.cancel")));
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 3) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 1) {
            Realms.setScreen(new StartMinigameWorldScreen(this.lastScreen, this.serverData));
         } else if (button.id() == 2) {
            String line2 = getLocalizedString("mco.minigame.world.restore.question.line1");
            String line3 = getLocalizedString("mco.minigame.world.restore.question.line2");
            Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 2));
         }

      }
   }

   public void confirmResult(boolean result, int id) {
      if (id == 2) {
         if (result) {
            this.stopMinigame();
         } else {
            Realms.setScreen(this);
         }
      }

   }

   private void stopMinigame() {
      ModifyMinigameWorldScreen.StopMinigameTask stopMinigameTask = new ModifyMinigameWorldScreen.StopMinigameTask();
      LongRunningMcoTaskScreen longRunningMcoTaskScreen = new LongRunningMcoTaskScreen(this.lastScreen, stopMinigameTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.minigame.world.modify.title"), this.width() / 2, 17, 16777215);
      this.drawCenteredString(getLocalizedString("mco.minigame.world.modify.new"), this.width() / 2, 102, 16777215);
      this.drawCenteredString(getLocalizedString("mco.minigame.world.modify.end"), this.width() / 2, 56, 16777215);
      super.render(xm, ym, a);
   }

   void callback(WorldTemplate t) {
   }

   private class StopMinigameTask extends LongRunningTask {
      public StopMinigameTask() {
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.restore");
         this.setTitle(title);

         try {
            boolean closeResult = client.putIntoNormalMode(ModifyMinigameWorldScreen.this.serverData.id);
            Thread.sleep(2000L);
            if (closeResult) {
               ModifyMinigameWorldScreen.this.serverData.worldType = RealmsServer.WorldType.NORMAL;
               ModifyMinigameWorldScreen.this.serverData.motd = "";
               this.init();
            }

            Realms.setScreen(ModifyMinigameWorldScreen.this.lastScreen);
         } catch (RealmsServiceException var4) {
            if (this.aborted()) {
               return;
            }

            ModifyMinigameWorldScreen.LOGGER.error("Couldn't start mini game!");
            this.error(var4.toString());
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            ModifyMinigameWorldScreen.LOGGER.error("Couldn't start mini game!");
            this.error(var5.toString());
         }

      }
   }
}
