package realms;

import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

public class af extends RealmsScreen {
   private final b a;
   private RealmsEditBox b;
   private RealmsEditBox c;
   private boolean d;
   private RealmsButton e;

   public af(b lastScreen) {
      this.a = lastScreen;
   }

   public void tick() {
      if (this.b != null) {
         this.b.tick();
         this.e.active(this.b());
      }

      if (this.c != null) {
         this.c.tick();
      }

   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      if (!this.d) {
         this.d = true;
         this.b = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20);
         this.focusOn(this.b);
         this.c = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20);
      }

      this.buttonsAdd(this.e = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")) {
         public void onPress() {
            af.this.a();
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(af.this.a);
         }
      });
      this.e.active(this.b());
      this.addWidget(this.b);
      this.addWidget(this.c);
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
            Realms.setScreen(this.a);
            return true;
         default:
            this.e.active(this.b());
            return false;
      }
   }

   private void a() {
      if (this.b()) {
         bh.j trialCreationTask = new bh.j(this.b.getValue(), this.c.getValue(), this.a);
         ak longRunningMcoTaskScreen = new ak(this.a, trialCreationTask);
         longRunningMcoTaskScreen.a();
         Realms.setScreen(longRunningMcoTaskScreen);
      }

   }

   private boolean b() {
      return this.b != null && this.b.getValue() != null && !this.b.getValue().trim().isEmpty();
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.trial.title"), this.width() / 2, 11, 16777215);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 10526880);
      if (this.b != null) {
         this.b.render(xm, ym, a);
      }

      if (this.c != null) {
         this.c.render(xm, ym, a);
      }

      super.render(xm, ym, a);
   }
}
