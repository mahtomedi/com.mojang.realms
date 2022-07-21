package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

public class RealmsResetNormalWorldScreen extends RealmsScreen {
   private final RealmsResetWorldScreen lastScreen;
   private RealmsEditBox seedEdit;
   private Boolean generateStructures = true;
   private Integer levelTypeIndex = 0;
   String[] levelTypes;
   private final int BUTTON_CANCEL_ID = 0;
   private final int BUTTON_RESET_ID = 1;
   private static final int BUTTON_LEVEL_TYPE_ID = 2;
   private static final int BUTTON_GENERATE_STRUCTURES_ID = 3;
   private final int SEED_EDIT_BOX = 4;
   private RealmsButton resetButton;
   private RealmsButton levelTypeButton;
   private RealmsButton generateStructuresButton;
   private String buttonTitle = getLocalizedString("mco.backup.button.reset");

   public RealmsResetNormalWorldScreen(RealmsResetWorldScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public RealmsResetNormalWorldScreen(RealmsResetWorldScreen lastScreen, String buttonTitle) {
      this(lastScreen);
      this.buttonTitle = buttonTitle;
   }

   public void tick() {
      this.seedEdit.tick();
      super.tick();
   }

   public void init() {
      this.levelTypes = new String[]{
         getLocalizedString("generator.default"),
         getLocalizedString("generator.flat"),
         getLocalizedString("generator.largeBiomes"),
         getLocalizedString("generator.amplified")
      };
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 8, RealmsConstants.row(12), 97, 20, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            Realms.setScreen(RealmsResetNormalWorldScreen.this.lastScreen);
         }
      });
      this.buttonsAdd(this.resetButton = new RealmsButton(1, this.width() / 2 - 102, RealmsConstants.row(12), 97, 20, this.buttonTitle) {
         public void onClick(double mouseX, double mouseY) {
            RealmsResetNormalWorldScreen.this.onReset();
         }
      });
      this.seedEdit = this.newEditBox(4, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20);
      this.seedEdit.setMaxLength(32);
      this.seedEdit.setValue("");
      this.addWidget(this.seedEdit);
      this.focusOn(this.seedEdit);
      this.buttonsAdd(
         this.levelTypeButton = new RealmsButton(2, this.width() / 2 - 102, RealmsConstants.row(4), 205, 20, this.levelTypeTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsResetNormalWorldScreen.this.levelTypeIndex = (RealmsResetNormalWorldScreen.this.levelTypeIndex + 1)
                  % RealmsResetNormalWorldScreen.this.levelTypes.length;
               this.msg(RealmsResetNormalWorldScreen.this.levelTypeTitle());
            }
         }
      );
      this.buttonsAdd(
         this.generateStructuresButton = new RealmsButton(3, this.width() / 2 - 102, RealmsConstants.row(6) - 2, 205, 20, this.generateStructuresTitle()) {
            public void onClick(double mouseX, double mouseY) {
               RealmsResetNormalWorldScreen.this.generateStructures = !RealmsResetNormalWorldScreen.this.generateStructures;
               this.msg(RealmsResetNormalWorldScreen.this.generateStructuresTitle());
            }
         }
      );
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 257 || eventKey == 335) {
         this.onReset();
         return true;
      } else if (eventKey == 256) {
         Realms.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void onReset() {
      this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures));
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.reset.world.generate"), this.width() / 2, 17, 16777215);
      this.drawString(getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, RealmsConstants.row(1), 10526880);
      this.seedEdit.render(xm, ym, a);
      super.render(xm, ym, a);
   }

   private String levelTypeTitle() {
      String levelType = getLocalizedString("selectWorld.mapType");
      return levelType + " " + this.levelTypes[this.levelTypeIndex];
   }

   private String generateStructuresTitle() {
      return getLocalizedString("selectWorld.mapFeatures")
         + " "
         + getLocalizedString(this.generateStructures ? "mco.configure.world.on" : "mco.configure.world.off");
   }
}
