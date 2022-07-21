package realms;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import net.minecraft.realms.Realms;
import org.apache.commons.io.FileUtils;

public class bh {
   public static bh.a a() {
      File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
      Gson gson = new Gson();

      try {
         return (bh.a)gson.fromJson(FileUtils.readFileToString(file), bh.a.class);
      } catch (IOException var3) {
         return new bh.a();
      }
   }

   public static void a(bh.a data) {
      File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
      Gson gson = new Gson();
      String json = gson.toJson(data);

      try {
         FileUtils.writeStringToFile(file, json);
      } catch (IOException var5) {
      }

   }

   public static class a {
      public String a;
      public boolean b = false;

      private a() {
      }

      private a(String newsLink, boolean hasUnreadNews) {
         this.a = newsLink;
         this.b = hasUnreadNews;
      }
   }
}
