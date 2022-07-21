package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class PendingInvitationScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BACK_BUTTON_ID = 0;
   private static final int ACCEPT_BUTTON_ID = 1;
   private static final int REJECT_BUTTON_ID = 2;
   private final RealmsScreen onlineScreenLastScreen;
   private PendingInvitationScreen.PendingInvitationList pendingList;
   private List<PendingInvite> pendingInvites = Lists.newArrayList();
   private int selectedItem = -1;

   public PendingInvitationScreen(RealmsScreen onlineScreenLastScreen) {
      this.onlineScreenLastScreen = onlineScreenLastScreen;
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.pendingList = new PendingInvitationScreen.PendingInvitationList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               PendingInvitationScreen.this.pendingInvites = client.pendingInvites().pendingInvites;
            } catch (RealmsServiceException var3) {
               PendingInvitationScreen.LOGGER.error("Couldn't list invites");
            }

         }
      }).start();
      this.postInit();
   }

   private void postInit() {
      this.buttonsAdd(newButton(1, this.width() / 2 - 154, this.height() - 52, 153, 20, getLocalizedString("mco.invites.button.accept")));
      this.buttonsAdd(newButton(2, this.width() / 2 + 6, this.height() - 52, 153, 20, getLocalizedString("mco.invites.button.reject")));
      this.buttonsAdd(newButton(0, this.width() / 2 - 75, this.height() - 28, 153, 20, getLocalizedString("gui.back")));
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            this.accept();
         } else if (button.id() == 0) {
            Realms.setScreen(new RealmsMainScreen(this.onlineScreenLastScreen));
         } else if (button.id() == 2) {
            this.reject();
         } else {
            this.pendingList.buttonClicked(button);
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(this.onlineScreenLastScreen);
      }

   }

   private void reject() {
      if (this.selectedItem >= 0 && this.selectedItem < this.pendingInvites.size()) {
         (new Thread("Realms-reject-invitation") {
               public void run() {
                  try {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     client.rejectInvitation(
                        ((PendingInvite)PendingInvitationScreen.this.pendingInvites.get(PendingInvitationScreen.this.selectedItem)).invitationId
                     );
                     PendingInvitationScreen.this.updateSelectedItemPointer();
                  } catch (RealmsServiceException var2) {
                     PendingInvitationScreen.LOGGER.error("Couldn't reject invite");
                  }
   
               }
            })
            .start();
      }

   }

   private void accept() {
      if (this.selectedItem >= 0 && this.selectedItem < this.pendingInvites.size()) {
         (new Thread("Realms-accept-invitation") {
               public void run() {
                  try {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     client.acceptInvitation(
                        ((PendingInvite)PendingInvitationScreen.this.pendingInvites.get(PendingInvitationScreen.this.selectedItem)).invitationId
                     );
                     PendingInvitationScreen.this.updateSelectedItemPointer();
                  } catch (RealmsServiceException var2) {
                     PendingInvitationScreen.LOGGER.error("Couldn't accept invite");
                  }
   
               }
            })
            .start();
      }

   }

   private void updateSelectedItemPointer() {
      int originalIndex = this.selectedItem;
      if (this.pendingInvites.size() - 1 == this.selectedItem) {
         --this.selectedItem;
      }

      this.pendingInvites.remove(originalIndex);
      if (this.pendingInvites.size() == 0) {
         this.selectedItem = -1;
         Realms.setScreen(new RealmsMainScreen(this.onlineScreenLastScreen));
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.pendingList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.invites.title"), this.width() / 2, 20, 16777215);
      super.render(xm, ym, a);
   }

   private class PendingInvitationList extends MCRSelectionList {
      public PendingInvitationList() {
         super(PendingInvitationScreen.this.width(), PendingInvitationScreen.this.height(), 32, PendingInvitationScreen.this.height() - 64, 36);
      }

      @Override
      protected int getNumberOfItems() {
         return PendingInvitationScreen.this.pendingInvites.size() + 1;
      }

      @Override
      protected void selectItem(int item, boolean doubleClick) {
         if (item < PendingInvitationScreen.this.pendingInvites.size()) {
            PendingInvitationScreen.this.selectedItem = item;
         }
      }

      @Override
      protected boolean isSelectedItem(int item) {
         return item == PendingInvitationScreen.this.selectedItem;
      }

      @Override
      protected boolean isMyWorld(int item) {
         return false;
      }

      @Override
      protected int getMaxPosition() {
         return this.getNumberOfItems() * 36;
      }

      @Override
      protected void renderBackground() {
         PendingInvitationScreen.this.renderBackground();
      }

      @Override
      protected void renderItem(int i, int x, int y, int h, int width, Tezzelator t) {
         if (i < PendingInvitationScreen.this.pendingInvites.size()) {
            this.renderPendingInvitationItem(i, x, y, h, t);
         }

      }

      private void renderPendingInvitationItem(int i, int x, int y, int h, Tezzelator t) {
         PendingInvite invite = (PendingInvite)PendingInvitationScreen.this.pendingInvites.get(i);
         PendingInvitationScreen.this.drawString(invite.worldName, x + 2, y + 1, 16777215);
         PendingInvitationScreen.this.drawString(invite.worldOwnerName, x + 2, y + 12, 7105644);
      }
   }
}
