package com.mojang.realmsclient.util;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import net.minecraft.realms.Realms;
import org.apache.commons.io.FileUtils;

public class RealmsPersistence {
   private static final String FILE_NAME = "realms_persistence.json";

   public static RealmsPersistence.RealmsPersistenceData readFile() {
      File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
      Gson gson = new Gson();

      try {
         return (RealmsPersistence.RealmsPersistenceData)gson.fromJson(FileUtils.readFileToString(file), RealmsPersistence.RealmsPersistenceData.class);
      } catch (IOException var3) {
         return new RealmsPersistence.RealmsPersistenceData();
      }
   }

   public static void writeFile(RealmsPersistence.RealmsPersistenceData data) {
      File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
      Gson gson = new Gson();
      String json = gson.toJson(data);

      try {
         FileUtils.writeStringToFile(file, json);
      } catch (IOException var5) {
      }

   }

   public static class RealmsPersistenceData {
      public String newsLink;
      public boolean hasUnreadNews = false;

      private RealmsPersistenceData() {
      }

      private RealmsPersistenceData(String newsLink, boolean hasUnreadNews) {
         this.newsLink = newsLink;
         this.hasUnreadNews = hasUnreadNews;
      }
   }
}
