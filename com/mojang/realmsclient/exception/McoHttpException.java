package com.mojang.realmsclient.exception;

public class McoHttpException extends RuntimeException {
   public McoHttpException(String s, Exception e) {
      super(s, e);
   }
}
