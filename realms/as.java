package realms;

import com.mojang.realmsclient.dto.RealmsServerAddress;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class as extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final RealmsServerAddress c;
   private final ReentrantLock d;

   public as(RealmsScreen lastScreen, RealmsServerAddress serverAddress, ReentrantLock connectLock) {
      this.b = lastScreen;
      this.c = serverAddress;
      this.d = connectLock;
   }

   public void confirmResult(boolean result, int id) {
      try {
         if (!result) {
            Realms.setScreen(this.b);
         } else {
            try {
               Realms.downloadResourcePack(this.c.resourcePackUrl, this.c.resourcePackHash).thenRun(() -> {
                  al longRunningMcoTaskScreen = new al(this.b, new bi.d(this.b, this.c));
                  longRunningMcoTaskScreen.a();
                  Realms.setScreen(longRunningMcoTaskScreen);
               }).exceptionally(t -> {
                  Realms.clearResourcePack();
                  a.error(t);
                  Realms.setScreen(new ai("Failed to download resource pack!", this.b));
                  return null;
               });
            } catch (Exception var7) {
               Realms.clearResourcePack();
               a.error(var7);
               Realms.setScreen(new ai("Failed to download resource pack!", this.b));
            }
         }
      } finally {
         if (this.d != null && this.d.isHeldByCurrentThread()) {
            this.d.unlock();
         }

      }

   }
}
