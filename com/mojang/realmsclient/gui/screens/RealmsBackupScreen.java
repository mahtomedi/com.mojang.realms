package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.client.renderer.system.GlStateManager;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsBackupScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String PLUS_ICON_LOCATION = "realms:textures/gui/realms/plus_icon.png";
   private static final String RESTORE_ICON_LOCATION = "realms:textures/gui/realms/restore_icon.png";
   private static int lastScrollPosition = -1;
   private final RealmsConfigureWorldScreen lastScreen;
   private List<Backup> backups = Collections.emptyList();
   private String toolTip;
   private RealmsBackupScreen.BackupSelectionList backupSelectionList;
   private int selectedBackup = -1;
   private static final int BACK_BUTTON_ID = 0;
   private static final int RESTORE_BUTTON_ID = 1;
   private static final int DOWNLOAD_BUTTON_ID = 2;
   private final int slotId;
   private RealmsButton downloadButton;
   private Boolean noBackups = false;
   private final RealmsServer serverData;
   private static final String UPLOADED_KEY = "Uploaded";

   public RealmsBackupScreen(RealmsConfigureWorldScreen lastscreen, RealmsServer serverData, int slotId) {
      this.lastScreen = lastscreen;
      this.serverData = serverData;
      this.slotId = slotId;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.backupSelectionList = new RealmsBackupScreen.BackupSelectionList();
      if (lastScrollPosition != -1) {
         this.backupSelectionList.scroll(lastScrollPosition);
      }

      (new Thread("Realms-fetch-backups") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsBackupScreen.this.backups = client.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
               RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.size() == 0;
               RealmsBackupScreen.this.generateChangeList();
            } catch (RealmsServiceException var3) {
               RealmsBackupScreen.LOGGER.error("Couldn't request backups", var3);
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
      this.buttonsAdd(this.downloadButton = new RealmsButton(2, this.width() - 135, 32, 120, 20, getLocalizedString("mco.backup.button.download")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsBackupScreen.this.downloadClicked();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() - 100, this.height() - 35, 85, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsBackupScreen.this.lastScreen);
         }
      });
      this.addWidget(this.backupSelectionList);
      this.focusOn(this.backupSelectionList);
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void restoreClicked(int selectedBackup) {
      if (selectedBackup >= 0 && selectedBackup < this.backups.size() && !this.serverData.expired) {
         this.selectedBackup = selectedBackup;
         Date backupDate = ((Backup)this.backups.get(selectedBackup)).lastModifiedDate;
         String datePresentation = DateFormat.getDateTimeInstance(3, 3).format(backupDate);
         String age = RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - backupDate.getTime());
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
      RealmsTasks.DownloadTask downloadTask = new RealmsTasks.DownloadTask(
         this.serverData.id,
         this.slotId,
         this.serverData.name
            + " ("
            + ((RealmsWorldOptions)this.serverData.slots.get(this.serverData.activeSlot)).getSlotName(this.serverData.activeSlot)
            + ")",
         this
      );
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), downloadTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
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
      RealmsTasks.RestoreTask restoreTask = new RealmsTasks.RestoreTask(backup, this.serverData.id, this.lastScreen);
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), restoreTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.backupSelectionList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.configure.world.backup"), this.width() / 2, 12, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.backup"), (this.width() - 150) / 2 - 90, 20, 10526880);
      if (this.noBackups) {
         this.drawString(getLocalizedString("mco.backup.nobackups"), 20, this.height() / 2 - 10, 16777215);
      }

      this.downloadButton.active(!this.noBackups);
      super.render(xm, ym, a);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x + 12;
         int ry = y - 12;
         int width = this.fontWidth(msg);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, rx, ry, 16777215);
      }
   }

   private class BackupSelectionList extends RealmsClickableScrolledSelectionList {
      public BackupSelectionList() {
         super(RealmsBackupScreen.this.width() - 150, RealmsBackupScreen.this.height(), 32, RealmsBackupScreen.this.height() - 15, 36);
      }

      public int getItemCount() {
         return RealmsBackupScreen.this.backups.size() + 1;
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         RealmsBackupScreen.this.renderBackground();
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum != 0) {
            return false;
         } else if (xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 92;
            int x1 = this.width();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + (int)this.yo();
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
            }

            return true;
         } else {
            return false;
         }
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         x += 16;
         if (i < RealmsBackupScreen.this.backups.size()) {
            this.renderBackupItem(i, x, y, h, RealmsBackupScreen.this.width, mouseX, mouseY);
         }

      }

      public int getScrollbarPosition() {
         return this.width() - 5;
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         int infox = this.width() - 40;
         int infoy = slot * this.itemHeight() + 36 - this.getScroll();
         int mx = infox + 10;
         int my = infoy - 3;
         if (xm >= (double)infox && xm <= (double)(infox + 9) && ym >= (double)infoy && ym <= (double)(infoy + 9)) {
            if (!((Backup)RealmsBackupScreen.this.backups.get(slot)).changeList.isEmpty()) {
               RealmsBackupScreen.lastScrollPosition = this.getScroll();
               Realms.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, (Backup)RealmsBackupScreen.this.backups.get(slot)));
            }
         } else if (xm >= (double)mx && xm < (double)(mx + 13) && ym >= (double)my && ym < (double)(my + 15)) {
            RealmsBackupScreen.lastScrollPosition = this.getScroll();
            RealmsBackupScreen.this.restoreClicked(slot);
         }

      }

      private void renderBackupItem(int i, int x, int y, int h, int width, int mouseX, int mouseY) {
         Backup backup = (Backup)RealmsBackupScreen.this.backups.get(i);
         int color = backup.isUploadedVersion() ? -8388737 : 16777215;
         RealmsBackupScreen.this.drawString(
            "Backup (" + RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - backup.lastModifiedDate.getTime()) + ")", x + 2, y + 1, color
         );
         RealmsBackupScreen.this.drawString(this.getMediumDatePresentation(backup.lastModifiedDate), x + 2, y + 12, 5000268);
         int dx = this.width() - 30;
         int dy = -3;
         int infox = dx - 10;
         int infoy = 0;
         if (!RealmsBackupScreen.this.serverData.expired) {
            this.drawRestore(dx, y + -3, mouseX, mouseY);
         }

         if (!backup.changeList.isEmpty()) {
            this.drawInfo(infox, y + 0, mouseX, mouseY);
         }

      }

      private String getMediumDatePresentation(Date lastModifiedDate) {
         return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
      }

      private void drawRestore(int x, int y, int xm, int ym) {
         boolean hovered = xm >= x && xm <= x + 12 && ym >= y && ym <= y + 14 && ym < RealmsBackupScreen.this.height() - 15 && ym > 32;
         RealmsScreen.bind("realms:textures/gui/realms/restore_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, hovered ? 28.0F : 0.0F, 23, 28, 23.0F, 56.0F);
         GlStateManager.popMatrix();
         if (hovered) {
            RealmsBackupScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.button.restore");
         }

      }

      private void drawInfo(int x, int y, int xm, int ym) {
         boolean hovered = xm >= x && xm <= x + 8 && ym >= y && ym <= y + 8 && ym < RealmsBackupScreen.this.height() - 15 && ym > 32;
         RealmsScreen.bind("realms:textures/gui/realms/plus_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, hovered ? 15.0F : 0.0F, 15, 15, 15.0F, 30.0F);
         GlStateManager.popMatrix();
         if (hovered) {
            RealmsBackupScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.backup.changes.tooltip");
         }

      }
   }
}
