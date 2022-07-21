package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsWorldTemplateScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String LINK_ICON = "realms:textures/gui/realms/link_icons.png";
   private final RealmsScreenWithCallback<WorldTemplate> lastScreen;
   private WorldTemplate selectedWorldTemplate;
   private List<WorldTemplate> templates = Collections.emptyList();
   private RealmsWorldTemplateScreen.WorldTemplateSelectionList worldTemplateSelectionList;
   private int selectedTemplate = -1;
   private static final int BACK_BUTTON_ID = 0;
   private static final int SELECT_BUTTON_ID = 1;
   private RealmsButton selectButton;
   private String toolTip = null;
   private boolean isMiniGame;

   public RealmsWorldTemplateScreen(RealmsScreenWithCallback<WorldTemplate> configureWorldScreen, WorldTemplate selectedWorldTemplate, boolean isMiniGame) {
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
      this.worldTemplateSelectionList = new RealmsWorldTemplateScreen.WorldTemplateSelectionList();
      final boolean isMiniGame = this.isMiniGame;
      (new Thread("Realms-minigame-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               if (isMiniGame) {
                  RealmsWorldTemplateScreen.this.templates = client.fetchMinigames().templates;
               } else {
                  RealmsWorldTemplateScreen.this.templates = client.fetchWorldTemplates().templates;
               }
            } catch (RealmsServiceException var3) {
               RealmsWorldTemplateScreen.LOGGER.error("Couldn't fetch templates");
            }

         }
      }).start();
      this.postInit();
   }

   private void postInit() {
      this.buttonsAdd(newButton(0, this.width() / 2 + 6, this.height() - 52, 153, 20, getLocalizedString("gui.cancel")));
      this.buttonsAdd(this.selectButton = newButton(1, this.width() / 2 - 154, this.height() - 52, 153, 20, getLocalizedString("mco.template.button.select")));
      this.selectButton.active(false);
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
      this.toolTip = null;
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
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

   }

   private void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var5) {
         LOGGER.error("Couldn't open link");
      }

   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, -1);
      }
   }

   private class WorldTemplateSelectionList extends RealmsClickableScrolledSelectionList {
      public WorldTemplateSelectionList() {
         super(RealmsWorldTemplateScreen.this.width(), RealmsWorldTemplateScreen.this.height(), 32, RealmsWorldTemplateScreen.this.height() - 64, 36);
      }

      public int getItemCount() {
         return RealmsWorldTemplateScreen.this.templates.size() + 1;
      }

      public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
         if (item < RealmsWorldTemplateScreen.this.templates.size()) {
            RealmsWorldTemplateScreen.this.selectButton.active(true);
            RealmsWorldTemplateScreen.this.selectedTemplate = item;
            RealmsWorldTemplateScreen.this.selectedWorldTemplate = null;
            if (doubleClick) {
               RealmsWorldTemplateScreen.this.selectTemplate();
            }

         }
      }

      public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
         if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
            int slot = clickSlotPos / itemHeight;
            if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
            }
         }

      }

      public boolean isSelectedItem(int item) {
         if (RealmsWorldTemplateScreen.this.templates.size() == 0) {
            return false;
         } else if (item >= RealmsWorldTemplateScreen.this.templates.size()) {
            return false;
         } else if (RealmsWorldTemplateScreen.this.selectedWorldTemplate != null) {
            boolean same = RealmsWorldTemplateScreen.this.selectedWorldTemplate
               .name
               .equals(((WorldTemplate)RealmsWorldTemplateScreen.this.templates.get(item)).name);
            if (same) {
               RealmsWorldTemplateScreen.this.selectedTemplate = item;
            }

            return same;
         } else {
            return item == RealmsWorldTemplateScreen.this.selectedTemplate;
         }
      }

      public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
         if (slot < RealmsWorldTemplateScreen.this.templates.size()) {
            int dx = this.getScrollbarPosition()
               - 240
               + RealmsWorldTemplateScreen.this.fontWidth(((WorldTemplate)RealmsWorldTemplateScreen.this.templates.get(slot)).author)
               + 12;
            int dy = clickSlotPos - slot * 36 - this.getScroll();
            if (xm >= dx
               && xm <= dx + 15
               && dy >= 12
               && dy <= 24
               && ym < RealmsWorldTemplateScreen.this.height() - 15
               && ym > 32
               && !((WorldTemplate)RealmsWorldTemplateScreen.this.templates.get(slot)).link.equals("")) {
               RealmsWorldTemplateScreen.this.browseTo(((WorldTemplate)RealmsWorldTemplateScreen.this.templates.get(slot)).link);
            }

         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         RealmsWorldTemplateScreen.this.renderBackground();
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         if (i < RealmsWorldTemplateScreen.this.templates.size()) {
            this.renderWorldTemplateItem(i, x, y, h);
         }

      }

      public void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = this.getScrollbarPosition() - 240;
         int x1 = this.getScrollbarPosition() - 10;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3553);
         t.begin();
         t.color(8421504);
         t.vertexUV((double)x0, (double)(y + h + 2), 0.0, 0.0, 1.0);
         t.vertexUV((double)x1, (double)(y + h + 2), 0.0, 1.0, 1.0);
         t.vertexUV((double)x1, (double)(y - 2), 0.0, 1.0, 0.0);
         t.vertexUV((double)x0, (double)(y - 2), 0.0, 0.0, 0.0);
         t.color(0);
         t.vertexUV((double)(x0 + 1), (double)(y + h + 1), 0.0, 0.0, 1.0);
         t.vertexUV((double)(x1 - 1), (double)(y + h + 1), 0.0, 1.0, 1.0);
         t.vertexUV((double)(x1 - 1), (double)(y - 1), 0.0, 1.0, 0.0);
         t.vertexUV((double)(x0 + 1), (double)(y - 1), 0.0, 0.0, 0.0);
         t.end();
         GL11.glEnable(3553);
      }

      private void renderWorldTemplateItem(int i, int x, int y, int h) {
         WorldTemplate worldTemplate = (WorldTemplate)RealmsWorldTemplateScreen.this.templates.get(i);
         RealmsWorldTemplateScreen.this.drawString(worldTemplate.name, x + 2, y + 1, 16777215);
         RealmsWorldTemplateScreen.this.drawString(worldTemplate.author, x + 2, y + 14, 7105644);
         RealmsWorldTemplateScreen.this.drawString(
            worldTemplate.version, x + 2 + 207 - RealmsWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 5000268
         );
         int dx = this.getScrollbarPosition() - 240 + RealmsWorldTemplateScreen.this.fontWidth(worldTemplate.author) + 12;
         int dy = 12;
         if (!worldTemplate.link.equals("")) {
            this.drawInfo(dx, y + dy, this.xm(), this.ym());
         }

      }

      private void drawInfo(int x, int y, int xm, int ym) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 9 && ym < RealmsWorldTemplateScreen.this.height() - 15 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/link_icons.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y - 3, hovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
         GL11.glPopMatrix();
         if (hovered) {
            RealmsWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
         }

      }
   }
}
