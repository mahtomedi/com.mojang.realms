package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.FileDownload;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class DownloadLatestWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsScreen lastScreen;
   private final String downloadLink;
   private RealmsButton cancelButton;
   private final String worldName;
   private final DownloadLatestWorldScreen.DownloadStatus downloadStatus;
   private volatile String errorMessage = null;
   private volatile String status = null;
   private volatile String progress = null;
   private volatile boolean cancelled = false;
   private volatile boolean showDots = true;
   private volatile boolean finished = false;
   private volatile boolean extracting = false;
   private Long previousWrittenBytes = null;
   private Long previousTimeSnapshot = null;
   private long bytesPersSecond = 0L;
   private int animTick = 0;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private int dotIndex = 0;
   private static final ReentrantLock downloadLock = new ReentrantLock();

   public DownloadLatestWorldScreen(RealmsScreen lastScreen, String downloadLink, String worldName) {
      this.lastScreen = lastScreen;
      this.worldName = worldName;
      this.downloadLink = downloadLink;
      this.downloadStatus = new DownloadLatestWorldScreen.DownloadStatus();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(this.cancelButton = newButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")));
      this.downloadSave();
   }

   public void tick() {
      super.tick();
      ++this.animTick;
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            this.cancelled = true;
            this.backButtonClicked();
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         this.cancelled = true;
         this.backButtonClicked();
      }

   }

   private void backButtonClicked() {
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
      this.progress = String.format("%.1f", percentage);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3553);
      Tezzelator t = Tezzelator.instance;
      t.begin();
      double base = (double)(this.width() / 2 - 100);
      double diff = 0.5;
      t.color(14275282);
      t.vertex(base - diff, 95.0 + diff, 0.0);
      t.vertex(base + 200.0 * percentage / 100.0 + diff, 95.0 + diff, 0.0);
      t.vertex(base + 200.0 * percentage / 100.0 + diff, 80.0 - diff, 0.0);
      t.vertex(base - diff, 80.0 - diff, 0.0);
      t.color(8421504);
      t.vertex(base, 95.0, 0.0);
      t.vertex(base + 200.0 * percentage / 100.0, 95.0, 0.0);
      t.vertex(base + 200.0 * percentage / 100.0, 80.0, 0.0);
      t.vertex(base, 80.0, 0.0);
      t.end();
      GL11.glEnable(3553);
      this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
   }

   private void drawDownloadSpeed() {
      if (this.animTick % RealmsSharedConstants.TICKS_PER_SECOND == 0) {
         if (this.previousWrittenBytes != null) {
            this.bytesPersSecond = 1000L
               * (this.downloadStatus.bytesWritten - this.previousWrittenBytes)
               / (System.currentTimeMillis() - this.previousTimeSnapshot);
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
         String stringPresentation = "(" + humanReadableByteCount(bytesPersSecond) + ")";
         this.drawString(stringPresentation, this.width() / 2 + progressLength / 2 + 15, 84, 16777215);
      }

   }

   public static String humanReadableByteCount(long bytes) {
      int unit = 1024;
      if (bytes < (long)unit) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log((double)unit));
         String pre = "KMGTPE".charAt(exp - 1) + "";
         return String.format("%.1f %sB/s", (double)bytes / Math.pow((double)unit, (double)exp), pre);
      }
   }

   public void mouseEvent() {
      super.mouseEvent();
   }

   private void downloadSave() {
      (new Thread() {
            public void run() {
               try {
                  if (DownloadLatestWorldScreen.downloadLock.tryLock(1L, TimeUnit.SECONDS)) {
                     DownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.preparing");
                     if (DownloadLatestWorldScreen.this.cancelled) {
                        DownloadLatestWorldScreen.this.downloadCancelled();
                        return;
                     }
   
                     DownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString(
                        "mco.download.downloading", new Object[]{DownloadLatestWorldScreen.this.worldName}
                     );
                     FileDownload fileDownload = new FileDownload();
                     fileDownload.download(
                        DownloadLatestWorldScreen.this.downloadLink,
                        DownloadLatestWorldScreen.this.worldName,
                        DownloadLatestWorldScreen.this.downloadStatus,
                        DownloadLatestWorldScreen.this.getLevelStorageSource()
                     );
   
                     while(!fileDownload.isFinished()) {
                        if (fileDownload.isError()) {
                           fileDownload.cancel();
                           DownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                           DownloadLatestWorldScreen.this.cancelButton.msg(RealmsScreen.getLocalizedString("gui.done"));
                           return;
                        }
   
                        if (fileDownload.isExtracting()) {
                           DownloadLatestWorldScreen.this.extracting = true;
                        }
   
                        if (DownloadLatestWorldScreen.this.cancelled) {
                           fileDownload.cancel();
                           DownloadLatestWorldScreen.this.downloadCancelled();
                           return;
                        }
   
                        try {
                           Thread.sleep(500L);
                        } catch (InterruptedException var8) {
                           DownloadLatestWorldScreen.LOGGER.error("Failed to check Realms backup download status");
                        }
                     }
   
                     DownloadLatestWorldScreen.this.finished = true;
                     DownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.done");
                     DownloadLatestWorldScreen.this.cancelButton.msg(RealmsScreen.getLocalizedString("gui.done"));
                     return;
                  }
               } catch (InterruptedException var9) {
                  DownloadLatestWorldScreen.LOGGER.error("Could not acquire upload lock");
                  return;
               } catch (Exception var10) {
                  DownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                  var10.printStackTrace();
                  return;
               } finally {
                  if (!DownloadLatestWorldScreen.downloadLock.isHeldByCurrentThread()) {
                     return;
                  }
   
                  DownloadLatestWorldScreen.downloadLock.unlock();
                  DownloadLatestWorldScreen.this.showDots = false;
                  DownloadLatestWorldScreen.this.buttonsRemove(DownloadLatestWorldScreen.this.cancelButton);
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
