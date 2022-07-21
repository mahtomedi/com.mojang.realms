package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class UploadInfo extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   @Expose
   private boolean worldClosed;
   @Expose
   private String token = "";
   @Expose
   private String uploadEndpoint = "";
   private int port;

   public static UploadInfo parse(String json) {
      UploadInfo uploadInfo = new UploadInfo();

      try {
         JsonParser parser = new JsonParser();
         JsonObject jsonObject = parser.parse(json).getAsJsonObject();
         uploadInfo.worldClosed = be.a("worldClosed", jsonObject, false);
         uploadInfo.token = be.a("token", jsonObject, null);
         uploadInfo.uploadEndpoint = be.a("uploadEndpoint", jsonObject, null);
         uploadInfo.port = be.a("port", jsonObject, 8080);
      } catch (Exception var4) {
         LOGGER.error("Could not parse UploadInfo: " + var4.getMessage());
      }

      return uploadInfo;
   }

   public String getToken() {
      return this.token;
   }

   public String getUploadEndpoint() {
      return this.uploadEndpoint;
   }

   public boolean isWorldClosed() {
      return this.worldClosed;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public int getPort() {
      return this.port;
   }
}
