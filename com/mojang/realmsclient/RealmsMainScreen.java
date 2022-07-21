package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateTrialScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsMainScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static boolean overrideConfigure;
   private boolean dontSetConnectedToRealms;
   protected static final int BUTTON_RENEW_ID = 0;
   protected static final int BUTTON_CONFIGURE_ID = 1;
   protected static final int BUTTON_BACK_ID = 2;
   protected static final int BUTTON_PLAY_ID = 3;
   private static final int LEAVE_ID = 4;
   private static final int BUTTON_BUY_ID = 5;
   private static final int BUTTON_TRY_ID = 6;
   protected static final int BUTTON_LEAVE_ID = 7;
   private static final int BUTTON_INVITES_ID = 8;
   private static final int BUTTON_NEWS_ID = 9;
   private static final int BUTTON_MORE_INFO_ID = 10;
   private static final int BUTTON_CLOSE_ID = 11;
   private static final String ON_ICON_LOCATION = "realms:textures/gui/realms/on_icon.png";
   private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
   private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
   private static final String EXPIRES_SOON_ICON_LOCATION = "realms:textures/gui/realms/expires_soon_icon.png";
   private static final String LEAVE_ICON_LOCATION = "realms:textures/gui/realms/leave_icon.png";
   private static final String INVITATION_ICONS_LOCATION = "realms:textures/gui/realms/invitation_icons.png";
   private static final String INVITE_ICON_LOCATION = "realms:textures/gui/realms/invite_icon.png";
   private static final String WORLDICON_LOCATION = "realms:textures/gui/realms/world_icon.png";
   private static final String LOGO_LOCATION = "realms:textures/gui/title/realms.png";
   private static final String CONFIGURE_LOCATION = "realms:textures/gui/realms/configure_icon.png";
   private static final String QUESTIONMARK_LOCATION = "realms:textures/gui/realms/questionmark.png";
   private static final String NEWS_LOCATION = "realms:textures/gui/realms/news_icon.png";
   private static final String POPUP_LOCATION = "realms:textures/gui/realms/popup.png";
   private static final String DARKEN_LOCATION = "realms:textures/gui/realms/darken.png";
   private static final String CROSS_ICON_LOCATION = "realms:textures/gui/realms/cross_icon.png";
   private static final String TRIAL_ICON_LOCATION = "realms:textures/gui/realms/trial_icon.png";
   private static final String BUTTON_LOCATION = "minecraft:textures/gui/widgets.png";
   private static final String[] IMAGES_LOCATION = new String[]{
      "realms:textures/gui/realms/images/sand_castle.png",
      "realms:textures/gui/realms/images/factory_floor.png",
      "realms:textures/gui/realms/images/escher_tunnel.png",
      "realms:textures/gui/realms/images/tree_houses.png",
      "realms:textures/gui/realms/images/balloon_trip.png",
      "realms:textures/gui/realms/images/halloween_woods.png",
      "realms:textures/gui/realms/images/flower_mountain.png",
      "realms:textures/gui/realms/images/dornenstein_estate.png",
      "realms:textures/gui/realms/images/desert.png",
      "realms:textures/gui/realms/images/gray.png",
      "realms:textures/gui/realms/images/imperium.png",
      "realms:textures/gui/realms/images/ludo.png",
      "realms:textures/gui/realms/images/makersspleef.png",
      "realms:textures/gui/realms/images/negentropy.png",
      "realms:textures/gui/realms/images/pumpkin_party.png",
      "realms:textures/gui/realms/images/sparrenhout.png",
      "realms:textures/gui/realms/images/spindlewood.png"
   };
   private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
   private static int lastScrollYPosition = -1;
   private final RealmsScreen lastScreen;
   private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
   private long selectedServerId = -1L;
   private RealmsButton playButton;
   private RealmsButton backButton;
   private RealmsButton renewButton;
   private RealmsButton configureButton;
   private RealmsButton leaveButton;
   private String toolTip;
   private List<RealmsServer> realmsServers = Lists.newArrayList();
   private volatile int numberOfPendingInvites;
   private int animTick;
   private static volatile boolean hasParentalConsent;
   private static volatile boolean checkedParentalConsent;
   private static volatile boolean checkedClientCompatability;
   private boolean hasFetchedServers;
   private boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   private volatile boolean hasUnreadNews;
   private volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private static RealmsScreen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private int mindex;
   private final char[] mchars = new char[]{'3', '2', '1', '4', '5', '6'};
   private int sindex;
   private final char[] schars = new char[]{'9', '8', '7', '1', '2', '3'};
   private int clicks;
   private int lindex;
   private final char[] lchars = new char[]{'9', '8', '7', '4', '5', '6'};
   private ReentrantLock connectLock = new ReentrantLock();
   private boolean expiredHover;
   private RealmsMainScreen.ShowPopupButton showPopupButton;
   private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
   private RealmsMainScreen.NewsButton newsButton;
   private RealmsButton createTrialButton;
   private RealmsButton buyARealmButton;
   private RealmsButton closeButton;

   public RealmsMainScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public boolean shouldShowMessageInList() {
      if (this.hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialsAvailable && !this.createdTrial) {
            return true;
         } else {
            for(RealmsServer realmsServer : this.realmsServers) {
               if (realmsServer.ownerUUID.equals(Realms.getUUID())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (!this.hasParentalConsent() || !this.hasFetchedServers) {
         return false;
      } else if (this.popupOpenedByUser) {
         return true;
      } else {
         return this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty() ? true : this.realmsServers.isEmpty();
      }
   }

   public void init() {
      if (realmsGenericErrorScreen != null) {
         Realms.setScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatability && !this.hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatability();
         this.checkUnreadNews();
         if (!this.dontSetConnectedToRealms) {
            Realms.setConnectedToRealms(false);
         }

         this.setKeyboardHandlerSendRepeatsToGui(true);
         if (this.hasParentalConsent()) {
            realmsDataFetcher.forceUpdate();
         }

         this.showingPopup = false;
         this.postInit();
      }
   }

   private boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addButtons() {
      this.buttonsAdd(
         this.configureButton = new RealmsButton(1, this.width() / 2 - 202, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.configure")) {
            public void onPress() {
               RealmsMainScreen.this.configureClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
            }
         }
      );
      this.buttonsAdd(this.playButton = new RealmsButton(3, this.width() / 2 - 98, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.play")) {
         public void onPress() {
            RealmsMainScreen.this.onPlay();
         }
      });
      this.buttonsAdd(this.backButton = new RealmsButton(2, this.width() / 2 + 6, this.height() - 32, 98, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            if (!RealmsMainScreen.this.justClosedPopup) {
               Realms.setScreen(RealmsMainScreen.this.lastScreen);
            }

         }
      });
      this.buttonsAdd(
         this.renewButton = new RealmsButton(0, this.width() / 2 + 110, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.expiredRenew")) {
            public void onPress() {
               RealmsMainScreen.this.onRenew();
            }
         }
      );
      this.buttonsAdd(
         this.leaveButton = new RealmsButton(7, this.width() / 2 - 202, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.leave")) {
            public void onPress() {
               RealmsMainScreen.this.leaveClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
            }
         }
      );
      this.buttonsAdd(this.pendingInvitesButton = new RealmsMainScreen.PendingInvitesButton());
      this.buttonsAdd(this.newsButton = new RealmsMainScreen.NewsButton());
      this.buttonsAdd(this.showPopupButton = new RealmsMainScreen.ShowPopupButton());
      this.buttonsAdd(this.closeButton = new RealmsMainScreen.CloseButton());
      this.buttonsAdd(
         this.createTrialButton = new RealmsButton(6, this.width() / 2 + 52, this.popupY0() + 137 - 20, 98, 20, getLocalizedString("mco.selectServer.trial")) {
            public void onPress() {
               RealmsMainScreen.this.createTrial();
            }
         }
      );
      this.buttonsAdd(
         this.buyARealmButton = new RealmsButton(5, this.width() / 2 + 52, this.popupY0() + 160 - 20, 98, 20, getLocalizedString("mco.selectServer.buy")) {
            public void onPress() {
               RealmsUtil.browseTo("https://minecraft.net/realms");
            }
         }
      );
      RealmsServer server = this.findServer(this.selectedServerId);
      this.updateButtonStates(server);
   }

   private void updateButtonStates(RealmsServer server) {
      this.playButton.active(this.shouldPlayButtonBeActive(server) && !this.shouldShowPopup());
      this.renewButton.setVisible(this.shouldRenewButtonBeActive(server));
      this.configureButton.setVisible(this.shouldConfigureButtonBeVisible(server));
      this.leaveButton.setVisible(this.shouldLeaveButtonBeVisible(server));
      this.createTrialButton.setVisible(this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial);
      this.buyARealmButton.setVisible(this.shouldShowPopup());
      this.closeButton.setVisible(this.shouldShowPopup());
      this.renewButton.active(!this.shouldShowPopup());
      this.configureButton.active(!this.shouldShowPopup());
      this.leaveButton.active(!this.shouldShowPopup());
      this.createTrialButton.active(!this.shouldShowPopup());
      this.newsButton.active(!this.shouldShowPopup());
      this.pendingInvitesButton.active(!this.shouldShowPopup());
      this.backButton.active(!this.shouldShowPopup());
      this.showPopupButton.active(!this.shouldShowPopup());
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && this.hasParentalConsent() && this.hasFetchedServers;
   }

   private boolean shouldPlayButtonBeActive(RealmsServer server) {
      return server != null && !server.expired && server.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(RealmsServer server) {
      return server != null && server.expired && this.isSelfOwnedServer(server);
   }

   private boolean shouldConfigureButtonBeVisible(RealmsServer server) {
      return server != null && this.isSelfOwnedServer(server);
   }

   private boolean shouldLeaveButtonBeVisible(RealmsServer server) {
      return server != null && !this.isSelfOwnedServer(server);
   }

   public void postInit() {
      if (this.hasParentalConsent() && this.hasFetchedServers) {
         this.addButtons();
      }

      this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
      this.realmSelectionList.setLeftPos(-15);
      if (lastScrollYPosition != -1) {
         this.realmSelectionList.scroll(lastScrollYPosition);
      }

      this.addWidget(this.realmSelectionList);
      this.focusOn(this.realmSelectionList);
   }

   public void tick() {
      this.tickButtons();
      this.justClosedPopup = false;
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

      if (this.hasParentalConsent()) {
         realmsDataFetcher.init();
         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
            List<RealmsServer> newServers = realmsDataFetcher.getServers();
            if (newServers != null) {
               boolean ownsNonExpiredRealmServer = false;

               for(RealmsServer retrievedServer : newServers) {
                  if (this.isSelfOwnedNonExpiredServer(retrievedServer)) {
                     ownsNonExpiredRealmServer = true;
                  }
               }

               if (this.shouldShowMessageInList()) {
                  this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListTrialEntry());
               }

               this.realmsServers = newServers;

               for(RealmsServer server : this.realmsServers) {
                  this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListEntry(server));
               }

               if (!regionsPinged && ownsNonExpiredRealmServer) {
                  regionsPinged = true;
                  this.pingRegions();
               }
            }

            if (!this.hasFetchedServers) {
               this.hasFetchedServers = true;
               this.addButtons();
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
            boolean newStatus = realmsDataFetcher.isTrialAvailable();
            if (newStatus != this.trialsAvailable && this.shouldShowPopup()) {
               this.trialsAvailable = newStatus;
               this.showingPopup = false;
            } else {
               this.trialsAvailable = newStatus;
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
            RealmsServerPlayerLists playerLists = realmsDataFetcher.getLivestats();

            for(RealmsServerPlayerList playerList : playerLists.servers) {
               for(RealmsServer server : this.realmsServers) {
                  if (server.id == playerList.serverId) {
                     server.updateServerPing(playerList);
                     break;
                  }
               }
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
            this.hasUnreadNews = realmsDataFetcher.hasUnreadNews();
            this.newsLink = realmsDataFetcher.newsLink();
         }

         realmsDataFetcher.markClean();
         if (this.shouldShowPopup()) {
            ++this.carouselTick;
         }

         if (this.showPopupButton != null) {
            this.showPopupButton.setVisible(this.shouldShowPopupButton());
         }

      }
   }

   private void browseURL(String url) {
      RealmsBridge.setClipboard(url);
      RealmsUtil.browseTo(url);
   }

   private void pingRegions() {
      (new Thread() {
         public void run() {
            List<RegionPingResult> regionPingResultList = Ping.pingAllRegions();
            RealmsClient client = RealmsClient.createRealmsClient();
            PingResult pingResult = new PingResult();
            pingResult.pingResults = regionPingResultList;
            pingResult.worldIds = RealmsMainScreen.this.getOwnedNonExpiredWorldIds();

            try {
               client.sendPingResults(pingResult);
            } catch (Throwable var5) {
               RealmsMainScreen.LOGGER.warn("Could not send ping result to Realms: ", var5);
            }

         }
      }).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      List<Long> ids = new ArrayList();

      for(RealmsServer server : this.realmsServers) {
         if (this.isSelfOwnedNonExpiredServer(server)) {
            ids.add(server.id);
         }
      }

      return ids;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
      this.stopRealmsFetcher();
   }

   public void setCreatedTrial(boolean createdTrial) {
      this.createdTrial = createdTrial;
   }

   private void onPlay() {
      RealmsServer server = this.findServer(this.selectedServerId);
      if (server != null) {
         this.play(server, this);
      }
   }

   private void onRenew() {
      RealmsServer server = this.findServer(this.selectedServerId);
      if (server != null) {
         String extensionUrl = "https://account.mojang.com/buy/realms?sid="
            + server.remoteSubscriptionId
            + "&pid="
            + Realms.getUUID()
            + "&ref="
            + (server.expiredTrial ? "expiredTrial" : "expiredRealm");
         this.browseURL(extensionUrl);
      }
   }

   private void createTrial() {
      if (this.trialsAvailable && !this.createdTrial) {
         Realms.setScreen(new RealmsCreateTrialScreen(this));
      }
   }

   private void checkClientCompatability() {
      if (!checkedClientCompatability) {
         checkedClientCompatability = true;
         (new Thread("MCO Compatability Checker #1") {
               public void run() {
                  RealmsClient client = RealmsClient.createRealmsClient();
   
                  try {
                     RealmsClient.CompatibleVersionResponse versionResponse = client.clientCompatible();
                     if (versionResponse.equals(RealmsClient.CompatibleVersionResponse.OUTDATED)) {
                        RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
                        Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     } else if (versionResponse.equals(RealmsClient.CompatibleVersionResponse.OTHER)) {
                        RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                        Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     } else {
                        RealmsMainScreen.this.checkParentalConsent();
                     }
                  } catch (RealmsServiceException var3) {
                     RealmsMainScreen.checkedClientCompatability = false;
                     RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var3.toString());
                     if (var3.httpResultCode == 401) {
                        RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.title"),
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.message"),
                           RealmsMainScreen.this.lastScreen
                        );
                        Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     } else {
                        Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
                     }
                  } catch (IOException var4) {
                     RealmsMainScreen.checkedClientCompatability = false;
                     RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
                     Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
                  }
               }
            })
            .start();
      }

   }

   private void checkUnreadNews() {
   }

   private void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               Boolean result = client.mcoEnabled();
               if (result) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  Realms.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var3.toString());
               Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
            } catch (IOException var4) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
               Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient client = RealmsClient.createRealmsClient();

               try {
                  Boolean result = client.stageAvailable();
                  if (result) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.realmsDataFetcher.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
               } catch (IOException var4) {
                  RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient client = RealmsClient.createRealmsClient();

               try {
                  Boolean result = client.stageAvailable();
                  if (result) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.realmsDataFetcher.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
               } catch (IOException var4) {
                  RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      realmsDataFetcher.forceUpdate();
   }

   private void stopRealmsFetcher() {
      realmsDataFetcher.stop();
   }

   private void configureClicked(RealmsServer selectedServer) {
      if (Realms.getUUID().equals(selectedServer.ownerUUID) || overrideConfigure) {
         this.saveListScrollPosition();
         Realms.setScreen(new RealmsConfigureWorldScreen(this, selectedServer.id));
      }

   }

   private void leaveClicked(RealmsServer selectedServer) {
      if (!Realms.getUUID().equals(selectedServer.ownerUUID)) {
         this.saveListScrollPosition();
         String line2 = getLocalizedString("mco.configure.world.leave.question.line1");
         String line3 = getLocalizedString("mco.configure.world.leave.question.line2");
         Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 4));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = this.realmSelectionList.getScroll();
   }

   private RealmsServer findServer(long id) {
      for(RealmsServer server : this.realmsServers) {
         if (server.id == id) {
            return server;
         }
      }

      return null;
   }

   private int findIndex(long serverId) {
      for(int i = 0; i < this.realmsServers.size(); ++i) {
         if (((RealmsServer)this.realmsServers.get(i)).id == serverId) {
            return i;
         }
      }

      return -1;
   }

   public void confirmResult(boolean result, int id) {
      if (id == 4) {
         if (result) {
            (new Thread("Realms-leave-server") {
               public void run() {
                  try {
                     RealmsServer server = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                     if (server != null) {
                        RealmsClient client = RealmsClient.createRealmsClient();
                        client.uninviteMyselfFrom(server.id);
                        RealmsMainScreen.realmsDataFetcher.removeItem(server);
                        RealmsMainScreen.this.realmsServers.remove(server);
                        RealmsMainScreen.this.selectedServerId = -1L;
                        RealmsMainScreen.this.playButton.active(false);
                     }
                  } catch (RealmsServiceException var3) {
                     RealmsMainScreen.LOGGER.error("Couldn't configure world");
                     Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
                  }

               }
            }).start();
         }

         Realms.setScreen(this);
      }

   }

   public void removeSelection() {
      this.selectedServerId = -1L;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.mindex = 0;
            this.sindex = 0;
            this.lindex = 0;
            if (this.shouldShowPopup() && this.popupOpenedByUser) {
               this.popupOpenedByUser = false;
            } else {
               Realms.setScreen(this.lastScreen);
            }

            return true;
         case 257:
            int index = this.realmSelectionList.getSelectedIndex();
            if (this.shouldShowMessageInList()) {
               if (index == 0) {
                  this.popupOpenedByUser = true;
                  return super.keyPressed(eventKey, scancode, mods);
               }

               --index;
            }

            if (index >= this.realmsServers.size()) {
               return super.keyPressed(eventKey, scancode, mods);
            } else {
               if (index != -1) {
                  RealmsServer server = (RealmsServer)this.realmsServers.get(index);
                  if (server == null) {
                     return super.keyPressed(eventKey, scancode, mods);
                  }

                  if (server.state == RealmsServer.State.UNINITIALIZED) {
                     this.selectedServerId = -1L;
                     Realms.setScreen(new RealmsCreateRealmScreen(server, this));
                  } else {
                     this.selectedServerId = server.id;
                  }
               }

               this.mindex = 0;
               this.sindex = 0;
               this.lindex = 0;
               return super.keyPressed(eventKey, scancode, mods);
            }
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public boolean charTyped(char ch, int mods) {
      if (this.mchars[this.mindex] == ch) {
         ++this.mindex;
         if (this.mindex == this.mchars.length) {
            this.mindex = 0;
            overrideConfigure = !overrideConfigure;
         }
      } else {
         this.mindex = 0;
      }

      if (this.schars[this.sindex] == ch) {
         ++this.sindex;
         if (this.sindex == this.schars.length) {
            this.sindex = 0;
            if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
               this.switchToProd();
            } else {
               this.switchToStage();
            }
         }
      } else {
         this.sindex = 0;
      }

      if (this.lchars[this.lindex] == ch) {
         ++this.lindex;
         if (this.lindex == this.lchars.length) {
            this.lindex = 0;
            if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
               this.switchToProd();
            } else {
               this.switchToLocal();
            }
         }
      } else {
         this.lindex = 0;
      }

      return true;
   }

   public void render(int xm, int ym, float a) {
      this.expiredHover = false;
      this.toolTip = null;
      this.renderBackground();
      this.realmSelectionList.render(xm, ym, a);
      this.drawRealmsLogo(this.width() / 2 - 50, 7);
      if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
         this.renderStage();
      }

      if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
         this.renderLocal();
      }

      if (this.shouldShowPopup()) {
         this.drawPopup(xm, ym);
      } else {
         if (this.showingPopup) {
            this.updateButtonStates(null);
            if (!this.hasWidget(this.realmSelectionList)) {
               this.addWidget(this.realmSelectionList);
            }

            RealmsServer server = this.findServer(this.selectedServerId);
            this.playButton.active(this.shouldPlayButtonBeActive(server));
         }

         this.showingPopup = false;
      }

      super.render(xm, ym, a);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         int ySprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            ySprite = 8;
         }

         int yo = this.height() / 2 - 83 - 3;
         int buttonHeight = yo + 147 - 20;
         RealmsScreen.blit(this.width() / 2 + 52 + 83, buttonHeight - 4, 0.0F, (float)ySprite, 8, 8, 8, 16);
         GlStateManager.popMatrix();
      }

   }

   private void drawRealmsLogo(int x, int y) {
      RealmsScreen.bind("realms:textures/gui/title/realms.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.scalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
      GlStateManager.popMatrix();
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.isOutsidePopup(x, y) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   private boolean isOutsidePopup(double xm, double ym) {
      int xo = this.popupX0();
      int yo = this.popupY0();
      return xm < (double)(xo - 5) || xm > (double)(xo + 315) || ym < (double)(yo - 5) || ym > (double)(yo + 171);
   }

   private void drawPopup(int xm, int ym) {
      int xo = this.popupX0();
      int yo = this.popupY0();
      String popupText = getLocalizedString("mco.selectServer.popup");
      List<String> strings = this.fontSplit(popupText, 100);
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates(null);
         if (this.hasWidget(this.realmSelectionList)) {
            this.removeWidget(this.realmSelectionList);
         }

         Realms.narrateNow(popupText);
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.7F);
      GlStateManager.enableBlend();
      RealmsScreen.bind("realms:textures/gui/realms/darken.png");
      GlStateManager.pushMatrix();
      int otherxo = 0;
      int otheryo = 32;
      RealmsScreen.blit(0, 32, 0.0F, 0.0F, this.width(), this.height() - 40 - 32, 310, 166);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RealmsScreen.bind("realms:textures/gui/realms/popup.png");
      GlStateManager.pushMatrix();
      RealmsScreen.blit(xo, yo, 0.0F, 0.0F, 310, 166, 310, 166);
      GlStateManager.popMatrix();
      RealmsScreen.bind(IMAGES_LOCATION[this.carouselIndex]);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(xo + 7, yo + 7, 0.0F, 0.0F, 195, 152, 195, 152);
      GlStateManager.popMatrix();
      if (this.carouselTick % 95 < 5) {
         if (!this.hasSwitchedCarouselImage) {
            if (this.carouselIndex == IMAGES_LOCATION.length - 1) {
               this.carouselIndex = 0;
            } else {
               ++this.carouselIndex;
            }

            this.hasSwitchedCarouselImage = true;
         }
      } else {
         this.hasSwitchedCarouselImage = false;
      }

      int index = 0;

      for(String s : strings) {
         int var10002 = this.width() / 2 + 52;
         ++index;
         this.drawString(s, var10002, yo + 10 * index - 3, 5000268, false);
      }

   }

   private int popupX0() {
      return (this.width() - 310) / 2;
   }

   private int popupY0() {
      return this.height() / 2 - 80;
   }

   private void drawInvitationPendingIcon(int xm, int ym, int x, int y, boolean selectedOrHovered) {
      int pendingInvitesCount = this.numberOfPendingInvites;
      boolean hovering = this.inPendingInvitationArea((double)xm, (double)ym);
      if (pendingInvitesCount != 0) {
         float scale = 0.25F + (1.0F + RealmsMth.sin((float)this.animTick * 0.5F)) * 0.25F;
         int color = 0xFF000000 | (int)(scale * 64.0F) << 16 | (int)(scale * 64.0F) << 8 | (int)(scale * 64.0F) << 0;
         this.fillGradient(x - 2, y - 2, x + 18, y + 18, color, color);
         color = 0xFF000000 | (int)(scale * 255.0F) << 16 | (int)(scale * 255.0F) << 8 | (int)(scale * 255.0F) << 0;
         this.fillGradient(x - 2, y - 2, x + 18, y - 1, color, color);
         this.fillGradient(x - 2, y - 2, x - 1, y + 18, color, color);
         this.fillGradient(x + 17, y - 2, x + 18, y + 18, color, color);
         this.fillGradient(x - 2, y + 17, x + 18, y + 18, color, color);
      }

      RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y - 6, selectedOrHovered ? 16.0F : 0.0F, 0.0F, 15, 25, 31, 25);
      GlStateManager.popMatrix();
      if (pendingInvitesCount != 0) {
         int spritePos = (Math.min(pendingInvitesCount, 6) - 1) * 8;
         int yOff = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x + 4, y + 4 + yOff, (float)spritePos, hovering ? 8.0F : 0.0F, 8, 8, 48, 16);
         GlStateManager.popMatrix();
      }

      int rx = xm + 12;
      if (hovering) {
         String message = getLocalizedString(pendingInvitesCount == 0 ? "mco.invites.nopending" : "mco.invites.pending");
         int width = this.fontWidth(message);
         this.fillGradient(rx - 3, ym - 3, rx + width + 3, ym + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(message, rx, ym, -1);
      }

   }

   private boolean inPendingInvitationArea(double xm, double ym) {
      int x1 = this.width() / 2 + 50;
      int x2 = this.width() / 2 + 66;
      int y1 = 11;
      int y2 = 23;
      if (this.numberOfPendingInvites != 0) {
         x1 -= 3;
         x2 += 3;
         y1 -= 5;
         y2 += 5;
      }

      return (double)x1 <= xm && xm <= (double)x2 && (double)y1 <= ym && ym <= (double)y2;
   }

   public void play(RealmsServer server, RealmsScreen cancelScreen) {
      if (server != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.connectToServer(server, cancelScreen);
      }

   }

   private void connectToServer(RealmsServer server, RealmsScreen cancelScreen) {
      RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
         cancelScreen, new RealmsTasks.RealmsGetServerDetailsTask(this, cancelScreen, server, this.connectLock)
      );
      longRunningMcoTaskScreen.start();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public boolean selectRealmsObjectListItem(int item, int buttonNum, double xMouse, double yMouse) {
      if (this.shouldShowMessageInList()) {
         if (item == 0) {
            this.updateButtonStates(null);
            return true;
         }

         --item;
      }

      if (item >= this.realmsServers.size()) {
         return false;
      } else {
         RealmsServer server = (RealmsServer)this.realmsServers.get(item);
         if (server.state == RealmsServer.State.UNINITIALIZED) {
            this.updateButtonStates(null);
            this.selectedServerId = -1L;
            return true;
         } else {
            this.selectedServerId = server.id;
            this.updateButtonStates(server);
            if (this.clicks >= 10 && this.playButton.active()) {
               this.play(this.findServer(this.selectedServerId), this);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private boolean isSelfOwnedServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID()) && !serverData.expired;
   }

   private void drawExpired(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         this.toolTip = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void drawExpiring(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      if (this.animTick % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         if (daysLeft <= 0) {
            this.toolTip = getLocalizedString("mco.selectServer.expires.soon");
         } else if (daysLeft == 1) {
            this.toolTip = getLocalizedString("mco.selectServer.expires.day");
         } else {
            this.toolTip = getLocalizedString("mco.selectServer.expires.days", new Object[]{daysLeft});
         }
      }

   }

   private void drawOpen(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         this.toolTip = getLocalizedString("mco.selectServer.open");
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         this.toolTip = getLocalizedString("mco.selectServer.closed");
      }

   }

   private void drawLeave(int x, int y, int xm, int ym) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/leave_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.selectServer.leave");
      }

   }

   private void drawConfigure(int x, int y, int xm, int ym) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/configure_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.selectServer.configure");
      }

   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int index = 0;
         int width = 0;

         for(String s : msg.split("\n")) {
            int theWidth = this.fontWidth(s);
            if (theWidth > width) {
               width = theWidth;
            }
         }

         int rx = x - width - 5;
         int ry = y;
         if (rx < 0) {
            rx = x + 12;
         }

         for(String s : msg.split("\n")) {
            this.fillGradient(rx - 3, ry - (index == 0 ? 3 : 0) + index, rx + width + 3, ry + 8 + 3 + index, -1073741824, -1073741824);
            this.fontDrawShadow(s, rx, ry + index, 16777215);
            index += 10;
         }

      }
   }

   private void renderMoreInfo(int xm, int ym, int x, int y, boolean hoveredOrFocused) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= y && ym <= y + 20) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/questionmark.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hoveredOrFocused ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.selectServer.info");
      }

   }

   private void renderNews(int xm, int ym, boolean unread, int x, int y, boolean selectedOrHovered) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= y && ym <= y + 20) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/news_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, selectedOrHovered ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
      GlStateManager.popMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.news");
      }

      if (unread) {
         int yOff = hovered
            ? 0
            : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x + 10, y + 2 + yOff, 40.0F, 0.0F, 8, 8, 48, 16);
         GlStateManager.popMatrix();
      }

   }

   private void renderLocal() {
      String text = "LOCAL!";
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scalef(1.5F, 1.5F, 1.5F);
      this.drawString("LOCAL!", 0, 0, 8388479);
      GlStateManager.popMatrix();
   }

   private void renderStage() {
      String text = "STAGE!";
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scalef(1.5F, 1.5F, 1.5F);
      this.drawString("STAGE!", 0, 0, -256);
      GlStateManager.popMatrix();
   }

   public RealmsMainScreen newScreen() {
      return new RealmsMainScreen(this.lastScreen);
   }

   public void closePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      }

   }

   static {
      String version = RealmsVersion.getVersion();
      if (version != null) {
         LOGGER.info("Realms library version == " + version);
      }

   }

   private class CloseButton extends RealmsButton {
      public CloseButton() {
         super(11, RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, RealmsScreen.getLocalizedString("mco.selectServer.close"));
      }

      public void tick() {
         super.tick();
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         RealmsScreen.bind("realms:textures/gui/realms/cross_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(this.x(), this.y(), 0.0F, this.getProxy().isHovered() ? 12.0F : 0.0F, 12, 12, 12, 24);
         GlStateManager.popMatrix();
         if (this.getProxy().isMouseOver((double)mouseX, (double)mouseY)) {
            RealmsMainScreen.this.toolTip = this.getProxy().getMessage();
         }

      }

      public void onPress() {
         RealmsMainScreen.this.popupOpenedByUser = false;
      }
   }

   private class NewsButton extends RealmsButton {
      public NewsButton() {
         super(9, RealmsMainScreen.this.width() - 62, 6, 20, 20, "");
      }

      public void tick() {
         this.setMessage(Realms.getLocalizedString("mco.news", new Object[0]));
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void onPress() {
         if (RealmsMainScreen.this.newsLink != null) {
            RealmsUtil.browseTo(RealmsMainScreen.this.newsLink);
            if (RealmsMainScreen.this.hasUnreadNews) {
               RealmsPersistence.RealmsPersistenceData data = RealmsPersistence.readFile();
               data.hasUnreadNews = false;
               RealmsMainScreen.this.hasUnreadNews = false;
               RealmsPersistence.writeFile(data);
            }

         }
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         RealmsMainScreen.this.renderNews(mouseX, mouseY, RealmsMainScreen.this.hasUnreadNews, this.x(), this.y(), this.getProxy().isHovered());
      }
   }

   private class PendingInvitesButton extends RealmsButton {
      public PendingInvitesButton() {
         super(8, RealmsMainScreen.this.width() / 2 + 47, 6, 22, 22, "");
      }

      public void tick() {
         this.setMessage(
            Realms.getLocalizedString(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending", new Object[0])
         );
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void onPress() {
         RealmsPendingInvitesScreen pendingInvitationScreen = new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen);
         Realms.setScreen(pendingInvitationScreen);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         RealmsMainScreen.this.drawInvitationPendingIcon(mouseX, mouseY, this.x(), this.y(), this.getProxy().isHovered());
      }
   }

   private class RealmSelectionList extends RealmsObjectSelectionList {
      public RealmSelectionList() {
         super(RealmsMainScreen.this.width() + 15, RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 40, 36);
      }

      public int getRowWidth() {
         return (int)((double)this.width() * 0.6);
      }

      public boolean isFocused() {
         return RealmsMainScreen.this.isFocused(this);
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 160;
            int x1 = this.getScrollbarPosition();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + 7;
               this.selectItem(slot, buttonNum, xm, ym);
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }

      protected void moveSelection(int dir) {
         int index = this.getSelectedIndex();
         int newIndex = RealmsMth.clamp(index + dir, 0, this.getItemCount() - 1);
         super.moveSelection(dir);
         this.selectItem(newIndex, 0, 0.0, 0.0);
         if (RealmsMainScreen.this.shouldShowMessageInList()) {
            if (newIndex == 0) {
               RealmsMainScreen.this.updateButtonStates(null);
               return;
            }

            --newIndex;
         }

         if (newIndex < RealmsMainScreen.this.realmsServers.size()) {
            RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(newIndex);
            if (server != null) {
               if (server.state == RealmsServer.State.UNINITIALIZED) {
                  RealmsMainScreen.this.updateButtonStates(null);
               } else {
                  RealmsMainScreen.this.updateButtonStates((RealmsServer)RealmsMainScreen.this.realmsServers.get(newIndex));
               }

            }
         }
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         this.setSelected(item);
         if (item != -1) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
               if (item == 0) {
                  String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + RealmsScreen.getLocalizedString("mco.trial.message.line2");
                  Realms.narrateNow(msg);
                  return RealmsMainScreen.this.selectRealmsObjectListItem(item, buttonNum, xMouse, yMouse);
               }

               --item;
            }

            if (item >= RealmsMainScreen.this.realmsServers.size()) {
               return false;
            }

            RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(item);
            if (server == null) {
               return false;
            }

            if (server.state == RealmsServer.State.UNINITIALIZED) {
               Realms.narrateNow(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"));
            } else {
               Realms.narrateNow(
                  RealmsScreen.getLocalizedString("narrator.select", new Object[]{((RealmsServer)RealmsMainScreen.this.realmsServers.get(item)).name})
               );
            }
         }

         return RealmsMainScreen.this.selectRealmsObjectListItem(item, buttonNum, xMouse, yMouse);
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (RealmsMainScreen.this.shouldShowMessageInList()) {
            if (slot == 0) {
               RealmsMainScreen.this.popupOpenedByUser = true;
               return;
            }

            --slot;
         }

         if (slot < RealmsMainScreen.this.realmsServers.size()) {
            RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(slot);
            if (server != null) {
               if (server.state == RealmsServer.State.UNINITIALIZED) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  Realms.setScreen(new RealmsCreateRealmScreen(server, RealmsMainScreen.this));
               } else {
                  RealmsMainScreen.this.selectedServerId = server.id;
               }

               if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))
                  )
                {
                  RealmsMainScreen.this.selectedServerId = server.id;
                  RealmsMainScreen.this.configureClicked(server);
               } else if (RealmsMainScreen.this.toolTip != null
                  && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
                  RealmsMainScreen.this.selectedServerId = server.id;
                  RealmsMainScreen.this.leaveClicked(server);
               } else if (RealmsMainScreen.this.isSelfOwnedServer(server) && server.expired && RealmsMainScreen.this.expiredHover) {
                  RealmsMainScreen.this.onRenew();
               }

            }
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }
   }

   private class RealmSelectionListEntry extends RealmListEntry {
      final RealmsServer mServerData;

      public RealmSelectionListEntry(RealmsServer serverData) {
         this.mServerData = serverData;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderMcoServerItem(this.mServerData, rowLeft, rowTop, mouseX, mouseY);
      }

      private void renderMcoServerItem(RealmsServer serverData, int x, int y, int mouseX, int mouseY) {
         if (serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlphaTest();
            GlStateManager.pushMatrix();
            RealmsScreen.blit(x + 10, y + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            GlStateManager.popMatrix();
            float scale = 0.5F + (1.0F + RealmsMth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int textColor = 0xFF000000 | (int)(127.0F * scale) << 16 | (int)(255.0F * scale) << 8 | (int)(127.0F * scale);
            RealmsMainScreen.this.drawCenteredString(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"), x + 10 + 40 + 75, y + 12, textColor);
         } else {
            int dx = 225;
            int dy = 2;
            if (serverData.expired) {
               RealmsMainScreen.this.drawExpired(x + 225 - 14, y + 2, mouseX, mouseY);
            } else if (serverData.state == RealmsServer.State.CLOSED) {
               RealmsMainScreen.this.drawClose(x + 225 - 14, y + 2, mouseX, mouseY);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.daysLeft < 7) {
               RealmsMainScreen.this.drawExpiring(x + 225 - 14, y + 2, mouseX, mouseY, serverData.daysLeft);
            } else if (serverData.state == RealmsServer.State.OPEN) {
               RealmsMainScreen.this.drawOpen(x + 225 - 14, y + 2, mouseX, mouseY);
            }

            if (!RealmsMainScreen.this.isSelfOwnedServer(serverData) && !RealmsMainScreen.overrideConfigure) {
               RealmsMainScreen.this.drawLeave(x + 225, y + 2, mouseX, mouseY);
            } else {
               RealmsMainScreen.this.drawConfigure(x + 225, y + 2, mouseX, mouseY);
            }

            if (!"0".equals(serverData.serverPing.nrOfPlayers)) {
               String coloredNumPlayers = ChatFormatting.GRAY + "" + serverData.serverPing.nrOfPlayers;
               RealmsMainScreen.this.drawString(coloredNumPlayers, x + 207 - RealmsMainScreen.this.fontWidth(coloredNumPlayers), y + 3, 8421504);
               if (mouseX >= x + 207 - RealmsMainScreen.this.fontWidth(coloredNumPlayers)
                  && mouseX <= x + 207
                  && mouseY >= y + 1
                  && mouseY <= y + 10
                  && mouseY < RealmsMainScreen.this.height() - 40
                  && mouseY > 32
                  && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.toolTip = serverData.serverPing.playerList;
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.expired) {
               boolean hovered = false;
               GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.enableBlend();
               RealmsScreen.bind("minecraft:textures/gui/widgets.png");
               GlStateManager.pushMatrix();
               GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
               String expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredList");
               String expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredRenew");
               if (serverData.expiredTrial) {
                  expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredTrial");
                  expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredSubscribe");
               }

               int buttonWidth = RealmsMainScreen.this.fontWidth(expirationButtonText) + 20;
               int buttonHeight = 16;
               int buttonX = x + RealmsMainScreen.this.fontWidth(expirationText) + 8;
               int buttonY = y + 13;
               if (mouseX >= buttonX
                  && mouseX < buttonX + buttonWidth
                  && mouseY > buttonY
                  && mouseY <= buttonY + 16 & mouseY < RealmsMainScreen.this.height() - 40
                  && mouseY > 32
                  && !RealmsMainScreen.this.shouldShowPopup()) {
                  hovered = true;
                  RealmsMainScreen.this.expiredHover = true;
               }

               int yImage = hovered ? 2 : 1;
               RealmsScreen.blit(buttonX, buttonY, 0.0F, (float)(46 + yImage * 20), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(buttonX + buttonWidth / 2, buttonY, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(buttonX, buttonY + 8, 0.0F, (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(
                  buttonX + buttonWidth / 2, buttonY + 8, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256, 256
               );
               GlStateManager.popMatrix();
               GlStateManager.disableBlend();
               int textHeight = y + 11 + 5;
               int buttonTextColor = hovered ? 16777120 : 16777215;
               RealmsMainScreen.this.drawString(expirationText, x + 2, textHeight + 1, 15553363);
               RealmsMainScreen.this.drawCenteredString(expirationButtonText, buttonX + buttonWidth / 2, textHeight + 1, buttonTextColor);
            } else {
               if (serverData.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
                  int motdColor = 13413468;
                  String miniGameStr = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
                  int mgWidth = RealmsMainScreen.this.fontWidth(miniGameStr);
                  RealmsMainScreen.this.drawString(miniGameStr, x + 2, y + 12, 13413468);
                  RealmsMainScreen.this.drawString(serverData.getMinigameName(), x + 2 + mgWidth, y + 12, 7105644);
               } else {
                  RealmsMainScreen.this.drawString(serverData.getDescription(), x + 2, y + 12, 7105644);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(serverData)) {
                  RealmsMainScreen.this.drawString(serverData.owner, x + 2, y + 12 + 11, 5000268);
               }
            }

            RealmsMainScreen.this.drawString(serverData.getName(), x + 2, y + 1, 16777215);
            RealmsTextureManager.withBoundFace(serverData.ownerUUID, () -> {
               GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
               RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            });
         }
      }
   }

   private class RealmSelectionListTrialEntry extends RealmListEntry {
      public RealmSelectionListTrialEntry() {
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderTrialItem(index, rowLeft, rowTop, mouseX, mouseY);
      }

      private void renderTrialItem(int i, int x, int y, int mouseX, int mouseY) {
         int ry = y + 8;
         int index = 0;
         String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
         boolean hovered = false;
         if (x <= mouseX && mouseX <= RealmsMainScreen.this.realmSelectionList.getScroll() && y <= mouseY && mouseY <= y + 32) {
            hovered = true;
         }

         int textColor = 8388479;
         if (hovered && !RealmsMainScreen.this.shouldShowPopup()) {
            textColor = 6077788;
         }

         for(String s : msg.split("\\\\n")) {
            RealmsMainScreen.this.drawCenteredString(s, RealmsMainScreen.this.width() / 2, ry + index, textColor);
            index += 10;
         }

      }
   }

   private class ShowPopupButton extends RealmsButton {
      public ShowPopupButton() {
         super(10, RealmsMainScreen.this.width() - 37, 6, 20, 20, RealmsScreen.getLocalizedString("mco.selectServer.info"));
      }

      public void tick() {
         super.tick();
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         RealmsMainScreen.this.renderMoreInfo(mouseX, mouseY, this.x(), this.y(), this.getProxy().isHovered());
      }

      public void onPress() {
         RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser;
      }
   }
}
