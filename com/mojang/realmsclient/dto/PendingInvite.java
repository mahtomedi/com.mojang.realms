package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.bd;
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
         invite.invitationId = bd.a("invitationId", json, "");
         invite.worldName = bd.a("worldName", json, "");
         invite.worldOwnerName = bd.a("worldOwnerName", json, "");
         invite.worldOwnerUuid = bd.a("worldOwnerUuid", json, "");
         invite.date = bd.a("date", json);
      } catch (Exception var3) {
         LOGGER.error("Could not parse PendingInvite: " + var3.getMessage());
      }

      return invite;
   }
}
