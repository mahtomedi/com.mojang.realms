package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import org.lwjgl.input.Keyboard;

public class RealmsResetWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
   private RealmsConfigureWorldScreen lastScreen;
   private RealmsServer serverData;
   private final int BUTTON_CANCEL_ID = 0;
   private static final int BUTTON_WORLD_TEMPLATE_ID = 1;
   private static final int BUTTON_UPLOAD_ID = 2;
   private static final int BUTTON_GENERATE_WORLD_ID = 3;

   public RealmsResetWorldScreen(RealmsConfigureWorldScreen lastScreen, RealmsServer serverData) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
   }

   public void tick() {
      super.tick();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(newButton(0, this.width() / 2 - 50, RealmsConstants.row(12), 100, 20, getLocalizedString("gui.cancel")));
      this.buttonsAdd(newButton(3, this.width() / 2 - 160, RealmsConstants.row(5), 100, 20, getLocalizedString("mco.backup.generate.world")));
      this.buttonsAdd(newButton(1, this.width() / 2 - 50, RealmsConstants.row(5), 100, 20, getLocalizedString("mco.template.default.name")));
      this.buttonsAdd(newButton(2, this.width() / 2 + 60, RealmsConstants.row(5), 100, 20, getLocalizedString("mco.backup.button.upload")));
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
         switch(button.id()) {
            case 0:
               Realms.setScreen(this.lastScreen);
               break;
            case 1:
               Realms.setScreen(new RealmsSelectWorldTemplateScreen(this, null, false));
               break;
            case 2:
               Realms.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.serverData.activeSlot, this.lastScreen, this));
               break;
            case 3:
               Realms.setScreen(new RealmsResetNormalWorldScreen(this));
               break;
            default:
               return;
         }

      }
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.reset.world.title"), this.width() / 2, 17, 16777215);
      this.drawCenteredString(getLocalizedString("mco.reset.world.warning"), this.width() / 2, RealmsConstants.row(1), 16711680);
      super.render(xm, ym, a);
   }

   void callback(WorldTemplate worldTemplate) {
      if (worldTemplate != null) {
         this.lastScreen.resetWorldWithTemplate(worldTemplate);
      }

   }

   public void resetWorld(String seed, int levelType, boolean generateStructures) {
      this.lastScreen.resetWorld(seed, levelType, generateStructures);
   }
}
