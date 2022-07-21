package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String LINK_ICON = "realms:textures/gui/realms/link_icons.png";
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private final RealmsScreenWithCallback<WorldTemplate> lastScreen;
   private WorldTemplate selectedWorldTemplate;
   private List<WorldTemplate> templates = Collections.emptyList();
   private RealmsSelectWorldTemplateScreen.WorldTemplateSelectionList worldTemplateSelectionList;
   private int selectedTemplate = -1;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_SELECT_ID = 1;
   private RealmsButton selectButton;
   private String toolTip = null;
   private String currentLink = null;
   private boolean isMiniGame;
   private int clicks = 0;

   public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback<WorldTemplate> lastScreen, WorldTemplate selectedWorldTemplate, boolean isMiniGame) {
      this.lastScreen = lastScreen;
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
      this.worldTemplateSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateSelectionList();
      final boolean isMiniGame = this.isMiniGame;
      (new Thread("Realms-minigame-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               if (isMiniGame) {
                  RealmsSelectWorldTemplateScreen.this.templates = client.fetchMinigames().templates;
               } else {
                  RealmsSelectWorldTemplateScreen.this.templates = client.fetchWorldTemplates().templates;
               }
            } catch (RealmsServiceException var3) {
               RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates");
            }

         }
      }).start();
      this.postInit();
   }

   private void postInit() {
      this.buttonsAdd(newButton(0, this.width() / 2 + 6, this.height() - 48, 153, 20, getLocalizedString("gui.cancel")));
      this.buttonsAdd(this.selectButton = newButton(1, this.width() / 2 - 154, this.height() - 48, 153, 20, getLocalizedString("mco.template.button.select")));
      this.selectButton.active(false);
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         switch(button.id()) {
            case 0:
               this.backButtonClicked();
               break;
            case 1:
               this.selectTemplate();
               break;
            default:
               return;
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
         WorldTemplate template = (WorldTemplate)this.templates.get(this.selectedTemplate);
         template.setMinigame(this.isMiniGame);
         this.lastScreen.callback(template);
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.currentLink = null;
      this.renderBackground();
      this.worldTemplateSelectionList.render(xm, ym, a);
      this.drawCenteredString(
         this.isMiniGame ? getLocalizedString("mco.template.title.minigame") : getLocalizedString("mco.template.title"), this.width() / 2, 13, 16777215
      );
      super.render(xm, ym, a);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private class WorldTemplateSelectionList extends RealmsClickableScrolledSelectionList {
      public WorldTemplateSelectionList() {
         super(
            RealmsSelectWorldTemplateScreen.this.width(),
            RealmsSelectWorldTemplateScreen.this.height(),
            32,
            RealmsSelectWorldTemplateScreen.this.height() - 64,
            46
         );
      }

      public int getItemCount() {
         return RealmsSelectWorldTemplateScreen.this.templates.size() + 1;
      }

      public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
         if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
            int x0 = this.width() / 2 - 150;
            int x1 = this.width();
            int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
            int slot = clickSlotPos / itemHeight;
            if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
               if (slot >= RealmsSelectWorldTemplateScreen.this.templates.size()) {
                  return;
               }

               RealmsSelectWorldTemplateScreen.this.selectButton.active(true);
               RealmsSelectWorldTemplateScreen.this.selectedTemplate = slot;
               RealmsSelectWorldTemplateScreen.this.selectedWorldTemplate = null;
               RealmsSelectWorldTemplateScreen.this.clicks += RealmsSharedConstants.TICKS_PER_SECOND / 3 + 1;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= RealmsSharedConstants.TICKS_PER_SECOND / 2) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }
            }
         }

      }

      public boolean isSelectedItem(int item) {
         if (RealmsSelectWorldTemplateScreen.this.templates.size() == 0) {
            return false;
         } else if (item >= RealmsSelectWorldTemplateScreen.this.templates.size()) {
            return false;
         } else if (RealmsSelectWorldTemplateScreen.this.selectedWorldTemplate != null) {
            boolean same = RealmsSelectWorldTemplateScreen.this.selectedWorldTemplate
               .name
               .equals(((WorldTemplate)RealmsSelectWorldTemplateScreen.this.templates.get(item)).name);
            if (same) {
               RealmsSelectWorldTemplateScreen.this.selectedTemplate = item;
            }

            return same;
         } else {
            return item == RealmsSelectWorldTemplateScreen.this.selectedTemplate;
         }
      }

      public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
         if (slot < RealmsSelectWorldTemplateScreen.this.templates.size()) {
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               RealmsUtil.browseTo(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

         }
      }

      public int getMaxPosition() {
         return (this.getItemCount() - 1) * 46;
      }

      public void renderBackground() {
         RealmsSelectWorldTemplateScreen.this.renderBackground();
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         if (i < RealmsSelectWorldTemplateScreen.this.templates.size()) {
            this.renderWorldTemplateItem(i, x, y, h);
         }

      }

      public int getScrollbarPosition() {
         return super.getScrollbarPosition() + 30;
      }

      public void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = this.getScrollbarPosition() - 290;
         int x1 = this.getScrollbarPosition() - 10;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3553);
         t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
         t.vertex((double)x0, (double)(y + h + 2), 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y + h + 2), 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y - 2), 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x0, (double)(y - 2), 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y + h + 1), 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y + h + 1), 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y - 1), 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y - 1), 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.end();
         GL11.glEnable(3553);
      }

      private void renderWorldTemplateItem(int i, int x, int y, int h) {
         WorldTemplate worldTemplate = (WorldTemplate)RealmsSelectWorldTemplateScreen.this.templates.get(i);
         int textStart = x + 20;
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.name, textStart, y + 2, 16777215);
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.author, textStart, y + 15, 7105644);
         RealmsSelectWorldTemplateScreen.this.drawString(
            worldTemplate.version, textStart + 227 - RealmsSelectWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 7105644
         );
         int dx = this.getScrollbarPosition() - 250 + RealmsSelectWorldTemplateScreen.this.fontWidth(worldTemplate.author) + 12;
         int dy = 13;
         if (!worldTemplate.link.equals("")) {
            this.drawInfo(dx, y + dy, this.xm(), this.ym(), worldTemplate.link);
         }

         this.drawImage(x - 25, y + 1, this.xm(), this.ym(), worldTemplate);
      }

      private void drawImage(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38.0F, 38.0F);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 40, 40, 40.0F, 40.0F);
      }

      private void drawInfo(int x, int y, int xm, int ym, String link) {
         boolean hovered = false;
         if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 9 && ym < RealmsSelectWorldTemplateScreen.this.height() - 15 && ym > 32) {
            hovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/link_icons.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y - 3, hovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
         GL11.glPopMatrix();
         if (hovered) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
            RealmsSelectWorldTemplateScreen.this.currentLink = link;
         }

      }
   }
}
