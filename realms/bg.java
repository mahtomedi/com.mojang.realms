package realms;

public class bg<A, B> {
   private final A a;
   private final B b;

   protected bg(A first, B second) {
      this.a = first;
      this.b = second;
   }

   public static <A, B> bg<A, B> a(A a, B b) {
      return new bg<>(a, b);
   }

   public A a() {
      return this.a;
   }

   public B b() {
      return this.b;
   }

   public String a(String separator) {
      return String.format("%s%s%s", this.a, separator, this.b);
   }
}
