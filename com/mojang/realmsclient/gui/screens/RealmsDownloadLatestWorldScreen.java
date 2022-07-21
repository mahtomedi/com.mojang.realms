package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
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

public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsScreen lastScreen;
   private final WorldDownload worldDownload;
   private RealmsButton cancelButton;
   private final String worldName;
   private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
   private volatile String errorMessage;
   private volatile String status;
   private volatile String progress;
   private volatile boolean cancelled;
   private volatile boolean showDots = true;
   private volatile boolean finished;
   private volatile boolean extracting;
   private Long previousWrittenBytes;
   private Long previousTimeSnapshot;
   private long bytesPersSecond;
   private int animTick;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private int dotIndex;
   private final int WARNING_ID = 100;
   private int confirmationId = -1;
   private boolean checked;
   private static final ReentrantLock downloadLock = new ReentrantLock();

   public RealmsDownloadLatestWorldScreen(RealmsScreen lastScreen, WorldDownload worldDownload, String worldName) {
      this.lastScreen = lastScreen;
      this.worldName = worldName;
      this.worldDownload = worldDownload;
      this.downloadStatus = new RealmsDownloadLatestWorldScreen.DownloadStatus();
   }

   public void setConfirmationId(int confirmationId) {
      this.confirmationId = confirmationId;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            RealmsDownloadLatestWorldScreen.this.cancelled = true;
            RealmsDownloadLatestWorldScreen.this.backButtonClicked();
         }
      });
      this.checkDownloadSize();
   }

   private void checkDownloadSize() {
      if (!this.finished) {
         if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
            String line1 = getLocalizedString("mco.download.confirmation.line1", new Object[]{humanReadableSize(5368709120L)});
            String line2 = getLocalizedString("mco.download.confirmation.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, line1, line2, false, 100));
         } else {
            this.downloadSave();
         }

      }
   }

   public void confirmResult(boolean result, int id) {
      this.checked = true;
      Realms.setScreen(this);
      this.downloadSave();
   }

   private long getContentLength(String downloadLink) {
      FileDownload fileDownload = new FileDownload();
      return fileDownload.contentLength(downloadLink);
   }

   public void tick() {
      super.tick();
      ++this.animTick;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.cancelled = true;
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void backButtonClicked() {
      if (this.finished && this.confirmationId != -1 && this.errorMessage == null) {
         this.lastScreen.confirmResult(true, this.confirmationId);
      }

      Realms.setScreen(this.lastScreen);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (this.extracting && !this.finished) {
         this.status = getLocalizedString("mco.download.extracting");
      }

      this.drawCenteredString(getLocalizedString("mco.download.title"), this.width() / 2, 20, 16777215);
      this.drawCenteredString(this.status, this.width() / 2, 50, 16777215);
      if (this.showDots) {
         this.drawDots();
      }

      if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
         this.drawProgressBar();
         this.drawDownloadSpeed();
      }

      if (this.errorMessage != null) {
         this.drawCenteredString(this.errorMessage, this.width() / 2, 110, 16711680);
      }

      super.render(xm, ym, a);
   }

   private void drawDots() {
      int statusWidth = this.fontWidth(this.status);
      if (this.animTick % 10 == 0) {
         ++this.dotIndex;
      }

      this.drawString(DOTS[this.dotIndex % DOTS.length], this.width() / 2 + statusWidth / 2 + 5, 50, 16777215);
   }

   private void drawProgressBar() {
      double percentage = this.downloadStatus.bytesWritten.doubleValue() / this.downloadStatus.totalBytes.doubleValue() * 100.0;
      this.progress = String.format(Locale.ROOT, "%.1f", percentage);
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
      this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
   }

   private void drawDownloadSpeed() {
      if (this.animTick % 20 == 0) {
         if (this.previousWrittenBytes != null) {
            long timeElapsed = System.currentTimeMillis() - this.previousTimeSnapshot;
            if (timeElapsed == 0L) {
               timeElapsed = 1L;
            }

            this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / timeElapsed;
            this.drawDownloadSpeed0(this.bytesPersSecond);
         }

         this.previousWrittenBytes = this.downloadStatus.bytesWritten;
         this.previousTimeSnapshot = System.currentTimeMillis();
      } else {
         this.drawDownloadSpeed0(this.bytesPersSecond);
      }

   }

   private void drawDownloadSpeed0(long bytesPersSecond) {
      if (bytesPersSecond > 0L) {
         int progressLength = this.fontWidth(this.progress);
         String stringPresentation = "(" + humanReadableSpeed(bytesPersSecond) + ")";
         this.drawString(stringPresentation, this.width() / 2 + progressLength / 2 + 15, 84, 16777215);
      }

   }

   public static String humanReadableSpeed(long bytes) {
      int unit = 1024;
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format(Locale.ROOT, "%.1f %sB/s", (double)bytes / Math.pow(1024.0, (double)exp), pre);
      }
   }

   public static String humanReadableSize(long bytes) {
      int unit = 1024;
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format(Locale.ROOT, "%.0f %sB", (double)bytes / Math.pow(1024.0, (double)exp), pre);
      }
   }

   private void downloadSave() {
      (new Thread() {
            public void run() {
               try {
                  if (RealmsDownloadLatestWorldScreen.downloadLock.tryLock(1L, TimeUnit.SECONDS)) {
                     RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.preparing");
                     if (RealmsDownloadLatestWorldScreen.this.cancelled) {
                        RealmsDownloadLatestWorldScreen.this.downloadCancelled();
                        return;
                     }
   
                     RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString(
                        "mco.download.downloading", new Object[]{RealmsDownloadLatestWorldScreen.this.worldName}
                     );
                     FileDownload fileDownload = new FileDownload();
                     fileDownload.contentLength(RealmsDownloadLatestWorldScreen.this.worldDownload.downloadLink);
                     fileDownload.download(
                        RealmsDownloadLatestWorldScreen.this.worldDownload,
                        RealmsDownloadLatestWorldScreen.this.worldName,
                        RealmsDownloadLatestWorldScreen.this.downloadStatus,
                        RealmsDownloadLatestWorldScreen.this.getLevelStorageSource()
                     );
   
                     while(!fileDownload.isFinished()) {
                        if (fileDownload.isError()) {
                           fileDownload.cancel();
                           RealmsDownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                           RealmsDownloadLatestWorldScreen.this.cancelButton.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                           return;
                        }
   
                        if (fileDownload.isExtracting()) {
                           RealmsDownloadLatestWorldScreen.this.extracting = true;
                        }
   
                        if (RealmsDownloadLatestWorldScreen.this.cancelled) {
                           fileDownload.cancel();
                           RealmsDownloadLatestWorldScreen.this.downloadCancelled();
                           return;
                        }
   
                        try {
                           Thread.sleep(500L);
                        } catch (InterruptedException var8) {
                           RealmsDownloadLatestWorldScreen.LOGGER.error("Failed to check Realms backup download status");
                        }
                     }
   
                     RealmsDownloadLatestWorldScreen.this.finished = true;
                     RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.done");
                     RealmsDownloadLatestWorldScreen.this.cancelButton.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                     return;
                  }
               } catch (InterruptedException var9) {
                  RealmsDownloadLatestWorldScreen.LOGGER.error("Could not acquire upload lock");
                  return;
               } catch (Exception var10) {
                  RealmsDownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                  var10.printStackTrace();
                  return;
               } finally {
                  if (!RealmsDownloadLatestWorldScreen.downloadLock.isHeldByCurrentThread()) {
                     return;
                  }
   
                  RealmsDownloadLatestWorldScreen.downloadLock.unlock();
                  RealmsDownloadLatestWorldScreen.this.showDots = false;
                  RealmsDownloadLatestWorldScreen.this.finished = true;
               }
   
            }
         })
         .start();
   }

   private void downloadCancelled() {
      this.status = getLocalizedString("mco.download.cancelled");
   }

   public class DownloadStatus {
      public volatile Long bytesWritten = 0L;
      public volatile Long totalBytes = 0L;
   }
}
