package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

public class ae extends RealmsScreen {
   private final RealmsServer a;
   private final b b;
   private RealmsEditBox c;
   private RealmsEditBox d;
   private RealmsButton e;
   private RealmsLabel f;

   public ae(RealmsServer server, b lastScreen) {
      this.a = server;
      this.b = lastScreen;
   }

   public void tick() {
      if (this.c != null) {
         this.c.tick();
      }

      if (this.d != null) {
         this.d.tick();
      }

   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(this.e = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")) {
         public void onPress() {
            ae.this.a();
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(ae.this.b);
         }
      });
      this.e.active(false);
      this.c = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20);
      this.addWidget(this.c);
      this.focusOn(this.c);
      this.d = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20);
      this.addWidget(this.d);
      this.f = new RealmsLabel(getLocalizedString("mco.selectServer.create"), this.width() / 2, 11, 16777215);
      this.addWidget(this.f);
      this.narrateLabels();
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean charTyped(char character, int mods) {
      this.e.active(this.b());
      return false;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.b);
            return true;
         default:
            this.e.active(this.b());
            return false;
      }
   }

   private void a() {
      if (this.b()) {
         aq resetWorldScreen = new aq(
            this.b,
            this.a,
            this.b.f(),
            getLocalizedString("mco.selectServer.create"),
            getLocalizedString("mco.create.world.subtitle"),
            10526880,
            getLocalizedString("mco.create.world.skip")
         );
         resetWorldScreen.a(getLocalizedString("mco.create.world.reset.title"));
         bh.k worldCreationTask = new bh.k(this.a.id, this.c.getValue(), this.d.getValue(), resetWorldScreen);
         ak longRunningMcoTaskScreen = new ak(this.b, worldCreationTask);
         longRunningMcoTaskScreen.a();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean b() {
      return this.c.getValue() != null && !this.c.getValue().trim().isEmpty();
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.f.render(this);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 10526880);
      if (this.c != null) {
         this.c.render(xm, ym, a);
      }

      if (this.d != null) {
         this.d.render(xm, ym, a);
      }

      super.render(xm, ym, a);
   }
}
