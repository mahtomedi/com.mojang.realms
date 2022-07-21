package com.mojang.realmsclient.dto;

public class PlayerInfo {
   private String name;
   private String uuid;
   private boolean operator = false;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   public boolean isOperator() {
      return this.operator;
   }

   public void setOperator(boolean operator) {
      this.operator = operator;
   }
}
