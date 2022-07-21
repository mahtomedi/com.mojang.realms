package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ay extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final b c;
   private final RealmsServer d;
   private RealmsButton e;
   private boolean f;
   private final String g = "https://minecraft.net/realms/terms";

   public ay(RealmsScreen lastScreen, b mainScreen, RealmsServer realmsServer) {
      this.b = lastScreen;
      this.c = mainScreen;
      this.d = realmsServer;
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      int column1X = this.width() / 4;
      int columnWidth = this.width() / 4 - 2;
      int column2X = this.width() / 2 + 4;
      this.buttonsAdd(this.e = new RealmsButton(1, column1X, u.a(12), columnWidth, 20, getLocalizedString("mco.terms.buttons.agree")) {
         public void onPress() {
            ay.this.a();
         }
      });
      this.buttonsAdd(new RealmsButton(2, column2X, u.a(12), columnWidth, 20, getLocalizedString("mco.terms.buttons.disagree")) {
         public void onPress() {
            Realms.setScreen(ay.this.b);
         }
      });
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.b);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void a() {
      g client = realms.g.a();

      try {
         client.l();
         ak longRunningMcoTaskScreen = new ak(this.b, new bh.e(this.c, this.b, this.d, new ReentrantLock()));
         longRunningMcoTaskScreen.a();
         Realms.setScreen(longRunningMcoTaskScreen);
      } catch (o var3) {
         a.error("Couldn't agree to TOS");
      }

   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.f) {
         Realms.setClipboard("https://minecraft.net/realms/terms");
         bj.c("https://minecraft.net/realms/terms");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.terms.title"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.terms.sentence.1"), this.width() / 2 - 120, u.a(5), 16777215);
      int firstPartWidth = this.fontWidth(getLocalizedString("mco.terms.sentence.1"));
      int x1 = this.width() / 2 - 121 + firstPartWidth;
      int y1 = u.a(5);
      int x2 = x1 + this.fontWidth("mco.terms.sentence.2") + 1;
      int y2 = y1 + 1 + this.fontLineHeight();
      if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
         this.f = true;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, u.a(5), 7107012);
      } else {
         this.f = false;
         this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, u.a(5), 3368635);
      }

      super.render(xm, ym, a);
   }
}
