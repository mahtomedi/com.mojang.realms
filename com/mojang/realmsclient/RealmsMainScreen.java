package com.mojang.realmsclient;

import com.google.common.collect.Lists;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class RealmsMainScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static boolean overrideConfigure;
   private boolean dontSetConnectedToRealms;
   protected static final int BUTTON_BACK_ID = 0;
   protected static final int BUTTON_PLAY_ID = 1;
   private static final int LEAVE_ID = 2;
   private static final int BUTTON_BUY_ID = 3;
   private static final int BUTTON_TRY_ID = 4;
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
   private volatile RealmsMainScreen.ServerSelectionList serverSelectionList;
   private long selectedServerId = -1L;
   private RealmsButton playButton;
   private RealmsButton backButton;
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
   private boolean serverListListening;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   private volatile boolean hasUnreadNews;
   private volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   boolean hasSwitchedCarouselImage;
   private static RealmsScreen realmsGenericErrorScreen;
   private AtomicReference<RealmsScreen> errorScreenToShow = new AtomicReference();
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
         this.buttonsClear();
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
      this.buttonsAdd(this.playButton = new RealmsButton(1, this.width() / 2 - 98, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.play")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsMainScreen.this.onPlay();
         }
      });
      this.buttonsAdd(this.backButton = new RealmsButton(0, this.width() / 2 + 6, this.height() - 32, 98, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            if (!RealmsMainScreen.this.justClosedPopup) {
               Realms.setScreen(RealmsMainScreen.this.lastScreen);
            }

         }
      });
      RealmsServer server = this.findServer(this.selectedServerId);
      this.playButton.active(this.shouldPlayButtonBeActive(server));
   }

   private boolean shouldPlayButtonBeActive(RealmsServer server) {
      return server != null && !server.expired && server.state == RealmsServer.State.OPEN;
   }

   public void postInit() {
      if (this.hasParentalConsent() && this.hasFetchedServers) {
         this.addButtons();
      }

      this.serverSelectionList = new RealmsMainScreen.ServerSelectionList();
      this.serverSelectionList.setLeftPos(-15);
      if (lastScrollYPosition != -1) {
         this.serverSelectionList.scroll(lastScrollYPosition);
      }

      this.addWidget(this.serverSelectionList);
      this.serverListListening = true;
      this.focusOn(this.serverSelectionList);
   }

   public void tick() {
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

               this.realmsServers = newServers;
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

      }
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
                        RealmsMainScreen.this.errorScreenToShow.set(RealmsMainScreen.realmsGenericErrorScreen);
                     } else if (versionResponse.equals(RealmsClient.CompatibleVersionResponse.OTHER)) {
                        RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                        RealmsMainScreen.this.errorScreenToShow.set(RealmsMainScreen.realmsGenericErrorScreen);
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
                        RealmsMainScreen.this.errorScreenToShow.set(RealmsMainScreen.realmsGenericErrorScreen);
                     } else {
                        RealmsMainScreen.this.errorScreenToShow.set(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
                     }
                  } catch (IOException var4) {
                     RealmsMainScreen.checkedClientCompatability = false;
                     RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
                     RealmsMainScreen.this.errorScreenToShow.set(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
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
                  RealmsMainScreen.this.errorScreenToShow.set(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var3.toString());
               RealmsMainScreen.this.errorScreenToShow.set(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
            } catch (IOException var4) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
               RealmsMainScreen.this.errorScreenToShow.set(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
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
         Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, line2, line3, true, 2));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = this.serverSelectionList.getScroll();
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
      if (id == 2) {
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
                     RealmsMainScreen.this.errorScreenToShow.set(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
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
            this.mindex = 0;
            this.sindex = 0;
            this.lindex = 0;
            if (!this.shouldShowPopup()) {
               return super.keyPressed(eventKey, scancode, mods);
            }

            if (!this.isKeyDown(340) && !this.isKeyDown(344)) {
               this.onPlay();
            } else {
               RealmsServer server = this.findServer(this.selectedServerId);
               if (server != null) {
                  this.configureClicked(server);
               }
            }

            return true;
         case 258:
         case 259:
         case 260:
         case 261:
         case 262:
         case 263:
         default:
            return super.keyPressed(eventKey, scancode, mods);
         case 264:
            if (this.selectedServerId != -1L && !this.shouldShowPopup()) {
               RealmsServer server = this.findServer(this.selectedServerId);
               int theIndex = this.realmsServers.indexOf(server);
               int maxScroll = Math.max(0, this.serverSelectionList.getMaxPosition() - (this.height() - 40 - 32));
               if (theIndex == this.realmsServers.size() - 1) {
                  this.serverSelectionList.scroll(maxScroll - this.serverSelectionList.getScroll() + 36);
                  return true;
               }

               if (server != null && theIndex > -1 && theIndex < this.realmsServers.size() - 1) {
                  int newIndex = theIndex + 1;
                  RealmsServer newServer = (RealmsServer)this.realmsServers.get(newIndex);
                  if (newIndex == this.realmsServers.size() - 1) {
                     this.selectedServerId = newServer.id;
                     this.serverSelectionList.scroll(maxScroll - this.serverSelectionList.getScroll() + 36);
                     return true;
                  }

                  if (newServer != null) {
                     this.selectedServerId = newServer.id;
                     int maxItemsInView = (int)Math.floor((double)((this.height() - 40 - 32) / 36));
                     int scroll = this.serverSelectionList.getScroll();
                     int hiddenItems = (int)Math.ceil((double)((float)scroll / 36.0F));
                     int scrollPerItem = maxScroll / this.realmsServers.size();
                     int positionNeeded = scrollPerItem * newIndex;
                     int proposedScroll = positionNeeded - this.serverSelectionList.getScroll();
                     if (proposedScroll > 0) {
                        proposedScroll += scrollPerItem;
                     }

                     if (newIndex < hiddenItems || newIndex >= hiddenItems + maxItemsInView) {
                        this.serverSelectionList.scroll(proposedScroll);
                     }

                     return true;
                  }
               }
            }

            if (!this.shouldShowPopup() && !this.realmsServers.isEmpty()) {
               this.selectedServerId = ((RealmsServer)this.realmsServers.get(0)).id;
               this.serverSelectionList.scroll(-(this.serverSelectionList.getItemCount() * 36));
            }

            return true;
         case 265:
            if (this.selectedServerId != -1L && !this.shouldShowPopup()) {
               RealmsServer server = this.findServer(this.selectedServerId);
               int theIndex = this.realmsServers.indexOf(server);
               if (theIndex == 0) {
                  this.serverSelectionList.scroll(0 - this.serverSelectionList.getScroll());
                  return true;
               }

               if (server != null && theIndex > 0) {
                  int newIndex = theIndex - 1;
                  RealmsServer newServer = (RealmsServer)this.realmsServers.get(newIndex);
                  if (newServer != null) {
                     this.selectedServerId = newServer.id;
                     int maxScroll = Math.max(0, this.serverSelectionList.getMaxPosition() - (this.height() - 40 - 32 - 4));
                     int maxItemsInView = (int)Math.floor((double)((this.height() - 40 - 32) / 36));
                     int scroll = this.serverSelectionList.getScroll();
                     int hiddenItems = (int)Math.ceil((double)((float)scroll / 36.0F));
                     int scrollPerItem = maxScroll / this.realmsServers.size();
                     int positionNeeded = scrollPerItem * newIndex;
                     int proposedScroll = positionNeeded - this.serverSelectionList.getScroll();
                     if (newIndex < hiddenItems || newIndex > hiddenItems + maxItemsInView) {
                        this.serverSelectionList.scroll(proposedScroll);
                     }

                     return true;
                  }
               }
            }

            if (!this.shouldShowPopup() && !this.realmsServers.isEmpty()) {
               this.selectedServerId = ((RealmsServer)this.realmsServers.get(0)).id;
               this.serverSelectionList.scroll(0 - this.serverSelectionList.getScroll());
            }

            return true;
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
      RealmsScreen errScr = (RealmsScreen)this.errorScreenToShow.getAndSet(null);
      if (errScr != null) {
         Realms.setScreen(errScr);
      }

      this.expiredHover = false;
      this.toolTip = null;
      this.renderBackground();
      this.serverSelectionList.render(xm, ym, a);
      this.drawRealmsLogo(this.width() / 2 - 50, 7);
      if ((!this.shouldShowPopup() || this.popupOpenedByUser) && this.hasParentalConsent() && this.hasFetchedServers) {
         this.renderMoreInfo(xm, ym);
      }

      this.renderNews(xm, ym, this.hasUnreadNews);
      this.drawInvitationPendingIcon(xm, ym);
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
            this.buttonsClear();
            this.buttonsAdd(this.playButton);
            this.buttonsAdd(this.backButton);
            if (!this.serverListListening) {
               this.addWidget(this.serverSelectionList);
               this.serverListListening = true;
            }

            RealmsServer server = this.findServer(this.selectedServerId);
            this.playButton.active(this.shouldPlayButtonBeActive(server));
         }

         this.showingPopup = false;
      }

      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

      super.render(xm, ym, a);
      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         int ySprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            ySprite = 8;
         }

         int yo = this.height() / 2 - 83 - 3;
         int buttonHeight = yo + 147 - 20;
         RealmsScreen.blit(this.width() / 2 + 52 + 83, buttonHeight - 4, 0.0F, (float)ySprite, 8, 8, 8.0F, 16.0F);
         GL11.glPopMatrix();
      }

   }

   private void drawRealmsLogo(int x, int y) {
      RealmsScreen.bind("realms:textures/gui/title/realms.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2 - 5, 0.0F, 0.0F, 200, 50, 200.0F, 50.0F);
      GL11.glPopMatrix();
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.inPendingInvitationArea(x, y)) {
         RealmsPendingInvitesScreen pendingInvitationScreen = new RealmsPendingInvitesScreen(this.lastScreen);
         Realms.setScreen(pendingInvitationScreen);
         return true;
      } else if (this.toolTip != null && this.toolTip.equals(getLocalizedString("mco.selectServer.info"))) {
         this.popupOpenedByUser = !this.popupOpenedByUser;
         return true;
      } else if (this.toolTip != null && this.toolTip.equals(getLocalizedString("mco.selectServer.close"))) {
         this.popupOpenedByUser = false;
         return true;
      } else if (this.toolTip != null && this.toolTip.equals(getLocalizedString("mco.news"))) {
         if (this.newsLink == null) {
            return false;
         } else {
            RealmsUtil.browseTo(this.newsLink);
            if (this.hasUnreadNews) {
               RealmsPersistence.RealmsPersistenceData data = RealmsPersistence.readFile();
               data.hasUnreadNews = false;
               this.hasUnreadNews = false;
               RealmsPersistence.writeFile(data);
            }

            return true;
         }
      } else if (this.isOutsidePopup(x, y) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   private boolean isOutsidePopup(double xm, double ym) {
      int xo = (this.width() - 310) / 2;
      int yo = this.height() / 2 - 83 - 3;
      return xm < (double)(xo - 5) || xm > (double)(xo + 315) || ym < (double)(yo - 5) || ym > (double)(yo + 171);
   }

   private void drawPopup(int xm, int ym) {
      int xo = (this.width() - 310) / 2;
      int yo = this.height() / 2 - 83 - 3;
      int buttonHeight = yo + 147 - 20;
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         if (this.hasFetchedServers && this.realmsServers.isEmpty()) {
            this.buttonsClear();
            this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 49, this.height() - 32, 98, 20, getLocalizedString("gui.back")) {
               public void onClick(double mouseX, double mouseY) {
                  Realms.setScreen(RealmsMainScreen.this.lastScreen);
               }
            });
         }

         if (this.trialsAvailable && !this.createdTrial) {
            buttonHeight -= 10;
            this.buttonsAdd(new RealmsButton(4, this.width() / 2 + 52, buttonHeight, 98, 20, getLocalizedString("mco.selectServer.trial")) {
               public void onClick(double mouseX, double mouseY) {
                  RealmsMainScreen.this.createTrial();
               }
            });
            buttonHeight = yo + 170 - 20 - 10;
         }

         this.buttonsAdd(new RealmsButton(3, this.width() / 2 + 52, buttonHeight, 98, 20, getLocalizedString("mco.selectServer.buy")) {
            public void onClick(double mouseX, double mouseY) {
               RealmsUtil.browseTo("https://minecraft.net/realms");
            }
         });
         this.playButton.active(false);
         if (this.serverListListening) {
            this.removeWidget(this.serverSelectionList);
            this.serverListListening = false;
         }
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
      GL11.glEnable(3042);
      RealmsScreen.bind("realms:textures/gui/realms/darken.png");
      GL11.glPushMatrix();
      int otherxo = 0;
      int otheryo = 32;
      RealmsScreen.blit(0, 32, 0.0F, 0.0F, this.width(), this.height() - 40 - 32, 310.0F, 166.0F);
      GL11.glPopMatrix();
      GL11.glDisable(3042);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      RealmsScreen.bind("realms:textures/gui/realms/popup.png");
      GL11.glPushMatrix();
      RealmsScreen.blit(xo, yo, 0.0F, 0.0F, 310, 166, 310.0F, 166.0F);
      GL11.glPopMatrix();
      RealmsScreen.bind(IMAGES_LOCATION[this.carouselIndex]);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(xo + 7, yo + 7, 0.0F, 0.0F, 195, 152, 195.0F, 152.0F);
      GL11.glPopMatrix();
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

      if (this.popupOpenedByUser) {
         boolean crossHovered = false;
         int bx = xo + 4;
         int by = yo + 4;
         if (xm >= bx && xm <= bx + 12 && ym >= by && ym <= by + 12) {
            this.toolTip = getLocalizedString("mco.selectServer.close");
            crossHovered = true;
         }

         RealmsScreen.bind("realms:textures/gui/realms/cross_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(bx, by, 0.0F, crossHovered ? 12.0F : 0.0F, 12, 12, 12.0F, 24.0F);
         GL11.glPopMatrix();
         if (crossHovered) {
            this.toolTip = getLocalizedString("mco.selectServer.close");
         }
      }

      List<String> strings = this.fontSplit(getLocalizedString("mco.selectServer.popup"), 100);
      int index = 0;

      for(String s : strings) {
         int var10002 = this.width() / 2 + 52;
         ++index;
         this.drawString(s, var10002, yo + 10 * index - 3, 5000268, false);
      }

   }

   private void drawInvitationPendingIcon(int xm, int ym) {
      int pendingInvitesCount = this.numberOfPendingInvites;
      boolean hovering = this.inPendingInvitationArea((double)xm, (double)ym);
      int baseX = this.width() / 2 + 50;
      int baseY = 8;
      if (pendingInvitesCount != 0) {
         float scale = 0.25F + (1.0F + RealmsMth.sin((float)this.animTick * 0.5F)) * 0.25F;
         int color = 0xFF000000 | (int)(scale * 64.0F) << 16 | (int)(scale * 64.0F) << 8 | (int)(scale * 64.0F) << 0;
         this.fillGradient(baseX - 2, 6, baseX + 18, 26, color, color);
         color = 0xFF000000 | (int)(scale * 255.0F) << 16 | (int)(scale * 255.0F) << 8 | (int)(scale * 255.0F) << 0;
         this.fillGradient(baseX - 2, 6, baseX + 18, 7, color, color);
         this.fillGradient(baseX - 2, 6, baseX - 1, 26, color, color);
         this.fillGradient(baseX + 17, 6, baseX + 18, 26, color, color);
         this.fillGradient(baseX - 2, 25, baseX + 18, 26, color, color);
      }

      RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(baseX, 2, hovering ? 16.0F : 0.0F, 0.0F, 15, 25, 31.0F, 25.0F);
      GL11.glPopMatrix();
      if (pendingInvitesCount != 0) {
         int spritePos = (Math.min(pendingInvitesCount, 6) - 1) * 8;
         int yOff = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(baseX + 4, 12 + yOff, (float)spritePos, hovering ? 8.0F : 0.0F, 8, 8, 48.0F, 16.0F);
         GL11.glPopMatrix();
      }

      if (hovering) {
         int rx = xm + 12;
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

   private boolean isSelfOwnedServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID()) && !serverData.expired;
   }

   private void drawExpired(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         this.toolTip = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void drawExpiring(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      if (this.animTick % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20.0F, 28.0F);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20.0F, 28.0F);
      }

      GL11.glPopMatrix();
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
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.shouldShowPopup()) {
         this.toolTip = getLocalizedString("mco.selectServer.open");
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10.0F, 28.0F);
      GL11.glPopMatrix();
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
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56.0F, 28.0F);
      GL11.glPopMatrix();
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
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56.0F, 28.0F);
      GL11.glPopMatrix();
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

   private void renderMoreInfo(int xm, int ym) {
      int x = this.width() - 17 - 20;
      int y = 6;
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= 6 && ym <= 26) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/questionmark.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, 6, hovered ? 20.0F : 0.0F, 0.0F, 20, 20, 40.0F, 20.0F);
      GL11.glPopMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.selectServer.info");
      }

   }

   private void renderNews(int xm, int ym, boolean unread) {
      int x = this.width() - 17 - 20 - 25;
      int y = 6;
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= 6 && ym <= 26) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/news_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(x, 6, hovered ? 20.0F : 0.0F, 0.0F, 20, 20, 40.0F, 20.0F);
      GL11.glPopMatrix();
      if (hovered) {
         this.toolTip = getLocalizedString("mco.news");
      }

      if (unread) {
         int yOff = hovered
            ? 0
            : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(x + 10, 8 + yOff, 40.0F, 0.0F, 8, 8, 48.0F, 16.0F);
         GL11.glPopMatrix();
      }

   }

   private void renderLocal() {
      String text = "LOCAL!";
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GL11.glScalef(1.5F, 1.5F, 1.5F);
      this.drawString("LOCAL!", 0, 0, 8388479);
      GL11.glPopMatrix();
   }

   private void renderStage() {
      String text = "STAGE!";
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GL11.glScalef(1.5F, 1.5F, 1.5F);
      this.drawString("STAGE!", 0, 0, -256);
      GL11.glPopMatrix();
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

   private class ServerSelectionList extends RealmsClickableScrolledSelectionList {
      public ServerSelectionList() {
         super(RealmsMainScreen.this.width() + 15, RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 40, 36);
      }

      public boolean isVisible() {
         return !RealmsMainScreen.this.shouldShowPopup();
      }

      public int getItemCount() {
         return RealmsMainScreen.this.shouldShowMessageInList() ? RealmsMainScreen.this.realmsServers.size() + 1 : RealmsMainScreen.this.realmsServers.size();
      }

      public boolean selectItem(int item, int buttonNum, double xMouse, double yMouse) {
         if (buttonNum != 0) {
            return false;
         } else {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
               if (item == 0) {
                  RealmsMainScreen.this.popupOpenedByUser = true;
                  return true;
               }

               --item;
            }

            if (item >= RealmsMainScreen.this.realmsServers.size()) {
               return false;
            } else {
               RealmsServer server = (RealmsServer)RealmsMainScreen.this.realmsServers.get(item);
               if (server.state == RealmsServer.State.UNINITIALIZED) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  Realms.setScreen(new RealmsCreateRealmScreen(server, RealmsMainScreen.this));
               } else {
                  RealmsMainScreen.this.selectedServerId = server.id;
               }

               RealmsMainScreen.this.playButton.active(RealmsMainScreen.this.shouldPlayButtonBeActive(server));
               if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active()) {
                  RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
                  return true;
               } else {
                  return false;
               }
            }
         }
      }

      public boolean isSelectedItem(int item) {
         if (RealmsMainScreen.this.shouldShowMessageInList()) {
            if (item == 0) {
               return false;
            }

            --item;
         }

         return item == RealmsMainScreen.this.findIndex(RealmsMainScreen.this.selectedServerId);
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getScrollbarPosition() {
         return super.getScrollbarPosition() + 15;
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         if (RealmsMainScreen.this.shouldShowMessageInList()) {
            if (i == 0) {
               this.renderTrialItem(0, x, y, mouseX, mouseY);
               return;
            }

            --i;
         }

         if (i < RealmsMainScreen.this.realmsServers.size()) {
            this.renderMcoServerItem(i, x, y, mouseX, mouseY);
         }

      }

      private void renderTrialItem(int i, int x, int y, int mouseX, int mouseY) {
         int ry = y + 8;
         int index = 0;
         String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
         boolean hovered = false;
         if (x <= mouseX && mouseX <= this.getScrollbarPosition() && y <= mouseY && mouseY <= y + 32) {
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

      public void renderSelected(int width, int y, int h, Tezzelator t) {
         int x0 = this.getScrollbarPosition() - 300;
         int x1 = this.getScrollbarPosition() - 5;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3553);
         t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
         t.vertex((double)x0, (double)(y + h + 2), 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y + h + 2), 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x1, (double)(y - 2), 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)x0, (double)(y - 2), 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y + h + 1), 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y + h + 1), 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x1 - 1), (double)(y - 1), 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.vertex((double)(x0 + 1), (double)(y - 1), 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
         t.end();
         GL11.glEnable(3553);
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
                  String extensionUrl = "https://account.mojang.com/buy/realms?sid="
                     + server.remoteSubscriptionId
                     + "&pid="
                     + Realms.getUUID()
                     + "&ref="
                     + (server.expiredTrial ? "expiredTrial" : "expiredRealm");
                  this.browseURL(extensionUrl);
               }

            }
         }
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = this.width() / 2 - 160;
            int x1 = this.getScrollbarPosition();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + (int)this.yo() - 4;
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

      private void renderMcoServerItem(int i, int x, int y, int mouseX, int mouseY) {
         RealmsServer serverData = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i);
         if (serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(3008);
            GL11.glPushMatrix();
            RealmsScreen.blit(x + 10, y + 6, 0.0F, 0.0F, 40, 20, 40.0F, 20.0F);
            GL11.glPopMatrix();
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
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               GL11.glEnable(3042);
               RealmsScreen.bind("minecraft:textures/gui/widgets.png");
               GL11.glPushMatrix();
               GL11.glBlendFunc(770, 771);
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
               RealmsScreen.blit(buttonX, buttonY, 0.0F, (float)(46 + yImage * 20), buttonWidth / 2, 8, 256.0F, 256.0F);
               RealmsScreen.blit(
                  buttonX + buttonWidth / 2, buttonY, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20), buttonWidth / 2, 8, 256.0F, 256.0F
               );
               RealmsScreen.blit(buttonX, buttonY + 8, 0.0F, (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256.0F, 256.0F);
               RealmsScreen.blit(
                  buttonX + buttonWidth / 2, buttonY + 8, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256.0F, 256.0F
               );
               GL11.glPopMatrix();
               GL11.glDisable(3042);
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
            RealmsTextureManager.bindFace(serverData.ownerUUID);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
            RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
         }
      }

      private boolean renderRealmNote(int i, int x, int y, String text, boolean hover, int mouseX, int mouseY) {
         String label = RealmsScreen.getLocalizedString("mco.selectServer.note") + " ";
         int labelWidth = RealmsMainScreen.this.fontWidth(label);
         int textWidth = RealmsMainScreen.this.fontWidth(text);
         int noteWidth = labelWidth + textWidth;
         int offsetX = x + 2;
         int offsetY = y + 12 + 11;
         boolean noteIsHovered = mouseX >= offsetX
            && mouseX < offsetX + noteWidth
            && mouseY > offsetY
            && mouseY <= offsetY + RealmsMainScreen.this.fontLineHeight()
            && hover;
         int labelColor = 15553363;
         int textColor = 16777215;
         if (noteIsHovered) {
            labelColor = 12535109;
            textColor = 10526880;
            label = "§n" + label;
            text = "§n" + text;
         }

         RealmsMainScreen.this.drawString(label, offsetX, offsetY, labelColor, true);
         RealmsMainScreen.this.drawString(text, offsetX + labelWidth, offsetY, textColor, true);
         return noteIsHovered;
      }

      private void browseURL(String url) {
         RealmsBridge.setClipboard(url);
         RealmsUtil.browseTo(url);
      }
   }
}
