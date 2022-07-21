package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.renderer.system.GlStateManager;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
   private List<TextRenderingUtils.Line> noTemplatesMessage;
   private boolean loading;
   private boolean stopLoadingTemplates;

   public RealmsSelectWorldTemplateScreen(
      RealmsScreenWithCallback<WorldTemplate> lastScreen, WorldTemplate selectedWorldTemplate, RealmsServer.WorldType worldType
   ) {
      this.lastScreen = lastScreen;
      this.selectedWorldTemplate = selectedWorldTemplate;
      this.worldType = worldType;
      this.title = getLocalizedString("mco.template.title");
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

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.hoverWarning && this.warningURL != null) {
         RealmsUtil.browseTo("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.worldTemplateSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateSelectionList();
      if (!this.prepopulated && this.templates.isEmpty()) {
         this.paginatedList.page = 0;
         this.paginatedList.size = 10;
         this.fetchMoreTemplatesAsync();
      }

      this.buttonsAdd(
         new RealmsButton(
            0,
            this.width() / 2 + 6,
            this.height() - 32,
            153,
            20,
            getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back")
         ) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSelectWorldTemplateScreen.this.backButtonClicked();
            }
         }
      );
      this.buttonsAdd(
         this.selectButton = new RealmsButton(1, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.template.button.select")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsSelectWorldTemplateScreen.this.selectTemplate();
            }
         }
      );
      this.selectButton.active(false);
      this.addWidget(this.worldTemplateSelectionList);
      this.focusOn(this.worldTemplateSelectionList);
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.backButtonClicked();
            return true;
         case 257:
         case 335:
            this.selectTemplate();
            return true;
         case 264:
            if (this.selectedTemplate != -1) {
               int theIndex = this.selectedTemplate;
               int maxScroll = Math.max(
                  0, this.worldTemplateSelectionList.getMaxPosition() - (this.height() - 40 - (this.displayWarning ? RealmsConstants.row(1) : 32))
               );
               if (theIndex == this.templates.size() - 1) {
                  this.worldTemplateSelectionList.scroll(maxScroll - this.worldTemplateSelectionList.getScroll() + 46);
                  return true;
               }

               int newIndex = theIndex + 1;
               if (newIndex == this.templates.size() - 1) {
                  this.selectedTemplate = newIndex;
                  this.worldTemplateSelectionList.scroll(maxScroll - this.worldTemplateSelectionList.getScroll() + 46);
                  return true;
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

                  return true;
               }
            }

            this.selectedTemplate = 0;
            this.worldTemplateSelectionList.scroll(-(this.worldTemplateSelectionList.getItemCount() * 46));
            return true;
         case 265:
            if (this.selectedTemplate != -1) {
               int theIndex = this.selectedTemplate;
               if (theIndex == 0) {
                  this.worldTemplateSelectionList.scroll(0 - this.worldTemplateSelectionList.getScroll());
                  return true;
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

                  return true;
               }
            }

            this.selectedTemplate = 0;
            this.worldTemplateSelectionList.scroll(0 - this.worldTemplateSelectionList.getScroll());
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
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
                     if (RealmsSelectWorldTemplateScreen.this.paginatedList.templates.isEmpty()) {
                        RealmsSelectWorldTemplateScreen.this.stopLoadingTemplates = true;
                        String withoutLink = RealmsScreen.getLocalizedString("mco.template.select.none", new Object[]{"%link"});
                        TextRenderingUtils.LineSegment link = TextRenderingUtils.LineSegment.link(
                           RealmsScreen.getLocalizedString("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/"
                        );
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(withoutLink, link);
                     }
                  } catch (RealmsServiceException var7) {
                     RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates");
                     RealmsSelectWorldTemplateScreen.this.stopLoadingTemplates = true;
                     if (RealmsSelectWorldTemplateScreen.this.paginatedList.templates.isEmpty()) {
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(
                           RealmsScreen.getLocalizedString("mco.template.select.failure")
                        );
                     }
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
      if (this.stopLoadingTemplates && this.paginatedList.templates.isEmpty() && this.noTemplatesMessage != null) {
         for(int i = 0; i < this.noTemplatesMessage.size(); ++i) {
            TextRenderingUtils.Line line = (TextRenderingUtils.Line)this.noTemplatesMessage.get(i);
            int lineY = RealmsConstants.row(4 + i);
            int lineWidth = line.segments.stream().mapToInt(s -> this.fontWidth(s.renderedText())).sum();
            int startX = this.width() / 2 - lineWidth / 2;

            for(TextRenderingUtils.LineSegment segment : line.segments) {
               int color = segment.isLink() ? 3368635 : 16777215;
               int endX = this.draw(segment.renderedText(), startX, lineY, color, true);
               if (segment.isLink() && xm > startX && xm < endX && ym > lineY - 3 && ym < lineY + 8) {
                  this.toolTip = segment.getLinkUrl();
                  this.currentLink = segment.getLinkUrl();
               }

               startX = endX;
            }
         }
      }

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

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               RealmsUtil.browseTo(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + (int)this.yo() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm < (double)this.getScrollbarPosition() && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               if (slot >= RealmsSelectWorldTemplateScreen.this.templates.size()) {
                  return super.mouseClicked(xm, ym, buttonNum);
               }

               RealmsSelectWorldTemplateScreen.this.selectButton.active(true);
               RealmsSelectWorldTemplateScreen.this.selectedTemplate = slot;
               RealmsSelectWorldTemplateScreen.this.selectedWorldTemplate = null;
               RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(xm, ym, buttonNum);
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

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot < RealmsSelectWorldTemplateScreen.this.templates.size()) {
            ;
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
            this.renderWorldTemplateItem(i, x, y, h, mouseX, mouseY);
         }

      }

      public int getScrollbarPosition() {
         return super.getScrollbarPosition() + 30;
      }

      public void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = this.getScrollbarPosition() - 290;
         int x1 = this.getScrollbarPosition() - 10;
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableTexture();
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
         GlStateManager.enableTexture();
      }

      private void renderWorldTemplateItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         WorldTemplate worldTemplate = (WorldTemplate)RealmsSelectWorldTemplateScreen.this.templates.get(i);
         int textStart = x + 20;
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.name, textStart, y + 2, 16777215);
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.author, textStart, y + 15, 7105644);
         RealmsSelectWorldTemplateScreen.this.drawString(
            worldTemplate.version, textStart + 227 - RealmsSelectWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 7105644
         );
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.drawIcons(textStart - 1, y + 25, mouseX, mouseY, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
         }

         this.drawImage(x - 25, y + 1, mouseX, mouseY, worldTemplate);
      }

      private void drawImage(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38.0F, 38.0F);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + offset, y, linkHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
            GlStateManager.popMatrix();
         }

         if (!"".equals(trailerLink)) {
            RealmsScreen.bind("realms:textures/gui/realms/trailer_icons.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + offset + ("".equals(link) ? 0 : 17), y, trailerHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30.0F, 15.0F);
            GlStateManager.popMatrix();
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
