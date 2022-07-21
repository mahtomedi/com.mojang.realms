package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ax extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final RealmsServer c;
   private final RealmsScreen d;
   private final int e = 0;
   private final int f = 1;
   private final int g = 2;
   private final String h;
   private final String i;
   private final String j;
   private final String k;
   private int l;
   private String m;
   private Subscription.a n;
   private final String o = "https://account.mojang.com/buy/realms";

   public ax(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen mainScreen) {
      this.b = lastScreen;
      this.c = serverData;
      this.d = mainScreen;
      this.h = getLocalizedString("mco.configure.world.subscription.title");
      this.i = getLocalizedString("mco.configure.world.subscription.start");
      this.j = getLocalizedString("mco.configure.world.subscription.timeleft");
      this.k = getLocalizedString("mco.configure.world.subscription.recurring.daysleft");
   }

   public void init() {
      this.a(this.c.id);
      Realms.narrateNow(new String[]{this.h, this.i, this.m, this.j, this.a(this.l)});
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(new RealmsButton(2, this.width() / 2 - 100, u.a(6), getLocalizedString("mco.configure.world.subscription.extend")) {
         public void onPress() {
            String extensionUrl = "https://account.mojang.com/buy/realms?sid=" + ax.this.c.remoteSubscriptionId + "&pid=" + Realms.getUUID();
            Realms.setClipboard(extensionUrl);
            bj.c(extensionUrl);
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, u.a(12), getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(ax.this.b);
         }
      });
      if (this.c.expired) {
         this.buttonsAdd(new RealmsButton(1, this.width() / 2 - 100, u.a(10), getLocalizedString("mco.configure.world.delete.button")) {
            public void onPress() {
               String line2 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line1");
               String line3 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line2");
               Realms.setScreen(new aj(ax.this, aj.a.a, line2, line3, true, 1));
            }
         });
      }

   }

   private void a(long worldId) {
      g client = realms.g.a();

      try {
         Subscription subscription = client.h(worldId);
         this.l = subscription.daysLeft;
         this.m = this.b(subscription.startDate);
         this.n = subscription.type;
      } catch (o var5) {
         a.error("Couldn't get subscription");
         Realms.setScreen(new ah(var5, this.b));
      } catch (IOException var6) {
         a.error("Couldn't parse response subscribing");
      }

   }

   public void confirmResult(boolean result, int id) {
      if (id == 1 && result) {
         (new Thread("Realms-delete-realm") {
            public void run() {
               try {
                  g client = realms.g.a();
                  client.i(ax.this.c.id);
               } catch (o var2) {
                  ax.a.error("Couldn't delete world");
                  ax.a.error(var2);
               } catch (IOException var3) {
                  ax.a.error("Couldn't delete world");
                  var3.printStackTrace();
               }

               Realms.setScreen(ax.this.d);
            }
         }).start();
      }

      Realms.setScreen(this);
   }

   private String b(long cetTime) {
      Calendar cal = new GregorianCalendar(TimeZone.getDefault());
      cal.setTimeInMillis(cetTime);
      return DateFormat.getDateTimeInstance().format(cal.getTime());
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

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      int center = this.width() / 2 - 100;
      this.drawCenteredString(this.h, this.width() / 2, 17, 16777215);
      this.drawString(this.i, center, u.a(0), 10526880);
      this.drawString(this.m, center, u.a(1), 16777215);
      if (this.n == Subscription.a.a) {
         this.drawString(this.j, center, u.a(3), 10526880);
      } else if (this.n == Subscription.a.b) {
         this.drawString(this.k, center, u.a(3), 10526880);
      }

      this.drawString(this.a(this.l), center, u.a(4), 16777215);
      super.render(xm, ym, a);
   }

   private String a(int daysLeft) {
      if (daysLeft == -1 && this.c.expired) {
         return getLocalizedString("mco.configure.world.subscription.expired");
      } else if (daysLeft <= 1) {
         return getLocalizedString("mco.configure.world.subscription.less_than_a_day");
      } else {
         int months = daysLeft / 30;
         int days = daysLeft % 30;
         StringBuilder sb = new StringBuilder();
         if (months > 0) {
            sb.append(months).append(" ");
            if (months == 1) {
               sb.append(getLocalizedString("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
            } else {
               sb.append(getLocalizedString("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
            }
         }

         if (days > 0) {
            if (sb.length() > 0) {
               sb.append(", ");
            }

            sb.append(days).append(" ");
            if (days == 1) {
               sb.append(getLocalizedString("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
            } else {
               sb.append(getLocalizedString("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
            }
         }

         return sb.toString();
      }
   }
}
