package realms;

import com.mojang.realmsclient.dto.Backup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.realms.Tezzelator;

public class z extends RealmsScreen {
   private final RealmsScreen c;
   private final int d = 0;
   private final Backup e;
   private final List<String> f = new ArrayList();
   private z.a g;
   String[] a = new String[]{
      getLocalizedString("options.difficulty.peaceful"),
      getLocalizedString("options.difficulty.easy"),
      getLocalizedString("options.difficulty.normal"),
      getLocalizedString("options.difficulty.hard")
   };
   String[] b = new String[]{
      getLocalizedString("selectWorld.gameMode.survival"),
      getLocalizedString("selectWorld.gameMode.creative"),
      getLocalizedString("selectWorld.gameMode.adventure")
   };

   public z(RealmsScreen lastScreen, Backup backup) {
      this.c = lastScreen;
      this.e = backup;
      if (backup.changeList != null) {
         for(Entry<String, String> entry : backup.changeList.entrySet()) {
            this.f.add(entry.getKey());
         }
      }

   }

   public void tick() {
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 24, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(z.this.c);
         }
      });
      this.g = new z.a();
      this.addWidget(this.g);
      this.focusOn(this.g);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.c);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString("Changes from last backup", this.width() / 2, 10, 16777215);
      this.g.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   private String a(String key, String value) {
      String k = key.toLowerCase(Locale.ROOT);
      if (k.contains("game") && k.contains("mode")) {
         return this.b(value);
      } else {
         return k.contains("game") && k.contains("difficulty") ? this.a(value) : value;
      }
   }

   private String a(String value) {
      try {
         return this.a[Integer.parseInt(value)];
      } catch (Exception var3) {
         return "UNKNOWN";
      }
   }

   private String b(String value) {
      try {
         return this.b[Integer.parseInt(value)];
      } catch (Exception var3) {
         return "UNKNOWN";
      }
   }

   class a extends RealmsSimpleScrolledSelectionList {
      public a() {
         super(z.this.width(), z.this.height(), 32, z.this.height() - 64, 36);
      }

      public int getItemCount() {
         return z.this.e.changeList.size();
      }

      public boolean isSelectedItem(int item) {
         return false;
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         String key = (String)z.this.f.get(i);
         z.this.drawString(key, this.width() / 2 - 40, y, 10526880);
         String metadataValue = (String)z.this.e.changeList.get(key);
         z.this.drawString(z.this.a(key, metadataValue), this.width() / 2 - 40, y + 12, 16777215);
      }
   }
}
