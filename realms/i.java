package realms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class i {
   private static final Logger a = LogManager.getLogger();
   private String b;
   private int c;

   public i(String error) {
      try {
         JsonParser parser = new JsonParser();
         JsonObject object = parser.parse(error).getAsJsonObject();
         this.b = be.a("errorMsg", object, "");
         this.c = be.a("errorCode", object, -1);
      } catch (Exception var4) {
         a.error("Could not parse RealmsError: " + var4.getMessage());
         a.error("The error was: " + error);
      }

   }

   public String a() {
      return this.b;
   }

   public int b() {
      return this.c;
   }
}
