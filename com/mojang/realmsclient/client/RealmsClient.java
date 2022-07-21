package com.mojang.realmsclient.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.realmsclient.RealmsVersion;
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
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import net.minecraft.realms.Realms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsClient {
   public static RealmsClient.Environment currentEnvironment = RealmsClient.Environment.PRODUCTION;
   private static boolean initialized;
   private static final Logger LOGGER = LogManager.getLogger();
   private final String sessionId;
   private final String username;
   private static final String WORLDS_RESOURCE_PATH = "worlds";
   private static final String INVITES_RESOURCE_PATH = "invites";
   private static final String MCO_RESOURCE_PATH = "mco";
   private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
   private static final String ACTIVITIES_RESOURCE = "activities";
   private static final String OPS_RESOURCE = "ops";
   private static final String REGIONS_RESOURCE = "regions/ping/stat";
   private static final String TRIALS_RESOURCE = "trial";
   private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
   private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
   private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
   private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
   private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
   private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
   private static final String PATH_AVAILABLE = "/available";
   private static final String PATH_TEMPLATES = "/templates/$WORLD_TYPE";
   private static final String PATH_WORLD_JOIN = "/v1/$ID/join/pc";
   private static final String PATH_WORLD_GET = "/$ID";
   private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
   private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
   private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
   private static final String PATH_PENDING_INVITES = "/pending";
   private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
   private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
   private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
   private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
   private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
   private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
   private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
   private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
   private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
   private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
   private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/slot/$SLOT_ID/download";
   private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
   private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
   private static final String PATH_TOS_AGREED = "/tos/agreed";
   private static final String PATH_NEWS = "/v1/news";
   private static final String PATH_STAGE_AVAILABLE = "/stageAvailable";
   private static final Gson gson = new Gson();

   public static RealmsClient createRealmsClient() {
      String username = Realms.userName();
      String sessionId = Realms.sessionId();
      if (username != null && sessionId != null) {
         if (!initialized) {
            initialized = true;
            String realmsEnvironment = System.getenv("realms.environment");
            if (realmsEnvironment == null) {
               realmsEnvironment = System.getProperty("realms.environment");
            }

            if (realmsEnvironment != null) {
               if ("LOCAL".equals(realmsEnvironment)) {
                  switchToLocal();
               } else if ("STAGE".equals(realmsEnvironment)) {
                  switchToStage();
               }
            }
         }

         return new RealmsClient(sessionId, username, Realms.getProxy());
      } else {
         return null;
      }
   }

   public static void switchToStage() {
      currentEnvironment = RealmsClient.Environment.STAGE;
   }

   public static void switchToProd() {
      currentEnvironment = RealmsClient.Environment.PRODUCTION;
   }

   public static void switchToLocal() {
      currentEnvironment = RealmsClient.Environment.LOCAL;
   }

   public RealmsClient(String sessionId, String username, Proxy proxy) {
      this.sessionId = sessionId;
      this.username = username;
      RealmsClientConfig.setProxy(proxy);
   }

   public RealmsServerList listWorlds() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds");
      String json = this.execute(Request.get(asciiUrl));
      return RealmsServerList.parse(json);
   }

   public RealmsServer getOwnWorld(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(worldId)));
      String json = this.execute(Request.get(asciiUrl));
      return RealmsServer.parse(json);
   }

   public ServerActivityList getActivity(long worldId) throws RealmsServiceException {
      String asciiUrl = this.url("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.get(asciiUrl));
      return ServerActivityList.parse(json);
   }

   public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
      String asciiUrl = this.url("activities/liveplayerlist");
      String json = this.execute(Request.get(asciiUrl));
      return RealmsServerPlayerLists.parse(json);
   }

   public RealmsServerAddress join(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + worldId));
      String json = this.execute(Request.get(asciiUrl, 5000, 30000));
      return RealmsServerAddress.parse(json);
   }

   public void initializeWorld(long worldId, String name, String motd) throws RealmsServiceException, IOException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = gson.toJson(realmsDescription);
      this.execute(Request.post(asciiUrl, json, 5000, 10000));
   }

   public Boolean mcoEnabled() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("mco/available");
      String json = this.execute(Request.get(asciiUrl));
      return Boolean.valueOf(json);
   }

   public Boolean stageAvailable() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("mco/stageAvailable");
      String json = this.execute(Request.get(asciiUrl));
      return Boolean.valueOf(json);
   }

   public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("mco/client/compatible");
      String response = this.execute(Request.get(asciiUrl));

      try {
         return RealmsClient.CompatibleVersionResponse.valueOf(response);
      } catch (IllegalArgumentException var5) {
         throw new RealmsServiceException(500, "Could not check compatible version, got response: " + response, -1, "");
      }
   }

   public void uninvite(long worldId, String profileUuid) throws RealmsServiceException {
      String asciiUrl = this.url("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$UUID", profileUuid));
      this.execute(Request.delete(asciiUrl));
   }

   public void uninviteMyselfFrom(long worldId) throws RealmsServiceException {
      String asciiUrl = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.execute(Request.delete(asciiUrl));
   }

   public RealmsServer invite(long worldId, String profileName) throws RealmsServiceException, IOException {
      PlayerInfo playerInfo = new PlayerInfo();
      playerInfo.setName(profileName);
      String asciiUrl = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.post(asciiUrl, gson.toJson(playerInfo)));
      return RealmsServer.parse(json);
   }

   public BackupList backupsFor(long worldId) throws RealmsServiceException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.get(asciiUrl));
      return BackupList.parse(json);
   }

   public void update(long worldId, String name, String motd) throws RealmsServiceException, UnsupportedEncodingException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String asciiUrl = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.execute(Request.post(asciiUrl, gson.toJson(realmsDescription)));
   }

   public void updateSlot(long worldId, int slot, RealmsWorldOptions options) throws RealmsServiceException, UnsupportedEncodingException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
      String json = options.toJson();
      this.execute(Request.post(asciiUrl, json));
   }

   public boolean switchSlot(long worldId, int slot) throws RealmsServiceException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slot)));
      String json = this.execute(Request.put(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public void restoreWorld(long worldId, String backupId) throws RealmsServiceException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(worldId)), "backupId=" + backupId);
      this.execute(Request.put(asciiUrl, "", 40000, 600000));
   }

   public WorldTemplatePaginatedList fetchWorldTemplates(int page, int pageSize, RealmsServer.WorldType type) throws RealmsServiceException {
      String asciiUrl = this.url(
         "worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", type.toString()), String.format("page=%d&pageSize=%d", page, pageSize)
      );
      String json = this.execute(Request.get(asciiUrl));
      return WorldTemplatePaginatedList.parse(json);
   }

   public Boolean putIntoMinigameMode(long worldId, String minigameId) throws RealmsServiceException {
      String path = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", minigameId).replace("$WORLD_ID", String.valueOf(worldId));
      String asciiUrl = this.url("worlds" + path);
      return Boolean.valueOf(this.execute(Request.put(asciiUrl, "")));
   }

   public Ops op(long worldId, String profileUuid) throws RealmsServiceException {
      String path = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
      String asciiUrl = this.url("ops" + path);
      return Ops.parse(this.execute(Request.post(asciiUrl, "")));
   }

   public Ops deop(long worldId, String profileUuid) throws RealmsServiceException {
      String path = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(worldId)).replace("$PROFILE_UUID", profileUuid);
      String asciiUrl = this.url("ops" + path);
      return Ops.parse(this.execute(Request.delete(asciiUrl)));
   }

   public Boolean open(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.put(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public Boolean close(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.put(asciiUrl, ""));
      return Boolean.valueOf(json);
   }

   public Boolean resetWorldWithSeed(long worldId, String seed, Integer levelType, boolean generateStructures) throws RealmsServiceException, IOException {
      RealmsWorldResetDto worldReset = new RealmsWorldResetDto(seed, -1L, levelType, generateStructures);
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.post(asciiUrl, gson.toJson(worldReset), 30000, 80000));
      return Boolean.valueOf(json);
   }

   public Boolean resetWorldWithTemplate(long worldId, String worldTemplateId) throws RealmsServiceException, IOException {
      RealmsWorldResetDto worldReset = new RealmsWorldResetDto(null, Long.valueOf(worldTemplateId), -1, false);
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.post(asciiUrl, gson.toJson(worldReset), 30000, 80000));
      return Boolean.valueOf(json);
   }

   public Subscription subscriptionFor(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      String json = this.execute(Request.get(asciiUrl));
      return Subscription.parse(json);
   }

   public int pendingInvitesCount() throws RealmsServiceException {
      String asciiUrl = this.url("invites/count/pending");
      String json = this.execute(Request.get(asciiUrl));
      return Integer.parseInt(json);
   }

   public PendingInvitesList pendingInvites() throws RealmsServiceException {
      String asciiUrl = this.url("invites/pending");
      String json = this.execute(Request.get(asciiUrl));
      return PendingInvitesList.parse(json);
   }

   public void acceptInvitation(String invitationId) throws RealmsServiceException {
      String asciiUrl = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", invitationId));
      this.execute(Request.put(asciiUrl, ""));
   }

   public WorldDownload download(long worldId, int slotId) throws RealmsServiceException {
      String asciiUrl = this.url(
         "worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(worldId)).replace("$SLOT_ID", String.valueOf(slotId))
      );
      String json = this.execute(Request.get(asciiUrl));
      return WorldDownload.parse(json);
   }

   public UploadInfo upload(long worldId, String uploadToken) throws RealmsServiceException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(worldId)));
      UploadInfo oldUploadInfo = new UploadInfo();
      if (uploadToken != null) {
         oldUploadInfo.setToken(uploadToken);
      }

      GsonBuilder builder = new GsonBuilder();
      builder.excludeFieldsWithoutExposeAnnotation();
      Gson theGson = builder.create();
      String content = theGson.toJson(oldUploadInfo);
      return UploadInfo.parse(this.execute(Request.put(asciiUrl, content)));
   }

   public void rejectInvitation(String invitationId) throws RealmsServiceException {
      String asciiUrl = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", invitationId));
      this.execute(Request.put(asciiUrl, ""));
   }

   public void agreeToTos() throws RealmsServiceException {
      String asciiUrl = this.url("mco/tos/agreed");
      this.execute(Request.post(asciiUrl, ""));
   }

   public RealmsNews getNews() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("mco/v1/news");
      String returnJson = this.execute(Request.get(asciiUrl, 5000, 10000));
      return RealmsNews.parse(returnJson);
   }

   public void sendPingResults(PingResult pingResult) throws RealmsServiceException {
      String asciiUrl = this.url("regions/ping/stat");
      this.execute(Request.post(asciiUrl, gson.toJson(pingResult)));
   }

   public Boolean trialAvailable() throws RealmsServiceException, IOException {
      String asciiUrl = this.url("trial");
      String json = this.execute(Request.get(asciiUrl));
      return Boolean.valueOf(json);
   }

   public RealmsServer createTrial(String name, String motd) throws RealmsServiceException, IOException {
      RealmsDescriptionDto realmsDescription = new RealmsDescriptionDto(name, motd);
      String json = gson.toJson(realmsDescription);
      String asciiUrl = this.url("trial");
      String returnJson = this.execute(Request.post(asciiUrl, json, 5000, 10000));
      return RealmsServer.parse(returnJson);
   }

   public void deleteWorld(long worldId) throws RealmsServiceException, IOException {
      String asciiUrl = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(worldId)));
      this.execute(Request.delete(asciiUrl));
   }

   private String url(String path) {
      return this.url(path, null);
   }

   private String url(String path, String queryString) {
      try {
         URI uri = new URI(currentEnvironment.protocol, currentEnvironment.baseUrl, "/" + path, queryString, null);
         return uri.toASCIIString();
      } catch (URISyntaxException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   private String execute(Request<?> r) throws RealmsServiceException {
      r.cookie("sid", this.sessionId);
      r.cookie("user", this.username);
      r.cookie("version", "1.13.1");
      String realmsVersion = RealmsVersion.getVersion();
      if (realmsVersion != null) {
         r.cookie("realms_version", realmsVersion);
      }

      try {
         int responseCode = r.responseCode();
         if (responseCode == 503) {
            int pauseTime = r.getRetryAfterHeader();
            throw new RetryCallException(pauseTime);
         } else {
            String responseText = r.text();
            if (responseCode >= 200 && responseCode < 300) {
               return responseText;
            } else if (responseCode == 401) {
               String authenticationHeader = r.getHeader("WWW-Authenticate");
               LOGGER.info("Could not authorize you against Realms server: " + authenticationHeader);
               throw new RealmsServiceException(responseCode, authenticationHeader, -1, authenticationHeader);
            } else if (responseText != null && responseText.length() != 0) {
               RealmsError error = new RealmsError(responseText);
               LOGGER.error(
                  "Realms http code: "
                     + responseCode
                     + " -  error code: "
                     + error.getErrorCode()
                     + " -  message: "
                     + error.getErrorMessage()
                     + " - raw body: "
                     + responseText
               );
               throw new RealmsServiceException(responseCode, responseText, error);
            } else {
               LOGGER.error("Realms error code: " + responseCode + " message: " + responseText);
               throw new RealmsServiceException(responseCode, responseText, responseCode, "");
            }
         }
      } catch (RealmsHttpException var6) {
         throw new RealmsServiceException(500, "Could not connect to Realms: " + var6.getMessage(), -1, "");
      }
   }

   public static enum CompatibleVersionResponse {
      COMPATIBLE,
      OUTDATED,
      OTHER;
   }

   public static enum Environment {
      PRODUCTION("pc.realms.minecraft.net", "https"),
      STAGE("pc-stage.realms.minecraft.net", "https"),
      LOCAL("localhost:8080", "http");

      public String baseUrl;
      public String protocol;

      private Environment(String baseUrl, String protocol) {
         this.baseUrl = baseUrl;
         this.protocol = protocol;
      }
   }
}
