package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.gui.screens.LongRunningMcoTaskScreen;
import net.minecraft.realms.RealmsButton;

public abstract class LongRunningTask implements Runnable, ErrorCallback, GuiCallback {
   protected LongRunningMcoTaskScreen longRunningMcoTaskScreen;

   public void setScreen(LongRunningMcoTaskScreen longRunningMcoTaskScreen) {
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

   @Override
   public void buttonClicked(RealmsButton button) {
   }

   public void init() {
   }

   public void abortTask() {
   }
}
