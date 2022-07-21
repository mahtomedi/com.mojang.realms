package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.McoServer;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.BuyRealmsScreen;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.ClientOutdatedScreen;
import com.mojang.realmsclient.gui.ConfigureWorldScreen;
import com.mojang.realmsclient.gui.CreateOnlineWorldScreen;
import com.mojang.realmsclient.gui.LongConfirmationScreen;
import com.mojang.realmsclient.gui.LongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.MCRSelectionList;
import com.mojang.realmsclient.gui.OnlineConnectTask;
import com.mojang.realmsclient.gui.PendingInvitationScreen;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsGenericErrorScreen;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsServerStatusPinger;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class RealmsMainScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final boolean overrideConfigure = false;
   protected static final int BACK_BUTTON_ID = 0;
   protected static final int PLAY_BUTTON_ID = 1;
   protected static final int BUY_BUTTON_ID = 2;
   protected static final int CONFIGURE_BUTTON_ID = 3;
   protected static final int GET_MORE_INFO_BUTTON_ID = 4;
   protected static final int LEAVE_BUTTON_ID = 5;
   private static final String ON_ICON_LOCATION = "realms:textures/gui/realms/on_icon.png";
   private static final String OFF_ICON_LOCATION = "realms:textures/gui/realms/off_icon.png";
   private static final String EXPIRED_ICON_LOCATION = "realms:textures/gui/realms/expired_icon.png";
   private static final String INVITATION_ICONS_LOCATION = "realms:textures/gui/realms/invitation_icons.png";
   private static final String INVITE_ICON_LOCATION = "realms:textures/gui/realms/invite_icon.png";
   private static final String WORLDICON_LOCATION = "realms:textures/gui/realms/world_icon.png";
   private static final String LOGO_LOCATION = "realms:textures/gui/title/realms.png";
   private static RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
   private static RealmsServerStatusPinger statusPinger = new RealmsServerStatusPinger();
   private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
      5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).build()
   );
   private static int lastScrollYPosition = -1;
   private RealmsScreen lastScreen;
   private volatile RealmsMainScreen.ServerSelectionList serverSelectionList;
   private long selectedServerId = -1L;
   private RealmsButton configureButton;
   private RealmsButton leaveButton;
   private RealmsButton playButton;
   private RealmsButton buyButton;
   private String toolTip;
   private List<McoServer> mcoServers = Lists.newArrayList();
   private static final String mcoInfoUrl = "https://minecraft.net/realms";
   private volatile int numberOfPendingInvites = 0;
   public static final int EXPIRATION_NOTIFICATION_DAYS = 7;
   private int animTick;
   private static volatile boolean mcoEnabled;
   private static boolean checkedMcoAvailability;
   private static RealmsScreen realmsGenericErrorScreen = null;
   private static boolean regionsPinged = false;
   private boolean onLink = false;

   public RealmsMainScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
      this.checkIfMcoEnabled();
   }

   public void init() {
      if (realmsGenericErrorScreen != null) {
         Realms.setScreen(realmsGenericErrorScreen);
      } else {
         Keyboard.enableRepeatEvents(true);
         this.buttonsClear();
         this.postInit();
         if (this.isMcoEnabled()) {
            realmsDataFetcher.init();
         }

      }
   }

   public void postInit() {
      this.buttonsAdd(this.playButton = newButton(1, this.width() / 2 - 154, this.height() - 52, 154, 20, getLocalizedString("mco.selectServer.play")));
      this.buttonsAdd(this.leaveButton = newButton(5, this.width() / 2 - 154, this.height() - 28, 102, 20, getLocalizedString("mco.selectServer.leave")));
      this.buttonsAdd(this.configureButton = newButton(3, this.width() / 2 + 6, this.height() - 52, 154, 20, getLocalizedString("mco.selectServer.configure")));
      this.buttonsAdd(this.buyButton = newButton(2, this.width() / 2 - 48, this.height() - 28, 102, 20, getLocalizedString("mco.selectServer.buy")));
      this.buttonsAdd(newButton(0, this.width() / 2 + 58, this.height() - 28, 102, 20, getLocalizedString("gui.back")));
      this.serverSelectionList = new RealmsMainScreen.ServerSelectionList();
      this.serverSelectionList.updateSize(this.width(), this.height(), 32, this.height() - 64);
      if (lastScrollYPosition != -1) {
         this.serverSelectionList.scroll(lastScrollYPosition);
      }

      McoServer server = this.findServer(this.selectedServerId);
      this.playButton.active(server != null && server.state == McoServer.State.OPEN && !server.expired);
      this.configureButton.active(server != null && server.state != McoServer.State.ADMIN_LOCK && server.owner.equals(Realms.getName()));
      this.leaveButton.active(server != null && !server.owner.equals(Realms.getName()));
   }

   public void tick() {
      ++this.animTick;
      if (this.isMcoEnabled()) {
         realmsDataFetcher.init();
         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
            List<McoServer> newServers = realmsDataFetcher.getServers();
            boolean ownsNonExpiredRealmServer = false;

            for(McoServer retrievedServer : newServers) {
               if (this.isSelfOwnedNonExpiredServer(retrievedServer)) {
                  ownsNonExpiredRealmServer = true;
               }

               for(McoServer oldServer : this.mcoServers) {
                  if (retrievedServer.id == oldServer.id) {
                     retrievedServer.latestStatFrom(oldServer);
                     break;
                  }
               }
            }

            this.mcoServers = newServers;
            if (!regionsPinged && ownsNonExpiredRealmServer) {
               regionsPinged = true;
               this.pingRegions();
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
         }

         realmsDataFetcher.markClean();
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

      for(McoServer server : this.mcoServers) {
         if (this.isSelfOwnedNonExpiredServer(server)) {
            ids.add(server.id);
         }
      }

      return ids;
   }

   private boolean isMcoEnabled() {
      return mcoEnabled;
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            this.play(this.selectedServerId);
         } else if (button.id() == 3) {
            this.configureClicked();
         } else if (button.id() == 5) {
            this.leaveClicked();
         } else if (button.id() == 0) {
            this.stopRealmsFetcherAndPinger();
            Realms.setScreen(this.lastScreen);
         } else if (button.id() == 2) {
            this.saveListScrollPosition();
            this.stopRealmsFetcherAndPinger();
            Realms.setScreen(new BuyRealmsScreen(this));
         } else if (button.id() == 4) {
            this.moreInfoButtonClicked();
         } else if (this.serverSelectionList != null) {
            this.serverSelectionList.buttonClicked(button);
         }

      }
   }

   private void checkIfMcoEnabled() {
      if (!checkedMcoAvailability) {
         checkedMcoAvailability = true;
         (new Thread("MCO Availability Checker #1") {
            public void run() {
               RealmsClient client = RealmsClient.createRealmsClient();

               try {
                  if (client.clientOutdated()) {
                     Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen = new ClientOutdatedScreen(RealmsMainScreen.this.lastScreen));
                     return;
                  }
               } catch (RealmsServiceException var9) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", new Object[]{var9.toString()});
                  if (var9.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(var9, RealmsMainScreen.this.lastScreen);
                  }

                  Realms.setScreen(new RealmsGenericErrorScreen(var9, RealmsMainScreen.this.lastScreen));
                  return;
               } catch (IOException var10) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", new Object[]{var10.getMessage()});
                  Realms.setScreen(new RealmsGenericErrorScreen(var10.getMessage(), RealmsMainScreen.this.lastScreen));
                  return;
               }

               boolean retry = false;

               for(int i = 0; i < 3; ++i) {
                  try {
                     Boolean result = client.mcoEnabled();
                     if (result) {
                        RealmsMainScreen.LOGGER.info("Realms is available for this user");
                        RealmsMainScreen.mcoEnabled = true;
                     } else {
                        RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                        RealmsMainScreen.mcoEnabled = false;
                     }
                  } catch (RetryCallException var6) {
                     retry = true;
                  } catch (RealmsServiceException var7) {
                     RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var7.toString());
                  } catch (IOException var8) {
                     RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var8.getMessage());
                  }

                  if (!retry) {
                     break;
                  }

                  try {
                     Thread.sleep(5000L);
                  } catch (InterruptedException var5) {
                     Thread.currentThread().interrupt();
                  }
               }

            }
         }).start();
      }

   }

   private void stopRealmsFetcherAndPinger() {
      if (this.isMcoEnabled()) {
         realmsDataFetcher.stop();
         statusPinger.removeAll();
      }

   }

   private void moreInfoButtonClicked() {
      String line2 = getLocalizedString("mco.more.info.question.line1");
      String line3 = getLocalizedString("mco.more.info.question.line2");
      Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 4));
   }

   private void configureClicked() {
      McoServer selectedServer = this.findServer(this.selectedServerId);
      if (selectedServer != null && Realms.getName().equals(selectedServer.owner)) {
         this.stopRealmsFetcherAndPinger();
         this.saveListScrollPosition();
         Realms.setScreen(new ConfigureWorldScreen(this, selectedServer.id));
      }

   }

   private void leaveClicked() {
      McoServer selectedServer = this.findServer(this.selectedServerId);
      if (selectedServer != null && !Realms.getName().equals(selectedServer.owner)) {
         this.saveListScrollPosition();
         String line2 = getLocalizedString("mco.configure.world.leave.question.line1");
         String line3 = getLocalizedString("mco.configure.world.leave.question.line2");
         Realms.setScreen(new LongConfirmationScreen(this, LongConfirmationScreen.Type.Info, line2, line3, 5));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = this.serverSelectionList.getScroll();
   }

   private McoServer findServer(long id) {
      for(McoServer server : this.mcoServers) {
         if (server.id == id) {
            return server;
         }
      }

      return null;
   }

   private int findIndex(long serverId) {
      for(int i = 0; i < this.mcoServers.size(); ++i) {
         if (((McoServer)this.mcoServers.get(i)).id == serverId) {
            return i;
         }
      }

      return -1;
   }

   public void confirmResult(boolean result, int id) {
      if (id == 5 && result) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  McoServer server = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                  if (server != null) {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     RealmsMainScreen.realmsDataFetcher.removeItem(server);
                     RealmsMainScreen.this.mcoServers.remove(server);
                     client.uninviteMyselfFrom(server.id);
                     RealmsMainScreen.realmsDataFetcher.removeItem(server);
                     RealmsMainScreen.this.mcoServers.remove(server);
                     RealmsMainScreen.this.updateSelectedItemPointer();
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

   private void updateSelectedItemPointer() {
      int originalIndex = this.findIndex(this.selectedServerId);
      if (this.mcoServers.size() - 1 == originalIndex) {
         --originalIndex;
      }

      if (this.mcoServers.size() == 0) {
         originalIndex = -1;
      }

      if (originalIndex >= 0 && originalIndex < this.mcoServers.size()) {
         this.selectedServerId = ((McoServer)this.mcoServers.get(originalIndex)).id;
      }

   }

   public void removeSelection() {
      this.selectedServerId = -1L;
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.playButton);
      } else if (eventKey == 1) {
         this.stopRealmsFetcherAndPinger();
         Realms.setScreen(this.lastScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.serverSelectionList.render(xm, ym, a);
      this.drawRealmsLogo(this.width() / 2 - 50, 7);
      this.renderLink(xm, ym);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

      this.drawInvitationPendingIcon(xm, ym);
      super.render(xm, ym, a);
   }

   private void drawRealmsLogo(int x, int y) {
      RealmsScreen.bind("realms:textures/gui/title/realms.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2 - 5, 0.0F, 0.0F, 200, 50, 200.0F, 50.0F);
      GL11.glPopMatrix();
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      if (this.inPendingInvitationArea(x, y) && this.numberOfPendingInvites != 0) {
         this.stopRealmsFetcherAndPinger();
         PendingInvitationScreen pendingInvitationScreen = new PendingInvitationScreen(this.lastScreen);
         Realms.setScreen(pendingInvitationScreen);
      }

      if (this.onLink) {
         this.browseTo("https://minecraft.net/realms");
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

   private void drawInvitationPendingIcon(int xm, int ym) {
      int pendingInvitesCount = this.numberOfPendingInvites;
      boolean hovering = this.inPendingInvitationArea(xm, ym);
      int baseX = this.width() / 2 + 50;
      int baseY = 12;
      if (pendingInvitesCount != 0) {
         float scale = 0.25F + (1.0F + RealmsMth.sin((float)this.animTick * 0.5F)) * 0.25F;
         int color = 0xFF000000 | (int)(scale * 64.0F) << 16 | (int)(scale * 64.0F) << 8 | (int)(scale * 64.0F) << 0;
         this.fillGradient(baseX - 2, 10, baseX + 18, 30, color, color);
         color = 0xFF000000 | (int)(scale * 255.0F) << 16 | (int)(scale * 255.0F) << 8 | (int)(scale * 255.0F) << 0;
         this.fillGradient(baseX - 2, 10, baseX + 18, 11, color, color);
         this.fillGradient(baseX - 2, 10, baseX - 1, 30, color, color);
         this.fillGradient(baseX + 17, 10, baseX + 18, 30, color, color);
         this.fillGradient(baseX - 2, 29, baseX + 18, 30, color, color);
      }

      RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      RealmsScreen.blit(baseX, 6, hovering ? 16.0F : 0.0F, 0.0F, 15, 25, 31.0F, 25.0F);
      GL11.glPopMatrix();
      if (pendingInvitesCount != 0) {
         int spritePos = (Math.min(pendingInvitesCount, 6) - 1) * 8;
         int yOff = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         RealmsScreen.blit(baseX + 4, 16 + yOff, (float)spritePos, 0.0F, 8, 8, 48.0F, 8.0F);
         GL11.glPopMatrix();
      }

      boolean renderToolTip = hovering || pendingInvitesCount != 0;
      if (renderToolTip) {
         int rx = xm + 12;
         int ry = ym - 12;
         if (!hovering) {
            rx = baseX + 22;
            ry = 12;
         }

         String message;
         if (pendingInvitesCount != 0) {
            message = getLocalizedString("mco.invites.pending");
         } else {
            message = getLocalizedString("mco.invites.nopending");
         }

         int width = this.fontWidth(message);
         this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(message, rx, ry, -1);
      }

   }

   private boolean inPendingInvitationArea(int xm, int ym) {
      int x1 = this.width() / 2 + 50;
      int x2 = this.width() / 2 + 66;
      int y1 = 13;
      int y2 = 27;
      return x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2;
   }

   private void play(long serverId) {
      McoServer server = this.findServer(serverId);
      if (server != null) {
         this.stopRealmsFetcherAndPinger();
         LongRunningMcoTaskScreen longRunningMcoTaskScreen = new LongRunningMcoTaskScreen(this, new OnlineConnectTask(this, server));
         longRunningMcoTaskScreen.start();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean isSelfOwnedServer(McoServer serverData) {
      return serverData.owner != null && serverData.owner.equals(Realms.getName());
   }

   private boolean isSelfOwnedNonExpiredServer(McoServer serverData) {
      return serverData.owner != null && serverData.owner.equals(Realms.getName()) && !serverData.expired;
   }

   private void drawExpired(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void drawExpiring(int x, int y, int xm, int ym, int daysLeft) {
      if (this.animTick % 20 < 10) {
         RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
         GL11.glPopMatrix();
      }

      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         if (daysLeft == 0) {
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
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.open");
      }

   }

   private void drawClose(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.closed");
      }

   }

   private void drawLocked(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2, 0.0F, 0.0F, 15, 15, 15.0F, 15.0F);
      GL11.glPopMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 9) {
         this.toolTip = getLocalizedString("mco.selectServer.locked");
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

   private void renderLink(int xm, int ym) {
      String text = getLocalizedString("mco.selectServer.whatisrealms");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      int linkColor = 3368635;
      int hoverColor = 7107012;
      int textWidth = this.fontWidth(text);
      int rightPadding = 10;
      int topPadding = 12;
      int x1 = this.width() - textWidth - rightPadding;
      int x2 = x1 + textWidth + 1;
      int y2 = topPadding + this.fontLineHeight();
      GL11.glTranslatef((float)x1, (float)topPadding, 0.0F);
      if (x1 <= xm && xm <= x2 && topPadding <= ym && ym <= y2) {
         this.onLink = true;
         this.drawString(text, 0, 0, hoverColor);
      } else {
         this.onLink = false;
         this.drawString(text, 0, 0, linkColor);
      }

      GL11.glPopMatrix();
   }

   static {
      String version = RealmsVersion.getVersion();
      if (version != null) {
         LOGGER.info("Realms library version == " + version);
      }

   }

   private class ServerSelectionList extends MCRSelectionList {
      public ServerSelectionList() {
         super(RealmsMainScreen.this.width(), RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 64, 36);
      }

      @Override
      protected int getNumberOfItems() {
         return RealmsMainScreen.this.mcoServers.size() + 1;
      }

      @Override
      protected void selectItem(int item, boolean doubleClick) {
         if (item < RealmsMainScreen.this.mcoServers.size()) {
            McoServer server = (McoServer)RealmsMainScreen.this.mcoServers.get(item);
            RealmsMainScreen.this.selectedServerId = server.id;
            if (server.state == McoServer.State.UNINITIALIZED) {
               RealmsMainScreen.this.stopRealmsFetcherAndPinger();
               Realms.setScreen(new CreateOnlineWorldScreen(server.id, RealmsMainScreen.this));
            }

            RealmsMainScreen.this.configureButton.active(RealmsMainScreen.this.isSelfOwnedServer(server) && server.state != McoServer.State.ADMIN_LOCK);
            RealmsMainScreen.this.leaveButton.active(!RealmsMainScreen.this.isSelfOwnedServer(server));
            RealmsMainScreen.this.playButton.active(server.state == McoServer.State.OPEN && !server.expired);
            if (doubleClick && RealmsMainScreen.this.playButton.active()) {
               RealmsMainScreen.this.play(RealmsMainScreen.this.selectedServerId);
            }

         }
      }

      @Override
      protected boolean isSelectedItem(int item) {
         return item == RealmsMainScreen.this.findIndex(RealmsMainScreen.this.selectedServerId);
      }

      @Override
      protected boolean isMyWorld(int item) {
         try {
            return item >= 0
               && item < RealmsMainScreen.this.mcoServers.size()
               && ((McoServer)RealmsMainScreen.this.mcoServers.get(item)).owner.toLowerCase().equals(Realms.getName());
         } catch (Exception var3) {
            return false;
         }
      }

      @Override
      protected int getMaxPosition() {
         return this.getNumberOfItems() * 36;
      }

      @Override
      protected void renderBackground() {
         RealmsMainScreen.this.renderBackground();
      }

      @Override
      protected void renderItem(int i, int x, int y, int h, int width, Tezzelator t) {
         if (i < RealmsMainScreen.this.mcoServers.size()) {
            this.renderMcoServerItem(i, x, y);
         }

      }

      private void renderMcoServerItem(int i, int x, int y) {
         final McoServer serverData = (McoServer)RealmsMainScreen.this.mcoServers.get(i);
         int nameColor = -1;
         if (RealmsMainScreen.this.isSelfOwnedServer(serverData)) {
            nameColor = -8388737;
         }

         if (serverData.state == McoServer.State.UNINITIALIZED) {
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
            if (serverData.shouldPing(Realms.currentTimeMillis())) {
               serverData.serverPing.lastPingSnapshot = Realms.currentTimeMillis();
               RealmsMainScreen.THREAD_POOL.submit(new Runnable() {
                  public void run() {
                     try {
                        RealmsMainScreen.statusPinger.pingServer(serverData.ip, serverData.serverPing);
                     } catch (UnknownHostException var2) {
                        RealmsMainScreen.LOGGER.error("Pinger: Could not resolve host");
                     }

                  }
               });
            }

            RealmsMainScreen.this.drawString(serverData.getName(), x + 2, y + 1, nameColor);
            int dx = 207;
            int dy = 1;
            if (serverData.expired) {
               RealmsMainScreen.this.drawExpired(x + dx, y + dy, this.xm, this.ym);
            } else if (serverData.state == McoServer.State.CLOSED) {
               RealmsMainScreen.this.drawClose(x + dx, y + dy, this.xm, this.ym);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.daysLeft < 7) {
               this.showStatus(x - 14, y, serverData);
               RealmsMainScreen.this.drawExpiring(x + dx, y + dy, this.xm, this.ym, serverData.daysLeft);
            } else if (serverData.state == McoServer.State.OPEN) {
               RealmsMainScreen.this.drawOpen(x + dx, y + dy, this.xm, this.ym);
               this.showStatus(x - 14, y, serverData);
            } else if (serverData.state == McoServer.State.ADMIN_LOCK) {
               RealmsMainScreen.this.drawLocked(x + dx, y + dy, this.xm, this.ym);
            }

            String noPlayers = "0";
            if (!serverData.serverPing.nrOfPlayers.equals(noPlayers)) {
               String coloredNumPlayers = ChatFormatting.GRAY + "" + serverData.serverPing.nrOfPlayers;
               RealmsMainScreen.this.drawString(coloredNumPlayers, x + 200 - RealmsMainScreen.this.fontWidth(coloredNumPlayers), y + 1, 8421504);
            }

            if (serverData.worldType.equals(McoServer.WorldType.MINIGAME)) {
               int motdColor = 9206892;
               if (RealmsMainScreen.this.animTick % 10 < 5) {
                  motdColor = 13413468;
               }

               String miniGameStr = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
               int mgWidth = RealmsMainScreen.this.fontWidth(miniGameStr);
               RealmsMainScreen.this.drawString(miniGameStr, x + 2, y + 12, motdColor);
               RealmsMainScreen.this.drawString(serverData.getMotd(), x + 2 + mgWidth, y + 12, 7105644);
            } else {
               RealmsMainScreen.this.drawString(serverData.getMotd(), x + 2, y + 12, 7105644);
            }

            RealmsMainScreen.this.drawString(serverData.owner, x + 2, y + 12 + 11, 5000268);
            RealmsScreen.bindFace(serverData.owner);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
            RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64.0F, 64.0F);
         }
      }

      private void showStatus(int x, int y, McoServer serverData) {
         if (serverData.ip != null) {
            if (serverData.status != null) {
               RealmsMainScreen.this.drawString(serverData.status, x + 215 - RealmsMainScreen.this.fontWidth(serverData.status), y + 1, 8421504);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.bind("textures/gui/icons.png");
         }
      }
   }
}
