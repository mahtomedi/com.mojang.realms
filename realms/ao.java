package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ao extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private String b;
   private final ac c;
   private final RealmsServer d;
   private ao.a e;
   private int f;
   private int g;
   private int h;
   private RealmsButton i;
   private RealmsButton j;
   private int k = -1;
   private String l;
   private int m = -1;
   private boolean n;
   private RealmsLabel o;

   public ao(ac lastScreen, RealmsServer serverData) {
      this.c = lastScreen;
      this.d = serverData;
   }

   public void tick() {
      super.tick();
   }

   public void init() {
      this.f = this.width() / 2 - 160;
      this.g = 150;
      this.h = this.width() / 2 + 12;
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(new RealmsButton(1, this.h, u.a(1), this.g + 10, 20, getLocalizedString("mco.configure.world.buttons.invite")) {
         public void onPress() {
            Realms.setScreen(new ai(ao.this.c, ao.this, ao.this.d));
         }
      });
      RealmsButton activityFeedButton = new RealmsButton(3, this.h, u.a(3), this.g + 10, 20, getLocalizedString("mco.configure.world.buttons.activity")) {
         public void onPress() {
            Realms.setScreen(new x(ao.this, ao.this.d));
         }
      };
      activityFeedButton.active(false);
      this.buttonsAdd(activityFeedButton);
      this.buttonsAdd(this.i = new RealmsButton(4, this.h, u.a(7), this.g + 10, 20, getLocalizedString("mco.configure.world.invites.remove.tooltip")) {
         public void onPress() {
            ao.this.d(ao.this.m);
         }
      });
      this.buttonsAdd(this.j = new RealmsButton(5, this.h, u.a(9), this.g + 10, 20, getLocalizedString("mco.configure.world.invites.ops.tooltip")) {
         public void onPress() {
            if (((PlayerInfo)ao.this.d.players.get(ao.this.m)).isOperator()) {
               ao.this.c(ao.this.m);
            } else {
               ao.this.b(ao.this.m);
            }

         }
      });
      this.buttonsAdd(new RealmsButton(0, this.h + this.g / 2 + 2, u.a(12), this.g / 2 + 10 - 2, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            ao.this.b();
         }
      });
      this.e = new ao.a();
      this.e.setLeftPos(this.f);
      this.addWidget(this.e);

      for(PlayerInfo playerInfo : this.d.players) {
         this.e.a(playerInfo);
      }

      this.addWidget(this.o = new RealmsLabel(getLocalizedString("mco.configure.world.players.title"), this.width() / 2, 17, 16777215));
      this.narrateLabels();
      this.a();
   }

   private void a() {
      this.i.setVisible(this.a(this.m));
      this.j.setVisible(this.a(this.m));
   }

   private boolean a(int player) {
      return player != -1;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.b();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void b() {
      if (this.n) {
         Realms.setScreen(this.c.b());
      } else {
         Realms.setScreen(this.c);
      }

   }

   private void b(int index) {
      this.a();
      g client = realms.g.a();
      String selectedInvite = ((PlayerInfo)this.d.players.get(index)).getUuid();

      try {
         this.a(client.e(this.d.id, selectedInvite));
      } catch (o var5) {
         a.error("Couldn't op the user");
      }

   }

   private void c(int index) {
      this.a();
      g client = realms.g.a();
      String selectedInvite = ((PlayerInfo)this.d.players.get(index)).getUuid();

      try {
         this.a(client.f(this.d.id, selectedInvite));
      } catch (o var5) {
         a.error("Couldn't deop the user");
      }

   }

   private void a(Ops ops) {
      for(PlayerInfo playerInfo : this.d.players) {
         playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
      }

   }

   private void d(int index) {
      this.a();
      if (index >= 0 && index < this.d.players.size()) {
         PlayerInfo playerInfo = (PlayerInfo)this.d.players.get(index);
         this.l = playerInfo.getUuid();
         this.k = index;
         ad confirmScreen = new ad(this, "Question", getLocalizedString("mco.configure.world.uninvite.question") + " '" + playerInfo.getName() + "' ?", 2);
         Realms.setScreen(confirmScreen);
      }

   }

   public void confirmResult(boolean result, int id) {
      if (id == 2) {
         if (result) {
            g client = realms.g.a();

            try {
               client.a(this.d.id, this.l);
            } catch (o var5) {
               a.error("Couldn't uninvite user");
            }

            this.e(this.k);
            this.m = -1;
            this.a();
         }

         this.n = true;
         Realms.setScreen(this);
      }

   }

   private void e(int selectedInvitedIndex) {
      this.d.players.remove(selectedInvitedIndex);
   }

   public void render(int xm, int ym, float a) {
      this.b = null;
      this.renderBackground();
      if (this.e != null) {
         this.e.render(xm, ym, a);
      }

      int bottomBorder = u.a(12) + 20;
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      Tezzelator t = Tezzelator.instance;
      bind("textures/gui/options_background.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
      t.vertex(0.0, (double)this.height(), 0.0).tex(0.0, (double)((float)(this.height() - bottomBorder) / 32.0F + 0.0F)).color(64, 64, 64, 255).endVertex();
      t.vertex((double)this.width(), (double)this.height(), 0.0)
         .tex((double)((float)this.width() / 32.0F), (double)((float)(this.height() - bottomBorder) / 32.0F + 0.0F))
         .color(64, 64, 64, 255)
         .endVertex();
      t.vertex((double)this.width(), (double)bottomBorder, 0.0).tex((double)((float)this.width() / 32.0F), 0.0).color(64, 64, 64, 255).endVertex();
      t.vertex(0.0, (double)bottomBorder, 0.0).tex(0.0, 0.0).color(64, 64, 64, 255).endVertex();
      t.end();
      this.o.render(this);
      if (this.d != null && this.d.players != null) {
         this.drawString(getLocalizedString("mco.configure.world.invited") + " (" + this.d.players.size() + ")", this.f, u.a(0), 10526880);
      } else {
         this.drawString(getLocalizedString("mco.configure.world.invited"), this.f, u.a(0), 10526880);
      }

      super.render(xm, ym, a);
      if (this.d != null) {
         if (this.b != null) {
            this.a(this.b, xm, ym);
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

   private void a(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < u.a(12) + 20 && ym > u.a(1);
      bind("realms:textures/gui/realms/cross_player_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 7.0F : 0.0F, 8, 7, 8, 14);
      GlStateManager.popMatrix();
      if (hovered) {
         this.b = getLocalizedString("mco.configure.world.invites.remove.tooltip");
      }

   }

   private void b(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < u.a(12) + 20 && ym > u.a(1);
      bind("realms:textures/gui/realms/op_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 8.0F : 0.0F, 8, 8, 8, 16);
      GlStateManager.popMatrix();
      if (hovered) {
         this.b = getLocalizedString("mco.configure.world.invites.ops.tooltip");
      }

   }

   private void c(int x, int y, int xm, int ym) {
      boolean hovered = xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < u.a(12) + 20 && ym > u.a(1);
      bind("realms:textures/gui/realms/user_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, hovered ? 8.0F : 0.0F, 8, 8, 8, 16);
      GlStateManager.popMatrix();
      if (hovered) {
         this.b = getLocalizedString("mco.configure.world.invites.normal.tooltip");
      }

   }

   class a extends RealmsObjectSelectionList {
      public a() {
         super(ao.this.g + 10, u.a(12) + 20, u.a(1), u.a(12) + 20, 13);
      }

      public void a(PlayerInfo playerInfo) {
         this.addEntry(ao.this.new b(playerInfo));
      }

      public int getRowWidth() {
         return (int)((double)this.width() * 1.0);
      }

      public boolean isFocused() {
         return ao.this.isFocused(this);
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = ao.this.f;
            int x1 = ao.this.f + ao.this.g;
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.selectItem(slot);
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot >= 0 && slot <= ao.this.d.players.size() && ao.this.b != null) {
            if (!ao.this.b.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip"))
               && !ao.this.b.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.normal.tooltip"))) {
               if (ao.this.b.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip"))) {
                  ao.this.d(slot);
               }
            } else if (((PlayerInfo)ao.this.d.players.get(slot)).isOperator()) {
               ao.this.c(slot);
            } else {
               ao.this.b(slot);
            }

         }
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((PlayerInfo)ao.this.d.players.get(item)).getName()}));
         }

         this.a(item);
      }

      public void a(int item) {
         ao.this.m = item;
         ao.this.a();
      }

      public void renderBackground() {
         ao.this.renderBackground();
      }

      public int getScrollbarPosition() {
         return ao.this.f + this.width() - 5;
      }

      public int getItemCount() {
         return ao.this.d == null ? 1 : ao.this.d.players.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 13;
      }
   }

   class b extends RealmListEntry {
      final PlayerInfo a;

      public b(PlayerInfo playerInfo) {
         this.a = playerInfo;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft, rowTop, mouseX, mouseY);
      }

      private void a(PlayerInfo invited, int x, int y, int mouseX, int mouseY) {
         int inviteColor;
         if (!invited.getAccepted()) {
            inviteColor = 10526880;
         } else if (invited.getOnline()) {
            inviteColor = 8388479;
         } else {
            inviteColor = 16777215;
         }

         ao.this.drawString(invited.getName(), ao.this.f + 3 + 12, y + 1, inviteColor);
         if (invited.isOperator()) {
            ao.this.b(ao.this.f + ao.this.g - 10, y + 1, mouseX, mouseY);
         } else {
            ao.this.c(ao.this.f + ao.this.g - 10, y + 1, mouseX, mouseY);
         }

         ao.this.a(ao.this.f + ao.this.g - 22, y + 2, mouseX, mouseY);
         ao.this.drawString(RealmsScreen.getLocalizedString("mco.configure.world.activityfeed.disabled"), ao.this.h, u.a(5), 10526880);
         bi.a(invited.getUuid(), (Runnable)(() -> {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(ao.this.f + 2 + 2, y + 1, 8.0F, 8.0F, 8, 8, 8, 8, 64, 64);
            RealmsScreen.blit(ao.this.f + 2 + 2, y + 1, 40.0F, 8.0F, 8, 8, 8, 8, 64, 64);
         }));
      }
   }
}
