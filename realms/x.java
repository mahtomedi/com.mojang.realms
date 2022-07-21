package realms;

import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.RealmsObjectSelectionList;

public abstract class x {
   public final int a;
   public final int b;
   public final int c;
   public final int d;

   public x(int width, int height, int xOffset, int yOffset) {
      this.a = width;
      this.b = height;
      this.c = xOffset;
      this.d = yOffset;
   }

   public void a(int rowX, int rowY, int mouseX, int mouseY) {
      int buttonX = rowX + this.c;
      int buttonY = rowY + this.d;
      boolean hovered = false;
      if (mouseX >= buttonX && mouseX <= buttonX + this.a && mouseY >= buttonY && mouseY <= buttonY + this.b) {
         hovered = true;
      }

      this.a(buttonX, buttonY, hovered);
   }

   protected abstract void a(int var1, int var2, boolean var3);

   public int a() {
      return this.c + this.a;
   }

   public int b() {
      return this.d + this.b;
   }

   public abstract void a(int var1);

   public static void a(List<x> buttons, RealmsObjectSelectionList list, int x, int y, int mouseX, int mouseY) {
      for(x rowButton : buttons) {
         if (list.getRowWidth() > rowButton.a()) {
            rowButton.a(x, y, mouseX, mouseY);
         }
      }

   }

   public static void a(RealmsObjectSelectionList list, RealmListEntry entry, List<x> buttons, int mouseButtonNum, double mouseX, double mouseY) {
      if (mouseButtonNum == 0) {
         int index = list.children().indexOf(entry);
         if (index > -1) {
            list.selectItem(index);
            int x0 = list.getRowLeft();
            int y0 = list.getRowTop(index);
            int dx = (int)(mouseX - (double)x0);
            int dy = (int)(mouseY - (double)y0);

            for(x rowButton : buttons) {
               if (dx >= rowButton.c && dx <= rowButton.a() && dy >= rowButton.d && dy <= rowButton.b()) {
                  rowButton.a(index);
               }
            }
         }
      }

   }
}
