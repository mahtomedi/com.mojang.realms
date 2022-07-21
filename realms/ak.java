package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ak extends RealmsScreen implements r {
   private static final Logger b = LogManager.getLogger();
   private final int c = 666;
   private final int d = 667;
   private final RealmsScreen e;
   private final t f;
   private volatile String g = "";
   private volatile boolean h;
   private volatile String i;
   private volatile boolean j;
   private int k;
   private final t l;
   private final int m = 212;
   public static final String[] a = new String[]{
      "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
      "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
      "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
      "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
      "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
      "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
      "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
      "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
      "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
      "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
      "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
      "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _",
      "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
      "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
      "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
      "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _",
      "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
      "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
      "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
      "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"
   };

   public ak(RealmsScreen lastScreen, t task) {
      this.e = lastScreen;
      this.l = task;
      task.a(this);
      this.f = task;
   }

   public void a() {
      Thread thread = new Thread(this.f, "Realms-long-running-task");
      thread.setUncaughtExceptionHandler(new m(b));
      thread.start();
   }

   public void tick() {
      super.tick();
      Realms.narrateRepeatedly(this.g);
      ++this.k;
      this.l.a();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.c();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void init() {
      this.l.c();
      this.buttonsAdd(new RealmsButton(666, this.width() / 2 - 106, u.a(12), 212, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            ak.this.c();
         }
      });
   }

   private void c() {
      this.j = true;
      this.l.d();
      Realms.setScreen(this.e);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.g, this.width() / 2, u.a(3), 16777215);
      if (!this.h) {
         this.drawCenteredString(ak.a[this.k % ak.a.length], this.width() / 2, u.a(8), 8421504);
      }

      if (this.h) {
         this.drawCenteredString(this.i, this.width() / 2, u.a(8), 16711680);
      }

      super.render(xm, ym, a);
   }

   @Override
   public void a(String errorMessage) {
      this.h = true;
      this.i = errorMessage;
      Realms.narrateNow(errorMessage);
      this.buttonsClear();
      this.buttonsAdd(new RealmsButton(667, this.width() / 2 - 106, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")) {
         public void onPress() {
            ak.this.c();
         }
      });
   }

   public void b(String title) {
      this.g = title;
   }

   public boolean b() {
      return this.j;
   }
}
