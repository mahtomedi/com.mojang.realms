package com.mojang.realmsclient.gui;

import net.minecraft.realms.RealmsButton;

public abstract class RealmsHideableButton extends RealmsButton {
   boolean visible = true;

   public RealmsHideableButton(int id, int x, int y, int width, int height, String msg) {
      super(id, x, y, width, height, msg);
   }

   public void render(int xm, int ym, float a) {
      if (this.visible) {
         super.render(xm, ym, a);
      }
   }

   public void onClick(double mx, double my) {
      if (this.visible) {
         this.clicked(mx, my);
      }
   }

   public abstract void clicked(double var1, double var3);

   public void onRelease(double mx, double my) {
      if (this.visible) {
         super.onRelease(mx, my);
      }
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public boolean getVisible() {
      return this.visible;
   }
}
