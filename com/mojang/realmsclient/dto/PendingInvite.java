package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class PendingInvite extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public String invitationId;
   public String worldName;
   public String worldOwnerName;
   public String worldOwnerUuid;
   public Date date;

   public static PendingInvite parse(JsonObject json) {
      PendingInvite invite = new PendingInvite();

      try {
         invite.invitationId = be.a("invitationId", json, "");
         invite.worldName = be.a("worldName", json, "");
         invite.worldOwnerName = be.a("worldOwnerName", json, "");
         invite.worldOwnerUuid = be.a("worldOwnerUuid", json, "");
         invite.date = be.a("date", json);
      } catch (Exception var3) {
         LOGGER.error("Could not parse PendingInvite: " + var3.getMessage());
      }

      return invite;
   }
}
