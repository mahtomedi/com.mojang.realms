package realms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import net.minecraft.realms.Realms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class g {
   public static g.b a = g.b.a;
   private static boolean b;
   private static final Logger c = LogManager.getLogger();
   private final String d;
   private final String e;
   private static final Gson f = new Gson();

   public static g a() {
      String username = Realms.userName();
      String sessionId = Realms.sessionId();
      if (username != null && sessionId != null) {
         if (!b) {
            b = true;
            String realmsEnvironment = System.getenv("realms.environment");
            if (realmsEnvironment == null) {
               realmsEnvironment = System.getProperty("realms.environment");
            }

            if (realmsEnvironment != null) {
               if ("LOCAL".equals(realmsEnvironment)) {
                  d();
               } else if ("STAGE".equals(realmsEnvironment)) {
                  b();
               }
            }
         }

         return new g(sessionId, username, Realms.getProxy());
      } else {
         return null;
      }
   }

   public static void b() {
      a = g.b.b;
   }

   public static void c() {
      a = g.b.a;
   }

   public static void d() {
      a = g.b.c;
   }

   public g(String sessionId, String username, Proxy proxy) {
      this.d = sessionId;
      this.e = username;
      h.a(proxy);
   }

   public RealmsServerList e() throws o, IOException {
      String asciiUrl = this.c("worlds");
      String json = this.a(j.a(asciiUrl));
      return RealmsServerList.parse(json);
   }

   public RealmsServer a(long worldId) throws o, IOException {
      String asciiUrl = this.c("worlds" + "/$ID".replace("$ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl));
      return RealmsServer.parse(json);
   }

   public ServerActivityList b(long worldId) throws o {
      String asciiUrl = this.c("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl));
      return ServerActivityList.parse(json);
   }

   public RealmsServerPlayerLists f() throws o {
      String asciiUrl = this.c("activities/liveplayerlist");
      String json = this.a(j.a(asciiUrl));
      return RealmsServerPlayerLists.parse(json);
   }

   public RealmsServerAddress c(long worldId) throws o, IOException {
      String asciiUrl = this.c("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + worldId));
      String json = this.a(j.a(asciiUrl, 5000, 30000));
      return RealmsServerAddress.parse(json);
   }

   public void a(long worldId, String name, String motd) throws o, IOException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = f.toJson(realmsDescription);
      this.a(j.a(asciiUrl, json, 5000, 10000));
   }

   public Boolean g() throws o, IOException {
      String asciiUrl = this.c("mco/available");
      String json = this.a(j.a(asciiUrl));
      return Boolean.valueOf(json);
   }

   public Boolean h() throws o, IOException {
      String asciiUrl = this.c("mco/stageAvailable");
      String json = this.a(j.a(asciiUrl));
      return Boolean.valueOf(json);
   }

   public g.a i() throws o, IOException {
      String asciiUrl = this.c("mco/client/compatible");
      String response = this.a(j.a(asciiUrl));

      try {
         return g.a.valueOf(response);
      } catch (IllegalArgumentException var5) {
         throw new o(500, "Could not check compatible version, got response: " + response, -1, "");
      }
   }

   public void a(long worldId, String profileUuid) throws o {
      String asciiUrl = this.c("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$UUID", profileUuid));
      this.a(j.b(asciiUrl));
   }

   public void d(long worldId) throws o {
      String asciiUrl = this.c("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.a(j.b(asciiUrl));
   }

   public RealmsServer b(long worldId, String profileName) throws o, IOException {
      PlayerInfo playerInfo = new PlayerInfo();
      playerInfo.setName(profileName);
      String asciiUrl = this.c("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.c(asciiUrl, f.toJson(playerInfo)));
      return RealmsServer.parse(json);
   }

   public BackupList e(long worldId) throws o {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl));
      return BackupList.parse(json);
   }

   public void b(long worldId, String name, String motd) throws o, UnsupportedEncodingException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String asciiUrl = this.c("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.a(j.c(asciiUrl, f.toJson(realmsDescription)));
   }

   public void a(long worldId, int slot, RealmsWorldOptions options) throws o, UnsupportedEncodingException {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
      String json = options.toJson();
      this.a(j.c(asciiUrl, json));
   }

   public boolean a(long worldId, int slot) throws o {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
      String json = this.a(j.d(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public void c(long worldId, String backupId) throws o {
      String asciiUrl = this.b("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(worldId)), "backupId=" + backupId);
      this.a(j.b(asciiUrl, "", 40000, 600000));
   }

   public WorldTemplatePaginatedList a(int page, int pageSize, RealmsServer.c type) throws o {
      String asciiUrl = this.b(
         "worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", type.toString()), String.format("page=%d&pageSize=%d", page, pageSize)
      );
      String json = this.a(j.a(asciiUrl));
      return WorldTemplatePaginatedList.parse(json);
   }

   public Boolean d(long worldId, String minigameId) throws o {
      String path = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", minigameId).replace("$WORLD_ID", String.valueOf(worldId));
      String asciiUrl = this.c("worlds" + path);
      return Boolean.valueOf(this.a(j.d(asciiUrl, "")));
   }

   public Ops e(long worldId, String profileUuid) throws o {
      String path = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
      String asciiUrl = this.c("ops" + path);
      return Ops.parse(this.a(j.c(asciiUrl, "")));
   }

   public Ops f(long worldId, String profileUuid) throws o {
      String path = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
      String asciiUrl = this.c("ops" + path);
      return Ops.parse(this.a(j.b(asciiUrl)));
   }

   public Boolean f(long worldId) throws o, IOException {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.d(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public Boolean g(long worldId) throws o, IOException {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.d(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public Boolean a(long worldId, String seed, Integer levelType, boolean generateStructures) throws o, IOException {
      RealmsWorldResetDto worldReset = new RealmsWorldResetDto(seed, -1L, levelType, generateStructures);
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl, f.toJson(worldReset), 30000, 80000));
      return Boolean.valueOf(json);
   }

   public Boolean g(long worldId, String worldTemplateId) throws o, IOException {
      RealmsWorldResetDto worldReset = new RealmsWorldResetDto(null, Long.valueOf(worldTemplateId), -1, false);
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl, f.toJson(worldReset), 30000, 80000));
      return Boolean.valueOf(json);
   }

   public Subscription h(long worldId) throws o, IOException {
      String asciiUrl = this.c("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.a(j.a(asciiUrl));
      return Subscription.parse(json);
   }

   public int j() throws o {
      String asciiUrl = this.c("invites/count/pending");
      String json = this.a(j.a(asciiUrl));
      return Integer.parseInt(json);
   }

   public PendingInvitesList k() throws o {
      String asciiUrl = this.c("invites/pending");
      String json = this.a(j.a(asciiUrl));
      return PendingInvitesList.parse(json);
   }

   public void a(String invitationId) throws o {
      String asciiUrl = this.c("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", invitationId));
      this.a(j.d(asciiUrl, ""));
   }

   public WorldDownload b(long worldId, int slotId) throws o {
      String asciiUrl = this.c(
         "worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slotId))
      );
      String json = this.a(j.a(asciiUrl));
      return WorldDownload.parse(json);
   }

   public UploadInfo h(long worldId, String uploadToken) throws o {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(worldId)));
      UploadInfo oldUploadInfo = new UploadInfo();
      if (uploadToken != null) {
         oldUploadInfo.setToken(uploadToken);
      }

      GsonBuilder builder = new GsonBuilder();
      builder.excludeFieldsWithoutExposeAnnotation();
      Gson theGson = builder.create();
      String content = theGson.toJson(oldUploadInfo);
      return UploadInfo.parse(this.a(j.d(asciiUrl, content)));
   }

   public void b(String invitationId) throws o {
      String asciiUrl = this.c("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", invitationId));
      this.a(j.d(asciiUrl, ""));
   }

   public void l() throws o {
      String asciiUrl = this.c("mco/tos/agreed");
      this.a(j.c(asciiUrl, ""));
   }

   public RealmsNews m() throws o, IOException {
      String asciiUrl = this.c("mco/v1/news");
      String returnJson = this.a(j.a(asciiUrl, 5000, 10000));
      return RealmsNews.parse(returnJson);
   }

   public void a(PingResult pingResult) throws o {
      String asciiUrl = this.c("regions/ping/stat");
      this.a(j.c(asciiUrl, f.toJson(pingResult)));
   }

   public Boolean n() throws o, IOException {
      String asciiUrl = this.c("trial");
      String json = this.a(j.a(asciiUrl));
      return Boolean.valueOf(json);
   }

   public RealmsServer a(String name, String motd) throws o, IOException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String json = f.toJson(realmsDescription);
      String asciiUrl = this.c("trial");
      String returnJson = this.a(j.a(asciiUrl, json, 5000, 10000));
      return RealmsServer.parse(returnJson);
   }

   public void i(long worldId) throws o, IOException {
      String asciiUrl = this.c("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.a(j.b(asciiUrl));
   }

   private String c(String path) {
      return this.b(path, null);
   }

   private String b(String path, String queryString) {
      try {
         URI uri = new URI(a.e, a.d, "/" + path, queryString, null);
         return uri.toASCIIString();
      } catch (URISyntaxException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   private String a(j<?> r) throws o {
      r.a("sid", this.d);
      r.a("user", this.e);
      r.a("version", Realms.getMinecraftVersionString());
      String realmsVersion = bd.a();
      if (realmsVersion != null) {
         r.a("realms_version", realmsVersion);
      }

      try {
         int responseCode = r.b();
         if (responseCode == 503) {
            int pauseTime = r.a();
            throw new p(pauseTime);
         } else {
            String responseText = r.c();
            if (responseCode >= 200 && responseCode < 300) {
               return responseText;
            } else if (responseCode == 401) {
               String authenticationHeader = r.c("WWW-Authenticate");
               c.info("Could not authorize you against Realms server: " + authenticationHeader);
               throw new o(responseCode, authenticationHeader, -1, authenticationHeader);
            } else if (responseText != null && responseText.length() != 0) {
               i error = new i(responseText);
               c.error("Realms http code: " + responseCode + " -  error code: " + error.b() + " -  message: " + error.a() + " - raw body: " + responseText);
               throw new o(responseCode, responseText, error);
            } else {
               c.error("Realms error code: " + responseCode + " message: " + responseText);
               throw new o(responseCode, responseText, responseCode, "");
            }
         }
      } catch (n var6) {
         throw new o(500, "Could not connect to Realms: " + var6.getMessage(), -1, "");
      }
   }

   public static enum a {
      a,
      b,
      c;
   }

   public static enum b {
      a("pc.realms.minecraft.net", "https"),
      b("pc-stage.realms.minecraft.net", "https"),
      c("localhost:8080", "http");

      public String d;
      public String e;

      private b(String baseUrl, String protocol) {
         this.d = baseUrl;
         this.e = protocol;
      }
   }
}
