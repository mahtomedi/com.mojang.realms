package com.mojang.realmsclient.exception;

public class RealmsServiceException extends Exception {
   public final int httpResultCode;
   public final String httpResponseContent;
   public final int errorCode;
   public final String errorMsg;

   public RealmsServiceException(int httpResultCode, String httpResponseText, int errorCode, String errorMsg) {
      super(httpResponseText);
      this.httpResultCode = httpResultCode;
      this.httpResponseContent = httpResponseText;
      this.errorCode = errorCode;
      this.errorMsg = errorMsg;
   }

   public String toString() {
      return this.errorCode != -1
         ? "Realms ( ErrorCode: " + this.errorCode + " ): " + this.errorMsg
         : "Realms (" + this.httpResultCode + ") " + this.httpResponseContent;
   }
}
