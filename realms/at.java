package realms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class at extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final aq b;
   private final long c;
   private final int d;
   private RealmsButton e;
   private final DateFormat f = new SimpleDateFormat();
   private List<RealmsLevelSummary> g = new ArrayList();
   private int h = -1;
   private at.b i;
   private String j;
   private String k;
   private final String[] l = new String[4];
   private RealmsLabel m;
   private RealmsLabel n;

   public at(long worldId, int slotId, aq lastScreen) {
      this.b = lastScreen;
      this.c = worldId;
      this.d = slotId;
   }

   private void a() throws Exception {
      RealmsAnvilLevelStorageSource levelSource = this.getLevelStorageSource();
      this.g = levelSource.getLevelList();
      Collections.sort(this.g);

      for(RealmsLevelSummary summary : this.g) {
         this.i.a(summary);
      }

   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.i = new at.b();

      try {
         this.a();
      } catch (Exception var2) {
         a.error("Couldn't load level list", var2);
         Realms.setScreen(new ah("Unable to load worlds", var2.getMessage(), this.b));
         return;
      }

      this.j = getLocalizedString("selectWorld.world");
      this.k = getLocalizedString("selectWorld.conversion");
      this.l[Realms.survivalId()] = getLocalizedString("gameMode.survival");
      this.l[Realms.creativeId()] = getLocalizedString("gameMode.creative");
      this.l[Realms.adventureId()] = getLocalizedString("gameMode.adventure");
      this.l[Realms.spectatorId()] = getLocalizedString("gameMode.spectator");
      this.addWidget(this.i);
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 6, this.height() - 32, 153, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(at.this.b);
         }
      });
      this.buttonsAdd(this.e = new RealmsButton(2, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.upload.button.name")) {
         public void onPress() {
            at.this.b();
         }
      });
      this.e.active(this.h >= 0 && this.h < this.g.size());
      this.addWidget(this.m = new RealmsLabel(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 13, 16777215));
      this.addWidget(this.n = new RealmsLabel(getLocalizedString("mco.upload.select.world.subtitle"), this.width() / 2, u.a(-1), 10526880));
      this.narrateLabels();
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   private void b() {
      if (this.h != -1 && !((RealmsLevelSummary)this.g.get(this.h)).isHardcore()) {
         RealmsLevelSummary selectedLevel = (RealmsLevelSummary)this.g.get(this.h);
         Realms.setScreen(new az(this.c, this.d, this.b, selectedLevel));
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.i.render(xm, ym, a);
      this.m.render(this);
      this.n.render(this);
      if (this.g.size() == 0) {
         this.drawCenteredString(getLocalizedString("mco.upload.select.world.none"), this.width() / 2, this.height() / 2 - 20, 16777215);
      }

      super.render(xm, ym, a);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.b);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void tick() {
      super.tick();
   }

   class a extends RealmListEntry {
      final RealmsLevelSummary a;

      public a(RealmsLevelSummary levelSummary) {
         this.a = levelSummary;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, index, rowLeft, rowTop, rowHeight, Tezzelator.instance, mouseX, mouseY);
      }

      public boolean mouseClicked(double x, double y, int buttonNum) {
         at.this.i.selectItem(at.this.g.indexOf(this.a));
         return true;
      }

      protected void a(RealmsLevelSummary levelSummary, int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         String name = levelSummary.getLevelName();
         if (name == null || name.isEmpty()) {
            name = at.this.j + " " + (i + 1);
         }

         String id = levelSummary.getLevelId();
         id = id + " (" + at.this.f.format(new Date(levelSummary.getLastPlayed()));
         id = id + ")";
         String info = "";
         if (levelSummary.isRequiresConversion()) {
            info = at.this.k + " " + info;
         } else {
            info = at.this.l[levelSummary.getGameMode()];
            if (levelSummary.isHardcore()) {
               info = q.e + RealmsScreen.getLocalizedString("mco.upload.hardcore") + q.v;
            }

            if (levelSummary.hasCheats()) {
               info = info + ", " + RealmsScreen.getLocalizedString("selectWorld.cheats");
            }
         }

         at.this.drawString(name, x + 2, y + 1, 16777215);
         at.this.drawString(id, x + 2, y + 12, 8421504);
         at.this.drawString(info, x + 2, y + 12 + 10, 8421504);
      }
   }

   class b extends RealmsObjectSelectionList {
      public b() {
         super(at.this.width(), at.this.height(), u.a(0), at.this.height() - 40, 36);
      }

      public void a(RealmsLevelSummary levelSummary) {
         this.addEntry(at.this.new a(levelSummary));
      }

      public int getItemCount() {
         return at.this.g.size();
      }

      public int getMaxPosition() {
         return at.this.g.size() * 36;
      }

      public boolean isFocused() {
         return at.this.isFocused(this);
      }

      public void renderBackground() {
         at.this.renderBackground();
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((RealmsLevelSummary)at.this.g.get(item)).getLevelName()}));
         }

         at.this.h = item;
         at.this.e.active(at.this.h >= 0 && at.this.h < this.getItemCount() && !((RealmsLevelSummary)at.this.g.get(at.this.h)).isHardcore());
      }
   }
}
