package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.Collections;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsScrolledSelectionList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class McoWorldTemplateScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ScreenWithCallback<WorldTemplate> lastScreen;
   private WorldTemplate selectedWorldTemplate;
   private List<WorldTemplate> templates = Collections.emptyList();
   private McoWorldTemplateScreen.WorldTemplateSelectionList worldTemplateSelectionList;
   private int selectedTemplate = -1;
   private static final int BACK_BUTTON_ID = 0;
   private static final int SELECT_BUTTON_ID = 1;
   private RealmsButton selectButton;
   private boolean isMiniGame;

   public McoWorldTemplateScreen(ScreenWithCallback<WorldTemplate> configureWorldScreen, WorldTemplate selectedWorldTemplate, boolean isMiniGame) {
      this.lastScreen = configureWorldScreen;
      this.selectedWorldTemplate = selectedWorldTemplate;
      this.isMiniGame = isMiniGame;
   }

   public void mouseEvent() {
      super.mouseEvent();
      this.worldTemplateSelectionList.mouseEvent();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.worldTemplateSelectionList = new McoWorldTemplateScreen.WorldTemplateSelectionList();
      final boolean isMiniGame = this.isMiniGame;
      (new Thread("Realms-minigame-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               if (isMiniGame) {
                  McoWorldTemplateScreen.this.templates = client.fetchMinigames().templates;
               } else {
                  McoWorldTemplateScreen.this.templates = client.fetchWorldTemplates().templates;
               }
            } catch (RealmsServiceException var3) {
               McoWorldTemplateScreen.LOGGER.error("Couldn't fetch templates");
            }

         }
      }).start();
      this.postInit();
   }

   private void postInit() {
      this.buttonsAdd(newButton(0, this.width() / 2 + 6, this.height() - 52, 153, 20, getLocalizedString("gui.cancel")));
      this.buttonsAdd(this.selectButton = newButton(1, this.width() / 2 - 154, this.height() - 52, 153, 20, getLocalizedString("mco.template.button.select")));
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            this.selectTemplate();
         } else if (button.id() == 0) {
            this.backButtonClicked();
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         this.backButtonClicked();
      }

   }

   private void backButtonClicked() {
      this.lastScreen.callback(null);
      Realms.setScreen(this.lastScreen);
   }

   private void selectTemplate() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.templates.size()) {
         this.lastScreen.callback((WorldTemplate)this.templates.get(this.selectedTemplate));
         Realms.setScreen(this.lastScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.worldTemplateSelectionList.render(xm, ym, a);
      String title = "";
      if (this.isMiniGame) {
         title = getLocalizedString("mco.template.title.minigame");
      } else {
         title = getLocalizedString("mco.template.title");
      }

      this.drawCenteredString(title, this.width() / 2, 20, 16777215);
      super.render(xm, ym, a);
   }

   private class WorldTemplateSelectionList extends RealmsScrolledSelectionList {
      public WorldTemplateSelectionList() {
         super(McoWorldTemplateScreen.this.width(), McoWorldTemplateScreen.this.height(), 32, McoWorldTemplateScreen.this.height() - 64, 36);
      }

      public int getItemCount() {
         return McoWorldTemplateScreen.this.templates.size() + 1;
      }

      public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
         if (item < McoWorldTemplateScreen.this.templates.size()) {
            McoWorldTemplateScreen.this.selectedTemplate = item;
            McoWorldTemplateScreen.this.selectedWorldTemplate = null;
         }
      }

      public boolean isSelectedItem(int item) {
         if (McoWorldTemplateScreen.this.templates.size() == 0) {
            return false;
         } else if (item >= McoWorldTemplateScreen.this.templates.size()) {
            return false;
         } else if (McoWorldTemplateScreen.this.selectedWorldTemplate != null) {
            boolean same = McoWorldTemplateScreen.this.selectedWorldTemplate.name.equals(((WorldTemplate)McoWorldTemplateScreen.this.templates.get(item)).name);
            if (same) {
               McoWorldTemplateScreen.this.selectedTemplate = item;
            }

            return same;
         } else {
            return item == McoWorldTemplateScreen.this.selectedTemplate;
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         McoWorldTemplateScreen.this.renderBackground();
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         if (i < McoWorldTemplateScreen.this.templates.size()) {
            this.renderWorldTemplateItem(i, x, y, h);
         }

      }

      private void renderWorldTemplateItem(int i, int x, int y, int h) {
         WorldTemplate worldTemplate = (WorldTemplate)McoWorldTemplateScreen.this.templates.get(i);
         McoWorldTemplateScreen.this.drawString(worldTemplate.name, x + 2, y + 1, 16777215);
         McoWorldTemplateScreen.this.drawString(worldTemplate.author, x + 2, y + 12, 7105644);
         McoWorldTemplateScreen.this.drawString(
            worldTemplate.version, x + 2 + 207 - McoWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 5000268
         );
      }
   }
}
