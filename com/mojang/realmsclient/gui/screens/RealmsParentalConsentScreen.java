package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsParentalConsentScreen extends RealmsScreen {
   private final RealmsScreen nextScreen;
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_OK_ID = 1;
   private static final int BUTTON_LINK_ID = 1;

   public RealmsParentalConsentScreen(RealmsScreen nextScreen) {
      this.nextScreen = nextScreen;
   }

   public void init() {
      Realms.narrateNow(getLocalizedString("mco.account.privacyinfo"));
      String updateAccount = getLocalizedString("mco.account.update");
      String back = getLocalizedString("gui.back");
      int buttonWidth = Math.max(this.fontWidth(updateAccount), this.fontWidth(back)) + 30;
      String linkText = getLocalizedString("mco.account.privacy.info");
      int linkWidth = (int)((double)this.fontWidth(linkText) * 1.2);
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 - linkWidth / 2, RealmsConstants.row(11), linkWidth, 20, linkText) {
         public void onPress() {
            RealmsUtil.browseTo("https://minecraft.net/privacy/gdpr/");
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 - (buttonWidth + 5), RealmsConstants.row(13), buttonWidth, 20, updateAccount) {
         public void onPress() {
            RealmsUtil.browseTo("https://minecraft.net/update-account");
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 5, RealmsConstants.row(13), buttonWidth, 20, back) {
         public void onPress() {
            Realms.setScreen(RealmsParentalConsentScreen.this.nextScreen);
         }
      });
   }

   public void tick() {
      super.tick();
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      return super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      List<String> translatedLines = this.getLocalizedStringWithLineWidth("mco.account.privacyinfo", (int)Math.round((double)this.width() * 0.9));
      int y = 15;

      for(String line : translatedLines) {
         this.drawCenteredString(line, this.width() / 2, y, 16777215);
         y += 15;
      }

      super.render(xm, ym, a);
   }
}
