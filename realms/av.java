package realms;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

public class av extends RealmsScreen {
   private final ac a;
   private final RealmsServer b;
   private final int c = 212;
   private RealmsButton d;
   private RealmsEditBox e;
   private RealmsEditBox f;
   private RealmsLabel g;

   public av(ac configureWorldScreen, RealmsServer serverData) {
      this.a = configureWorldScreen;
      this.b = serverData;
   }

   public void tick() {
      this.f.tick();
      this.e.tick();
      this.d.active(this.f.getValue() != null && !this.f.getValue().trim().isEmpty());
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      int center = this.width() / 2 - 106;
      this.buttonsAdd(this.d = new RealmsButton(1, center - 2, u.a(12), 106, 20, getLocalizedString("mco.configure.world.buttons.done")) {
         public void onPress() {
            av.this.a();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 2, u.a(12), 106, 20, getLocalizedString("gui.cancel")) {
         public void onPress() {
            Realms.setScreen(av.this.a);
         }
      });
      this.buttonsAdd(
         new RealmsButton(
            5,
            this.width() / 2 - 53,
            u.a(0),
            106,
            20,
            getLocalizedString(this.b.state.equals(RealmsServer.b.b) ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open")
         ) {
            public void onPress() {
               if (av.this.b.state.equals(RealmsServer.b.b)) {
                  String line2 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line1");
                  String line3 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line2");
                  Realms.setScreen(new aj(av.this, aj.a.b, line2, line3, true, 5));
               } else {
                  av.this.a.a(false, av.this);
               }
   
            }
         }
      );
      this.f = this.newEditBox(2, center, u.a(4), 212, 20);
      this.f.setMaxLength(32);
      if (this.b.getName() != null) {
         this.f.setValue(this.b.getName());
      }

      this.addWidget(this.f);
      this.focusOn(this.f);
      this.e = this.newEditBox(3, center, u.a(8), 212, 20);
      this.e.setMaxLength(32);
      if (this.b.getDescription() != null) {
         this.e.setValue(this.b.getDescription());
      }

      this.addWidget(this.e);
      this.addWidget(this.g = new RealmsLabel(getLocalizedString("mco.configure.world.settings.title"), this.width() / 2, 17, 16777215));
      this.narrateLabels();
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public void confirmResult(boolean result, int id) {
      switch(id) {
         case 5:
            if (result) {
               this.a.a(this);
            } else {
               Realms.setScreen(this);
            }
      }
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            Realms.setScreen(this.a);
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.g.render(this);
      this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 106, u.a(3), 10526880);
      this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 106, u.a(7), 10526880);
      this.f.render(xm, ym, a);
      this.e.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   public void a() {
      this.a.a(this.f.getValue(), this.e.getValue());
   }
}
