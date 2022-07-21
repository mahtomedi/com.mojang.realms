package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.obfuscate.DontObfuscateOrShrink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realms.be;
import realms.l;

@DontObfuscateOrShrink
public class WorldTemplatePaginatedList extends l {
   private static final Logger LOGGER = LogManager.getLogger();
   public List<WorldTemplate> templates;
   public int page;
   public int size;
   public int total;

   public WorldTemplatePaginatedList() {
   }

   public WorldTemplatePaginatedList(int size) {
      this.templates = Collections.emptyList();
      this.page = 0;
      this.size = size;
      this.total = -1;
   }

   public boolean isLastPage() {
      return this.page * this.size >= this.total && this.page > 0 && this.total > 0 && this.size > 0;
   }

   public static WorldTemplatePaginatedList parse(String json) {
      WorldTemplatePaginatedList list = new WorldTemplatePaginatedList();
      list.templates = new ArrayList();

      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(json).getAsJsonObject();
         if (object.get("templates").isJsonArray()) {
            Iterator<JsonElement> it = object.get("templates").getAsJsonArray().iterator();

            while(it.hasNext()) {
               list.templates.add(WorldTemplate.parse(((JsonElement)it.next()).getAsJsonObject()));
            }
         }

         list.page = be.a("page", object, 0);
         list.size = be.a("size", object, 0);
         list.total = be.a("total", object, 0);
      } catch (Exception var5) {
         LOGGER.error("Could not parse WorldTemplatePaginatedList: " + var5.getMessage());
      }

      return list;
   }
}
