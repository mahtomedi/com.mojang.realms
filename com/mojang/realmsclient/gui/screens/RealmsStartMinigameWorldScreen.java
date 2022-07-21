package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsStartMinigameWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsConfigureWorldScreen lastScreen;
   private RealmsServer serverData;
   private final int START_BUTTON_ID = 1;
   private final int CANCEL_BUTTON = 2;
   private static int WORLD_TEMPLATE_BUTTON = 3;
   private WorldTemplate selectedWorldTemplate;
   private RealmsButton startButton;

   public RealmsStartMinigameWorldScreen(RealmsConfigureWorldScreen lastScreen, RealmsServer serverData) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(
         this.startButton = newButton(1, this.width() / 2 - 100, this.height() / 4 + 120 + 12, 97, 20, getLocalizedString("mco.minigame.world.startButton"))
      );
      this.buttonsAdd(newButton(2, this.width() / 2 + 5, this.height() / 4 + 120 + 12, 97, 20, getLocalizedString("gui.cancel")));
      this.startButton.active(this.selectedWorldTemplate != null);
      if (this.selectedWorldTemplate == null) {
         this.buttonsAdd(newButton(WORLD_TEMPLATE_BUTTON, this.width() / 2 - 100, 102, 200, 20, getLocalizedString("mco.minigame.world.noSelection")));
      } else {
         this.buttonsAdd(newButton(WORLD_TEMPLATE_BUTTON, this.width() / 2 - 100, 102, 200, 20, this.selectedWorldTemplate.name));
      }

   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.startButton);
      }

      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 2) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 1) {
            this.lastScreen.switchMinigame(this.selectedWorldTemplate);
         } else if (button.id() == WORLD_TEMPLATE_BUTTON) {
            Realms.setScreen(new RealmsSelectWorldTemplateScreen(this, this.selectedWorldTemplate, true));
         }

      }
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.minigame.world.title"), this.width() / 2, 17, 16777215);
      this.drawCenteredString(getLocalizedString("mco.minigame.world.info1"), this.width() / 2, 56, 16777215);
      this.drawCenteredString(getLocalizedString("mco.minigame.world.info2"), this.width() / 2, 68, 16777215);
      this.drawString(getLocalizedString("mco.minigame.world.selected"), this.width() / 2 - 100, 90, 10526880);
      super.render(xm, ym, a);
   }

   void callback(WorldTemplate worldTemplate) {
      this.selectedWorldTemplate = worldTemplate;
      Realms.setScreen(this);
   }
}
