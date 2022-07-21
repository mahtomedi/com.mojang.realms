package realms;

public abstract class be<A> {
   public abstract A a();

   public static <A> be.b<A> a(A a) {
      return new be.b<>(a);
   }

   public static <A> be.a<A> b() {
      return new be.a<>();
   }

   public static final class a<A> extends be<A> {
      @Override
      public A a() {
         throw new RuntimeException("None has no value");
      }
   }

   public static final class b<A> extends be<A> {
      private final A a;

      public b(A a) {
         this.a = a;
      }

      @Override
      public A a() {
         return this.a;
      }

      public static <A> be<A> b(A value) {
         return new be.b<>(value);
      }
   }
}
