package com.mojang.realmsclient.exception;

public class RetryCallException extends RealmsServiceException {
   public final int delaySeconds;

   public RetryCallException(int delaySeconds) {
      super(503, "Retry operation", -1, "");
      this.delaySeconds = delaySeconds;
   }
}
