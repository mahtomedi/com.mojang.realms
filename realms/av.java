package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Either;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class av extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final at<WorldTemplate> b;
   private av.a c;
   private int d = -1;
   private String e;
   private RealmsButton f;
   private RealmsButton g;
   private RealmsButton h;
   private String i;
   private String j;
   private final RealmsServer.c k;
   private int l;
   private String m;
   private String n;
   private boolean o;
   private boolean p;
   private List<bm.a> q;

   public av(at<WorldTemplate> lastScreen, RealmsServer.c worldType) {
      this(lastScreen, worldType, null);
   }

   public av(at<WorldTemplate> lastScreen, RealmsServer.c worldType, @Nullable WorldTemplatePaginatedList alreadyFetched) {
      this.b = lastScreen;
      this.k = worldType;
      if (alreadyFetched == null) {
         this.c = new av.a();
         this.a(new WorldTemplatePaginatedList(10));
      } else {
         this.c = new av.a(new ArrayList(alreadyFetched.templates));
         this.a(alreadyFetched);
      }

      this.e = getLocalizedString("mco.template.title");
   }

   public void a(String title) {
      this.e = title;
   }

   public void b(String string) {
      this.m = string;
      this.o = true;
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.p && this.n != null) {
         bk.c("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.c = new av.a(this.c.b());
      this.buttonsAdd(this.g = new RealmsButton(2, this.width() / 2 - 206, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.trailer")) {
         public void onPress() {
            av.this.i();
         }
      });
      this.buttonsAdd(this.f = new RealmsButton(1, this.width() / 2 - 100, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.select")) {
         public void onPress() {
            av.this.h();
         }
      });
      this.buttonsAdd(
         new RealmsButton(0, this.width() / 2 + 6, this.height() - 32, 100, 20, getLocalizedString(this.k == RealmsServer.c.b ? "gui.cancel" : "gui.back")) {
            public void onPress() {
               av.this.g();
            }
         }
      );
      this.h = new RealmsButton(3, this.width() / 2 + 112, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.publisher")) {
         public void onPress() {
            av.this.j();
         }
      };
      this.buttonsAdd(this.h);
      this.f.active(false);
      this.g.setVisible(false);
      this.h.setVisible(false);
      this.addWidget(this.c);
      this.focusOn(this.c);
      Realms.narrateNow((Iterable)Stream.of(this.e, this.m).filter(Objects::nonNull).collect(Collectors.toList()));
   }

   private void b() {
      this.h.setVisible(this.d());
      this.g.setVisible(this.f());
      this.f.active(this.c());
   }

   private boolean c() {
      return this.d != -1;
   }

   private boolean d() {
      return this.d != -1 && !this.e().link.isEmpty();
   }

   private WorldTemplate e() {
      return this.c.a(this.d);
   }

   private boolean f() {
      return this.d != -1 && !this.e().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.l;
      if (this.l < 0) {
         this.l = 0;
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.g();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void g() {
      this.b.a(null);
      Realms.setScreen(this.b);
   }

   private void h() {
      if (this.d >= 0 && this.d < this.c.getItemCount()) {
         WorldTemplate template = this.e();
         this.b.a(template);
      }

   }

   private void i() {
      if (this.d >= 0 && this.d < this.c.getItemCount()) {
         WorldTemplate template = this.e();
         if (!"".equals(template.trailer)) {
            bk.c(template.trailer);
         }
      }

   }

   private void j() {
      if (this.d >= 0 && this.d < this.c.getItemCount()) {
         WorldTemplate template = this.e();
         if (!"".equals(template.link)) {
            bk.c(template.link);
         }
      }

   }

   private void a(final WorldTemplatePaginatedList startPage) {
      (new Thread("realms-template-fetcher") {
            public void run() {
               WorldTemplatePaginatedList page = startPage;
   
               Either<WorldTemplatePaginatedList, String> result;
               for(g client = realms.g.a();
                  page != null;
                  page = (WorldTemplatePaginatedList)Realms.execute(
                        () -> {
                           if (result.right().isPresent()) {
                              av.a.error("Couldn't fetch templates: {}", result.right().get());
                              if (av.this.c.a()) {
                                 av.this.q = bm.a(RealmsScreen.getLocalizedString("mco.template.select.failure"));
                              }
            
                              return null;
                           } else {
                              assert result.left().isPresent();
            
                              WorldTemplatePaginatedList currentPage = (WorldTemplatePaginatedList)result.left().get();
            
                              for(WorldTemplate template : currentPage.templates) {
                                 av.this.c.a(template);
                              }
            
                              if (currentPage.templates.isEmpty()) {
                                 if (av.this.c.a()) {
                                    String withoutLink = RealmsScreen.getLocalizedString("mco.template.select.none", new Object[]{"%link"});
                                    bm.b link = bm.b.a(
                                       RealmsScreen.getLocalizedString("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/"
                                    );
                                    av.this.q = bm.a(withoutLink, link);
                                 }
            
                                 return null;
                              } else {
                                 return currentPage;
                              }
                           }
                        }
                     )
                     .join()
               ) {
                  result = av.this.a(page, client);
               }
   
            }
         })
         .start();
   }

   private Either<WorldTemplatePaginatedList, String> a(WorldTemplatePaginatedList paginatedList, g client) {
      try {
         return Either.left(client.a(paginatedList.page + 1, paginatedList.size, this.k));
      } catch (o var4) {
         return Either.right(var4.getMessage());
      }
   }

   public void render(int xm, int ym, float a) {
      this.i = null;
      this.j = null;
      this.p = false;
      this.renderBackground();
      this.c.render(xm, ym, a);
      if (this.q != null) {
         this.a(xm, ym, this.q);
      }

      this.drawCenteredString(this.e, this.width() / 2, 13, 16777215);
      if (this.o) {
         String[] lines = this.m.split("\\\\n");

         for(int index = 0; index < lines.length; ++index) {
            int fontWidth = this.fontWidth(lines[index]);
            int offsetX = this.width() / 2 - fontWidth / 2;
            int offsetY = u.a(-1 + index);
            if (xm >= offsetX && xm <= offsetX + fontWidth && ym >= offsetY && ym <= offsetY + this.fontLineHeight()) {
               this.p = true;
            }
         }

         for(int index = 0; index < lines.length; ++index) {
            String line = lines[index];
            int warningColor = 10526880;
            if (this.n != null) {
               if (this.p) {
                  warningColor = 7107012;
                  line = "§n" + line;
               } else {
                  warningColor = 3368635;
               }
            }

            this.drawCenteredString(line, this.width() / 2, u.a(-1 + index), warningColor);
         }
      }

      super.render(xm, ym, a);
      if (this.i != null) {
         this.a(this.i, xm, ym);
      }

   }

   private void a(int xm, int ym, List<bm.a> noTemplatesMessage) {
      for(int i = 0; i < noTemplatesMessage.size(); ++i) {
         bm.a line = (bm.a)noTemplatesMessage.get(i);
         int lineY = u.a(4 + i);
         int lineWidth = line.a.stream().mapToInt(s -> this.fontWidth(s.a())).sum();
         int startX = this.width() / 2 - lineWidth / 2;

         for(bm.b segment : line.a) {
            int color = segment.b() ? 3368635 : 16777215;
            int endX = this.draw(segment.a(), startX, lineY, color, true);
            if (segment.b() && xm > startX && xm < endX && ym > lineY - 3 && ym < lineY + 8) {
               this.i = segment.c();
               this.j = segment.c();
            }

            startX = endX;
         }
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

   class a extends RealmsObjectSelectionList<av.b> {
      public a() {
         this(Collections.emptyList());
      }

      public a(Iterable<WorldTemplate> templates) {
         super(av.this.width(), av.this.height(), av.this.o ? u.a(1) : 32, av.this.height() - 40, 46);
         templates.forEach(this::a);
      }

      public void a(WorldTemplate template) {
         this.addEntry(av.this.new b(template));
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 150;
            if (av.this.j != null) {
               bk.c(av.this.j);
            }

            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm < (double)this.getScrollbarPosition() && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.selectItem(slot);
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               if (slot >= av.this.c.getItemCount()) {
                  return super.mouseClicked(xm, ym, buttonNum);
               }

               av.this.d = slot;
               av.this.b();
               av.this.l = av.this.l + 7;
               if (av.this.l >= 10) {
                  av.this.h();
               }

               return true;
            }
         }

         return super.mouseClicked(xm, ym, buttonNum);
      }

      public void selectItem(int item) {
         av.this.d = item;
         this.setSelected(item);
         if (item != -1) {
            WorldTemplate template = av.this.c.a(item);
            String positionInList = RealmsScreen.getLocalizedString("narrator.select.list.position", new Object[]{item + 1, av.this.c.getItemCount()});
            String version = RealmsScreen.getLocalizedString("mco.template.select.narrate.version", new Object[]{template.version});
            String author = RealmsScreen.getLocalizedString("mco.template.select.narrate.authors", new Object[]{template.author});
            String narration = Realms.joinNarrations(Arrays.asList(template.name, author, template.recommendedPlayers, version, positionInList));
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{narration}));
         }

         av.this.b();
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot < av.this.c.getItemCount()) {
            ;
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void renderBackground() {
         av.this.renderBackground();
      }

      public boolean isFocused() {
         return av.this.isFocused(this);
      }

      public boolean a() {
         return this.getItemCount() == 0;
      }

      public WorldTemplate a(int index) {
         return ((av.b)this.children().get(index)).a;
      }

      public List<WorldTemplate> b() {
         return (List<WorldTemplate>)this.children().stream().map(c -> c.a).collect(Collectors.toList());
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
         int textStart = x + 45 + 20;
         av.this.drawString(worldTemplate.name, textStart, y + 2, 16777215);
         av.this.drawString(worldTemplate.author, textStart, y + 15, 7105644);
         av.this.drawString(worldTemplate.version, textStart + 227 - av.this.fontWidth(worldTemplate.version), y + 1, 7105644);
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.a(textStart - 1, y + 25, mouseX, mouseY, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
         }

         this.a(x, y + 1, mouseX, mouseY, worldTemplate);
      }

      private void a(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         bj.a(worldTemplate.id, worldTemplate.image);
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void a(int x, int y, int xm, int ym, String link, String trailerLink, String recommendedPlayers) {
         if (!"".equals(recommendedPlayers)) {
            av.this.drawString(recommendedPlayers, x, y + 4, 5000268);
         }

         int offset = "".equals(recommendedPlayers) ? 0 : av.this.fontWidth(recommendedPlayers) + 2;
         boolean linkHovered = false;
         boolean trailerHovered = false;
         if (xm >= x + offset && xm <= x + offset + 32 && ym >= y && ym <= y + 15 && ym < av.this.height() - 15 && ym > 32) {
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
            av.this.i = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
            av.this.j = link;
         } else if (trailerHovered && !"".equals(trailerLink)) {
            av.this.i = RealmsScreen.getLocalizedString("mco.template.trailer.tooltip");
            av.this.j = trailerLink;
         }

      }
   }
}
