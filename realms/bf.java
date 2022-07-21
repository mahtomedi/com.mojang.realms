package realms;

public abstract class bf<A> {
   public abstract A a();

   public static <A> bf.b<A> a(A a) {
      return new bf.b<>(a);
   }

   public static <A> bf.a<A> b() {
      return new bf.a<>();
   }

   public static final class a<A> extends bf<A> {
      @Override
      public A a() {
         throw new RuntimeException("None has no value");
      }
   }

   public static final class b<A> extends bf<A> {
      private final A a;

      public b(A a) {
         this.a = a;
      }

      @Override
      public A a() {
         return this.a;
      }

      public static <A> bf<A> b(A value) {
         return new bf.b<>(value);
      }
   }
}
