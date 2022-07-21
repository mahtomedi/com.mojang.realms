package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class WorldManagementScreen extends RealmsScreen {
   private boolean showUpload = true;
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String PLUS_ICON_LOCATION = "realms:textures/gui/realms/plus_icon.png";
   private static final String RESTORE_ICON_LOCATION = "realms:textures/gui/realms/restore_icon.png";
   private static int lastScrollPosition = -1;
   private final ConfigureWorldScreen lastScreen;
   private final RealmsScreen onlineScreen;
   private List<Backup> backups = Collections.emptyList();
   private String toolTip = null;
   private WorldManagementScreen.BackupSelectionList backupSelectionList;
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
   private McoServer serverData;
   private static final String UPLOADED_KEY = "Uploaded";

   public WorldManagementScreen(ConfigureWorldScreen configureWorldScreen, RealmsScreen onlineScreen, McoServer serverData) {
      this.lastScreen = configureWorldScreen;
      this.onlineScreen = onlineScreen;
      this.serverData = serverData;
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.backupSelectionList = new WorldManagementScreen.BackupSelectionList();
      if (lastScrollPosition != -1) {
         this.backupSelectionList.scroll(lastScrollPosition);
      }

      (new Thread("Realms-fetch-backups") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               WorldManagementScreen.this.backups = client.backupsFor(WorldManagementScreen.this.serverData.id).backups;
               WorldManagementScreen.this.noBackups = WorldManagementScreen.this.backups.size() == 0;
               WorldManagementScreen.this.generateChangeList();
            } catch (RealmsServiceException var3) {
               WorldManagementScreen.LOGGER.error("Couldn't request backups", var3);
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
            Realms.setScreen(new ResetWorldScreen(this.lastScreen, this.onlineScreen, this, this.serverData));
         } else if (button.id() == 4 && this.showUpload) {
            Realms.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this));
         } else {
            this.backupSelectionList.buttonClicked(button);
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   private void restoreClicked() {
      if (this.selectedBackup >= 0 && this.selectedBackup < this.backups.size()) {
         Date backupDate = ((Backup)this.backups.get(this.selectedBackup)).lastModifiedDate;
         String datePresentation = DateFormat.getDateTimeInstance(3, 3).format(backupDate);
         String age = this.convertToAgePresentation(System.currentTimeMillis() - backupDate.getTime());
         String line2 = getLocalizedString("mco.configure.world.restore.question.line1", new Object[]{datePresentation, age});
         String line3 = getLocalizedString("mco.configure.world.restore.question.line2");
         Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Warning, line2, line3, 1));
      }

   }

   private void downloadClicked() {
      String line2 = getLocalizedString("mco.configure.world.restore.download.question.line1");
      String line3 = getLocalizedString("mco.configure.world.restore.download.question.line2");
      Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 2));
   }

   private void downloadWorldData() {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         String downloadLink = client.download(this.serverData.id);
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(new StringSelection(downloadLink), null);
         this.browseTo(downloadLink);
         Realms.setScreen(new BackupLinkScreen(this, downloadLink));
      } catch (RealmsServiceException var4) {
         LOGGER.error("Couldn't download world data");
         Realms.setScreen(new RealmsGenericErrorScreen(var4, this));
      }

   }

   private void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var5) {
         LOGGER.error("Couldn't open link");
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
      WorldManagementScreen.RestoreTask restoreTask = new WorldManagementScreen.RestoreTask(backup);
      LongRunningMcoTaskScreen longRunningMcoTaskScreen = new LongRunningMcoTaskScreen(this.lastScreen, restoreTask);
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

   private class BackupSelectionList extends MCRSelectionList {
      public BackupSelectionList() {
         super(WorldManagementScreen.this.width() - 150, WorldManagementScreen.this.height(), 32, WorldManagementScreen.this.height() - 15, 36);
      }

      @Override
      protected int getNumberOfItems() {
         return WorldManagementScreen.this.backups.size() + 1;
      }

      @Override
      protected void selectItem(int item, boolean doubleClick) {
         if (item < WorldManagementScreen.this.backups.size()) {
            WorldManagementScreen.this.selectedBackup = item;
         }
      }

      @Override
      protected boolean isSelectedItem(int item) {
         return item == WorldManagementScreen.this.selectedBackup;
      }

      @Override
      protected boolean isMyWorld(int item) {
         return false;
      }

      @Override
      protected int getMaxPosition() {
         return this.getNumberOfItems() * 36;
      }

      @Override
      protected void renderBackground() {
         WorldManagementScreen.this.renderBackground();
      }

      @Override
      protected void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = width / 2 - 92;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3553);
         t.begin();
         t.color(8421504);
         t.vertexUV((double)x0, (double)(y + h + 2), 0.0, 0.0, 1.0);
         t.vertexUV((double)width, (double)(y + h + 2), 0.0, 1.0, 1.0);
         t.vertexUV((double)width, (double)(y - 2), 0.0, 1.0, 0.0);
         t.vertexUV((double)x0, (double)(y - 2), 0.0, 0.0, 0.0);
         t.color(0);
         t.vertexUV((double)(x0 + 1), (double)(y + h + 1), 0.0, 0.0, 1.0);
         t.vertexUV((double)(width - 1), (double)(y + h + 1), 0.0, 1.0, 1.0);
         t.vertexUV((double)(width - 1), (double)(y - 1), 0.0, 1.0, 0.0);
         t.vertexUV((double)(x0 + 1), (double)(y - 1), 0.0, 0.0, 0.0);
         t.end();
         GL11.glEnable(3553);
      }

      @Override
      protected void renderItem(int i, int x, int y, int h, int width, Tezzelator t) {
         x += 16;
         if (i < WorldManagementScreen.this.backups.size()) {
            this.renderBackupItem(i, x, y, h, width, t);
         }

      }

      @Override
      protected int getScrollbarPosition() {
         return WorldManagementScreen.this.width() - 150;
      }

      @Override
      public void itemClicked(int x, int y, int xm, int ym, int width) {
         int infox = width - 30;
         int infoy = y + 4;
         int mx = infox + 10;
         int my = infoy - 3;
         if (xm >= infox && xm <= infox + 9 && ym >= infoy && ym <= infoy + 9) {
            if (!((Backup)WorldManagementScreen.this.backups.get(WorldManagementScreen.this.selectedBackup)).changeList.isEmpty()) {
               WorldManagementScreen.lastScrollPosition = this.getScroll();
               Realms.setScreen(
                  new BackupInfoScreen(WorldManagementScreen.this, (Backup)WorldManagementScreen.this.backups.get(WorldManagementScreen.this.selectedBackup))
               );
            }
         } else if (xm >= mx && xm <= mx + 9 && ym >= my && ym <= my + 9) {
            WorldManagementScreen.lastScrollPosition = this.getScroll();
            WorldManagementScreen.this.restoreClicked();
         }

      }

      private void renderBackupItem(int i, int x, int y, int h, int width, Tezzelator t) {
         Backup backup = (Backup)WorldManagementScreen.this.backups.get(i);
         int color = backup.isUploadedVersion() ? -8388737 : 16777215;
         WorldManagementScreen.this.drawString(
            "Backup (" + WorldManagementScreen.this.convertToAgePresentation(System.currentTimeMillis() - backup.lastModifiedDate.getTime()) + ")",
            x + 2,
            y + 1,
            color
         );
         WorldManagementScreen.this.drawString(this.getMediumDatePresentation(backup.lastModifiedDate), x + 2, y + 12, 7105644);
         int dx = width - 20;
         int dy = 1;
         int infox = dx - 10;
         int infoy = dy + 3;
         if (!WorldManagementScreen.this.serverData.expired) {
            this.drawRestore(dx, y + dy, this.xm, this.ym);
         }

         if (!backup.changeList.isEmpty()) {
            this.drawInfo(infox, y + infoy, this.xm, this.ym);
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
         if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
            WorldManagementScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.button.restore");
         }

      }

      private void drawInfo(int x, int y, int xm, int ym) {
         RealmsScreen.bind("realms:textures/gui/realms/plus_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
         GL11.glPopMatrix();
         if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
            WorldManagementScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.changes.tooltip");
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
               client.restoreWorld(WorldManagementScreen.this.serverData.id, this.backup.backupId);
               this.pause(1);
               if (this.aborted()) {
                  return;
               }

               Realms.setScreen(WorldManagementScreen.this.lastScreen);
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

               WorldManagementScreen.LOGGER.error("Couldn't restore backup");
               Realms.setScreen(new RealmsGenericErrorScreen(var4, WorldManagementScreen.this.lastScreen));
               return;
            } catch (Exception var5) {
               if (this.aborted()) {
                  return;
               }

               WorldManagementScreen.LOGGER.error("Couldn't restore backup");
               this.error(var5.getLocalizedMessage());
               return;
            }
         }

      }

      private void pause(int pauseSeconds) {
         try {
            Thread.sleep((long)(pauseSeconds * 1000));
         } catch (InterruptedException var3) {
            WorldManagementScreen.LOGGER.error(var3);
         }

      }
   }
}
