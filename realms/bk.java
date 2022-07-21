package realms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.annotation.Nullable;

public class bk {
   private int[] a;
   private int b;
   private int c;

   @Nullable
   public BufferedImage a(BufferedImage image) {
      if (image == null) {
         return null;
      } else {
         this.b = 64;
         this.c = 64;
         BufferedImage out = new BufferedImage(this.b, this.c, 2);
         Graphics outGraphics = out.getGraphics();
         outGraphics.drawImage(image, 0, 0, null);
         boolean isLegacy = image.getHeight() == 32;
         if (isLegacy) {
            outGraphics.setColor(new Color(0, 0, 0, 0));
            outGraphics.fillRect(0, 32, 64, 32);
            outGraphics.drawImage(out, 24, 48, 20, 52, 4, 16, 8, 20, null);
            outGraphics.drawImage(out, 28, 48, 24, 52, 8, 16, 12, 20, null);
            outGraphics.drawImage(out, 20, 52, 16, 64, 8, 20, 12, 32, null);
            outGraphics.drawImage(out, 24, 52, 20, 64, 4, 20, 8, 32, null);
            outGraphics.drawImage(out, 28, 52, 24, 64, 0, 20, 4, 32, null);
            outGraphics.drawImage(out, 32, 52, 28, 64, 12, 20, 16, 32, null);
            outGraphics.drawImage(out, 40, 48, 36, 52, 44, 16, 48, 20, null);
            outGraphics.drawImage(out, 44, 48, 40, 52, 48, 16, 52, 20, null);
            outGraphics.drawImage(out, 36, 52, 32, 64, 48, 20, 52, 32, null);
            outGraphics.drawImage(out, 40, 52, 36, 64, 44, 20, 48, 32, null);
            outGraphics.drawImage(out, 44, 52, 40, 64, 40, 20, 44, 32, null);
            outGraphics.drawImage(out, 48, 52, 44, 64, 52, 20, 56, 32, null);
         }

         outGraphics.dispose();
         this.a = ((DataBufferInt)out.getRaster().getDataBuffer()).getData();
         this.b(0, 0, 32, 16);
         if (isLegacy) {
            this.a(32, 0, 64, 32);
         }

         this.b(0, 16, 64, 32);
         this.b(16, 48, 48, 64);
         return out;
      }
   }

   private void a(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            int pix = this.a[x + y * this.b];
            if ((pix >> 24 & 0xFF) < 128) {
               return;
            }
         }
      }

      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            this.a[x + y * this.b] &= 16777215;
         }
      }

   }

   private void b(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            this.a[x + y * this.b] |= -16777216;
         }
      }

   }
}
