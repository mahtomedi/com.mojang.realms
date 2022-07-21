package realms;

import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.pluginapi.LoadedRealmsPlugin;

public class bc implements LoadedRealmsPlugin {
   public RealmsScreen getMainScreen(RealmsScreen lastScreen) {
      return new b(lastScreen);
   }

   public RealmsScreen getNotificationsScreen(RealmsScreen lastScreen) {
      return new am(lastScreen);
   }
}
