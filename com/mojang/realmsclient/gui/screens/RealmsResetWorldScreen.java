package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class RealmsResetWorldScreen extends RealmsScreenWithCallback<WorldTemplate> {
   private static final Logger LOGGER = LogManager.getLogger();
   private RealmsScreen lastScreen;
   private RealmsServer serverData;
   private RealmsScreen returnScreen;
   private String title = getLocalizedString("mco.reset.world.title");
   private String subtitle = getLocalizedString("mco.reset.world.warning");
   private String buttonTitle = getLocalizedString("gui.cancel");
   private int subtitleColor = 16711680;
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private static final String UPLOAD_LOCATION = "realms:textures/gui/realms/upload.png";
   private static final String ADVENTURE_MAP_LOCATION = "realms:textures/gui/realms/adventure.png";
   private static final String SURVIVAL_SPAWN_LOCATION = "realms:textures/gui/realms/survival_spawn.png";
   private static final String NEW_WORLD_LOCATION = "realms:textures/gui/realms/new_world.png";
   private final int BUTTON_CANCEL_ID = 0;
   private final WorldTemplatePaginatedList templates = new WorldTemplatePaginatedList();
   private final WorldTemplatePaginatedList adventuremaps = new WorldTemplatePaginatedList();
   private RealmsResetWorldScreen.ResetType selectedType = RealmsResetWorldScreen.ResetType.NONE;
   public int slot = -1;
   private RealmsResetWorldScreen.ResetType typeToReset = RealmsResetWorldScreen.ResetType.NONE;
   private RealmsResetWorldScreen.ResetWorldInfo worldInfoToReset = null;
   private WorldTemplate worldTemplateToReset = null;
   private String resetTitle = null;
   private int confirmationId = -1;

   public RealmsResetWorldScreen(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen) {
      this.lastScreen = lastScreen;
      this.serverData = serverData;
      this.returnScreen = returnScreen;
   }

   public RealmsResetWorldScreen(
      RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen, String title, String subtitle, int subtitleColor, String buttonTitle
   ) {
      this(lastScreen, serverData, returnScreen);
      this.title = title;
      this.subtitle = subtitle;
      this.subtitleColor = subtitleColor;
      this.buttonTitle = buttonTitle;
   }

   public void setConfirmationId(int confirmationId) {
      this.confirmationId = confirmationId;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public void setResetTitle(String title) {
      this.resetTitle = title;
   }

   public void init() {
      this.buttonsClear();
      this.buttonsAdd(newButton(0, this.width() / 2 - 40, RealmsConstants.row(14) - 10, 80, 20, this.buttonTitle));
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsResetWorldScreen.this.templates.set(client.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL));
               RealmsResetWorldScreen.this.adventuremaps.set(client.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP));
            } catch (RealmsServiceException var3) {
               RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", var3);
            }

         }
      }).start();
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
         if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
         }

      }
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      switch(this.selectedType) {
         case NONE:
            break;
         case GENERATE:
            Realms.setScreen(new RealmsResetNormalWorldScreen(this, this.title));
            break;
         case UPLOAD:
            Realms.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this));
            break;
         case ADVENTURE:
            RealmsSelectWorldTemplateScreen screen = new RealmsSelectWorldTemplateScreen(
               this, null, RealmsServer.WorldType.ADVENTUREMAP, new WorldTemplatePaginatedList(this.adventuremaps)
            );
            screen.setTitle(getLocalizedString("mco.reset.world.adventure"));
            Realms.setScreen(screen);
            break;
         case SURVIVAL_SPAWN:
            RealmsSelectWorldTemplateScreen templateScreen = new RealmsSelectWorldTemplateScreen(
               this, null, RealmsServer.WorldType.NORMAL, new WorldTemplatePaginatedList(this.templates)
            );
            templateScreen.setTitle(getLocalizedString("mco.reset.world.template"));
            Realms.setScreen(templateScreen);
            break;
         default:
            return;
      }

   }

   private int frame(int i) {
      return this.width() / 2 - 80 + (i - 1) * 100;
   }

   public void render(int xm, int ym, float a) {
      this.selectedType = RealmsResetWorldScreen.ResetType.NONE;
      this.renderBackground();
      this.drawCenteredString(this.title, this.width() / 2, 7, 16777215);
      this.drawCenteredString(this.subtitle, this.width() / 2, 22, this.subtitleColor);
      this.drawFrame(
         this.frame(1),
         RealmsConstants.row(0) + 10,
         xm,
         ym,
         getLocalizedString("mco.reset.world.generate"),
         -1L,
         "realms:textures/gui/realms/new_world.png",
         RealmsResetWorldScreen.ResetType.GENERATE
      );
      this.drawFrame(
         this.frame(2),
         RealmsConstants.row(0) + 10,
         xm,
         ym,
         getLocalizedString("mco.reset.world.upload"),
         -1L,
         "realms:textures/gui/realms/upload.png",
         RealmsResetWorldScreen.ResetType.UPLOAD
      );
      this.drawFrame(
         this.frame(1),
         RealmsConstants.row(6) + 20,
         xm,
         ym,
         getLocalizedString("mco.reset.world.adventure"),
         -1L,
         "realms:textures/gui/realms/adventure.png",
         RealmsResetWorldScreen.ResetType.ADVENTURE
      );
      this.drawFrame(
         this.frame(2),
         RealmsConstants.row(6) + 20,
         xm,
         ym,
         getLocalizedString("mco.reset.world.template"),
         -1L,
         "realms:textures/gui/realms/survival_spawn.png",
         RealmsResetWorldScreen.ResetType.SURVIVAL_SPAWN
      );
      super.render(xm, ym, a);
   }

   private void drawFrame(int x, int y, int xm, int ym, String text, long imageId, String image, RealmsResetWorldScreen.ResetType resetType) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 60 && ym >= y - 12 && ym <= y + 60) {
         hovered = true;
         this.selectedType = resetType;
      }

      if (imageId != -1L) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else {
         bind(image);
      }

      if (hovered) {
         GL11.glColor4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RealmsScreen.blit(x + 2, y + 2, 0.0F, 0.0F, 56, 56, 56.0F, 56.0F);
      bind("realms:textures/gui/realms/slot_frame.png");
      if (hovered) {
         GL11.glColor4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RealmsScreen.blit(x, y, 0.0F, 0.0F, 60, 60, 60.0F, 60.0F);
      this.drawCenteredString(text, x + 30, y - 12, hovered ? 10526880 : 16777215);
   }

   void callback(WorldTemplate worldTemplate) {
      if (worldTemplate != null) {
         if (this.slot != -1) {
            this.typeToReset = worldTemplate.recommendedPlayers.equals("")
               ? RealmsResetWorldScreen.ResetType.SURVIVAL_SPAWN
               : RealmsResetWorldScreen.ResetType.ADVENTURE;
            this.worldTemplateToReset = worldTemplate;
            this.switchSlot();
         } else {
            this.resetWorldWithTemplate(worldTemplate);
         }
      }

   }

   private void switchSlot() {
      this.switchSlot(this);
   }

   public void switchSlot(RealmsScreen screen) {
      RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, this.slot, screen, 100);
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void confirmResult(boolean result, int id) {
      if (id == 100 && result) {
         switch(this.typeToReset) {
            case GENERATE:
               if (this.worldInfoToReset != null) {
                  this.triggerResetWorld(this.worldInfoToReset);
               }
               break;
            case UPLOAD:
            default:
               return;
            case ADVENTURE:
            case SURVIVAL_SPAWN:
               if (this.worldTemplateToReset != null) {
                  this.resetWorldWithTemplate(this.worldTemplateToReset);
               }
         }

      } else {
         if (result) {
            Realms.setScreen(this.returnScreen);
            if (this.confirmationId != -1) {
               this.returnScreen.confirmResult(true, this.confirmationId);
            }
         }

      }
   }

   public void resetWorldWithTemplate(WorldTemplate template) {
      RealmsTasks.ResettingWorldTask resettingWorldTask = new RealmsTasks.ResettingWorldTask(this.serverData.id, this.returnScreen, template);
      if (this.resetTitle != null) {
         resettingWorldTask.setResetTitle(this.resetTitle);
      }

      if (this.confirmationId != -1) {
         resettingWorldTask.setConfirmationId(this.confirmationId);
      }

      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, resettingWorldTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void resetWorld(RealmsResetWorldScreen.ResetWorldInfo resetWorldInfo) {
      if (this.slot != -1) {
         this.typeToReset = RealmsResetWorldScreen.ResetType.GENERATE;
         this.worldInfoToReset = resetWorldInfo;
         this.switchSlot();
      } else {
         this.triggerResetWorld(resetWorldInfo);
      }

   }

   private void triggerResetWorld(RealmsResetWorldScreen.ResetWorldInfo resetWorldInfo) {
      RealmsTasks.ResettingWorldTask resettingWorldTask = new RealmsTasks.ResettingWorldTask(
         this.serverData.id, this.returnScreen, resetWorldInfo.seed, resetWorldInfo.levelType, resetWorldInfo.generateStructures
      );
      if (this.resetTitle != null) {
         resettingWorldTask.setResetTitle(this.resetTitle);
      }

      if (this.confirmationId != -1) {
         resettingWorldTask.setConfirmationId(this.confirmationId);
      }

      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, resettingWorldTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   static enum ResetType {
      NONE,
      GENERATE,
      UPLOAD,
      ADVENTURE,
      SURVIVAL_SPAWN;
   }

   public static class ResetWorldInfo {
      String seed;
      int levelType;
      boolean generateStructures;

      public ResetWorldInfo(String seed, int levelType, boolean generateStructures) {
         this.seed = seed;
         this.levelType = levelType;
         this.generateStructures = generateStructures;
      }
   }
}
