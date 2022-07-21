package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class z extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private static int b = -1;
   private final ac c;
   private List<Backup> d = Collections.emptyList();
   private String e;
   private z.a f;
   private int g = -1;
   private final int h;
   private RealmsButton i;
   private RealmsButton j;
   private RealmsButton k;
   private Boolean l = false;
   private final RealmsServer m;
   private RealmsLabel n;

   public z(ac lastscreen, RealmsServer serverData, int slotId) {
      this.c = lastscreen;
      this.m = serverData;
      this.h = slotId;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.f = new z.a();
      if (b != -1) {
         this.f.scroll(b);
      }

      (new Thread("Realms-fetch-backups") {
         public void run() {
            g client = realms.g.a();

            try {
               z.this.d = client.e(z.this.m.id).backups;
               z.this.l = z.this.d.size() == 0;

               for(Backup backup : z.this.d) {
                  z.this.f.a(backup);
               }

               z.this.b();
            } catch (o var4) {
               z.a.error("Couldn't request backups", var4);
            }

         }
      }).start();
      this.c();
   }

   private void b() {
      if (this.d.size() > 1) {
         for(int i = 0; i < this.d.size() - 1; ++i) {
            Backup backup = (Backup)this.d.get(i);
            Backup olderBackup = (Backup)this.d.get(i + 1);
            if (!backup.metadata.isEmpty() && !olderBackup.metadata.isEmpty()) {
               for(String key : backup.metadata.keySet()) {
                  if (key.contains("Uploaded") || !olderBackup.metadata.containsKey(key)) {
                     this.a(backup, key);
                  } else if (!((String)backup.metadata.get(key)).equals(olderBackup.metadata.get(key))) {
                     this.a(backup, key);
                  }
               }
            }
         }

      }
   }

   private void a(Backup backup, String key) {
      if (key.contains("Uploaded")) {
         String uploadedTime = DateFormat.getDateTimeInstance(3, 3).format(backup.lastModifiedDate);
         backup.changeList.put(key, uploadedTime);
         backup.setUploadedVersion(true);
      } else {
         backup.changeList.put(key, backup.metadata.get(key));
      }

   }

   private void c() {
      this.buttonsAdd(this.i = new RealmsButton(2, this.width() - 135, u.a(1), 120, 20, getLocalizedString("mco.backup.button.download")) {
         public void onPress() {
            z.this.g();
         }
      });
      this.buttonsAdd(this.j = new RealmsButton(3, this.width() - 135, u.a(3), 120, 20, getLocalizedString("mco.backup.button.restore")) {
         public void onPress() {
            z.this.b(z.this.g);
         }
      });
      this.buttonsAdd(this.k = new RealmsButton(4, this.width() - 135, u.a(5), 120, 20, getLocalizedString("mco.backup.changes.tooltip")) {
         public void onPress() {
            Realms.setScreen(new y(z.this, (Backup)z.this.d.get(z.this.g)));
            z.this.g = -1;
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() - 100, this.height() - 35, 85, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(z.this.c);
         }
      });
      this.addWidget(this.f);
      this.addWidget(this.n = new RealmsLabel(getLocalizedString("mco.configure.world.backup"), this.width() / 2, 12, 16777215));
      this.focusOn(this.f);
      this.d();
      this.narrateLabels();
   }

   private void d() {
      this.j.setVisible(this.f());
      this.k.setVisible(this.e());
   }

   private boolean e() {
      if (this.g == -1) {
         return false;
      } else {
         return !((Backup)this.d.get(this.g)).changeList.isEmpty();
      }
   }

   private boolean f() {
      if (this.g == -1) {
         return false;
      } else {
         return !this.m.expired;
      }
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.c);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void b(int selectedBackup) {
      if (selectedBackup >= 0 && selectedBackup < this.d.size() && !this.m.expired) {
         this.g = selectedBackup;
         Date backupDate = ((Backup)this.d.get(selectedBackup)).lastModifiedDate;
         String datePresentation = DateFormat.getDateTimeInstance(3, 3).format(backupDate);
         String age = bj.a(System.currentTimeMillis() - backupDate.getTime());
         String line2 = getLocalizedString("mco.configure.world.restore.question.line1", new Object[]{datePresentation, age});
         String line3 = getLocalizedString("mco.configure.world.restore.question.line2");
         Realms.setScreen(new aj(this, aj.a.a, line2, line3, true, 1));
      }

   }

   private void g() {
      String line2 = getLocalizedString("mco.configure.world.restore.download.question.line1");
      String line3 = getLocalizedString("mco.configure.world.restore.download.question.line2");
      Realms.setScreen(new aj(this, aj.a.b, line2, line3, true, 2));
   }

   private void h() {
      bh.b downloadTask = new bh.b(
         this.m.id, this.h, this.m.name + " (" + ((RealmsWorldOptions)this.m.slots.get(this.m.activeSlot)).getSlotName(this.m.activeSlot) + ")", this
      );
      ak longRunningMcoTaskScreen = new ak(this.c.b(), downloadTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void confirmResult(boolean result, int id) {
      if (result && id == 1) {
         this.i();
      } else if (id == 1) {
         this.g = -1;
         Realms.setScreen(this);
      } else if (result && id == 2) {
         this.h();
      } else {
         Realms.setScreen(this);
      }

   }

   private void i() {
      Backup backup = (Backup)this.d.get(this.g);
      this.g = -1;
      bh.g restoreTask = new bh.g(backup, this.m.id, this.c);
      ak longRunningMcoTaskScreen = new ak(this.c.b(), restoreTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void render(int xm, int ym, float a) {
      this.e = null;
      this.renderBackground();
      this.f.render(xm, ym, a);
      this.n.render(this);
      this.drawString(getLocalizedString("mco.configure.world.backup"), (this.width() - 150) / 2 - 90, 20, 10526880);
      if (this.l) {
         this.drawString(getLocalizedString("mco.backup.nobackups"), 20, this.height() / 2 - 10, 16777215);
      }

      this.i.active(!this.l);
      super.render(xm, ym, a);
      if (this.e != null) {
         this.a(this.e, xm, ym);
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
         super(z.this.width() - 150, z.this.height(), 32, z.this.height() - 15, 36);
      }

      public void a(Backup backup) {
         this.addEntry(z.this.new b(backup));
      }

      public int getRowWidth() {
         return (int)((double)this.width() * 0.93);
      }

      public boolean isFocused() {
         return z.this.isFocused(this);
      }

      public int getItemCount() {
         return z.this.d.size();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         z.this.renderBackground();
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum != 0) {
            return false;
         } else if (xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll();
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.selectItem(slot);
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
            }

            return true;
         } else {
            return false;
         }
      }

      public int getScrollbarPosition() {
         return this.width() - 5;
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         int infox = this.width() - 35;
         int infoy = slot * this.itemHeight() + 36 - this.getScroll();
         int mx = infox + 10;
         int my = infoy - 3;
         if (xm >= (double)infox && xm <= (double)(infox + 9) && ym >= (double)infoy && ym <= (double)(infoy + 9)) {
            if (!((Backup)z.this.d.get(slot)).changeList.isEmpty()) {
               z.this.g = -1;
               z.b = this.getScroll();
               Realms.setScreen(new y(z.this, (Backup)z.this.d.get(slot)));
            }
         } else if (xm >= (double)mx && xm < (double)(mx + 13) && ym >= (double)my && ym < (double)(my + 15)) {
            z.b = this.getScroll();
            z.this.b(slot);
         }

      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((Backup)z.this.d.get(item)).lastModifiedDate.toString()}));
         }

         this.a(item);
      }

      public void a(int item) {
         z.this.g = item;
         z.this.d();
      }
   }

   class b extends RealmListEntry {
      final Backup a;

      public b(Backup backup) {
         this.a = backup;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft - 40, rowTop, mouseX, mouseY);
      }

      private void a(Backup backup, int x, int y, int mouseX, int mouseY) {
         int color = backup.isUploadedVersion() ? -8388737 : 16777215;
         z.this.drawString("Backup (" + bj.a(System.currentTimeMillis() - backup.lastModifiedDate.getTime()) + ")", x + 40, y + 1, color);
         z.this.drawString(this.a(backup.lastModifiedDate), x + 40, y + 12, 5000268);
         int dx = z.this.width() - 175;
         int dy = -3;
         int infox = dx - 10;
         int infoy = 0;
         if (!z.this.m.expired) {
            this.a(dx, y + -3, mouseX, mouseY);
         }

         if (!backup.changeList.isEmpty()) {
            this.b(infox, y + 0, mouseX, mouseY);
         }

      }

      private String a(Date lastModifiedDate) {
         return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
      }

      private void a(int x, int y, int xm, int ym) {
         boolean hovered = xm >= x && xm <= x + 12 && ym >= y && ym <= y + 14 && ym < z.this.height() - 15 && ym > 32;
         RealmsScreen.bind("realms:textures/gui/realms/restore_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, hovered ? 28.0F : 0.0F, 23, 28, 23, 56);
         GlStateManager.popMatrix();
         if (hovered) {
            z.this.e = RealmsScreen.getLocalizedString("mco.backup.button.restore");
         }

      }

      private void b(int x, int y, int xm, int ym) {
         boolean hovered = xm >= x && xm <= x + 8 && ym >= y && ym <= y + 8 && ym < z.this.height() - 15 && ym > 32;
         RealmsScreen.bind("realms:textures/gui/realms/plus_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, hovered ? 15.0F : 0.0F, 15, 15, 15, 30);
         GlStateManager.popMatrix();
         if (hovered) {
            z.this.e = RealmsScreen.getLocalizedString("mco.backup.changes.tooltip");
         }

      }
   }
}
