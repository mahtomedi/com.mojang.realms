package realms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class f {
   private final Map<String, String> a = new HashMap();

   public static f a(String key, String value) {
      f queryBuilder = new f();
      queryBuilder.a.put(key, value);
      return queryBuilder;
   }

   public static f a() {
      return new f();
   }

   public f b(String key, String value) {
      this.a.put(key, value);
      return this;
   }

   public f a(Object key, Object value) {
      this.a.put(String.valueOf(key), String.valueOf(value));
      return this;
   }

   public String b() {
      StringBuilder stringBuilder = new StringBuilder();
      Iterator<String> keyIterator = this.a.keySet().iterator();
      if (!keyIterator.hasNext()) {
         return null;
      } else {
         String firstKey = (String)keyIterator.next();
         stringBuilder.append(firstKey).append("=").append(this.a((String)this.a.get(firstKey)));

         while(keyIterator.hasNext()) {
            String key = (String)keyIterator.next();
            stringBuilder.append("&").append(key).append("=").append(this.a((String)this.a.get(key)));
         }

         return stringBuilder.toString();
      }
   }

   private String a(String value) {
      try {
         return URLEncoder.encode(value, "UTF-8");
      } catch (UnsupportedEncodingException var3) {
         return value;
      }
   }
}
