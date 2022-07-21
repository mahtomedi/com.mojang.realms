package com.mojang.realmsclient.util;

public abstract class Option<A> {
   public abstract A get();

   public static <A> Option.Some<A> some(A a) {
      return new Option.Some<>(a);
   }

   public static <A> Option.None<A> none() {
      return new Option.None<>();
   }

   public static final class None<A> extends Option<A> {
      @Override
      public A get() {
         throw new RuntimeException("None has no value");
      }
   }

   public static final class Some<A> extends Option<A> {
      private final A a;

      public Some(A a) {
         this.a = a;
      }

      @Override
      public A get() {
         return this.a;
      }

      public static <A> Option<A> of(A value) {
         return new Option.Some<>(value);
      }
   }
}
