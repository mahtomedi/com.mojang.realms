package realms;

import java.util.Arrays;

public class a {
   private final char[] a;
   private int b;
   private final Runnable c;

   public a(char[] chars, Runnable onCompletion) {
      this.c = onCompletion;
      if (chars.length < 1) {
         throw new IllegalArgumentException("Must have at least one char");
      } else {
         this.a = chars;
         this.b = 0;
      }
   }

   public a(char[] chars) {
      this(chars, () -> {
      });
   }

   public boolean a(char c) {
      if (c == this.a[this.b]) {
         ++this.b;
         if (this.b == this.a.length) {
            this.a();
            this.c.run();
            return true;
         } else {
            return false;
         }
      } else {
         this.a();
         return false;
      }
   }

   public void a() {
      this.b = 0;
   }

   public String toString() {
      return "KeyCombo{chars=" + Arrays.toString(this.a) + ", matchIndex=" + this.b + '}';
   }
}
