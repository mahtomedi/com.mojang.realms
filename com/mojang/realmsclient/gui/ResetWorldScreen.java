package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class ResetWorldScreen extends ScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsScreen lastScreen;
   private RealmsScreen onlineScreen;
   private RealmsScreen worldManagementScreen;
   private McoServer serverData;
   private RealmsEditBox seedEdit;
   private Boolean generateStructures = true;
   private Integer levelTypeIndex = 0;
   String[] levelTypes;
   private final int RESET_BUTTON_ID = 1;
   private final int CANCEL_BUTTON = 2;
   private static final int WORLD_TEMPLATE_BUTTON = 3;
   private static final int LEVEL_TYPE_BUTTON_ID = 4;
   private static final int GENERATE_STRUCTURES_BUTTON_ID = 5;
   private WorldTemplate selectedWorldTemplate;
   private RealmsButton resetButton;
   private RealmsButton levelTypeButton;
   private RealmsButton generateStructuresButton;

   public ResetWorldScreen(RealmsScreen lastScreen, RealmsScreen onlineScreen, RealmsScreen worldManagementScreen, McoServer serverData) {
      this.lastScreen = lastScreen;
      this.onlineScreen = onlineScreen;
      this.worldManagementScreen = worldManagementScreen;
      this.serverData = serverData;
   }

   public void tick() {
      this.seedEdit.tick();
   }

   public void init() {
      this.levelTypes = new String[]{
         getLocalizedString("generator.default"),
         getLocalizedString("generator.flat"),
         getLocalizedString("generator.largeBiomes"),
         getLocalizedString("generator.amplified")
      };
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(
         this.resetButton = newButton(1, this.width() / 2 - 100, this.height() / 4 + 120 + 12, 97, 20, getLocalizedString("mco.configure.world.buttons.reset"))
      );
      this.buttonsAdd(newButton(2, this.width() / 2 + 5, this.height() / 4 + 120 + 12, 97, 20, getLocalizedString("gui.cancel")));
      this.seedEdit = this.newEditBox(this.width() / 2 - 100, 99, 200, 20);
      this.seedEdit.setFocus(true);
      this.seedEdit.setMaxLength(32);
      this.seedEdit.setValue("");
      this.buttonsAdd(this.levelTypeButton = newButton(4, this.width() / 2 - 102, 145, 205, 20, this.levelTypeTitle()));
      this.buttonsAdd(this.generateStructuresButton = newButton(5, this.width() / 2 - 102, 165, 205, 20, this.generateStructuresTitle()));
      if (this.selectedWorldTemplate == null) {
         this.buttonsAdd(newButton(3, this.width() / 2 - 102, 125, 205, 20, getLocalizedString("mco.template.default.name")));
      } else {
         this.seedEdit.setValue("");
         this.seedEdit.setIsEditable(false);
         this.seedEdit.setFocus(false);
         this.buttonsAdd(newButton(3, this.width() / 2 - 102, 125, 205, 20, getLocalizedString("mco.template.name") + ": " + this.selectedWorldTemplate.name));
      }

   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void keyPressed(char ch, int eventKey) {
      this.seedEdit.keyPressed(ch, eventKey);
      if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.resetButton);
      }

      if (eventKey == 1) {
         Realms.setScreen(this.worldManagementScreen);
      }

   }

   protected void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 2) {
            Realms.setScreen(this.worldManagementScreen);
         } else if (button.id() == 1) {
            String line2 = getLocalizedString("mco.configure.world.reset.question.line1");
            String line3 = getLocalizedString("mco.configure.world.reset.question.line2");
            Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Warning, line2, line3, 1));
         } else if (button.id() == 3) {
            Realms.setScreen(new McoWorldTemplateScreen(this, this.selectedWorldTemplate, false));
         } else if (button.id() == 4) {
            this.levelTypeIndex = (this.levelTypeIndex + 1) % this.levelTypes.length;
            button.msg(this.levelTypeTitle());
         } else if (button.id() == 5) {
            this.generateStructures = !this.generateStructures;
            button.msg(this.generateStructuresTitle());
         }

      }
   }

   public void confirmResult(boolean result, int id) {
      if (result && id == 1) {
         this.resetWorld();
      } else {
         Realms.setScreen(this);
      }

   }

   private void resetWorld() {
      ResetWorldScreen.ResettingWorldTask resettingWorldTask = new ResetWorldScreen.ResettingWorldTask(
         this.serverData.id, this.seedEdit.getValue(), this.selectedWorldTemplate, this.levelTypeIndex, this.generateStructures
      );
      LongRunningMcoTaskScreen longRunningMcoTaskScreen = new LongRunningMcoTaskScreen(this.lastScreen, resettingWorldTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   protected void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.seedEdit.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.reset.world.title"), this.width() / 2, 17, 16777215);
      this.drawCenteredString(getLocalizedString("mco.reset.world.warning"), this.width() / 2, 56, 16711680);
      this.drawString(getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, 86, 10526880);
      this.seedEdit.render();
      super.render(xm, ym, a);
   }

   void callback(WorldTemplate worldTemplate) {
      this.selectedWorldTemplate = worldTemplate;
   }

   private String levelTypeTitle() {
      String levelType = getLocalizedString("selectWorld.mapType");
      return levelType + " " + this.levelTypes[this.levelTypeIndex];
   }

   private String generateStructuresTitle() {
      return getLocalizedString("selectWorld.mapFeatures")
         + " "
         + (this.generateStructures ? getLocalizedString("mco.configure.world.on") : getLocalizedString("mco.configure.world.off"));
   }

   private class ResettingWorldTask extends LongRunningTask {
      private final long worldId;
      private final String seed;
      private final WorldTemplate worldTemplate;
      private final int levelType;
      private final boolean generateStructures;

      public ResettingWorldTask(long worldId, String seed, WorldTemplate worldTemplate, int levelType, boolean generateStructures) {
         this.worldId = worldId;
         this.seed = seed;
         this.worldTemplate = worldTemplate;
         this.levelType = levelType;
         this.generateStructures = generateStructures;
      }

      public void run() {
         RealmsClient client = RealmsClient.createRealmsClient();
         String title = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");
         this.setTitle(title);

         try {
            if (this.aborted()) {
               return;
            }

            if (this.worldTemplate != null) {
               client.resetWorldWithTemplate(this.worldId, this.worldTemplate.id);
               Realms.setScreen(new ConfigureWorldScreen(ResetWorldScreen.this.onlineScreen, this.worldId));
               return;
            }

            client.resetWorldWithSeed(this.worldId, this.seed, this.levelType, this.generateStructures);
            if (this.aborted()) {
               return;
            }

            Realms.setScreen(new ConfigureWorldScreen(ResetWorldScreen.this.onlineScreen, this.worldId));
         } catch (RealmsServiceException var4) {
            if (this.aborted()) {
               return;
            }

            ResetWorldScreen.LOGGER.error("Couldn't reset world");
            this.error(var4.toString());
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            ResetWorldScreen.LOGGER.error("Couldn't reset world");
            this.error(var5.toString());
         }

      }
   }
}
