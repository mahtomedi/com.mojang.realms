package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsScrolledSelectionList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsPendingInvitesScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BUTTON_BACK_ID = 0;
   private static final int BUTTON_ACCEPT_ID = 1;
   private static final int BUTTON_REJECT_ID = 2;
   private final RealmsScreen lastScreen;
   private RealmsButton acceptButton;
   private RealmsButton rejectButton;
   private RealmsPendingInvitesScreen.PendingInvitationList pendingList;
   private List<PendingInvite> pendingInvites = Lists.newArrayList();
   private int selectedItem = -1;

   public RealmsPendingInvitesScreen(RealmsScreen lastScreen) {
      this.lastScreen = lastScreen;
   }

   public void mouseEvent() {
      super.mouseEvent();
      this.pendingList.mouseEvent();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.pendingList = new RealmsPendingInvitesScreen.PendingInvitationList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               RealmsPendingInvitesScreen.this.pendingInvites = client.pendingInvites().pendingInvites;
               if (RealmsPendingInvitesScreen.this.pendingInvites.size() == 1) {
                  RealmsPendingInvitesScreen.this.selectedItem = 0;
                  RealmsPendingInvitesScreen.this.changeButtonState(true);
               }
            } catch (RealmsServiceException var3) {
               RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
            }

         }
      }).start();
      this.postInit();
   }

   private void postInit() {
      this.buttonsAdd(this.acceptButton = newButton(1, this.width() / 2 - 154, this.height() - 57, 153, 20, getLocalizedString("mco.invites.button.accept")));
      this.buttonsAdd(this.rejectButton = newButton(2, this.width() / 2 + 6, this.height() - 57, 153, 20, getLocalizedString("mco.invites.button.reject")));
      this.buttonsAdd(newButton(0, this.width() / 2 - 75, this.height() - 32, 153, 20, getLocalizedString("gui.back")));
      this.changeButtonState(false);
   }

   private void changeButtonState(boolean active) {
      this.acceptButton.active(active);
      this.rejectButton.active(active);
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         switch(button.id()) {
            case 0:
               Realms.setScreen(new RealmsMainScreen(this.lastScreen));
            default:
               return;
            case 1:
               this.accept();
               break;
            case 2:
               this.reject();
         }

      }
   }

   public void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         Realms.setScreen(new RealmsMainScreen(this.lastScreen));
      }

   }

   private void reject() {
      if (this.selectedItem >= 0 && this.selectedItem < this.pendingInvites.size()) {
         (new Thread("Realms-reject-invitation") {
               public void run() {
                  try {
                     RealmsClient client = RealmsClient.createRealmsClient();
                     client.rejectInvitation(
                        ((PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(RealmsPendingInvitesScreen.this.selectedItem)).invitationId
                     );
                     RealmsPendingInvitesScreen.this.updateSelectedItemPointer();
                  } catch (RealmsServiceException var2) {
                     RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
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
                        ((PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(RealmsPendingInvitesScreen.this.selectedItem)).invitationId
                     );
                     RealmsPendingInvitesScreen.this.updateSelectedItemPointer();
                  } catch (RealmsServiceException var2) {
                     RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
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
         Realms.setScreen(new RealmsMainScreen(this.lastScreen));
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.pendingList.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
      super.render(xm, ym, a);
   }

   private class PendingInvitationList extends RealmsScrolledSelectionList {
      public PendingInvitationList() {
         super(RealmsPendingInvitesScreen.this.width(), RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 64, 36);
      }

      public int getItemCount() {
         return RealmsPendingInvitesScreen.this.pendingInvites.size() + 1;
      }

      public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
         if (item < RealmsPendingInvitesScreen.this.pendingInvites.size()) {
            RealmsPendingInvitesScreen.this.selectedItem = item;
            RealmsPendingInvitesScreen.this.changeButtonState(true);
         }
      }

      public boolean isSelectedItem(int item) {
         return item == RealmsPendingInvitesScreen.this.selectedItem;
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground() {
         RealmsPendingInvitesScreen.this.renderBackground();
      }

      public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
         if (i < RealmsPendingInvitesScreen.this.pendingInvites.size()) {
            this.renderPendingInvitationItem(i, x, y, h);
         }

      }

      private void renderPendingInvitationItem(int i, int x, int y, int h) {
         PendingInvite invite = (PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(i);
         RealmsPendingInvitesScreen.this.drawString(invite.worldName, x + 2, y + 1, 16777215);
         RealmsPendingInvitesScreen.this.drawString(invite.worldOwnerName, x + 2, y + 12, 7105644);
      }
   }
}
