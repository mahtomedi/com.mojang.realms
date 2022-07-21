package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.FileUpload;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.UploadTokenCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.realms.Tezzelator;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class RealmsUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int CANCEL_BUTTON = 0;
   private static final int BACK_BUTTON = 1;
   private final RealmsScreen lastScreen;
   private final RealmsLevelSummary selectedLevel;
   private final long worldId;
   private final int slotId;
   private final UploadStatus uploadStatus;
   private volatile String errorMessage = null;
   private volatile String status = null;
   private volatile String progress = null;
   private volatile boolean cancelled = false;
   private volatile boolean uploadFinished = false;
   private volatile boolean showDots = true;
   private RealmsButton backButton;
   private RealmsButton cancelButton;
   private int animTick = 0;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private int dotIndex = 0;
   private Long previousWrittenBytes = null;
   private Long previousTimeSnapshot = null;
   private long bytesPersSecond = 0L;
   private static final ReentrantLock uploadLock = new ReentrantLock();
   public static final long SIZE_LIMIT = 524288000L;

   public RealmsUploadScreen(long worldId, int slotId, RealmsScreen lastScreen, RealmsLevelSummary selectedLevel) {
      this.worldId = worldId;
      this.slotId = slotId;
      this.lastScreen = lastScreen;
      this.selectedLevel = selectedLevel;
      this.uploadStatus = new UploadStatus();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.backButton = newButton(1, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.back"));
      this.buttonsAdd(this.cancelButton = newButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")));
      this.upload();
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 0) {
            this.cancelled = true;
         }

      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
         this.status = getLocalizedString("mco.upload.verifying");
      }

      this.drawCenteredString(this.status, this.width() / 2, 50, 16777215);
      if (this.showDots) {
         this.drawDots();
      }

      if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
         this.drawProgressBar();
         this.drawUploadSpeed();
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
      double percentage = this.uploadStatus.bytesWritten.doubleValue() / this.uploadStatus.totalBytes.doubleValue() * 100.0;
      if (percentage > 100.0) {
         percentage = 100.0;
      }

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

   private void drawUploadSpeed() {
      if (this.animTick % RealmsSharedConstants.TICKS_PER_SECOND == 0) {
         if (this.previousWrittenBytes != null) {
            long timeElapsed = System.currentTimeMillis() - this.previousTimeSnapshot;
            if (timeElapsed == 0L) {
               timeElapsed = 1L;
            }

            this.bytesPersSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / timeElapsed;
            this.drawUploadSpeed0(this.bytesPersSecond);
         }

         this.previousWrittenBytes = this.uploadStatus.bytesWritten;
         this.previousTimeSnapshot = System.currentTimeMillis();
      } else {
         this.drawUploadSpeed0(this.bytesPersSecond);
      }

   }

   private void drawUploadSpeed0(long bytesPersSecond) {
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

   public void tick() {
      super.tick();
      ++this.animTick;
   }

   private void upload() {
      (new Thread() {
            public void run() {
               File archive = null;
               RealmsClient client = RealmsClient.createRealmsClient();
               long wid = RealmsUploadScreen.this.worldId;
   
               try {
                  if (RealmsUploadScreen.uploadLock.tryLock(1L, TimeUnit.SECONDS)) {
                     RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.preparing");
                     UploadInfo uploadInfo = client.upload(wid, UploadTokenCache.get(wid));
                     UploadTokenCache.put(wid, uploadInfo.getToken());
                     if (!uploadInfo.isWorldClosed()) {
                        RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.close.failure");
                        return;
                     }
   
                     if (RealmsUploadScreen.this.cancelled) {
                        RealmsUploadScreen.this.uploadCancelled(wid);
                        return;
                     }
   
                     File saves = new File(Realms.getGameDirectoryPath(), "saves");
                     archive = RealmsUploadScreen.this.tarGzipArchive(new File(saves, RealmsUploadScreen.this.selectedLevel.getLevelId()));
                     if (RealmsUploadScreen.this.cancelled) {
                        RealmsUploadScreen.this.uploadCancelled(wid);
                        return;
                     }
   
                     if (!RealmsUploadScreen.this.verify(archive)) {
                        RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString(
                           "mco.upload.size.failure", new Object[]{RealmsUploadScreen.this.selectedLevel.getLevelName()}
                        );
                        return;
                     }
   
                     RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString(
                        "mco.upload.uploading", new Object[]{RealmsUploadScreen.this.selectedLevel.getLevelName()}
                     );
                     FileUpload fileUpload = new FileUpload();
                     fileUpload.upload(
                        archive,
                        RealmsUploadScreen.this.worldId,
                        RealmsUploadScreen.this.slotId,
                        uploadInfo,
                        Realms.getSessionId(),
                        Realms.getName(),
                        RealmsSharedConstants.VERSION_STRING,
                        RealmsUploadScreen.this.uploadStatus
                     );
   
                     while(!fileUpload.isFinished()) {
                        if (RealmsUploadScreen.this.cancelled) {
                           fileUpload.cancel();
                           RealmsUploadScreen.this.uploadCancelled(wid);
                           return;
                        }
   
                        try {
                           Thread.sleep(500L);
                        } catch (InterruptedException var28) {
                           RealmsUploadScreen.LOGGER.error("Failed to check Realms file upload status");
                        }
                     }
   
                     if (fileUpload.getStatusCode() >= 200 && fileUpload.getStatusCode() < 300) {
                        RealmsUploadScreen.this.uploadFinished = true;
                        RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.done");
                        RealmsUploadScreen.this.backButton.msg(RealmsScreen.getLocalizedString("gui.done"));
                        UploadTokenCache.invalidate(wid);
                     } else {
                        RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{fileUpload.getStatusCode()});
                     }
   
                     return;
                  }
               } catch (IOException var29) {
                  RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var29.getMessage()});
                  return;
               } catch (RealmsServiceException var30) {
                  RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var30.toString()});
                  return;
               } catch (InterruptedException var31) {
                  RealmsUploadScreen.LOGGER.error("Could not acquire upload lock");
                  return;
               } finally {
                  if (RealmsUploadScreen.uploadLock.isHeldByCurrentThread()) {
                     RealmsUploadScreen.uploadLock.unlock();
                     RealmsUploadScreen.this.showDots = false;
                     RealmsUploadScreen.this.buttonsRemove(RealmsUploadScreen.this.cancelButton);
                     RealmsUploadScreen.this.buttonsAdd(RealmsUploadScreen.this.backButton);
                     if (archive != null) {
                        RealmsUploadScreen.LOGGER.debug("Deleting file " + archive.getAbsolutePath());
                        archive.delete();
                     }
   
                     try {
                        client.uploadFinished(wid);
                     } catch (RealmsServiceException var27) {
                        RealmsUploadScreen.LOGGER.error("Failed to request upload-finished to Realms", new Object[]{var27.toString()});
                     }
   
                  }
   
                  return;
               }
   
            }
         })
         .start();
   }

   private void uploadCancelled(long worldId) {
      this.status = getLocalizedString("mco.upload.cancelled");
      UploadTokenCache.invalidate(worldId);
   }

   private boolean verify(File archive) {
      return archive.length() < 524288000L;
   }

   private File tarGzipArchive(File pathToDirectoryFile) throws IOException {
      TarArchiveOutputStream tar = null;

      File var4;
      try {
         File file = File.createTempFile("realms-upload-file", ".tar.gz");
         tar = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
         this.addFileToTarGz(tar, pathToDirectoryFile.getAbsolutePath(), "world", true);
         tar.finish();
         var4 = file;
      } finally {
         if (tar != null) {
            tar.close();
         }

      }

      return var4;
   }

   private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base, boolean root) throws IOException {
      if (!this.cancelled) {
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
                  this.addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/", false);
               }
            }
         }

      }
   }
}
