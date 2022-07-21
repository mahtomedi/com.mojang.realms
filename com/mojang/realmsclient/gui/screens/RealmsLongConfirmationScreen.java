package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsLongConfirmationScreen extends RealmsScreen {
   private final RealmsLongConfirmationScreen.Type type;
   private final String line2;
   private final String line3;
   protected final RealmsScreen parent;
   protected final String yesButton;
   protected final String noButton;
   private final String okButton;
   protected final int id;
   private final boolean yesNoQuestion;

   public RealmsLongConfirmationScreen(RealmsScreen parent, RealmsLongConfirmationScreen.Type type, String line2, String line3, boolean yesNoQuestion, int id) {
      this.parent = parent;
      this.id = id;
      this.type = type;
      this.line2 = line2;
      this.line3 = line3;
      this.yesNoQuestion = yesNoQuestion;
      this.yesButton = getLocalizedString("gui.yes");
      this.noButton = getLocalizedString("gui.no");
      this.okButton = getLocalizedString("mco.gui.ok");
   }

   public void init() {
      if (this.yesNoQuestion) {
         this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, RealmsConstants.row(8), 100, 20, this.yesButton) {
            public void onClick(double mouseX, double mouseY) {
               RealmsLongConfirmationScreen.this.parent.confirmResult(true, RealmsLongConfirmationScreen.this.id);
            }
         });
         this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, RealmsConstants.row(8), 100, 20, this.noButton) {
            public void onClick(double mouseX, double mouseY) {
               RealmsLongConfirmationScreen.this.parent.confirmResult(false, RealmsLongConfirmationScreen.this.id);
            }
         });
      } else {
         this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, RealmsConstants.row(8), 100, 20, this.okButton) {
            public void onClick(double mouseX, double mouseY) {
               RealmsLongConfirmationScreen.this.parent.confirmResult(true, RealmsLongConfirmationScreen.this.id);
            }
         });
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.parent.confirmResult(false, this.id);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.type.text, this.width() / 2, RealmsConstants.row(2), this.type.colorCode);
      this.drawCenteredString(this.line2, this.width() / 2, RealmsConstants.row(4), 16777215);
      this.drawCenteredString(this.line3, this.width() / 2, RealmsConstants.row(6), 16777215);
      super.render(xm, ym, a);
   }

   public static enum Type {
      Warning("Warning!", 16711680),
      Info("Info!", 8226750);

      public final int colorCode;
      public final String text;

      private Type(String text, int colorCode) {
         this.text = text;
         this.colorCode = colorCode;
      }
   }
}
