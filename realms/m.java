package realms;

import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.logging.log4j.Logger;

public class m implements UncaughtExceptionHandler {
   private final Logger a;

   public m(Logger logger) {
      this.a = logger;
   }

   public void uncaughtException(Thread t, Throwable e) {
      this.a.error("Caught previously unhandled exception :");
      this.a.error(e);
   }
}
