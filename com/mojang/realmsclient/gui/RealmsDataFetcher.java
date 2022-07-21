package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

public class RealmsDataFetcher {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
   private static final int SERVER_UPDATE_INTERVAL = 60;
   private static final int PENDING_INVITES_INTERVAL = 10;
   private volatile boolean stopped = true;
   private RealmsDataFetcher.ServerListUpdateTask serverListUpdateTask = new RealmsDataFetcher.ServerListUpdateTask();
   private RealmsDataFetcher.PendingInviteUpdateTask pendingInviteUpdateTask = new RealmsDataFetcher.PendingInviteUpdateTask();
   private Set<RealmsServer> removedServers = Sets.newHashSet();
   private List<RealmsServer> servers = Lists.newArrayList();
   private int pendingInvitesCount;
   private ScheduledFuture<?> serverListScheduledFuture;
   private ScheduledFuture<?> pendingInviteScheduledFuture;
   private Map<String, Boolean> fetchStatus = new ConcurrentHashMap(RealmsDataFetcher.Task.values().length);

   public RealmsDataFetcher() {
      this.scheduleTasks();
   }

   public synchronized void init() {
      if (this.stopped) {
         this.stopped = false;
         this.cancelTasks();
         this.scheduleTasks();
      }

   }

   public synchronized boolean isFetchedSinceLastTry(RealmsDataFetcher.Task task) {
      Boolean result = (Boolean)this.fetchStatus.get(task.toString());
      return result == null ? false : result;
   }

   public synchronized void markClean() {
      for(String task : this.fetchStatus.keySet()) {
         this.fetchStatus.put(task, false);
      }

   }

   public synchronized List<RealmsServer> getServers() {
      return Lists.newArrayList(this.servers);
   }

   public int getPendingInvitesCount() {
      return this.pendingInvitesCount;
   }

   public synchronized void stop() {
      this.stopped = true;
      this.cancelTasks();
   }

   private void scheduleTasks() {
      this.serverListScheduledFuture = this.scheduler.scheduleAtFixedRate(this.serverListUpdateTask, 0L, 60L, TimeUnit.SECONDS);
      this.pendingInviteScheduledFuture = this.scheduler.scheduleAtFixedRate(this.pendingInviteUpdateTask, 0L, 10L, TimeUnit.SECONDS);

      for(RealmsDataFetcher.Task task : RealmsDataFetcher.Task.values()) {
         this.fetchStatus.put(task.toString(), false);
      }

   }

   private void cancelTasks() {
      try {
         this.serverListScheduledFuture.cancel(false);
         this.pendingInviteScheduledFuture.cancel(false);
      } catch (Exception var2) {
         LOGGER.error("Failed to cancel Realms tasks");
      }

   }

   private synchronized void setServers(List<RealmsServer> newServers) {
      int removedCnt = 0;

      for(RealmsServer server : this.removedServers) {
         if (newServers.remove(server)) {
            ++removedCnt;
         }
      }

      if (removedCnt == 0) {
         this.removedServers.clear();
      }

      this.servers = newServers;
   }

   public synchronized void removeItem(RealmsServer server) {
      this.servers.remove(server);
      this.removedServers.add(server);
   }

   private void sort(List<RealmsServer> servers) {
      Collections.sort(servers, new RealmsServer.McoServerComparator(Realms.getName()));
   }

   private boolean isActive() {
      return !this.stopped && Display.isActive();
   }

   private class PendingInviteUpdateTask implements Runnable {
      private PendingInviteUpdateTask() {
      }

      public void run() {
         if (RealmsDataFetcher.this.isActive()) {
            this.updatePendingInvites();
         }

      }

      private void updatePendingInvites() {
         try {
            RealmsClient client = RealmsClient.createRealmsClient();
            if (client != null) {
               RealmsDataFetcher.this.pendingInvitesCount = client.pendingInvitesCount();
               RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE.toString(), true);
            }
         } catch (RealmsServiceException var2) {
            RealmsDataFetcher.LOGGER.error("Couldn't get pending invite count", var2);
         }

      }
   }

   private class ServerListUpdateTask implements Runnable {
      private ServerListUpdateTask() {
      }

      public void run() {
         if (RealmsDataFetcher.this.isActive()) {
            this.updateServersList();
         }

      }

      private void updateServersList() {
         try {
            RealmsClient client = RealmsClient.createRealmsClient();
            if (client != null) {
               List<RealmsServer> servers = client.listWorlds().servers;
               if (servers != null && !servers.isEmpty()) {
                  RealmsDataFetcher.this.sort(servers);
                  RealmsDataFetcher.this.setServers(servers);
                  RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST.toString(), true);
               } else {
                  RealmsDataFetcher.LOGGER.warn("Realms server list was null or empty");
               }
            }
         } catch (RealmsServiceException var3) {
            RealmsDataFetcher.LOGGER.error("Couldn't get server list", var3);
         } catch (IOException var4) {
            RealmsDataFetcher.LOGGER.error("Couldn't parse response from server getting list");
         }

      }
   }

   public static enum Task {
      SERVER_LIST,
      PENDING_INVITE;
   }
}
