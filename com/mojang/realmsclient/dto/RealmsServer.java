package com.mojang.realmsclient.dto;

import com.google.common.collect.ComparisonChain;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import net.minecraft.realms.Realms;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
import realms.bj;
import realms.l;

@DontObfuscateOrShrink
public class RealmsServer extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String motd;
   public RealmsServer.b state;
   public String owner;
   public String ownerUUID;
   public List<PlayerInfo> players;
   public Map<Integer, RealmsWorldOptions> slots;
   public boolean expired;
   public boolean expiredTrial;
   public int daysLeft;
   public RealmsServer.c worldType;
   public int activeSlot;
   public String minigameName;
   public int minigameId;
   public String minigameImage;
   public RealmsServerPing serverPing = new RealmsServerPing();

   public String getDescription() {
      return this.motd;
   }

   public String getName() {
      return this.name;
   }

   public String getMinigameName() {
      return this.minigameName;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDescription(String motd) {
      this.motd = motd;
   }

   public void updateServerPing(RealmsServerPlayerList serverPlayerList) {
      StringBuilder builder = new StringBuilder();
      int players = 0;

      for(String uuid : serverPlayerList.players) {
         if (!uuid.equals(Realms.getUUID())) {
            String name = "";

            try {
               name = bj.a(uuid);
            } catch (Exception var8) {
               LOGGER.error("Could not get name for " + uuid, var8);
               continue;
            }

            if (builder.length() > 0) {
               builder.append("\n");
            }

            builder.append(name);
            ++players;
         }
      }

      this.serverPing.nrOfPlayers = String.valueOf(players);
      this.serverPing.playerList = builder.toString();
   }

   public static RealmsServer parse(JsonObject node) {
      RealmsServer server = new RealmsServer();

      try {
         server.id = bd.a("id", node, -1L);
         server.remoteSubscriptionId = bd.a("remoteSubscriptionId", node, null);
         server.name = bd.a("name", node, null);
         server.motd = bd.a("motd", node, null);
         server.state = getState(bd.a("state", node, RealmsServer.b.a.name()));
         server.owner = bd.a("owner", node, null);
         if (node.get("players") != null && node.get("players").isJsonArray()) {
            server.players = parseInvited(node.get("players").getAsJsonArray());
            sortInvited(server);
         } else {
            server.players = new ArrayList();
         }

         server.daysLeft = bd.a("daysLeft", node, 0);
         server.expired = bd.a("expired", node, false);
         server.expiredTrial = bd.a("expiredTrial", node, false);
         server.worldType = getWorldType(bd.a("worldType", node, RealmsServer.c.a.name()));
         server.ownerUUID = bd.a("ownerUUID", node, "");
         if (node.get("slots") != null && node.get("slots").isJsonArray()) {
            server.slots = parseSlots(node.get("slots").getAsJsonArray());
         } else {
            server.slots = getEmptySlots();
         }

         server.minigameName = bd.a("minigameName", node, null);
         server.activeSlot = bd.a("activeSlot", node, -1);
         server.minigameId = bd.a("minigameId", node, -1);
         server.minigameImage = bd.a("minigameImage", node, null);
      } catch (Exception var3) {
         LOGGER.error("Could not parse McoServer: " + var3.getMessage());
      }

      return server;
   }

   private static void sortInvited(RealmsServer server) {
      Collections.sort(
         server.players,
         new Comparator<PlayerInfo>() {
            public int a(PlayerInfo o1, PlayerInfo o2) {
               return ComparisonChain.start()
                  .compare(o2.getAccepted(), o1.getAccepted())
                  .compare(o1.getName().toLowerCase(Locale.ROOT), o2.getName().toLowerCase(Locale.ROOT))
                  .result();
            }
         }
      );
   }

   private static List<PlayerInfo> parseInvited(JsonArray jsonArray) {
      ArrayList<PlayerInfo> invited = new ArrayList();

      for(JsonElement aJsonArray : jsonArray) {
         try {
            JsonObject node = aJsonArray.getAsJsonObject();
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setName(bd.a("name", node, null));
            playerInfo.setUuid(bd.a("uuid", node, null));
            playerInfo.setOperator(bd.a("operator", node, false));
            playerInfo.setAccepted(bd.a("accepted", node, false));
            playerInfo.setOnline(bd.a("online", node, false));
            invited.add(playerInfo);
         } catch (Exception var6) {
         }
      }

      return invited;
   }

   private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray jsonArray) {
      Map<Integer, RealmsWorldOptions> slots = new HashMap();

      for(JsonElement aJsonArray : jsonArray) {
         try {
            JsonObject node = aJsonArray.getAsJsonObject();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(node.get("options").getAsString());
            RealmsWorldOptions options;
            if (element == null) {
               options = RealmsWorldOptions.getDefaults();
            } else {
               options = RealmsWorldOptions.parse(element.getAsJsonObject());
            }

            int slot = bd.a("slotId", node, -1);
            slots.put(slot, options);
         } catch (Exception var9) {
         }
      }

      for(int i = 1; i <= 3; ++i) {
         if (!slots.containsKey(i)) {
            slots.put(i, RealmsWorldOptions.getEmptyDefaults());
         }
      }

      return slots;
   }

   private static Map<Integer, RealmsWorldOptions> getEmptySlots() {
      HashMap<Integer, RealmsWorldOptions> slots = new HashMap();
      slots.put(1, RealmsWorldOptions.getEmptyDefaults());
      slots.put(2, RealmsWorldOptions.getEmptyDefaults());
      slots.put(3, RealmsWorldOptions.getEmptyDefaults());
      return slots;
   }

   public static RealmsServer parse(String json) {
      RealmsServer server = new RealmsServer();

      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(json).getAsJsonObject();
         server = parse(object);
      } catch (Exception var4) {
         LOGGER.error("Could not parse McoServer: " + var4.getMessage());
      }

      return server;
   }

   private static RealmsServer.b getState(String state) {
      try {
         return RealmsServer.b.valueOf(state);
      } catch (Exception var2) {
         return RealmsServer.b.a;
      }
   }

   private static RealmsServer.c getWorldType(String state) {
      try {
         return RealmsServer.c.valueOf(state);
      } catch (Exception var2) {
         return RealmsServer.c.a;
      }
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
         RealmsServer rhs = (RealmsServer)obj;
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

   public RealmsServer clone() {
      RealmsServer server = new RealmsServer();
      server.id = this.id;
      server.remoteSubscriptionId = this.remoteSubscriptionId;
      server.name = this.name;
      server.motd = this.motd;
      server.state = this.state;
      server.owner = this.owner;
      server.players = this.players;
      server.slots = this.cloneSlots(this.slots);
      server.expired = this.expired;
      server.expiredTrial = this.expiredTrial;
      server.daysLeft = this.daysLeft;
      server.serverPing = new RealmsServerPing();
      server.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
      server.serverPing.playerList = this.serverPing.playerList;
      server.worldType = this.worldType;
      server.ownerUUID = this.ownerUUID;
      server.minigameName = this.minigameName;
      server.activeSlot = this.activeSlot;
      server.minigameId = this.minigameId;
      server.minigameImage = this.minigameImage;
      return server;
   }

   public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> slots) {
      Map<Integer, RealmsWorldOptions> newSlots = new HashMap();

      for(Entry<Integer, RealmsWorldOptions> entry : slots.entrySet()) {
         newSlots.put(entry.getKey(), ((RealmsWorldOptions)entry.getValue()).clone());
      }

      return newSlots;
   }

   public static class a implements Comparator<RealmsServer> {
      private final String a;

      public a(String owner) {
         this.a = owner;
      }

      public int a(RealmsServer server1, RealmsServer server2) {
         return ComparisonChain.start()
            .compareTrueFirst(server1.state.equals(RealmsServer.b.c), server2.state.equals(RealmsServer.b.c))
            .compareTrueFirst(server1.expiredTrial, server2.expiredTrial)
            .compareTrueFirst(server1.owner.equals(this.a), server2.owner.equals(this.a))
            .compareFalseFirst(server1.expired, server2.expired)
            .compareTrueFirst(server1.state.equals(RealmsServer.b.b), server2.state.equals(RealmsServer.b.b))
            .compare(server1.id, server2.id)
            .result();
      }
   }

   public static enum b {
      a,
      b,
      c;
   }

   public static enum c {
      a,
      b,
      c,
      d,
      e;
   }
}
