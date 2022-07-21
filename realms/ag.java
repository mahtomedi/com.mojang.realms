package realms;

import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.WorldDownload;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ag extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final WorldDownload c;
   private final String d;
   private final RateLimiter e;
   private RealmsButton f;
   private final String g;
   private final ag.a h;
   private volatile String i;
   private volatile String j;
   private volatile String k;
   private volatile boolean l;
   private volatile boolean m = true;
   private volatile boolean n;
   private volatile boolean o;
   private Long p;
   private Long q;
   private long r;
   private int s;
   private static final String[] t = new String[]{"", ".", ". .", ". . ."};
   private int u;
   private final int v = 100;
   private int w = -1;
   private boolean x;
   private static final ReentrantLock y = new ReentrantLock();

   public ag(RealmsScreen lastScreen, WorldDownload worldDownload, String worldName) {
      this.b = lastScreen;
      this.g = worldName;
      this.c = worldDownload;
      this.h = new ag.a();
      this.d = getLocalizedString("mco.download.title");
      this.e = RateLimiter.create(0.1F);
   }

   public void a(int confirmationId) {
      this.w = confirmationId;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(this.f = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            ag.this.l = true;
            ag.this.d();
         }
      });
      this.c();
   }

   private void c() {
      if (!this.n) {
         if (!this.x && this.a(this.c.downloadLink) >= 5368709120L) {
            String line1 = getLocalizedString("mco.download.confirmation.line1", new Object[]{b(5368709120L)});
            String line2 = getLocalizedString("mco.download.confirmation.line2");
            Realms.setScreen(new aj(this, aj.a.a, line1, line2, false, 100));
         } else {
            this.h();
         }

      }
   }

   public void confirmResult(boolean result, int id) {
      this.x = true;
      Realms.setScreen(this);
      this.h();
   }

   private long a(String downloadLink) {
      c fileDownload = new c();
      return fileDownload.a(downloadLink);
   }

   public void tick() {
      super.tick();
      ++this.s;
      if (this.j != null && this.e.tryAcquire(1)) {
         ArrayList<String> elements = new ArrayList();
         elements.add(this.d);
         elements.add(this.j);
         if (this.k != null) {
            elements.add(this.k + "%");
            elements.add(a(this.r));
         }

         if (this.i != null) {
            elements.add(this.i);
         }

         String toNarrate = String.join(System.lineSeparator(), elements);
         Realms.narrateNow(toNarrate);
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.l = true;
         this.d();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void d() {
      if (this.n && this.w != -1 && this.i == null) {
         this.b.confirmResult(true, this.w);
      }

      Realms.setScreen(this.b);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (this.o && !this.n) {
         this.j = getLocalizedString("mco.download.extracting");
      }

      this.drawCenteredString(this.d, this.width() / 2, 20, 16777215);
      this.drawCenteredString(this.j, this.width() / 2, 50, 16777215);
      if (this.m) {
         this.e();
      }

      if (this.h.a != 0L && !this.l) {
         this.f();
         this.g();
      }

      if (this.i != null) {
         this.drawCenteredString(this.i, this.width() / 2, 110, 16711680);
      }

      super.render(xm, ym, a);
   }

   private void e() {
      int statusWidth = this.fontWidth(this.j);
      if (this.s % 10 == 0) {
         ++this.u;
      }

      this.drawString(t[this.u % t.length], this.width() / 2 + statusWidth / 2 + 5, 50, 16777215);
   }

   private void f() {
      double percentage = this.h.a.doubleValue() / this.h.b.doubleValue() * 100.0;
      this.k = String.format(Locale.ROOT, "%.1f", percentage);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableTexture();
      Tezzelator t = Tezzelator.instance;
      t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
      double base = (double)(this.width() / 2 - 100);
      double diff = 0.5;
      t.vertex(base - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
      t.vertex(base + 200.0 * percentage / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
      t.vertex(base + 200.0 * percentage / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
      t.vertex(base - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
      t.vertex(base, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
      t.vertex(base + 200.0 * percentage / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
      t.vertex(base + 200.0 * percentage / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
      t.vertex(base, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
      t.end();
      GlStateManager.enableTexture();
      this.drawCenteredString(this.k + " %", this.width() / 2, 84, 16777215);
   }

   private void g() {
      if (this.s % 20 == 0) {
         if (this.p != null) {
            long timeElapsed = System.currentTimeMillis() - this.q;
            if (timeElapsed == 0L) {
               timeElapsed = 1L;
            }

            this.r = 1000L * (this.h.a - this.p) / timeElapsed;
            this.c(this.r);
         }

         this.p = this.h.a;
         this.q = System.currentTimeMillis();
      } else {
         this.c(this.r);
      }

   }

   private void c(long bytesPersSecond) {
      if (bytesPersSecond > 0L) {
         int progressLength = this.fontWidth(this.k);
         String stringPresentation = "(" + a(bytesPersSecond) + ")";
         this.drawString(stringPresentation, this.width() / 2 + progressLength / 2 + 15, 84, 16777215);
      }

   }

   public static String a(long bytes) {
      int unit = 1024;
      if (bytes < 1024L) {
         return bytes + " B/s";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format(Locale.ROOT, "%.1f %sB/s", (double)bytes / Math.pow(1024.0, (double)exp), pre);
      }
   }

   public static String b(long bytes) {
      int unit = 1024;
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format(Locale.ROOT, "%.0f %sB", (double)bytes / Math.pow(1024.0, (double)exp), pre);
      }
   }

   private void h() {
      (new Thread() {
         public void run() {
            try {
               if (ag.y.tryLock(1L, TimeUnit.SECONDS)) {
                  ag.this.j = RealmsScreen.getLocalizedString("mco.download.preparing");
                  if (ag.this.l) {
                     ag.this.i();
                     return;
                  }

                  ag.this.j = RealmsScreen.getLocalizedString("mco.download.downloading", new Object[]{ag.this.g});
                  c fileDownload = new c();
                  fileDownload.a(ag.this.c.downloadLink);
                  fileDownload.a(ag.this.c, ag.this.g, ag.this.h, ag.this.getLevelStorageSource());

                  while(!fileDownload.b()) {
                     if (fileDownload.c()) {
                        fileDownload.a();
                        ag.this.i = RealmsScreen.getLocalizedString("mco.download.failed");
                        ag.this.f.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                        return;
                     }

                     if (fileDownload.d()) {
                        ag.this.o = true;
                     }

                     if (ag.this.l) {
                        fileDownload.a();
                        ag.this.i();
                        return;
                     }

                     try {
                        Thread.sleep(500L);
                     } catch (InterruptedException var8) {
                        ag.a.error("Failed to check Realms backup download status");
                     }
                  }

                  ag.this.n = true;
                  ag.this.j = RealmsScreen.getLocalizedString("mco.download.done");
                  ag.this.f.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                  return;
               }
            } catch (InterruptedException var9) {
               ag.a.error("Could not acquire upload lock");
               return;
            } catch (Exception var10) {
               ag.this.i = RealmsScreen.getLocalizedString("mco.download.failed");
               var10.printStackTrace();
               return;
            } finally {
               if (!ag.y.isHeldByCurrentThread()) {
                  return;
               }

               ag.y.unlock();
               ag.this.m = false;
               ag.this.n = true;
            }

         }
      }).start();
   }

   private void i() {
      this.j = getLocalizedString("mco.download.cancelled");
   }

   public class a {
      public volatile Long a = 0L;
      public volatile Long b = 0L;
   }
}
