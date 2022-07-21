package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.ArrayList;
import java.util.Collection;
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
   private static final String TRAILER_ICON = "realms:textures/gui/realms/trailer_icons.png";
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private final RealmsScreenWithCallback<WorldTemplate> lastScreen;
   private WorldTemplate selectedWorldTemplate;
   private final List<WorldTemplate> templates = new ArrayList();
   private RealmsSelectWorldTemplateScreen.WorldTemplateSelectionList worldTemplateSelectionList;
   private int selectedTemplate = -1;
   private String title;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_SELECT_ID = 1;
   private RealmsButton selectButton;
   private String toolTip;
   private String currentLink;
   private final RealmsServer.WorldType worldType;
   private int clicks;
   private String warning;
   private String warningURL;
   private boolean displayWarning;
   private boolean hoverWarning;
   private boolean prepopulated;
   private WorldTemplatePaginatedList paginatedList;
   private boolean loading;
   private boolean stopLoadingTemplates;

   public RealmsSelectWorldTemplateScreen(
      RealmsScreenWithCallback<WorldTemplate> lastScreen, WorldTemplate selectedWorldTemplate, RealmsServer.WorldType worldType
   ) {
      this.lastScreen = lastScreen;
      this.selectedWorldTemplate = selectedWorldTemplate;
      this.worldType = worldType;
      this.title = getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "mco.template.title.minigame" : "mco.template.title");
      if (this.paginatedList == null) {
         this.paginatedList = new WorldTemplatePaginatedList();
         this.paginatedList.size = 10;
      }

      if (this.paginatedList.size == 0) {
         this.paginatedList.size = 10;
      }

   }

   public RealmsSelectWorldTemplateScreen(
      RealmsScreenWithCallback<WorldTemplate> lastScreen,
      WorldTemplate selectedWorldTemplate,
      RealmsServer.WorldType worldType,
      WorldTemplatePaginatedList list
   ) {
      this(lastScreen, selectedWorldTemplate, worldType);
      this.prepopulated = true;
      this.templates.addAll((Collection)(list == null ? new ArrayList() : list.templates));
      this.paginatedList = list;
      if (this.paginatedList.size == 0) {
         this.paginatedList.size = 10;
      }

   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setWarning(String string) {
      this.warning = string;
      this.displayWarning = true;
   }

   public void setWarningURL(String string) {
      this.warningURL = string;
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      if (this.hoverWarning && this.warningURL != null) {
         RealmsUtil.browseTo("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
      }

   }

   public void mouseEvent() {
      super.mouseEvent();
      this.worldTemplateSelectionList.mouseEvent();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.worldTemplateSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateSelectionList();
      if (!this.prepopulated && this.templates.isEmpty()) {
         this.paginatedList.page = 0;
         this.paginatedList.size = 10;
         this.fetchMoreTemplatesAsync();
      }

      this.buttonsAdd(
         newButton(
            0,
            this.width() / 2 + 6,
            this.height() - 32,
            153,
            20,
            getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back")
         )
      );
      this.buttonsAdd(this.selectButton = newButton(1, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.template.button.select")));
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
      switch(eventKey) {
         case 1:
            this.backButtonClicked();
            break;
         case 28:
         case 156:
            this.selectTemplate();
            break;
         case 200:
            if (this.selectedTemplate != -1) {
               int theIndex = this.selectedTemplate;
               if (theIndex == 0) {
                  this.worldTemplateSelectionList.scroll(0 - this.worldTemplateSelectionList.getScroll());
                  return;
               }

               int newIndex = theIndex - 1;
               if (newIndex > -1) {
                  this.selectedTemplate = newIndex;
                  int maxScroll = Math.max(
                     0, this.worldTemplateSelectionList.getMaxPosition() - (this.height() - 40 - (this.displayWarning ? RealmsConstants.row(1) : 32) - 4)
                  );
                  int maxItemsInView = (int)Math.floor((double)((this.height() - 40 - (this.displayWarning ? RealmsConstants.row(1) : 32)) / 46));
                  int scroll = this.worldTemplateSelectionList.getScroll();
                  int hiddenItems = (int)Math.ceil((double)((float)scroll / 46.0F));
                  int scrollPerItem = maxScroll / this.templates.size();
                  int positionNeeded = scrollPerItem * newIndex;
                  int proposedScroll = positionNeeded - this.worldTemplateSelectionList.getScroll();
                  if (newIndex < hiddenItems || newIndex > hiddenItems + maxItemsInView) {
                     this.worldTemplateSelectionList.scroll(proposedScroll);
                  }

                  return;
               }
            }

            this.selectedTemplate = 0;
            this.worldTemplateSelectionList.scroll(0 - this.worldTemplateSelectionList.getScroll());
            break;
         case 208:
            if (this.selectedTemplate != -1) {
               int theIndex = this.selectedTemplate;
               int maxScroll = Math.max(
                  0, this.worldTemplateSelectionList.getMaxPosition() - (this.height() - 40 - (this.displayWarning ? RealmsConstants.row(1) : 32))
               );
               if (theIndex == this.templates.size() - 1) {
                  this.worldTemplateSelectionList.scroll(maxScroll - this.worldTemplateSelectionList.getScroll() + 46);
                  return;
               }

               int newIndex = theIndex + 1;
               if (newIndex == this.templates.size() - 1) {
                  this.selectedTemplate = newIndex;
                  this.worldTemplateSelectionList.scroll(maxScroll - this.worldTemplateSelectionList.getScroll() + 46);
                  return;
               }

               if (newIndex < this.templates.size()) {
                  this.selectedTemplate = newIndex;
                  int maxItemsInView = (int)Math.floor((double)((this.height() - 40 - (this.displayWarning ? RealmsConstants.row(1) : 32)) / 46));
                  int scroll = this.worldTemplateSelectionList.getScroll();
                  int hiddenItems = (int)Math.ceil((double)((float)scroll / 46.0F));
                  int scrollPerItem = maxScroll / this.templates.size();
                  int positionNeeded = scrollPerItem * newIndex;
                  int proposedScroll = positionNeeded - this.worldTemplateSelectionList.getScroll();
                  if (proposedScroll > 0) {
                     proposedScroll += scrollPerItem;
                  }

                  if (newIndex < hiddenItems || newIndex >= hiddenItems + maxItemsInView) {
                     this.worldTemplateSelectionList.scroll(proposedScroll);
                  }

                  return;
               }
            }

            this.selectedTemplate = 0;
            this.worldTemplateSelectionList.scroll(-(this.worldTemplateSelectionList.getItemCount() * 46));
      }

   }

   private void backButtonClicked() {
      this.lastScreen.callback(null);
      Realms.setScreen(this.lastScreen);
   }

   private void selectTemplate() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.templates.size()) {
         WorldTemplate template = (WorldTemplate)this.templates.get(this.selectedTemplate);
         this.lastScreen.callback(template);
      }

   }

   private void fetchMoreTemplatesAsync() {
      if (!this.loading && !this.stopLoadingTemplates) {
         this.loading = true;
         (new Thread("realms-template-fetcher") {
               public void run() {
                  try {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     RealmsSelectWorldTemplateScreen.this.paginatedList = client.fetchWorldTemplates(
                        RealmsSelectWorldTemplateScreen.this.paginatedList.page + 1,
                        RealmsSelectWorldTemplateScreen.this.paginatedList.size,
                        RealmsSelectWorldTemplateScreen.this.worldType
                     );
                     RealmsSelectWorldTemplateScreen.this.templates.addAll(RealmsSelectWorldTemplateScreen.this.paginatedList.templates);
                     if (RealmsSelectWorldTemplateScreen.this.paginatedList.templates.size() == 0) {
                        RealmsSelectWorldTemplateScreen.this.stopLoadingTemplates = true;
                     }
                  } catch (RealmsServiceException var5) {
                     RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates");
                     RealmsSelectWorldTemplateScreen.this.stopLoadingTemplates = true;
                  } finally {
                     RealmsSelectWorldTemplateScreen.this.loading = false;
                  }
   
               }
            })
            .start();
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.currentLink = null;
      this.hoverWarning = false;
      if (!this.paginatedList.isLastPage()) {
         this.fetchMoreTemplatesAsync();
      }

      this.renderBackground();
      this.worldTemplateSelectionList.render(xm, ym, a);
      this.drawCenteredString(this.title, this.width() / 2, 13, 16777215);
      if (this.displayWarning) {
         String[] lines = this.warning.split("\\\\n");

         for(int index = 0; index < lines.length; ++index) {
            int fontWidth = this.fontWidth(lines[index]);
            int offsetX = this.width() / 2 - fontWidth / 2;
            int offsetY = RealmsConstants.row(-1 + index);
            if (xm >= offsetX && xm <= offsetX + fontWidth && ym >= offsetY && ym <= offsetY + this.fontLineHeight()) {
               this.hoverWarning = true;
            }
         }

         for(int index = 0; index < lines.length; ++index) {
            String line = lines[index];
            int warningColor = 10526880;
            if (this.warningURL != null) {
               if (this.hoverWarning) {
                  warningColor = 7107012;
                  line = "§n" + line;
               } else {
                  warningColor = 3368635;
               }
            }

            this.drawCenteredString(line, this.width() / 2, RealmsConstants.row(-1 + index), warningColor);
         }
      }

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
            RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsConstants.row(1) : 32,
            RealmsSelectWorldTemplateScreen.this.height() - 40,
            46
         );
      }

      public int getItemCount() {
         return RealmsSelectWorldTemplateScreen.this.templates.size();
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
               RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + RealmsSharedConstants.TICKS_PER_SECOND / 3 + 1;
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
         return this.getItemCount() * 46;
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
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.drawIcons(textStart - 1, y + 25, this.xm(), this.ym(), worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
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

      private void drawIcons(int x, int y, int xm, int ym, String link, String trailerLink, String recommendedPlayers) {
         if (!"".equals(recommendedPlayers)) {
            RealmsSelectWorldTemplateScreen.this.drawString(recommendedPlayers, x, y + 4, 5000268);
         }

         int offset = "".equals(recommendedPlayers) ? 0 : RealmsSelectWorldTemplateScreen.this.fontWidth(recommendedPlayers) + 2;
         boolean linkHovered = false;
         boolean trailerHovered = false;
         if (xm >= x + offset && xm <= x + offset + 32 && ym >= y && ym <= y + 15 && ym < RealmsSelectWorldTemplateScreen.this.height() - 15 && ym > 32) {
            if (xm <= x + 15 + offset && xm > offset) {
               if ("".equals(link)) {
                  trailerHovered = true;
               } else {
                  linkHovered = true;
               }
            } else if (!"".equals(link)) {
               trailerHovered = true;
            }
         }

         if (!"".equals(link)) {
            RealmsScreen.bind("realms:textures/gui/realms/link_icons.png");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + offset, y, linkHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
            GL11.glPopMatrix();
         }

         if (!"".equals(trailerLink)) {
            RealmsScreen.bind("realms:textures/gui/realms/trailer_icons.png");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + offset + ("".equals(link) ? 0 : 17), y, trailerHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
            GL11.glPopMatrix();
         }

         if (linkHovered && !"".equals(link)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
            RealmsSelectWorldTemplateScreen.this.currentLink = link;
         } else if (trailerHovered && !"".equals(trailerLink)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.trailer.tooltip");
            RealmsSelectWorldTemplateScreen.this.currentLink = trailerLink;
         }

      }
   }
}
