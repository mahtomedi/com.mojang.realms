package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsBrokenWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private static final String EMPTY_FRAME_LOCATION = "realms:textures/gui/realms/empty_frame.png";
   private final RealmsScreen lastScreen;
   private final RealmsMainScreen mainScreen;
   private RealmsServer serverData;
   private final long serverId;
   private String title = getLocalizedString("mco.brokenworld.title");
   private final String message = getLocalizedString("mco.brokenworld.message.line1") + "\\n" + getLocalizedString("mco.brokenworld.message.line2");
   private int left_x;
   private int right_x;
   private final int default_button_width = 80;
   private final int default_button_offset = 5;
   private static final int BUTTON_BACK_ID = 0;
   private static final List<Integer> playButtonIds = Arrays.asList(1, 2, 3);
   private static final List<Integer> resetButtonIds = Arrays.asList(4, 5, 6);
   private static final List<Integer> downloadButtonIds = Arrays.asList(7, 8, 9);
   private static final List<Integer> downloadConfirmationIds = Arrays.asList(10, 11, 12);
   private final List<Integer> slotsThatHasBeenDownloaded = new ArrayList();
   private static final int SWITCH_SLOT_ID_RESULT = 13;
   private static final int RESET_CONFIRMATION_ID = 14;
   private int animTick;

   public RealmsBrokenWorldScreen(RealmsScreen lastScreen, RealmsMainScreen mainScreen, long serverId) {
      this.lastScreen = lastScreen;
      this.mainScreen = mainScreen;
      this.serverId = serverId;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void init() {
      this.left_x = this.width() / 2 - 150;
      this.right_x = this.width() / 2 + 190;
      this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsBrokenWorldScreen.this.backButtonClicked();
         }
      });
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      } else {
         this.addButtons();
      }

      this.setKeyboardHandlerSendRepeatsToGui(true);
   }

   public void addButtons() {
      for(Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
         RealmsWorldOptions slot = (RealmsWorldOptions)entry.getValue();
         boolean canPlay = entry.getKey() != this.serverData.activeSlot || this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
         RealmsButton downloadButton;
         if (canPlay) {
            downloadButton = new RealmsBrokenWorldScreen.PlayButton(
               playButtonIds.get(entry.getKey() - 1), this.getFramePositionX(entry.getKey()), getLocalizedString("mco.brokenworld.play")
            );
         } else {
            downloadButton = new RealmsBrokenWorldScreen.DownloadButton(
               downloadButtonIds.get(entry.getKey() - 1), this.getFramePositionX(entry.getKey()), getLocalizedString("mco.brokenworld.download")
            );
         }

         if (this.slotsThatHasBeenDownloaded.contains(entry.getKey())) {
            downloadButton.active(false);
            downloadButton.msg(getLocalizedString("mco.brokenworld.downloaded"));
         }

         this.buttonsAdd(downloadButton);
         this.buttonsAdd(
            new RealmsButton(
               resetButtonIds.get(entry.getKey() - 1),
               this.getFramePositionX(entry.getKey()),
               RealmsConstants.row(10),
               80,
               20,
               getLocalizedString("mco.brokenworld.reset")
            ) {
               public void onClick(double mouseX, double mouseY) {
                  int slot = RealmsBrokenWorldScreen.resetButtonIds.indexOf(this.id()) + 1;
                  RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
                     RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this
                  );
                  if (slot != RealmsBrokenWorldScreen.this.serverData.activeSlot
                     || RealmsBrokenWorldScreen.this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
                     realmsResetWorldScreen.setSlot(slot);
                  }
   
                  realmsResetWorldScreen.setConfirmationId(14);
                  Realms.setScreen(realmsResetWorldScreen);
               }
            }
         );
      }

   }

   public void tick() {
      ++this.animTick;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      super.render(xm, ym, a);
      this.drawCenteredString(this.title, this.width() / 2, 17, 16777215);
      String[] lines = this.message.split("\\\\n");

      for(int i = 0; i < lines.length; ++i) {
         this.drawCenteredString(lines[i], this.width() / 2, RealmsConstants.row(-1) + 3 + i * 12, 10526880);
      }

      if (this.serverData != null) {
         for(Entry<Integer, RealmsWorldOptions> entry : this.serverData.slots.entrySet()) {
            if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
               this.drawSlotFrame(
                  this.getFramePositionX(entry.getKey()),
                  RealmsConstants.row(1) + 5,
                  xm,
                  ym,
                  this.serverData.activeSlot == entry.getKey() && !this.isMinigame(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  ((RealmsWorldOptions)entry.getValue()).templateId,
                  ((RealmsWorldOptions)entry.getValue()).templateImage,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            } else {
               this.drawSlotFrame(
                  this.getFramePositionX(entry.getKey()),
                  RealmsConstants.row(1) + 5,
                  xm,
                  ym,
                  this.serverData.activeSlot == entry.getKey() && !this.isMinigame(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  -1L,
                  null,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            }
         }

      }
   }

   private int getFramePositionX(int i) {
      return this.left_x + (i - 1) * 110;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void backButtonClicked() {
      Realms.setScreen(this.lastScreen);
   }

   private void fetchServerData(final long worldId) {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsBrokenWorldScreen.this.serverData = client.getOwnWorld(worldId);
               RealmsBrokenWorldScreen.this.addButtons();
            } catch (RealmsServiceException var3) {
               RealmsBrokenWorldScreen.LOGGER.error("Couldn't get own world");
               Realms.setScreen(new RealmsGenericErrorScreen(var3.getMessage(), RealmsBrokenWorldScreen.this.lastScreen));
            } catch (IOException var4) {
               RealmsBrokenWorldScreen.LOGGER.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   public void confirmResult(boolean result, int id) {
      if (!result) {
         Realms.setScreen(this);
      } else {
         if (id != 13 && id != 14) {
            if (downloadButtonIds.contains(id)) {
               this.downloadWorld(downloadButtonIds.indexOf(id) + 1);
            } else if (downloadConfirmationIds.contains(id)) {
               this.slotsThatHasBeenDownloaded.add(downloadConfirmationIds.indexOf(id) + 1);
               this.childrenClear();
               this.addButtons();
            }
         } else {
            (new Thread() {
                  public void run() {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     if (RealmsBrokenWorldScreen.this.serverData.state.equals(RealmsServer.State.CLOSED)) {
                        RealmsTasks.OpenServerTask openServerTask = new RealmsTasks.OpenServerTask(
                           RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.lastScreen, true
                        );
                        RealmsLongRunningMcoTaskScreen openWorldLongRunningTaskScreen = new RealmsLongRunningMcoTaskScreen(
                           RealmsBrokenWorldScreen.this, openServerTask
                        );
                        openWorldLongRunningTaskScreen.start();
                        Realms.setScreen(openWorldLongRunningTaskScreen);
                     } else {
                        try {
                           RealmsBrokenWorldScreen.this.mainScreen
                              .newScreen()
                              .play(client.getOwnWorld(RealmsBrokenWorldScreen.this.serverId), RealmsBrokenWorldScreen.this);
                        } catch (RealmsServiceException var4) {
                           RealmsBrokenWorldScreen.LOGGER.error("Couldn't get own world");
                           Realms.setScreen(RealmsBrokenWorldScreen.this.lastScreen);
                        } catch (IOException var5) {
                           RealmsBrokenWorldScreen.LOGGER.error("Couldn't parse response getting own world");
                           Realms.setScreen(RealmsBrokenWorldScreen.this.lastScreen);
                        }
                     }
   
                  }
               })
               .start();
         }

      }
   }

   private void downloadWorld(int slotId) {
      RealmsClient client = RealmsClient.createRealmsClient();

      try {
         WorldDownload worldDownload = client.download(this.serverData.id, slotId);
         RealmsDownloadLatestWorldScreen downloadScreen = new RealmsDownloadLatestWorldScreen(
            this, worldDownload, this.serverData.name + " (" + ((RealmsWorldOptions)this.serverData.slots.get(slotId)).getSlotName(slotId) + ")"
         );
         downloadScreen.setConfirmationId(downloadConfirmationIds.get(slotId - 1));
         Realms.setScreen(downloadScreen);
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't download world data");
         Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
   }

   private void drawSlotFrame(int x, int y, int xm, int ym, boolean active, String text, int i, long imageId, String image, boolean empty) {
      if (empty) {
         bind("realms:textures/gui/realms/empty_frame.png");
      } else if (image != null && imageId != -1L) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (i == 1) {
         bind("textures/gui/title/background/panorama_0.png");
      } else if (i == 2) {
         bind("textures/gui/title/background/panorama_2.png");
      } else if (i == 3) {
         bind("textures/gui/title/background/panorama_3.png");
      } else {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
      }

      if (!active) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else if (active) {
         float c = 0.9F + 0.1F * RealmsMth.cos((float)this.animTick * 0.2F);
         GlStateManager.color4f(c, c, c, 1.0F);
      }

      RealmsScreen.blit(x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74.0F, 74.0F);
      bind("realms:textures/gui/realms/slot_frame.png");
      if (active) {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      RealmsScreen.blit(x, y, 0.0F, 0.0F, 80, 80, 80.0F, 80.0F);
      this.drawCenteredString(text, x + 40, y + 66, 16777215);
   }

   private void switchSlot(int id) {
      RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, id, this, 13);
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   private class DownloadButton extends RealmsButton {
      public DownloadButton(int id, int x, String msg) {
         super(id, x, RealmsConstants.row(8), 80, 20, msg);
      }

      public void onClick(double mouseX, double mouseY) {
         String line2 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
         String line3 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
         Realms.setScreen(new RealmsLongConfirmationScreen(RealmsBrokenWorldScreen.this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, this.id()));
      }
   }

   private class PlayButton extends RealmsButton {
      public PlayButton(int id, int x, String msg) {
         super(id, x, RealmsConstants.row(8), 80, 20, msg);
      }

      public void onClick(double mouseX, double mouseY) {
         int slot = RealmsBrokenWorldScreen.playButtonIds.indexOf(this.id()) + 1;
         if (((RealmsWorldOptions)RealmsBrokenWorldScreen.this.serverData.slots.get(slot)).empty) {
            RealmsResetWorldScreen resetWorldScreen = new RealmsResetWorldScreen(
               RealmsBrokenWorldScreen.this,
               RealmsBrokenWorldScreen.this.serverData,
               RealmsBrokenWorldScreen.this,
               RealmsScreen.getLocalizedString("mco.configure.world.switch.slot"),
               RealmsScreen.getLocalizedString("mco.configure.world.switch.slot.subtitle"),
               10526880,
               RealmsScreen.getLocalizedString("gui.cancel")
            );
            resetWorldScreen.setSlot(slot);
            resetWorldScreen.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
            resetWorldScreen.setConfirmationId(14);
            Realms.setScreen(resetWorldScreen);
         } else {
            RealmsBrokenWorldScreen.this.switchSlot(slot);
         }

      }
   }
}
