package com.mojang.realmsclient.gui;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RendererUtility;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class MovableScrolledSelectionList {
   private static final int NO_DRAG = -1;
   private static final int DRAG_OUTSIDE = -2;
   private final int x0;
   private final int y0;
   protected int x1;
   protected int y1;
   protected final int itemHeight;
   private int xButton;
   private int yButton;
   protected int xm;
   protected int ym;
   private float yDrag = -2.0F;
   private float yDragScale;
   private float yo;
   private int lastSelection = -1;
   private long lastSelectionTime;
   protected RealmsButton button;

   public MovableScrolledSelectionList(int x0, int y0, int width, int height, int itemHeight) {
      this.y0 = y0;
      this.y1 = y0 + height;
      this.itemHeight = itemHeight;
      this.x0 = x0;
      this.x1 = x0 + width;
   }

   public MovableScrolledSelectionList(int x0, int y0, int width, int height, int itemHeight, String buttonMsg) {
      this(x0, y0, width, height, itemHeight);
      this.xButton = x0;
      this.yButton = y0 + height - 10;
      this.y1 -= 15;
      this.button = RealmsScreen.newButton(1000, this.xButton, this.yButton, width, 20, buttonMsg);
   }

   protected abstract int getNumberOfItems();

   protected abstract void selectItem(int var1, boolean var2);

   protected abstract boolean isSelectedItem(int var1);

   protected int getMaxPosition() {
      return this.getNumberOfItems() * this.itemHeight;
   }

   protected abstract void renderBackground();

   protected abstract void renderItem(int var1, int var2, int var3, int var4, Tezzelator var5);

   protected void renderHeader(int x, int y, Tezzelator t) {
   }

   protected void buttonClicked() {
   }

   public void itemClicked(int x, int y, int xm, int ym) {
   }

   private void capYPosition() {
      int max = this.getMaxScroll();
      if (max < 0) {
         max = 0;
      }

      if (this.yo < 0.0F) {
         this.yo = 0.0F;
      }

      if (this.yo > (float)max) {
         this.yo = (float)max;
      }

   }

   public int getMaxScroll() {
      return this.getMaxPosition() - (this.y1 - this.y0 - 4);
   }

   public void render(int xm, int ym, float a) {
      this.xm = xm;
      this.ym = ym;
      this.renderBackground();
      boolean itemClicked = false;
      int itemCount = this.getNumberOfItems();
      int xx0 = this.getScrollbarPosition();
      int xx1 = xx0 + 6;
      if (Mouse.isButtonDown(0)) {
         if (this.yDrag == -1.0F) {
            boolean doDrag = true;
            if (ym >= this.y0 && ym <= this.y1) {
               int x0 = this.x0 + 2;
               int x1 = this.x1 - 2;
               int clickSlotPos = ym - this.y0 + (int)this.yo - 4;
               int slot = clickSlotPos / this.itemHeight;
               if (xm >= x0 && xm <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < itemCount) {
                  boolean doubleClick = slot == this.lastSelection && Realms.currentTimeMillis() - this.lastSelectionTime < 250L;
                  this.selectItem(slot, doubleClick);
                  itemClicked = true;
                  this.lastSelection = slot;
                  this.lastSelectionTime = Realms.currentTimeMillis();
               } else if (xm >= x0 && xm <= x1 && clickSlotPos < 0) {
                  doDrag = false;
               }

               if (xm >= xx0 && xm <= xx1) {
                  this.yDragScale = -1.0F;
                  int max = this.getMaxScroll();
                  if (max < 1) {
                     max = 1;
                  }

                  int barHeight = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                  if (barHeight < 32) {
                     barHeight = 32;
                  }

                  if (barHeight > this.y1 - this.y0 - 8) {
                     barHeight = this.y1 - this.y0 - 8;
                  }

                  this.yDragScale /= (float)(this.y1 - this.y0 - barHeight) / (float)max;
               } else {
                  this.yDragScale = 1.0F;
               }

               if (doDrag) {
                  this.yDrag = (float)ym;
               } else {
                  this.yDrag = -2.0F;
               }
            } else if (ym < this.y1 || ym > this.y1 + 25) {
               this.yDrag = -2.0F;
            } else if (this.button != null) {
               int xButton_1 = this.xButton + this.button.getWidth();
               if (xm >= this.xButton && xm < xButton_1) {
                  this.buttonClicked();
               }
            }
         } else if (this.yDrag >= 0.0F) {
            this.yo -= ((float)ym - this.yDrag) * this.yDragScale;
            this.yDrag = (float)ym;
         }
      } else {
         while(!Realms.isTouchScreen() && Mouse.next()) {
            int wheel = Mouse.getEventDWheel();
            if (wheel != 0) {
               if (wheel > 0) {
                  wheel = -1;
               } else if (wheel < 0) {
                  wheel = 1;
               }

               this.yo += (float)(wheel * this.itemHeight / 2);
            }
         }

         this.yDrag = -1.0F;
      }

      this.capYPosition();
      GL11.glDisable(2896);
      GL11.glDisable(2912);
      Tezzelator t = Tezzelator.instance;
      RealmsScreen.bind("textures/gui/options_background.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(2105376);
      if (this.button != null) {
         t.vertexUV((double)this.x0, (double)(this.y1 + 25), 0.0, (double)((float)this.x0 / s), (double)((float)(this.y1 + 25 + (int)this.yo) / s));
         t.vertexUV((double)this.x1, (double)(this.y1 + 25), 0.0, (double)((float)this.x1 / s), (double)((float)(this.y1 + 25 + (int)this.yo) / s));
         t.vertexUV((double)this.x1, (double)this.y0, 0.0, (double)((float)this.x1 / s), (double)((float)(this.y0 + (int)this.yo) / s));
         t.vertexUV((double)this.x0, (double)this.y0, 0.0, (double)((float)this.x0 / s), (double)((float)(this.y0 + (int)this.yo) / s));
      } else {
         t.vertexUV((double)this.x0, (double)this.y1, 0.0, (double)((float)this.x0 / s), (double)((float)(this.y1 + (int)this.yo) / s));
         t.vertexUV((double)this.x1, (double)this.y1, 0.0, (double)((float)this.x1 / s), (double)((float)(this.y1 + (int)this.yo) / s));
         t.vertexUV((double)this.x1, (double)this.y0, 0.0, (double)((float)this.x1 / s), (double)((float)(this.y0 + (int)this.yo) / s));
         t.vertexUV((double)this.x0, (double)this.y0, 0.0, (double)((float)this.x0 / s), (double)((float)(this.y0 + (int)this.yo) / s));
      }

      t.end();
      int rowX = this.x0 + 2;
      int rowBaseY = this.y0 + 4 - (int)this.yo;
      if (this.button != null) {
         RendererUtility.render(this.button, xm, ym);
      }

      for(int i = 0; i < itemCount; ++i) {
         int y = rowBaseY + i * this.itemHeight;
         int h = this.itemHeight - 4;
         if (y + this.itemHeight <= this.y1 && y - 4 >= this.y0) {
            if (this.isSelectedItem(i)) {
               int x0 = this.x0 + 2;
               int x1 = this.x1 - 2;
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               GL11.glDisable(3553);
               t.begin();
               t.color(8421504);
               t.vertexUV((double)x0, (double)(y + h + 2), 0.0, 0.0, 1.0);
               t.vertexUV((double)x1, (double)(y + h + 2), 0.0, 1.0, 1.0);
               t.vertexUV((double)x1, (double)(y - 2), 0.0, 1.0, 0.0);
               t.vertexUV((double)x0, (double)(y - 2), 0.0, 0.0, 0.0);
               t.color(0);
               t.vertexUV((double)(x0 + 1), (double)(y + h + 1), 0.0, 0.0, 1.0);
               t.vertexUV((double)(x1 - 1), (double)(y + h + 1), 0.0, 1.0, 1.0);
               t.vertexUV((double)(x1 - 1), (double)(y - 1), 0.0, 1.0, 0.0);
               t.vertexUV((double)(x0 + 1), (double)(y - 1), 0.0, 0.0, 0.0);
               t.end();
               GL11.glEnable(3553);
            }

            this.renderItem(i, rowX, y, h, t);
            if (itemClicked) {
               this.itemClicked(rowX, y, xm, ym);
            }
         }
      }

      GL11.glDisable(2929);
      int d = 4;
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glDisable(3553);
      t.begin();
      t.color(0, 0);
      t.vertexUV((double)this.x0, (double)(this.y0 + d), 0.0, 0.0, 1.0);
      t.vertexUV((double)this.x1, (double)(this.y0 + d), 0.0, 1.0, 1.0);
      t.color(0, 255);
      t.vertexUV((double)this.x1, (double)this.y0, 0.0, 1.0, 0.0);
      t.vertexUV((double)this.x0, (double)this.y0, 0.0, 0.0, 0.0);
      t.end();
      t.begin();
      t.color(0, 255);
      t.vertexUV((double)this.x0, (double)this.y1, 0.0, 0.0, 1.0);
      t.vertexUV((double)this.x1, (double)this.y1, 0.0, 1.0, 1.0);
      t.color(0, 0);
      t.vertexUV((double)this.x1, (double)(this.y1 - d), 0.0, 1.0, 0.0);
      t.vertexUV((double)this.x0, (double)(this.y1 - d), 0.0, 0.0, 0.0);
      t.end();
      int max = this.getMaxScroll();
      if (max > 0) {
         int barHeight = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
         if (barHeight < 32) {
            barHeight = 32;
         }

         if (barHeight > this.y1 - this.y0 - 8) {
            barHeight = this.y1 - this.y0 - 8;
         }

         int yp = (int)this.yo * (this.y1 - this.y0 - barHeight) / max + this.y0;
         if (yp < this.y0) {
            yp = this.y0;
         }

         t.begin();
         t.color(0, 255);
         t.vertexUV((double)xx0, (double)this.y1, 0.0, 0.0, 1.0);
         t.vertexUV((double)xx1, (double)this.y1, 0.0, 1.0, 1.0);
         t.vertexUV((double)xx1, (double)this.y0, 0.0, 1.0, 0.0);
         t.vertexUV((double)xx0, (double)this.y0, 0.0, 0.0, 0.0);
         t.end();
         t.begin();
         t.color(8421504, 255);
         t.vertexUV((double)xx0, (double)(yp + barHeight), 0.0, 0.0, 1.0);
         t.vertexUV((double)xx1, (double)(yp + barHeight), 0.0, 1.0, 1.0);
         t.vertexUV((double)xx1, (double)yp, 0.0, 1.0, 0.0);
         t.vertexUV((double)xx0, (double)yp, 0.0, 0.0, 0.0);
         t.end();
         t.begin();
         t.color(12632256, 255);
         t.vertexUV((double)xx0, (double)(yp + barHeight - 1), 0.0, 0.0, 1.0);
         t.vertexUV((double)(xx1 - 1), (double)(yp + barHeight - 1), 0.0, 1.0, 1.0);
         t.vertexUV((double)(xx1 - 1), (double)yp, 0.0, 1.0, 0.0);
         t.vertexUV((double)xx0, (double)yp, 0.0, 0.0, 0.0);
         t.end();
      }

      GL11.glEnable(3553);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glDisable(3042);
   }

   protected int getScrollbarPosition() {
      return this.x1 - 8;
   }
}
