package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.LongRunningTask;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsWorldManagementScreen extends RealmsScreen {
   private boolean showUpload = true;
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String PLUS_ICON_LOCATION = "realms:textures/gui/realms/plus_icon.png";
   private static final String RESTORE_ICON_LOCATION = "realms:textures/gui/realms/restore_icon.png";
   private static int lastScrollPosition = -1;
   private final RealmsConfigureWorldScreen lastScreen;
   private final RealmsScreen onlineScreen;
   private List<Backup> backups = Collections.emptyList();
   private String toolTip = null;
   private RealmsWorldManagementScreen.BackupSelectionList backupSelectionList;
   private int selectedBackup = -1;
   private static final int BACK_BUTTON_ID = 0;
   private static final int RESTORE_BUTTON_ID = 1;
   private static final int DOWNLOAD_BUTTON_ID = 2;
   private static final int RESET_BUTTON_ID = 3;
   private static final int UPLOAD_BUTTON_ID = 4;
   private static final int MINUTES = 60;
   private static final int HOURS = 3600;
   private static final int DAYS = 86400;
   private RealmsButton downloadButton;
   private RealmsButton uploadButton;
   private RealmsButton resetButton;
   private Boolean noBackups = false;
   private RealmsServer serverData;
   private static final String UPLOADED_KEY = "Uploaded";

   public RealmsWorldManagementScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsScreen onlineScreen, RealmsServer serverData) {
      this.lastScreen = realmsConfigureWorldScreen;
      this.onlineScreen = onlineScreen;
      this.serverData = serverData;
   }

   public void mouseEvent() {
      super.mouseEvent();
      this.backupSelectionList.mouseEvent();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.backupSelectionList = new RealmsWorldManagementScreen.BackupSelectionList();
      if (lastScrollPosition != -1) {
         this.backupSelectionList.scroll(lastScrollPosition);
      }

      (new Thread("Realms-fetch-backups") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsWorldManagementScreen.this.backups = client.backupsFor(RealmsWorldManagementScreen.this.serverData.id).backups;
               RealmsWorldManagementScreen.this.noBackups = RealmsWorldManagementScreen.this.backups.size() == 0;
               RealmsWorldManagementScreen.this.generateChangeList();
            } catch (RealmsServiceException var3) {
               RealmsWorldManagementScreen.LOGGER.error("Couldn't request backups", var3);
            }

         }
      }).start();
      this.postInit();
   }

   private void generateChangeList() {
      if (this.backups.size() > 1) {
         for(int i = 0; i < this.backups.size() - 1; ++i) {
            Backup backup = (Backup)this.backups.get(i);
            Backup olderBackup = (Backup)this.backups.get(i + 1);
            if (!backup.metadata.isEmpty() && !olderBackup.metadata.isEmpty()) {
               for(String key : backup.metadata.keySet()) {
                  if (key.contains("Uploaded") || !olderBackup.metadata.containsKey(key)) {
                     this.addToChangeList(backup, key);
                  } else if (!((String)backup.metadata.get(key)).equals(olderBackup.metadata.get(key))) {
                     this.addToChangeList(backup, key);
                  }
               }
            }
         }

      }
   }

   private void addToChangeList(Backup backup, String key) {
      if (key.contains("Uploaded")) {
         String uploadedTime = DateFormat.getDateTimeInstance(3, 3).format(backup.lastModifiedDate);
         backup.changeList.put(key, uploadedTime);
         backup.setUploadedVersion(true);
      } else {
         backup.changeList.put(key, backup.metadata.get(key));
      }

   }

   private void postInit() {
      this.buttonsAdd(this.resetButton = newButton(3, this.width() - 125, 35, 100, 20, getLocalizedString("mco.backup.button.reset")));
      this.buttonsAdd(
         this.downloadButton = newButton(2, this.width() - 125, this.showUpload ? 85 : 60, 100, 20, getLocalizedString("mco.backup.button.download"))
      );
      this.buttonsAdd(newButton(0, this.width() - 125, this.height() - 35, 85, 20, getLocalizedString("gui.back")));
      if (this.showUpload) {
         this.buttonsAdd(this.uploadButton = newButton(4, this.width() - 125, 60, 100, 20, getLocalizedString("mco.backup.button.upload")));
      }

   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 2) {
            this.downloadClicked();
         } else if (button.id() == 3) {
            Realms.setScreen(new RealmsResetWorldScreen(this.lastScreen, this.onlineScreen, this, this.serverData));
         } else if (button.id() == 4 && this.showUpload) {
            Realms.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this));
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   private void restoreClicked(int selectedBackup) {
      if (selectedBackup >= 0 && selectedBackup < this.backups.size()) {
         this.selectedBackup = selectedBackup;
         Date backupDate = ((Backup)this.backups.get(selectedBackup)).lastModifiedDate;
         String datePresentation = DateFormat.getDateTimeInstance(3, 3).format(backupDate);
         String age = this.convertToAgePresentation(System.currentTimeMillis() - backupDate.getTime());
         String line2 = getLocalizedString("mco.configure.world.restore.question.line1", new Object[]{datePresentation, age});
         String line3 = getLocalizedString("mco.configure.world.restore.question.line2");
         Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, line2, line3, true, 1));
      }

   }

   private void downloadClicked() {
      String line2 = getLocalizedString("mco.configure.world.restore.download.question.line1");
      String line3 = getLocalizedString("mco.configure.world.restore.download.question.line2");
      Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 2));
   }

   private void downloadWorldData() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         String downloadLink = client.download(this.serverData.id);
         Realms.setScreen(new RealmsDownloadLatestWorldScreen(this, downloadLink, this.serverData.name));
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't download world data");
         Realms.setScreen(new RealmsGenericErrorScreen(var3, this));
      }

   }

   public void confirmResult(boolean result, int id) {
      if (result && id == 1) {
         this.restore();
      } else if (result && id == 2) {
         this.downloadWorldData();
      } else {
         Realms.setScreen(this);
      }

   }

   private void restore() {
      Backup backup = (Backup)this.backups.get(this.selectedBackup);
      RealmsWorldManagementScreen.RestoreTask restoreTask = new RealmsWorldManagementScreen.RestoreTask(backup);
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, restoreTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.backupSelectionList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.backup.title"), this.width() / 2, 10, 16777215);
      this.drawString(getLocalizedString("mco.backup.backup"), (this.width() - 150) / 2 - 90, 20, 10526880);
      if (this.noBackups) {
         this.drawString(getLocalizedString("mco.backup.nobackups"), 20, this.height() / 2 - 10, 16777215);
      }

      this.downloadButton.active(!this.noBackups);
      if (this.showUpload) {
         this.uploadButton.active(!this.serverData.expired);
      }

      this.resetButton.active(!this.serverData.expired);
      super.render(xm, ym, a);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

   }

   private String convertToAgePresentation(Long timeDiff) {
      if (timeDiff < 0L) {
         return "right now";
      } else {
         long timeDiffInSeconds = timeDiff / 1000L;
         if (timeDiffInSeconds < 60L) {
            return (timeDiffInSeconds == 1L ? "1 second" : timeDiffInSeconds + " seconds") + " ago";
         } else if (timeDiffInSeconds < 3600L) {
            long minutes = timeDiffInSeconds / 60L;
            return (minutes == 1L ? "1 minute" : minutes + " minutes") + " ago";
         } else if (timeDiffInSeconds < 86400L) {
            long hours = timeDiffInSeconds / 3600L;
            return (hours == 1L ? "1 hour" : hours + " hours") + " ago";
         } else {
            long days = timeDiffInSeconds / 86400L;
            return (days == 1L ? "1 day" : days + " days") + " ago";
         }
      }
   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, -1);
      }
   }

   private class BackupSelectionList extends RealmsClickableScrolledSelectionList {
      public BackupSelectionList() {
         super(
            RealmsWorldManagementScreen.this.width() - 150, RealmsWorldManagementScreen.this.height(), 32, RealmsWorldManagementScreen.this.height() - 15, 36
         );
      }

      public int getItemCount() {
         return RealmsWorldManagementScreen.this.backups.size() + 1;
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         RealmsWorldManagementScreen.this.renderBackground();
      }

      public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
         if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
            int slot = clickSlotPos / itemHeight;
            if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
            }
         }

      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         x += 16;
         if (i < RealmsWorldManagementScreen.this.backups.size()) {
            this.renderBackupItem(i, x, y, h, RealmsWorldManagementScreen.this.width);
         }

      }

      public int getScrollbarPosition() {
         return this.width() - 5;
      }

      public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
         int infox = this.width() - 40;
         int infoy = clickSlotPos + 30 - this.getScroll();
         int mx = infox + 10;
         int my = infoy - 3;
         if (xm >= infox && xm <= infox + 9 && ym >= infoy && ym <= infoy + 9) {
            if (!((Backup)RealmsWorldManagementScreen.this.backups.get(slot)).changeList.isEmpty()) {
               RealmsWorldManagementScreen.lastScrollPosition = this.getScroll();
               Realms.setScreen(new RealmsBackupInfoScreen(RealmsWorldManagementScreen.this, (Backup)RealmsWorldManagementScreen.this.backups.get(slot)));
            }
         } else if (xm >= mx && xm <= mx + 9 && ym >= my && ym <= my + 9) {
            RealmsWorldManagementScreen.lastScrollPosition = this.getScroll();
            RealmsWorldManagementScreen.this.restoreClicked(slot);
         }

      }

      private void renderBackupItem(int i, int x, int y, int h, int width) {
         Backup backup = (Backup)RealmsWorldManagementScreen.this.backups.get(i);
         int color = backup.isUploadedVersion() ? -8388737 : 16777215;
         RealmsWorldManagementScreen.this.drawString(
            "Backup (" + RealmsWorldManagementScreen.this.convertToAgePresentation(System.currentTimeMillis() - backup.lastModifiedDate.getTime()) + ")",
            x + 2,
            y + 1,
            color
         );
         RealmsWorldManagementScreen.this.drawString(this.getMediumDatePresentation(backup.lastModifiedDate), x + 2, y + 12, 7105644);
         int dx = this.width() - 30;
         int dy = -3;
         int infox = dx - 10;
         int infoy = dy + 3;
         if (!RealmsWorldManagementScreen.this.serverData.expired) {
            this.drawRestore(dx, y + dy, this.xm(), this.ym());
         }

         if (!backup.changeList.isEmpty()) {
            this.drawInfo(infox, y + infoy, this.xm(), this.ym());
         }

      }

      private String getMediumDatePresentation(Date lastModifiedDate) {
         return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
      }

      private void drawRestore(int x, int y, int xm, int ym) {
         RealmsScreen.bind("realms:textures/gui/realms/restore_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 23, 28, 23.0F, 28.0F);
         GL11.glPopMatrix();
         if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < RealmsWorldManagementScreen.this.height() - 15 && ym > 32) {
            RealmsWorldManagementScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.button.restore");
         }

      }

      private void drawInfo(int x, int y, int xm, int ym) {
         RealmsScreen.bind("realms:textures/gui/realms/plus_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
         GL11.glPopMatrix();
         if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9 && ym < RealmsWorldManagementScreen.this.height() - 15 && ym > 32) {
            RealmsWorldManagementScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.changes.tooltip");
         }

      }
   }

   private class RestoreTask extends LongRunningTask {
      private final Backup backup;

      private RestoreTask(Backup backup) {
         this.backup = backup;
      }

      public void run() {
         this.setTitle(RealmsScreen.getLocalizedString("mco.backup.restoring"));
         int i = 0;

         while(i < 6) {
            try {
               if (this.aborted()) {
                  return;
               }

               RealmsClient client = RealmsClient.createRealmsClient();
               client.restoreWorld(RealmsWorldManagementScreen.this.serverData.id, this.backup.backupId);
               this.pause(1);
               if (this.aborted()) {
                  return;
               }

               Realms.setScreen(RealmsWorldManagementScreen.this.lastScreen);
               return;
            } catch (RetryCallException var3) {
               if (this.aborted()) {
                  return;
               }

               this.pause(var3.delaySeconds);
               ++i;
            } catch (RealmsServiceException var4) {
               if (this.aborted()) {
                  return;
               }

               RealmsWorldManagementScreen.LOGGER.error("Couldn't restore backup");
               Realms.setScreen(new RealmsGenericErrorScreen(var4, RealmsWorldManagementScreen.this.lastScreen));
               return;
            } catch (Exception var5) {
               if (this.aborted()) {
                  return;
               }

               RealmsWorldManagementScreen.LOGGER.error("Couldn't restore backup");
               this.error(var5.getLocalizedMessage());
               return;
            }
         }

      }

      private void pause(int pauseSeconds) {
         try {
            Thread.sleep((long)(pauseSeconds * 1000));
         } catch (InterruptedException var3) {
            RealmsWorldManagementScreen.LOGGER.error(var3);
         }

      }
   }
}
