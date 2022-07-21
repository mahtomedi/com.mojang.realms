package realms;

import java.util.HashMap;
import java.util.Map;

public class bn {
   private static final Map<Long, String> a = new HashMap();

   public static String a(long worldId) {
      return (String)a.get(worldId);
   }

   public static void b(long world) {
      a.remove(world);
   }

   public static void a(long wid, String token) {
      a.put(wid, token);
   }
}
