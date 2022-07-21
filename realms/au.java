package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class au extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final as<WorldTemplate> b;
   private WorldTemplate c;
   private final List<WorldTemplate> d = new ArrayList();
   private au.a e;
   private int f = -1;
   private String g;
   private RealmsButton h;
   private RealmsButton i;
   private RealmsButton j;
   private String k;
   private String l;
   private final RealmsServer.c m;
   private int n;
   private String o;
   private String p;
   private boolean q;
   private boolean r;
   private WorldTemplatePaginatedList s;
   private List<bl.a> t;
   private boolean u;
   private boolean v;

   public au(as<WorldTemplate> lastScreen, WorldTemplate selectedWorldTemplate, RealmsServer.c worldType) {
      this.b = lastScreen;
      this.c = selectedWorldTemplate;
      this.m = worldType;
      this.g = getLocalizedString("mco.template.title");
      this.s = new WorldTemplatePaginatedList(10);
   }

   public au(as<WorldTemplate> lastScreen, WorldTemplate selectedWorldTemplate, RealmsServer.c worldType, WorldTemplatePaginatedList list) {
      this(lastScreen, selectedWorldTemplate, worldType);
      this.d.addAll((Collection)(list == null ? new ArrayList() : list.templates));
      this.s = list;
      if (this.s.size == 0) {
         this.s.size = 10;
      }

   }

   public void a(String title) {
      this.g = title;
   }

   public void b(String string) {
      this.o = string;
      this.q = true;
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.r && this.p != null) {
         bj.c("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.e = new au.a();
      if (this.d.isEmpty()) {
         this.s.page = 0;
         this.s.size = 10;
         this.j();
      } else {
         for(WorldTemplate template : this.d) {
            this.e.a(template);
         }
      }

      this.buttonsAdd(this.i = new RealmsButton(2, this.width() / 2 - 206, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.trailer")) {
         public void onPress() {
            au.this.h();
         }
      });
      this.buttonsAdd(this.h = new RealmsButton(1, this.width() / 2 - 100, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.select")) {
         public void onPress() {
            au.this.g();
         }
      });
      this.buttonsAdd(
         new RealmsButton(0, this.width() / 2 + 6, this.height() - 32, 100, 20, getLocalizedString(this.m == RealmsServer.c.b ? "gui.cancel" : "gui.back")) {
            public void onPress() {
               au.this.f();
            }
         }
      );
      this.buttonsAdd(this.j = new RealmsButton(1, this.width() / 2 + 112, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.publisher")) {
         public void onPress() {
            au.this.i();
         }
      });
      this.h.active(false);
      this.i.setVisible(false);
      this.j.setVisible(false);
      this.addWidget(this.e);
      this.focusOn(this.e);
      Realms.narrateNow((Iterable)Stream.of(this.g, this.o).filter(Objects::nonNull).collect(Collectors.toList()));
   }

   private void b() {
      this.j.setVisible(this.d());
      this.i.setVisible(this.e());
      this.h.active(this.c());
   }

   private boolean c() {
      return this.f != -1;
   }

   private boolean d() {
      return this.f != -1 && !((WorldTemplate)this.d.get(this.f)).link.isEmpty();
   }

   private boolean e() {
      return this.f != -1 && !((WorldTemplate)this.d.get(this.f)).trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.n;
      if (this.n < 0) {
         this.n = 0;
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.f();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void f() {
      this.b.a(null);
      Realms.setScreen(this.b);
   }

   private void g() {
      if (this.f >= 0 && this.f < this.d.size()) {
         WorldTemplate template = (WorldTemplate)this.d.get(this.f);
         this.b.a(template);
      }

   }

   private void h() {
      if (this.f >= 0 && this.f < this.d.size()) {
         WorldTemplate template = (WorldTemplate)this.d.get(this.f);
         if (!"".equals(template.trailer)) {
            bj.c(template.trailer);
         }
      }

   }

   private void i() {
      if (this.f >= 0 && this.f < this.d.size()) {
         WorldTemplate template = (WorldTemplate)this.d.get(this.f);
         if (!"".equals(template.link)) {
            bj.c(template.link);
         }
      }

   }

   private void j() {
      if (!this.u && !this.v) {
         this.u = true;
         (new Thread("realms-template-fetcher") {
            public void run() {
               try {
                  g client = realms.g.a();
                  au.this.s = client.a(au.this.s.page + 1, au.this.s.size, au.this.m);
                  au.this.d.addAll(au.this.s.templates);

                  for(WorldTemplate template : au.this.d) {
                     au.this.e.a(template);
                  }

                  if (au.this.s.templates.isEmpty()) {
                     au.this.v = true;
                     String withoutLink = RealmsScreen.getLocalizedString("mco.template.select.none", new Object[]{"%link"});
                     bl.b link = bl.b.a(RealmsScreen.getLocalizedString("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/");
                     au.this.t = bl.a(withoutLink, link);
                  }
               } catch (o var7) {
                  au.a.error("Couldn't fetch templates");
                  au.this.v = true;
                  if (au.this.s.templates.isEmpty()) {
                     au.this.t = bl.a(RealmsScreen.getLocalizedString("mco.template.select.failure"));
                  }
               } finally {
                  au.this.u = false;
               }

            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      this.k = null;
      this.l = null;
      this.r = false;
      if (!this.s.isLastPage()) {
         this.e.clear();
         this.j();
      }

      this.renderBackground();
      this.e.render(xm, ym, a);
      if (this.v && this.s.templates.isEmpty() && this.t != null) {
         for(int i = 0; i < this.t.size(); ++i) {
            bl.a line = (bl.a)this.t.get(i);
            int lineY = realms.u.a(4 + i);
            int lineWidth = line.a.stream().mapToInt(s -> this.fontWidth(s.a())).sum();
            int startX = this.width() / 2 - lineWidth / 2;

            for(bl.b segment : line.a) {
               int color = segment.b() ? 3368635 : 16777215;
               int endX = this.draw(segment.a(), startX, lineY, color, true);
               if (segment.b() && xm > startX && xm < endX && ym > lineY - 3 && ym < lineY + 8) {
                  this.k = segment.c();
                  this.l = segment.c();
               }

               startX = endX;
            }
         }
      }

      this.drawCenteredString(this.g, this.width() / 2, 13, 16777215);
      if (this.q) {
         String[] lines = this.o.split("\\\\n");

         for(int index = 0; index < lines.length; ++index) {
            int fontWidth = this.fontWidth(lines[index]);
            int offsetX = this.width() / 2 - fontWidth / 2;
            int offsetY = realms.u.a(-1 + index);
            if (xm >= offsetX && xm <= offsetX + fontWidth && ym >= offsetY && ym <= offsetY + this.fontLineHeight()) {
               this.r = true;
            }
         }

         for(int index = 0; index < lines.length; ++index) {
            String line = lines[index];
            int warningColor = 10526880;
            if (this.p != null) {
               if (this.r) {
                  warningColor = 7107012;
                  line = "§n" + line;
               } else {
                  warningColor = 3368635;
               }
            }

            this.drawCenteredString(line, this.width() / 2, realms.u.a(-1 + index), warningColor);
         }
      }

      super.render(xm, ym, a);
      if (this.k != null) {
         this.a(this.k, xm, ym);
      }

   }

   protected void a(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   class a extends RealmsObjectSelectionList {
      public a() {
         super(au.this.width(), au.this.height(), au.this.q ? realms.u.a(1) : 32, au.this.height() - 40, 46);
      }

      public void a(WorldTemplate template) {
         this.addEntry(au.this.new b(template));
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 150;
            if (au.this.l != null) {
               bj.c(au.this.l);
            }

            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm < (double)this.getScrollbarPosition() && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.selectItem(slot);
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               if (slot >= au.this.d.size()) {
                  return super.mouseClicked(xm, ym, buttonNum);
               }

               au.this.f = slot;
               au.this.c = null;
               au.this.b();
               au.this.n = au.this.n + 7;
               if (au.this.n >= 10) {
                  au.this.g();
               }

               return true;
            }
         }

         return super.mouseClicked(xm, ym, buttonNum);
      }

      public void selectItem(int item) {
         au.this.f = item;
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((WorldTemplate)au.this.d.get(item)).name}));
         }

         au.this.b();
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot < au.this.d.size()) {
            ;
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public void renderBackground() {
         au.this.renderBackground();
      }

      public int getScrollbarPosition() {
         return super.getScrollbarPosition() + 30;
      }

      public boolean isFocused() {
         return au.this.isFocused(this);
      }
   }

   class b extends RealmListEntry {
      final WorldTemplate a;

      public b(WorldTemplate template) {
         this.a = template;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft, rowTop, mouseX, mouseY);
      }

      private void a(WorldTemplate worldTemplate, int x, int y, int mouseX, int mouseY) {
         int textStart = x + 20;
         au.this.drawString(worldTemplate.name, textStart, y + 2, 16777215);
         au.this.drawString(worldTemplate.author, textStart, y + 15, 7105644);
         au.this.drawString(worldTemplate.version, textStart + 227 - au.this.fontWidth(worldTemplate.version), y + 1, 7105644);
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.a(textStart - 1, y + 25, mouseX, mouseY, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
         }

         this.a(x - 45, y + 1, mouseX, mouseY, worldTemplate);
      }

      private void a(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         bi.a(worldTemplate.id, worldTemplate.image);
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void a(int x, int y, int xm, int ym, String link, String trailerLink, String recommendedPlayers) {
         if (!"".equals(recommendedPlayers)) {
            au.this.drawString(recommendedPlayers, x, y + 4, 5000268);
         }

         int offset = "".equals(recommendedPlayers) ? 0 : au.this.fontWidth(recommendedPlayers) + 2;
         boolean linkHovered = false;
         boolean trailerHovered = false;
         if (xm >= x + offset && xm <= x + offset + 32 && ym >= y && ym <= y + 15 && ym < au.this.height() - 15 && ym > 32) {
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
            au.this.k = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
            au.this.l = link;
         } else if (trailerHovered && !"".equals(trailerLink)) {
            au.this.k = RealmsScreen.getLocalizedString("mco.template.trailer.tooltip");
            au.this.l = trailerLink;
         }

      }
   }
}
