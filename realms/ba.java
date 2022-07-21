package realms;

import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.UploadInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ba extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final ar b;
   private final RealmsLevelSummary c;
   private final long d;
   private final int e;
   private final k f;
   private final RateLimiter g;
   private volatile String h;
   private volatile String i;
   private volatile String j;
   private volatile boolean k;
   private volatile boolean l;
   private volatile boolean m = true;
   private volatile boolean n;
   private RealmsButton o;
   private RealmsButton p;
   private int q;
   private static final String[] r = new String[]{"", ".", ". .", ". . ."};
   private int s;
   private Long t;
   private Long u;
   private long v;
   private static final ReentrantLock w = new ReentrantLock();

   public ba(long worldId, int slotId, ar lastScreen, RealmsLevelSummary selectedLevel) {
      this.d = worldId;
      this.e = slotId;
      this.b = lastScreen;
      this.c = selectedLevel;
      this.f = new k();
      this.g = RateLimiter.create(0.1F);
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.o = new RealmsButton(1, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            ba.this.c();
         }
      };
      this.buttonsAdd(this.p = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            ba.this.d();
         }
      });
      if (!this.n) {
         if (this.b.a == -1) {
            this.h();
         } else {
            this.b.a(this);
         }
      }

   }

   public void confirmResult(boolean result, int buttonId) {
      if (result && !this.n) {
         this.n = true;
         Realms.setScreen(this);
         this.h();
      }

   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   private void c() {
      this.b.confirmResult(true, 0);
   }

   private void d() {
      this.k = true;
      Realms.setScreen(this.b);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         if (this.m) {
            this.d();
         } else {
            this.c();
         }

         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (!this.l && this.f.a != 0L && this.f.a == this.f.b) {
         this.i = getLocalizedString("mco.upload.verifying");
         this.p.active(false);
      }

      this.drawCenteredString(this.i, this.width() / 2, 50, 16777215);
      if (this.m) {
         this.e();
      }

      if (this.f.a != 0L && !this.k) {
         this.f();
         this.g();
      }

      if (this.h != null) {
         String[] errorMessages = this.h.split("\\\\n");

         for(int i = 0; i < errorMessages.length; ++i) {
            this.drawCenteredString(errorMessages[i], this.width() / 2, 110 + 12 * i, 16711680);
         }
      }

      super.render(xm, ym, a);
   }

   private void e() {
      int statusWidth = this.fontWidth(this.i);
      if (this.q % 10 == 0) {
         ++this.s;
      }

      this.drawString(r[this.s % r.length], this.width() / 2 + statusWidth / 2 + 5, 50, 16777215);
   }

   private void f() {
      double percentage = this.f.a.doubleValue() / this.f.b.doubleValue() * 100.0;
      if (percentage > 100.0) {
         percentage = 100.0;
      }

      this.j = String.format(Locale.ROOT, "%.1f", percentage);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableTexture();
      double base = (double)(this.width() / 2 - 100);
      double diff = 0.5;
      Tezzelator t = Tezzelator.instance;
      t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
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
      this.drawCenteredString(this.j + " %", this.width() / 2, 84, 16777215);
   }

   private void g() {
      if (this.q % 20 == 0) {
         if (this.t != null) {
            long timeElapsed = System.currentTimeMillis() - this.u;
            if (timeElapsed == 0L) {
               timeElapsed = 1L;
            }

            this.v = 1000L * (this.f.a - this.t) / timeElapsed;
            this.c(this.v);
         }

         this.t = this.f.a;
         this.u = System.currentTimeMillis();
      } else {
         this.c(this.v);
      }

   }

   private void c(long bytesPersSecond) {
      if (bytesPersSecond > 0L) {
         int progressLength = this.fontWidth(this.j);
         String stringPresentation = "(" + a(bytesPersSecond) + ")";
         this.drawString(stringPresentation, this.width() / 2 + progressLength / 2 + 15, 84, 16777215);
      }

   }

   public static String a(long bytes) {
      int unit = 1024;
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format(Locale.ROOT, "%.1f %sB/s", (double)bytes / Math.pow(1024.0, (double)exp), pre);
      }
   }

   public void tick() {
      super.tick();
      ++this.q;
      if (this.i != null && this.g.tryAcquire(1)) {
         ArrayList<String> elements = new ArrayList();
         elements.add(this.i);
         if (this.j != null) {
            elements.add(this.j + "%");
         }

         if (this.h != null) {
            elements.add(this.h);
         }

         Realms.narrateNow(String.join(System.lineSeparator(), elements));
      }

   }

   public static ba.a b(long bytes) {
      if (bytes < 1024L) {
         return ba.a.a;
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";

         try {
            return ba.a.valueOf(pre + "B");
         } catch (Exception var5) {
            return ba.a.d;
         }
      }
   }

   public static double a(long bytes, ba.a unit) {
      return unit.equals(ba.a.a) ? (double)bytes : (double)bytes / Math.pow(1024.0, (double)unit.ordinal());
   }

   public static String b(long bytes, ba.a unit) {
      return String.format("%." + (unit.equals(ba.a.d) ? "1" : "0") + "f %s", a(bytes, unit), unit.name());
   }

   private void h() {
      this.n = true;
      (new Thread() {
            public void run() {
               File archive = null;
               g client = realms.g.a();
               long wid = ba.this.d;
   
               try {
                  if (ba.w.tryLock(1L, TimeUnit.SECONDS)) {
                     ba.this.i = RealmsScreen.getLocalizedString("mco.upload.preparing");
                     UploadInfo uploadInfo = null;
   
                     for(int i = 0; i < 20; ++i) {
                        try {
                           if (ba.this.k) {
                              ba.this.i();
                              return;
                           }
   
                           uploadInfo = client.h(wid, bn.a(wid));
                           break;
                        } catch (p var20) {
                           Thread.sleep((long)(var20.e * 1000));
                        }
                     }
   
                     if (uploadInfo == null) {
                        ba.this.i = RealmsScreen.getLocalizedString("mco.upload.close.failure");
                        return;
                     }
   
                     bn.a(wid, uploadInfo.getToken());
                     if (!uploadInfo.isWorldClosed()) {
                        ba.this.i = RealmsScreen.getLocalizedString("mco.upload.close.failure");
                        return;
                     }
   
                     if (ba.this.k) {
                        ba.this.i();
                        return;
                     }
   
                     File saves = new File(Realms.getGameDirectoryPath(), "saves");
                     archive = ba.this.b(new File(saves, ba.this.c.getLevelId()));
                     if (ba.this.k) {
                        ba.this.i();
                        return;
                     }
   
                     if (ba.this.a(archive)) {
                        ba.this.i = RealmsScreen.getLocalizedString("mco.upload.uploading", new Object[]{ba.this.c.getLevelName()});
                        d fileUpload = new d(
                           archive, ba.this.d, ba.this.e, uploadInfo, Realms.getSessionId(), Realms.getName(), Realms.getMinecraftVersionString(), ba.this.f
                        );
                        fileUpload.a(uploadResult -> {
                           if (uploadResult.a >= 200 && uploadResult.a < 300) {
                              ba.this.l = true;
                              ba.this.i = RealmsScreen.getLocalizedString("mco.upload.done");
                              ba.this.o.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                              bn.b(wid);
                           } else if (uploadResult.a == 400 && uploadResult.b != null) {
                              ba.this.h = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{uploadResult.b});
                           } else {
                              ba.this.h = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{uploadResult.a});
                           }
   
                        });
   
                        while(!fileUpload.b()) {
                           if (ba.this.k) {
                              fileUpload.a();
                              ba.this.i();
                              return;
                           }
   
                           try {
                              Thread.sleep(500L);
                           } catch (InterruptedException var19) {
                              ba.a.error("Failed to check Realms file upload status");
                           }
                        }
   
                        return;
                     }
   
                     long length = archive.length();
                     ba.a lengthUnit = ba.b(length);
                     ba.a maxUnit = ba.b(5368709120L);
                     if (ba.b(length, lengthUnit).equals(ba.b(5368709120L, maxUnit)) && lengthUnit != ba.a.a) {
                        ba.a unitToUse = ba.a.values()[lengthUnit.ordinal() - 1];
                        ba.this.h = RealmsScreen.getLocalizedString("mco.upload.size.failure.line1", new Object[]{ba.this.c.getLevelName()})
                           + "\\n"
                           + RealmsScreen.getLocalizedString(
                              "mco.upload.size.failure.line2", new Object[]{ba.b(length, unitToUse), ba.b(5368709120L, unitToUse)}
                           );
                        return;
                     }
   
                     ba.this.h = RealmsScreen.getLocalizedString("mco.upload.size.failure.line1", new Object[]{ba.this.c.getLevelName()})
                        + "\\n"
                        + RealmsScreen.getLocalizedString("mco.upload.size.failure.line2", new Object[]{ba.b(length, lengthUnit), ba.b(5368709120L, maxUnit)});
                     return;
                  }
               } catch (IOException var21) {
                  ba.this.h = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var21.getMessage()});
                  return;
               } catch (o var22) {
                  ba.this.h = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var22.toString()});
                  return;
               } catch (InterruptedException var23) {
                  ba.a.error("Could not acquire upload lock");
                  return;
               } finally {
                  ba.this.l = true;
                  if (ba.w.isHeldByCurrentThread()) {
                     ba.w.unlock();
                     ba.this.m = false;
                     ba.this.childrenClear();
                     ba.this.buttonsAdd(ba.this.o);
                     if (archive != null) {
                        ba.a.debug("Deleting file " + archive.getAbsolutePath());
                        archive.delete();
                     }
   
                  }
   
                  return;
               }
   
            }
         })
         .start();
   }

   private void i() {
      this.i = getLocalizedString("mco.upload.cancelled");
      a.debug("Upload was cancelled");
   }

   private boolean a(File archive) {
      return archive.length() < 5368709120L;
   }

   private File b(File pathToDirectoryFile) throws IOException {
      TarArchiveOutputStream tar = null;

      File var4;
      try {
         File file = File.createTempFile("realms-upload-file", ".tar.gz");
         tar = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
         tar.setLongFileMode(3);
         this.a(tar, pathToDirectoryFile.getAbsolutePath(), "world", true);
         tar.finish();
         var4 = file;
      } finally {
         if (tar != null) {
            tar.close();
         }

      }

      return var4;
   }

   private void a(TarArchiveOutputStream tOut, String path, String base, boolean root) throws IOException {
      if (!this.k) {
         File f = new File(path);
         String entryName = root ? base : base + f.getName();
         TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
         tOut.putArchiveEntry(tarEntry);
         if (f.isFile()) {
            IOUtils.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
         } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
               for(File child : children) {
                  this.a(tOut, child.getAbsolutePath(), entryName + "/", false);
               }
            }
         }

      }
   }

   static enum a {
      a,
      b,
      c,
      d;
   }
}
