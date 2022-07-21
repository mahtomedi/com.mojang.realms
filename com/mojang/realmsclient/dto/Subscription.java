package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class Subscription extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public long startDate;
   public int daysLeft;
   public Subscription.a type = Subscription.a.a;

   public static Subscription parse(String json) {
      Subscription sub = new Subscription();

      try {
         JsonParser parser = new JsonParser();
         JsonObject jsonObject = parser.parse(json).getAsJsonObject();
         sub.startDate = be.a("startDate", jsonObject, 0L);
         sub.daysLeft = be.a("daysLeft", jsonObject, 0);
         sub.type = typeFrom(be.a("subscriptionType", jsonObject, Subscription.a.a.name()));
      } catch (Exception var4) {
         LOGGER.error("Could not parse Subscription: " + var4.getMessage());
      }

      return sub;
   }

   private static Subscription.a typeFrom(String subscriptionType) {
      try {
         return Subscription.a.valueOf(subscriptionType);
      } catch (Exception var2) {
         return Subscription.a.a;
      }
   }

   public static enum a {
      a,
      b;
   }
}
