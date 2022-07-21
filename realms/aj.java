package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class aj extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private RealmsEditBox b;
   private final RealmsServer c;
   private final ad d;
   private final RealmsScreen e;
   private final int f = 0;
   private final int g = 1;
   private RealmsButton h;
   private final int i = 2;
   private String j;
   private boolean k;

   public aj(ad configureScreen, RealmsScreen lastScreen, RealmsServer serverData) {
      this.d = configureScreen;
      this.e = lastScreen;
      this.c = serverData;
   }

   public void tick() {
      this.b.tick();
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(this.h = new RealmsButton(0, this.width() / 2 - 100, u.a(10), getLocalizedString("mco.configure.world.buttons.invite")) {
         public void onPress() {
            aj.this.a();
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 - 100, u.a(12), getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(aj.this.e);
         }
      });
      this.b = this.newEditBox(2, this.width() / 2 - 100, u.a(2), 200, 20, getLocalizedString("mco.configure.world.invite.profile.name"));
      this.focusOn(this.b);
      this.addWidget(this.b);
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   private void a() {
      g client = realms.g.a();
      if (this.b.getValue() != null && !this.b.getValue().isEmpty()) {
         try {
            RealmsServer realmsServer = client.b(this.c.id, this.b.getValue().trim());
            if (realmsServer != null) {
               this.c.players = realmsServer.players;
               Realms.setScreen(new ap(this.d, this.c));
            } else {
               this.a(getLocalizedString("mco.configure.world.players.error"));
            }
         } catch (Exception var3) {
            a.error("Couldn't invite user");
            this.a(getLocalizedString("mco.configure.world.players.error"));
         }

      }
   }

   private void a(String errorMsg) {
      this.k = true;
      this.j = errorMsg;
      Realms.narrateNow(errorMsg);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.e);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawString(getLocalizedString("mco.configure.world.invite.profile.name"), this.width() / 2 - 100, u.a(1), 10526880);
      if (this.k) {
         this.drawCenteredString(this.j, this.width() / 2, u.a(5), 16711680);
      }

      this.b.render(xm, ym, a);
      super.render(xm, ym, a);
   }
}
