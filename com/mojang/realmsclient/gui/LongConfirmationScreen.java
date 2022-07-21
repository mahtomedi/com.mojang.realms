package com.mojang.realmsclient.gui;

import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class LongConfirmationScreen extends RealmsScreen {
   private final LongConfirmationScreen.Type type;
   private final String line2;
   private final String line3;
   protected final RealmsScreen parent;
   protected final String yesButton;
   protected final String noButton;
   protected final int id;

   public LongConfirmationScreen(RealmsScreen parent, LongConfirmationScreen.Type type, String line2, String line3, int id) {
      this.parent = parent;
      this.id = id;
      this.type = type;
      this.line2 = line2;
      this.line3 = line3;
      this.yesButton = getLocalizedString("gui.yes");
      this.noButton = getLocalizedString("gui.no");
   }

   public void init() {
      this.buttonsAdd(newButton(0, this.width() / 2 - 105, this.height() / 6 + 112, 100, 20, this.yesButton));
      this.buttonsAdd(newButton(1, this.width() / 2 + 5, this.height() / 6 + 112, 100, 20, this.noButton));
   }

   protected void buttonClicked(RealmsButton button) {
      this.parent.confirmResult(button.id() == 0, this.id);
   }

   protected void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         this.parent.confirmResult(false, 1);
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.type.text, this.width() / 2, 70, this.type.colorCode);
      this.drawCenteredString(this.line2, this.width() / 2, 90, 16777215);
      this.drawCenteredString(this.line3, this.width() / 2, 110, 16777215);
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
