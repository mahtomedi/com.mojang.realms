package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;

public abstract class LongRunningTask implements Runnable, ErrorCallback, GuiCallback {
   protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

   public void setScreen(RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen) {
      this.longRunningMcoTaskScreen = longRunningMcoTaskScreen;
   }

   @Override
   public void error(String errorMessage) {
      this.longRunningMcoTaskScreen.error(errorMessage);
   }

   public void setTitle(String title) {
      this.longRunningMcoTaskScreen.setTitle(title);
   }

   public boolean aborted() {
      return this.longRunningMcoTaskScreen.aborted();
   }

   @Override
   public void tick() {
   }

   public void init() {
   }

   public void abortTask() {
   }
}
