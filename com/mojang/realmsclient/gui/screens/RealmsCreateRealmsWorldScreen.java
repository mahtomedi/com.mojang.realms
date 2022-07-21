package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.LongRunningTask;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsCreateRealmsWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final long worldId;
   private RealmsScreen lastScreen;
   private RealmsEditBox nameBox;
   private String name;
   private static int CREATE_BUTTON = 0;
   private static int CANCEL_BUTTON = 1;
   private static int WORLD_TEMPLATE_BUTTON = 2;
   private static int NAME_BOX_ID = 3;
   private boolean error;
   private String errorMessage = "You must enter a name!";
   private WorldTemplate selectedWorldTemplate;
   private RealmsButton templateButton;

   public RealmsCreateRealmsWorldScreen(long worldId, RealmsScreen lastScreen) {
      this.worldId = worldId;
      this.lastScreen = lastScreen;
   }

   public void tick() {
      this.nameBox.tick();
      this.name = this.nameBox.getValue();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(newButton(CREATE_BUTTON, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")));
      this.buttonsAdd(newButton(CANCEL_BUTTON, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")));
      this.nameBox = this.newEditBox(NAME_BOX_ID, this.width() / 2 - 100, 65, 200, 20);
      this.nameBox.setFocus(true);
      if (this.name != null) {
         this.nameBox.setValue(this.name);
      }

      if (this.selectedWorldTemplate == null) {
         this.buttonsAdd(
            this.templateButton = newButton(WORLD_TEMPLATE_BUTTON, this.width() / 2 - 100, 107, 200, 20, getLocalizedString("mco.template.default.name"))
         );
      } else {
         this.buttonsAdd(
            this.templateButton = newButton(
               WORLD_TEMPLATE_BUTTON, this.width() / 2 - 100, 107, 200, 20, getLocalizedString("mco.template.name") + ": " + this.selectedWorldTemplate.name
            )
         );
      }

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
         } else if (button.id() == WORLD_TEMPLATE_BUTTON) {
            Realms.setScreen(new RealmsSelectWorldTemplateScreen(this, this.selectedWorldTemplate, false));
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      this.nameBox.keyPressed(ch, eventKey);
      if (eventKey == 15) {
         this.nameBox.setFocus(!this.nameBox.isFocused());
      }

      if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.templateButton);
      }

      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   private void createWorld() {
      if (this.valid()) {
         RealmsCreateRealmsWorldScreen.WorldCreationTask worldCreationTask = new RealmsCreateRealmsWorldScreen.WorldCreationTask(
            this.worldId, this.nameBox.getValue(), this.selectedWorldTemplate
         );
         RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, worldCreationTask);
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean valid() {
      this.error = this.nameBox.getValue() == null || this.nameBox.getValue().trim().equals("");
      return !this.error;
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.nameBox.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.selectServer.create"), this.width() / 2, 11, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
      if (this.error) {
         this.drawCenteredString(this.errorMessage, this.width() / 2, 167, 16711680);
      }

      this.nameBox.render();
      super.render(xm, ym, a);
   }

   public void callback(WorldTemplate worldTemplate) {
      this.selectedWorldTemplate = worldTemplate;
      Realms.setScreen(this);
   }

   class WorldCreationTask extends LongRunningTask {
      private final String name;
      private final WorldTemplate selectedWorldTemplate;
      private final long worldId;

      public WorldCreationTask(long worldId, String name, WorldTemplate selectedWorldTemplate) {
         this.worldId = worldId;
         this.name = name;
         this.selectedWorldTemplate = selectedWorldTemplate;
      }

      public void run() {
         String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
         this.setTitle(title);
         RealmsClient client = RealmsClient.createRealmsClient();

         try {
            if (this.selectedWorldTemplate != null) {
               client.initializeWorld(this.worldId, this.name, this.selectedWorldTemplate.id);
            } else {
               client.initializeWorld(this.worldId, this.name, "-1");
            }

            Realms.setScreen(RealmsCreateRealmsWorldScreen.this.lastScreen);
         } catch (RealmsServiceException var4) {
            RealmsCreateRealmsWorldScreen.LOGGER.error("Couldn't create world");
            this.error(var4.toString());
         } catch (UnsupportedEncodingException var5) {
            RealmsCreateRealmsWorldScreen.LOGGER.error("Couldn't create world");
            this.error(var5.getLocalizedMessage());
         } catch (IOException var6) {
            RealmsCreateRealmsWorldScreen.LOGGER.error("Could not parse response creating world");
            this.error(var6.getLocalizedMessage());
         } catch (Exception var7) {
            RealmsCreateRealmsWorldScreen.LOGGER.error("Could not create world");
            this.error(var7.getLocalizedMessage());
         }

      }
   }
}
