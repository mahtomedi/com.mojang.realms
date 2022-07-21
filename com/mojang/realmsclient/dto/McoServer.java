package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.realms.ServerPing;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McoServer extends ValueObject {
   private static final Logger LOGGER = LogManager.getLogger();
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String motd;
   public McoServer.State state;
   public String owner;
   public List<PlayerInfo> players;
   public McoOptions options;
   public String ip;
   public boolean expired;
   public int difficulty;
   public int gameMode;
   public int daysLeft;
   public McoServer.WorldType worldType;
   public int protocol;
   public String status = "";
   public ServerPing serverPing = new ServerPing();

   public String getMotd() {
      return this.motd;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setMotd(String motd) {
      this.motd = motd;
   }

   public void latestStatFrom(McoServer oldServer) {
      this.status = oldServer.status;
      this.protocol = oldServer.protocol;
      this.serverPing.nrOfPlayers = oldServer.serverPing.nrOfPlayers;
      this.serverPing.lastPingSnapshot = oldServer.serverPing.lastPingSnapshot;
   }

   public static McoServer parse(JsonObject node) {
      McoServer server = new McoServer();

      try {
         server.id = JsonUtils.getLongOr("id", node, -1L);
         server.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", node, null);
         server.name = JsonUtils.getStringOr("name", node, null);
         server.motd = JsonUtils.getStringOr("motd", node, null);
         server.state = getState(JsonUtils.getStringOr("state", node, McoServer.State.CLOSED.name()));
         server.owner = JsonUtils.getStringOr("owner", node, null);
         if (node.get("players") != null && node.get("players").isJsonArray()) {
            server.players = parseInvited(node.get("players").getAsJsonArray());
            sortInvited(server);
         } else {
            server.players = new ArrayList();
         }

         server.daysLeft = JsonUtils.getIntOr("daysLeft", node, 0);
         server.ip = JsonUtils.getStringOr("ip", node, null);
         server.expired = JsonUtils.getBooleanOr("expired", node, false);
         server.difficulty = JsonUtils.getIntOr("difficulty", node, 0);
         server.gameMode = JsonUtils.getIntOr("gameMode", node, 0);
         server.worldType = getWorldType(JsonUtils.getStringOr("worldType", node, McoServer.WorldType.NORMAL.name()));
         if (node.get("options") != null && !node.get("options").isJsonNull()) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(node.get("options").getAsString());
            if (element == null) {
               server.options = McoOptions.getDefaults();
            } else {
               server.options = McoOptions.parse(element.getAsJsonObject());
            }
         } else {
            server.options = McoOptions.getDefaults();
         }
      } catch (Exception var4) {
         LOGGER.error("Could not parse McoServer: " + var4.getMessage());
      }

      return server;
   }

   private static void sortInvited(McoServer server) {
      Collections.sort(server.players, new Comparator<PlayerInfo>() {
         public int compare(PlayerInfo o1, PlayerInfo o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
         }
      });
   }

   private static List<PlayerInfo> parseInvited(JsonArray jsonArray) {
      ArrayList<PlayerInfo> invited = new ArrayList();

      for(JsonElement aJsonArray : jsonArray) {
         try {
            JsonObject node = aJsonArray.getAsJsonObject();
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setName(JsonUtils.getStringOr("name", node, null));
            playerInfo.setUuid(JsonUtils.getStringOr("uuid", node, null));
            playerInfo.setOperator(JsonUtils.getBooleanOr("operator", node, false));
            invited.add(playerInfo);
         } catch (Exception var6) {
         }
      }

      return invited;
   }

   public static McoServer parse(String json) {
      McoServer server = new McoServer();

      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(json).getAsJsonObject();
         server = parse(object);
      } catch (Exception var4) {
         LOGGER.error("Could not parse McoServer: " + var4.getMessage());
      }

      return server;
   }

   private static McoServer.State getState(String state) {
      try {
         return McoServer.State.valueOf(state);
      } catch (Exception var2) {
         return McoServer.State.CLOSED;
      }
   }

   private static McoServer.WorldType getWorldType(String state) {
      try {
         return McoServer.WorldType.valueOf(state);
      } catch (Exception var2) {
         return McoServer.WorldType.NORMAL;
      }
   }

   public boolean shouldPing(long now) {
      return now - this.serverPing.lastPingSnapshot >= 6000L;
   }

   public int hashCode() {
      return new HashCodeBuilder(17, 37)
         .append(this.id)
         .append(this.name)
         .append(this.motd)
         .append(this.state)
         .append(this.owner)
         .append(this.expired)
         .toHashCode();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (obj.getClass() != this.getClass()) {
         return false;
      } else {
         McoServer rhs = (McoServer)obj;
         return new EqualsBuilder()
            .append(this.id, rhs.id)
            .append(this.name, rhs.name)
            .append(this.motd, rhs.motd)
            .append(this.state, rhs.state)
            .append(this.owner, rhs.owner)
            .append(this.expired, rhs.expired)
            .append(this.worldType, this.worldType)
            .isEquals();
      }
   }

   public McoServer clone() {
      McoServer server = new McoServer();
      server.id = this.id;
      server.remoteSubscriptionId = this.remoteSubscriptionId;
      server.name = this.name;
      server.motd = this.motd;
      server.state = this.state;
      server.owner = this.owner;
      server.players = this.players;
      server.options = new McoOptions(
         this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNPCs, this.options.spawnProtection
      );
      server.ip = this.ip;
      server.expired = this.expired;
      server.difficulty = this.difficulty;
      server.gameMode = this.gameMode;
      server.daysLeft = this.daysLeft;
      server.protocol = this.protocol;
      server.status = this.status;
      server.serverPing = new ServerPing();
      server.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
      server.serverPing.lastPingSnapshot = this.serverPing.lastPingSnapshot;
      server.worldType = this.worldType;
      return server;
   }

   public static class McoServerComparator implements Comparator<McoServer> {
      private final String refOwner;

      public McoServerComparator(String owner) {
         this.refOwner = owner;
      }

      public int compare(McoServer server1, McoServer server2) {
         if (server1.owner.equals(server2.owner)) {
            if (server1.state == McoServer.State.UNINITIALIZED) {
               return -1;
            } else {
               return server2.state == McoServer.State.UNINITIALIZED ? 1 : (int)(server2.id - server1.id);
            }
         } else if (server1.owner.equals(this.refOwner)) {
            return -1;
         } else if (server2.owner.equals(this.refOwner)) {
            return 1;
         } else if (server1.state != McoServer.State.CLOSED && server2.state != McoServer.State.CLOSED) {
            if (server1.id < server2.id) {
               return 1;
            } else {
               return server1.id > server2.id ? -1 : 0;
            }
         } else {
            return server1.state == McoServer.State.CLOSED ? 1 : 0;
         }
      }
   }

   public static enum State {
      CLOSED,
      OPEN,
      ADMIN_LOCK,
      UNINITIALIZED;
   }

   public static enum WorldType {
      NORMAL,
      MINIGAME,
      ADVENTUREMAP;
   }
}
