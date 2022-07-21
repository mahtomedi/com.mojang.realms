package com.mojang.realmsclient.exception;

import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.logging.log4j.Logger;

public class RealmsDefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
   private final Logger logger;

   public RealmsDefaultUncaughtExceptionHandler(Logger logger) {
      this.logger = logger;
   }

   public void uncaughtException(Thread t, Throwable e) {
      this.logger.error("Caught previously unhandled exception :");
      this.logger.error(e);
   }
}
