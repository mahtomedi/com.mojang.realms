package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.PendingInvite;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ao extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private String c;
   private boolean d;
   private ao.a e;
   private RealmsLabel f;
   private int g = -1;
   private RealmsButton h;
   private RealmsButton i;

   public ao(RealmsScreen lastScreen) {
      this.b = lastScreen;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.e = new ao.a();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            g client = realms.g.a();

            try {
               List<PendingInvite> pendingInvites = client.k().pendingInvites;
               List<ao.b> entries = (List)pendingInvites.stream().map(x$0 -> ao.this.new b(x$0)).collect(Collectors.toList());
               Realms.execute(() -> ao.this.e.replaceEntries(entries));
            } catch (o var7) {
               ao.a.error("Couldn't list invites");
            } finally {
               ao.this.d = true;
            }

         }
      }).start();
      this.buttonsAdd(this.h = new RealmsButton(1, this.width() / 2 - 174, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.accept")) {
         public void onPress() {
            ao.this.c(ao.this.g);
            ao.this.g = -1;
            ao.this.b();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, this.height() - 32, 100, 20, getLocalizedString("gui.done")) {
         public void onPress() {
            Realms.setScreen(new realms.b(ao.this.b));
         }
      });
      this.buttonsAdd(this.i = new RealmsButton(2, this.width() / 2 + 74, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.reject")) {
         public void onPress() {
            ao.this.b(ao.this.g);
            ao.this.g = -1;
            ao.this.b();
         }
      });
      this.f = new RealmsLabel(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
      this.addWidget(this.f);
      this.addWidget(this.e);
      this.narrateLabels();
      this.b();
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(new realms.b(this.b));
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void a(int slot) {
      this.e.a(slot);
   }

   private void b(final int slot) {
      if (slot < this.e.getItemCount()) {
         (new Thread("Realms-reject-invitation") {
            public void run() {
               try {
                  g client = realms.g.a();
                  client.b(((ao.b)ao.this.e.children().get(slot)).a.invitationId);
                  Realms.execute(() -> ao.this.a(slot));
               } catch (o var2) {
                  ao.a.error("Couldn't reject invite");
               }

            }
         }).start();
      }

   }

   private void c(final int slot) {
      if (slot < this.e.getItemCount()) {
         (new Thread("Realms-accept-invitation") {
            public void run() {
               try {
                  g client = realms.g.a();
                  client.a(((ao.b)ao.this.e.children().get(slot)).a.invitationId);
                  Realms.execute(() -> ao.this.a(slot));
               } catch (o var2) {
                  ao.a.error("Couldn't accept invite");
               }

            }
         }).start();
      }

   }

   public void render(int xm, int ym, float a) {
      this.c = null;
      this.renderBackground();
      this.e.render(xm, ym, a);
      this.f.render(this);
      if (this.c != null) {
         this.a(this.c, xm, ym);
      }

      if (this.e.getItemCount() == 0 && this.d) {
         this.drawCenteredString(getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 16777215);
      }

      super.render(xm, ym, a);
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

   private void b() {
      this.h.setVisible(this.d(this.g));
      this.i.setVisible(this.d(this.g));
   }

   private boolean d(int invite) {
      return invite != -1;
   }

   public static String a(PendingInvite invite) {
      return bk.a(System.currentTimeMillis() - invite.date.getTime());
   }

   class a extends RealmsObjectSelectionList<ao.b> {
      public a() {
         super(ao.this.width(), ao.this.height(), 32, ao.this.height() - 40, 36);
      }

      public void a(int index) {
         this.remove(index);
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 260;
      }

      public boolean isFocused() {
         return ao.this.isFocused(this);
      }

      public void renderBackground() {
         ao.this.renderBackground();
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            List<ao.b> children = ao.this.e.children();
            PendingInvite pendingInvite = ((ao.b)children.get(item)).a;
            String positionInList = RealmsScreen.getLocalizedString("narrator.select.list.position", new Object[]{item + 1, children.size()});
            String narration = Realms.joinNarrations(Arrays.asList(pendingInvite.worldName, pendingInvite.worldOwnerName, ao.a(pendingInvite), positionInList));
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{narration}));
         }

         this.b(item);
      }

      public void b(int item) {
         ao.this.g = item;
         ao.this.b();
      }
   }

   class b extends RealmListEntry {
      final PendingInvite a;
      private final List<x> c;

      b(PendingInvite pendingInvite) {
         this.a = pendingInvite;
         this.c = Arrays.asList(new ao.b.a(), new ao.b.b());
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft, rowTop, mouseX, mouseY);
      }

      public boolean mouseClicked(double x, double y, int buttonNum) {
         x.a(ao.this.e, this, this.c, buttonNum, x, y);
         return true;
      }

      private void a(PendingInvite invite, int x, int y, int mouseX, int mouseY) {
         ao.this.drawString(invite.worldName, x + 38, y + 1, 16777215);
         ao.this.drawString(invite.worldOwnerName, x + 38, y + 12, 7105644);
         ao.this.drawString(ao.a(invite), x + 38, y + 24, 7105644);
         x.a(this.c, ao.this.e, x, y, mouseX, mouseY);
         bj.a(invite.worldOwnerUuid, (Runnable)(() -> {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x, y, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            RealmsScreen.blit(x, y, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
         }));
      }

      class a extends x {
         a() {
            super(15, 15, 215, 5);
         }

         @Override
         protected void a(int x, int y, boolean hovered) {
            RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
            GlStateManager.popMatrix();
            if (hovered) {
               ao.this.c = RealmsScreen.getLocalizedString("mco.invites.button.accept");
            }

         }

         @Override
         public void a(int index) {
            ao.this.c(index);
         }
      }

      class b extends x {
         b() {
            super(15, 15, 235, 5);
         }

         @Override
         protected void a(int x, int y, boolean hovered) {
            RealmsScreen.bind("realms:textures/gui/realms/reject_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            RealmsScreen.blit(x, y, hovered ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
            GlStateManager.popMatrix();
            if (hovered) {
               ao.this.c = RealmsScreen.getLocalizedString("mco.invites.button.reject");
            }

         }

         @Override
         public void a(int index) {
            ao.this.b(index);
         }
      }
   }
}
