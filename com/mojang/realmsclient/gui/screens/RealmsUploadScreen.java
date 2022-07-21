package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.FileUpload;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.UploadTokenCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.lwjgl.opengl.GL11;

public class RealmsUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int CANCEL_BUTTON = 0;
   private static final int BACK_BUTTON = 1;
   private final RealmsResetWorldScreen lastScreen;
   private final RealmsLevelSummary selectedLevel;
   private final long worldId;
   private final int slotId;
   private final UploadStatus uploadStatus;
   private volatile String errorMessage;
   private volatile String status;
   private volatile String progress;
   private volatile boolean cancelled;
   private volatile boolean uploadFinished;
   private volatile boolean showDots = true;
   private volatile boolean uploadStarted;
   private RealmsButton backButton;
   private RealmsButton cancelButton;
   private int animTick;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private int dotIndex;
   private Long previousWrittenBytes;
   private Long previousTimeSnapshot;
   private long bytesPersSecond;
   private static final ReentrantLock uploadLock = new ReentrantLock();
   private static final int baseUnit = 1024;

   public RealmsUploadScreen(long worldId, int slotId, RealmsResetWorldScreen lastScreen, RealmsLevelSummary selectedLevel) {
      this.worldId = worldId;
      this.slotId = slotId;
      this.lastScreen = lastScreen;
      this.selectedLevel = selectedLevel;
      this.uploadStatus = new UploadStatus();
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.backButton = new RealmsButton(1, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsUploadScreen.this.onBack();
         }
      };
      this.buttonsAdd(this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsUploadScreen.this.onCancel();
         }
      });
      if (!this.uploadStarted) {
         if (this.lastScreen.slot == -1) {
            this.upload();
         } else {
            this.lastScreen.switchSlot(this);
         }
      }

   }

   public void confirmResult(boolean result, int buttonId) {
      if (result && !this.uploadStarted) {
         this.uploadStarted = true;
         Realms.setScreen(this);
         this.upload();
      }

   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   private void onBack() {
      this.lastScreen.confirmResult(true, 0);
   }

   private void onCancel() {
      this.cancelled = true;
      Realms.setScreen(this.lastScreen);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         if (this.showDots) {
            this.onCancel();
         } else {
            this.onBack();
         }

         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
         this.status = getLocalizedString("mco.upload.verifying");
         this.cancelButton.active(false);
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
         String[] errorMessages = this.errorMessage.split("\\\\n");

         for(int i = 0; i < errorMessages.length; ++i) {
            this.drawCenteredString(errorMessages[i], this.width() / 2, 110 + 12 * i, 16711680);
         }
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

      this.progress = String.format(Locale.ROOT, "%.1f", percentage);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3553);
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
      GL11.glEnable(3553);
      this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
   }

   private void drawUploadSpeed() {
      if (this.animTick % 20 == 0) {
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
      ++this.animTick;
   }

   public static RealmsUploadScreen.Unit getLargestUnit(long bytes) {
      if (bytes < 1024L) {
         return RealmsUploadScreen.Unit.B;
      } else {
         int exp = (int)(Math.log((double)bytes) / Math.log(1024.0));
         String pre = "KMGTPE".charAt(exp - 1) + "";

         try {
            return RealmsUploadScreen.Unit.valueOf(pre + "B");
         } catch (Exception var5) {
            return RealmsUploadScreen.Unit.GB;
         }
      }
   }

   public static double convertToUnit(long bytes, RealmsUploadScreen.Unit unit) {
      return unit.equals(RealmsUploadScreen.Unit.B) ? (double)bytes : (double)bytes / Math.pow(1024.0, (double)unit.ordinal());
   }

   public static String humanReadableSize(long bytes, RealmsUploadScreen.Unit unit) {
      return String.format("%." + (unit.equals(RealmsUploadScreen.Unit.GB) ? "1" : "0") + "f %s", convertToUnit(bytes, unit), unit.name());
   }

   private void upload() {
      this.uploadStarted = true;
      (new Thread() {
            public void run() {
               File archive = null;
               RealmsClient client = RealmsClient.createRealmsClient();
               long wid = RealmsUploadScreen.this.worldId;
   
               try {
                  if (RealmsUploadScreen.uploadLock.tryLock(1L, TimeUnit.SECONDS)) {
                     RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.preparing");
                     UploadInfo uploadInfo = null;
   
                     for(int i = 0; i < 20; ++i) {
                        try {
                           if (RealmsUploadScreen.this.cancelled) {
                              RealmsUploadScreen.this.uploadCancelled();
                              return;
                           }
   
                           uploadInfo = client.upload(wid, UploadTokenCache.get(wid));
                           break;
                        } catch (RetryCallException var20) {
                           Thread.sleep((long)(var20.delaySeconds * 1000));
                        }
                     }
   
                     if (uploadInfo == null) {
                        RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.close.failure");
                        return;
                     }
   
                     UploadTokenCache.put(wid, uploadInfo.getToken());
                     if (!uploadInfo.isWorldClosed()) {
                        RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.close.failure");
                        return;
                     }
   
                     if (RealmsUploadScreen.this.cancelled) {
                        RealmsUploadScreen.this.uploadCancelled();
                        return;
                     }
   
                     File saves = new File(Realms.getGameDirectoryPath(), "saves");
                     archive = RealmsUploadScreen.this.tarGzipArchive(new File(saves, RealmsUploadScreen.this.selectedLevel.getLevelId()));
                     if (RealmsUploadScreen.this.cancelled) {
                        RealmsUploadScreen.this.uploadCancelled();
                        return;
                     }
   
                     if (RealmsUploadScreen.this.verify(archive)) {
                        RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString(
                           "mco.upload.uploading", new Object[]{RealmsUploadScreen.this.selectedLevel.getLevelName()}
                        );
                        FileUpload fileUpload = new FileUpload(
                           archive,
                           RealmsUploadScreen.this.worldId,
                           RealmsUploadScreen.this.slotId,
                           uploadInfo,
                           Realms.getSessionId(),
                           Realms.getName(),
                           "1.13.1",
                           RealmsUploadScreen.this.uploadStatus
                        );
                        fileUpload.upload(
                           uploadResult -> {
                              if (uploadResult.statusCode >= 200 && uploadResult.statusCode < 300) {
                                 RealmsUploadScreen.this.uploadFinished = true;
                                 RealmsUploadScreen.this.status = RealmsScreen.getLocalizedString("mco.upload.done");
                                 RealmsUploadScreen.this.backButton.msg(RealmsScreen.getLocalizedString("gui.done"));
                                 UploadTokenCache.invalidate(wid);
                              } else if (uploadResult.statusCode == 400 && uploadResult.errorMessage != null) {
                                 RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString(
                                    "mco.upload.failed", new Object[]{uploadResult.errorMessage}
                                 );
                              } else {
                                 RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString(
                                    "mco.upload.failed", new Object[]{uploadResult.statusCode}
                                 );
                              }
      
                           }
                        );
   
                        while(!fileUpload.isFinished()) {
                           if (RealmsUploadScreen.this.cancelled) {
                              fileUpload.cancel();
                              RealmsUploadScreen.this.uploadCancelled();
                              return;
                           }
   
                           try {
                              Thread.sleep(500L);
                           } catch (InterruptedException var19) {
                              RealmsUploadScreen.LOGGER.error("Failed to check Realms file upload status");
                           }
                        }
   
                        return;
                     }
   
                     long length = archive.length();
                     RealmsUploadScreen.Unit lengthUnit = RealmsUploadScreen.getLargestUnit(length);
                     RealmsUploadScreen.Unit maxUnit = RealmsUploadScreen.getLargestUnit(5368709120L);
                     if (RealmsUploadScreen.humanReadableSize(length, lengthUnit).equals(RealmsUploadScreen.humanReadableSize(5368709120L, maxUnit))
                        && lengthUnit != RealmsUploadScreen.Unit.B) {
                        RealmsUploadScreen.Unit unitToUse = RealmsUploadScreen.Unit.values()[lengthUnit.ordinal() - 1];
                        RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString(
                              "mco.upload.size.failure.line1", new Object[]{RealmsUploadScreen.this.selectedLevel.getLevelName()}
                           )
                           + "\\n"
                           + RealmsScreen.getLocalizedString(
                              "mco.upload.size.failure.line2",
                              new Object[]{
                                 RealmsUploadScreen.humanReadableSize(length, unitToUse), RealmsUploadScreen.humanReadableSize(5368709120L, unitToUse)
                              }
                           );
                        return;
                     }
   
                     RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString(
                           "mco.upload.size.failure.line1", new Object[]{RealmsUploadScreen.this.selectedLevel.getLevelName()}
                        )
                        + "\\n"
                        + RealmsScreen.getLocalizedString(
                           "mco.upload.size.failure.line2",
                           new Object[]{RealmsUploadScreen.humanReadableSize(length, lengthUnit), RealmsUploadScreen.humanReadableSize(5368709120L, maxUnit)}
                        );
                     return;
                  }
               } catch (IOException var21) {
                  RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var21.getMessage()});
                  return;
               } catch (RealmsServiceException var22) {
                  RealmsUploadScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.upload.failed", new Object[]{var22.toString()});
                  return;
               } catch (InterruptedException var23) {
                  RealmsUploadScreen.LOGGER.error("Could not acquire upload lock");
                  return;
               } finally {
                  RealmsUploadScreen.this.uploadFinished = true;
                  if (RealmsUploadScreen.uploadLock.isHeldByCurrentThread()) {
                     RealmsUploadScreen.uploadLock.unlock();
                     RealmsUploadScreen.this.showDots = false;
                     RealmsUploadScreen.this.childrenClear();
                     RealmsUploadScreen.this.buttonsAdd(RealmsUploadScreen.this.backButton);
                     if (archive != null) {
                        RealmsUploadScreen.LOGGER.debug("Deleting file " + archive.getAbsolutePath());
                        archive.delete();
                     }
   
                  }
   
                  return;
               }
   
            }
         })
         .start();
   }

   private void uploadCancelled() {
      this.status = getLocalizedString("mco.upload.cancelled");
      LOGGER.debug("Upload was cancelled");
   }

   private boolean verify(File archive) {
      return archive.length() < 5368709120L;
   }

   private File tarGzipArchive(File pathToDirectoryFile) throws IOException {
      TarArchiveOutputStream tar = null;

      File var4;
      try {
         File file = File.createTempFile("realms-upload-file", ".tar.gz");
         tar = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
         tar.setLongFileMode(3);
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

   static enum Unit {
      B,
      KB,
      MB,
      GB;
   }
}
