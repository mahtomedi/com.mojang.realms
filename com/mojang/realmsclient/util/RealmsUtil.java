package com.mojang.realmsclient.util;

import java.net.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsUtil {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void browseTo(String uri) {
      try {
         URI link = new URI(uri);
         Class<?> desktopClass = Class.forName("java.awt.Desktop");
         Object o = desktopClass.getMethod("getDesktop").invoke(null);
         desktopClass.getMethod("browse", URI.class).invoke(o, link);
      } catch (Throwable var4) {
         LOGGER.error("Couldn't open link");
      }

   }
}
