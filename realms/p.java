package realms;

public class p extends o {
   public final int e;

   public p(int delaySeconds) {
      super(503, "Retry operation", -1, "");
      if (delaySeconds >= 0 && delaySeconds <= 120) {
         this.e = delaySeconds;
      } else {
         this.e = 5;
      }

   }
}
