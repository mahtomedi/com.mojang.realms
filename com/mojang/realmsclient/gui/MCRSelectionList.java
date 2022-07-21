package com.mojang.realmsclient.gui;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class MCRSelectionList {
   private static final int NO_DRAG = -1;
   private static final int DRAG_OUTSIDE = -2;
   private int width;
   private int height;
   protected int y0;
   protected int y1;
   private int x1;
   private int x0;
   protected final int itemHeight;
   protected int xm;
   protected int ym;
   private float yDrag = -2.0F;
   private float yDragScale;
   private float yo;
   private int lastSelection = -1;
   private long lastSelectionTime;
   private boolean renderSelection = true;
   private boolean renderHeader;
   private int headerHeight;

   public MCRSelectionList(int width, int height, int y0, int y1, int itemHeight) {
      this.width = width;
      this.height = height;
      this.y0 = y0;
      this.y1 = y1;
      this.itemHeight = itemHeight;
      this.x0 = 0;
      this.x1 = width;
   }

   public void updateSize(int width, int height, int y0, int y1) {
      this.width = width;
      this.height = height;
      this.y0 = y0;
      this.y1 = y1;
      this.x0 = 0;
      this.x1 = width;
   }

   public void setRenderSelection(boolean renderSelection) {
      this.renderSelection = renderSelection;
   }

   protected void setRenderHeader(boolean renderHeader, int headerHeight) {
      this.renderHeader = renderHeader;
      this.headerHeight = headerHeight;
      if (!renderHeader) {
         this.headerHeight = 0;
      }

   }

   protected abstract int getNumberOfItems();

   protected abstract void selectItem(int var1, boolean var2);

   protected abstract boolean isSelectedItem(int var1);

   protected abstract boolean isMyWorld(int var1);

   protected int getMaxPosition() {
      return this.getNumberOfItems() * this.itemHeight + this.headerHeight;
   }

   protected abstract void renderBackground();

   protected abstract void renderItem(int var1, int var2, int var3, int var4, int var5, Tezzelator var6);

   protected void renderHeader(int x, int y, Tezzelator t) {
   }

   protected void clickedHeader(int headerMouseX, int headerMouseY) {
   }

   protected void renderDecorations(int mouseX, int mouseY) {
   }

   public int getItemAtPosition(int x, int y) {
      int x0 = this.width / 2 - 110;
      int x1 = this.width / 2 + 110;
      int clickSlotPos = y - this.y0 - this.headerHeight + (int)this.yo - 4;
      int slot = clickSlotPos / this.itemHeight;
      return x >= x0 && x <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getNumberOfItems() ? slot : -1;
   }

   private void capYPosition() {
      int max = this.getMaxScroll();
      if (max < 0) {
         max /= 2;
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

   public int getScroll() {
      return (int)this.yo;
   }

   public int getScrollBottom() {
      return (int)this.yo - this.height - this.headerHeight;
   }

   public void scroll(int amount) {
      this.yo += (float)amount;
      this.capYPosition();
      this.yDrag = -2.0F;
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         ;
      }
   }

   public void itemClicked(int x, int y, int xm, int ym, int width) {
   }

   protected void renderSelected(int width, int y, int h, Tezzelator t) {
      int x0 = width / 2 - 110;
      int x1 = width / 2 + 110;
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

   public void render(int xm, int ym, float a) {
      this.xm = xm;
      this.ym = ym;
      this.renderBackground();
      int itemCount = this.getNumberOfItems();
      int xx0 = this.getScrollbarPosition();
      int xx1 = xx0 + 6;
      boolean itemClicked = false;
      if (Mouse.isButtonDown(0)) {
         if (this.yDrag == -1.0F) {
            boolean doDrag = true;
            if (ym >= this.y0 && ym <= this.y1) {
               int x0 = this.width / 2 - 110;
               int x1 = this.width / 2 + 110;
               int clickSlotPos = ym - this.y0 - this.headerHeight + (int)this.yo - 4;
               int slot = clickSlotPos / this.itemHeight;
               if (xm >= x0 && xm <= xx0 && slot >= 0 && clickSlotPos >= 0 && slot < itemCount) {
                  boolean doubleClick = slot == this.lastSelection && Realms.currentTimeMillis() - this.lastSelectionTime < 250L;
                  this.selectItem(slot, doubleClick);
                  itemClicked = true;
                  this.lastSelection = slot;
                  this.lastSelectionTime = Realms.currentTimeMillis();
               } else if (xm >= x0 && xm <= x1 && clickSlotPos < 0) {
                  this.clickedHeader(xm - x0, ym - this.y0 + (int)this.yo - 4);
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
            } else {
               this.yDrag = -2.0F;
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
      t.vertexUV((double)this.x0, (double)this.y1, 0.0, (double)((float)this.x0 / s), (double)((float)(this.y1 + (int)this.yo) / s));
      t.vertexUV((double)this.x1, (double)this.y1, 0.0, (double)((float)this.x1 / s), (double)((float)(this.y1 + (int)this.yo) / s));
      t.vertexUV((double)this.x1, (double)this.y0, 0.0, (double)((float)this.x1 / s), (double)((float)(this.y0 + (int)this.yo) / s));
      t.vertexUV((double)this.x0, (double)this.y0, 0.0, (double)((float)this.x0 / s), (double)((float)(this.y0 + (int)this.yo) / s));
      t.end();
      int rowX = this.width / 2 - 92 - 16;
      int rowBaseY = this.y0 + 4 - (int)this.yo;
      if (this.renderHeader) {
         this.renderHeader(rowX, rowBaseY, t);
      }

      for(int i = 0; i < itemCount; ++i) {
         int y = rowBaseY + i * this.itemHeight + this.headerHeight;
         int h = this.itemHeight - 4;
         if (y <= this.y1 && y + h >= this.y0) {
            if (this.renderSelection && this.isSelectedItem(i)) {
               this.renderSelected(this.width, y, h, t);
            }

            this.renderItem(i, rowX, y, h, this.width, t);
            if (itemClicked) {
               this.itemClicked(rowX, y, xm, ym, this.width);
            }
         }
      }

      GL11.glDisable(2929);
      int d = 4;
      this.renderHoleBackground(0, this.y0, 255, 255);
      this.renderHoleBackground(this.y1, this.height, 255, 255);
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

      this.renderDecorations(xm, ym);
      GL11.glEnable(3553);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glDisable(3042);
   }

   protected int getScrollbarPosition() {
      return this.width / 2 + 124;
   }

   private void renderHoleBackground(int y0, int y1, int a0, int a1) {
      Tezzelator t = Tezzelator.instance;
      RealmsScreen.bind("textures/gui/options_background.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(4210752, a1);
      t.vertexUV(0.0, (double)y1, 0.0, 0.0, (double)((float)y1 / s));
      t.vertexUV((double)this.width, (double)y1, 0.0, (double)((float)this.width / s), (double)((float)y1 / s));
      t.color(4210752, a0);
      t.vertexUV((double)this.width, (double)y0, 0.0, (double)((float)this.width / s), (double)((float)y0 / s));
      t.vertexUV(0.0, (double)y0, 0.0, 0.0, (double)((float)y0 / s));
      t.end();
   }
}
