package realms;

import com.google.common.base.Suppliers;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class bd {
   private static final Supplier<String> a = Suppliers.memoize(() -> a("/realmsVersion"));
   private static final Supplier<String> b = Suppliers.memoize(() -> a("/realmsBuiltForMinecraftVersion"));

   private bd() {
   }

   @Nullable
   public static String a() {
      return (String)a.get();
   }

   @Nullable
   public static String b() {
      return (String)b.get();
   }

   private static String a(String resourceName) {
      try {
         InputStream inputStream = bd.class.getResourceAsStream(resourceName);
         Throwable var2 = null;

         Object var5;
         try {
            if (inputStream == null) {
               return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            Throwable var4 = null;

            try {
               var5 = reader.readLine();
            } catch (Throwable var31) {
               var5 = var31;
               var4 = var31;
               throw var31;
            } finally {
               if (reader != null) {
                  if (var4 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var30) {
                        var4.addSuppressed(var30);
                     }
                  } else {
                     reader.close();
                  }
               }

            }
         } catch (Throwable var33) {
            var2 = var33;
            throw var33;
         } finally {
            if (inputStream != null) {
               if (var2 != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var29) {
                     var2.addSuppressed(var29);
                  }
               } else {
                  inputStream.close();
               }
            }

         }

         return (String)var5;
      } catch (Exception var35) {
         return null;
      }
   }
}
