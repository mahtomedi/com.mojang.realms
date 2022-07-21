package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
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
   private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
   private int selectedTemplate = -1;
   private String title;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_SELECT_ID = 1;
   private static final int BUTTON_TRAILER_ID = 2;
   private static final int BUTTON_PUBLISHER_ID = 3;
   private RealmsButton selectButton;
   private RealmsButton trailerButton;
   private RealmsButton publisherButton;
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
      this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
      if (!this.prepopulated && this.templates.isEmpty()) {
         this.paginatedList.page = 0;
         this.paginatedList.size = 10;
         this.fetchMoreTemplatesAsync();
      }

      this.buttonsAdd(
         this.trailerButton = new RealmsButton(2, this.width() / 2 - 206, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.trailer")) {
            public void onPress() {
               RealmsSelectWorldTemplateScreen.this.onTrailer();
            }
         }
      );
      this.buttonsAdd(
         this.selectButton = new RealmsButton(1, this.width() / 2 - 100, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.select")) {
            public void onPress() {
               RealmsSelectWorldTemplateScreen.this.selectTemplate();
            }
         }
      );
      this.buttonsAdd(
         new RealmsButton(
            0,
            this.width() / 2 + 6,
            this.height() - 32,
            100,
            20,
            getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back")
         ) {
            public void onPress() {
               RealmsSelectWorldTemplateScreen.this.backButtonClicked();
            }
         }
      );
      this.buttonsAdd(
         this.publisherButton = new RealmsButton(1, this.width() / 2 + 112, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.publisher")) {
            public void onPress() {
               RealmsSelectWorldTemplateScreen.this.onPublish();
            }
         }
      );
      this.selectButton.active(false);
      this.trailerButton.setVisible(false);
      this.publisherButton.setVisible(false);
      this.addWidget(this.worldTemplateObjectSelectionList);
      this.focusOn(this.worldTemplateObjectSelectionList);
      Realms.narrateNow((Iterable)Stream.of(this.title, this.warning).filter(Objects::nonNull).collect(Collectors.toList()));
   }

   private void updateButtonStates() {
      this.publisherButton.setVisible(this.shouldPublisherBeVisible());
      this.trailerButton.setVisible(this.shouldTrailerBeVisible());
   }

   private boolean shouldPublisherBeVisible() {
      return !"".equals(((WorldTemplate)this.templates.get(this.selectedTemplate)).link);
   }

   private boolean shouldTrailerBeVisible() {
      return !"".equals(((WorldTemplate)this.templates.get(this.selectedTemplate)).trailer);
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

   private void onTrailer() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.templates.size()) {
         WorldTemplate template = (WorldTemplate)this.templates.get(this.selectedTemplate);
         if (!"".equals(template.trailer)) {
            RealmsUtil.browseTo(template.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.templates.size()) {
         WorldTemplate template = (WorldTemplate)this.templates.get(this.selectedTemplate);
         if (!"".equals(template.link)) {
            RealmsUtil.browseTo(template.link);
         }
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
   
                     for(WorldTemplate template : RealmsSelectWorldTemplateScreen.this.templates) {
                        RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(template);
                     }
   
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
      this.worldTemplateObjectSelectionList.render(xm, ym, a);
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

   private class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList {
      public WorldTemplateObjectSelectionList() {
         super(
            RealmsSelectWorldTemplateScreen.this.width(),
            RealmsSelectWorldTemplateScreen.this.height(),
            RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsConstants.row(1) : 32,
            RealmsSelectWorldTemplateScreen.this.height() - 40,
            46
         );
      }

      public void addEntry(WorldTemplate template) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new WorldTemplateObjectSelectionListEntry(template));
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

            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm < (double)this.getScrollbarPosition() && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.selectItem(slot, buttonNum, xm, ym);
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               if (slot >= RealmsSelectWorldTemplateScreen.this.templates.size()) {
                  return super.mouseClicked(xm, ym, buttonNum);
               }

               RealmsSelectWorldTemplateScreen.this.selectedTemplate = slot;
               RealmsSelectWorldTemplateScreen.this.selectedWorldTemplate = null;
               RealmsSelectWorldTemplateScreen.this.selectButton.active(true);
               RealmsSelectWorldTemplateScreen.this.updateButtonStates();
               RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(xm, ym, buttonNum);
      }

      protected void moveSelection(int dir) {
         int index = this.getSelectedIndex();
         int newIndex = RealmsMth.clamp(index + dir, 0, this.getItemCount() - 1);
         super.moveSelection(dir);
         this.selectItem(newIndex, 0, 0.0, 0.0);
         RealmsSelectWorldTemplateScreen.this.selectButton.active(true);
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = item;
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(
               RealmsScreen.getLocalizedString("narrator.select", new Object[]{((WorldTemplate)RealmsSelectWorldTemplateScreen.this.templates.get(item)).name})
            );
         }

         return true;
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

      public int getScrollbarPosition() {
         return super.getScrollbarPosition() + 30;
      }

      public boolean isFocused() {
         return RealmsSelectWorldTemplateScreen.this.isFocused(this);
      }
   }

   private class WorldTemplateObjectSelectionListEntry extends RealmListEntry {
      final WorldTemplate mTemplate;

      public WorldTemplateObjectSelectionListEntry(WorldTemplate template) {
         this.mTemplate = template;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderWorldTemplateItem(this.mTemplate, rowLeft, rowTop, mouseX, mouseY);
      }

      private void renderWorldTemplateItem(WorldTemplate worldTemplate, int x, int y, int mouseX, int mouseY) {
         int textStart = x + 20;
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.name, textStart, y + 2, 16777215);
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.author, textStart, y + 15, 7105644);
         RealmsSelectWorldTemplateScreen.this.drawString(
            worldTemplate.version, textStart + 227 - RealmsSelectWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 7105644
         );
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.drawIcons(textStart - 1, y + 25, mouseX, mouseY, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
         }

         this.drawImage(x - 45, y + 1, mouseX, mouseY, worldTemplate);
      }

      private void drawImage(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 40, 40, 40, 40);
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
            RealmsScreen.blit(x + offset, y, linkHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
            GlStateManager.popMatrix();
         }

         if (!"".equals(trailerLink)) {
            RealmsScreen.bind("realms:textures/gui/realms/trailer_icons.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + offset + ("".equals(link) ? 0 : 17), y, trailerHovered ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
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
