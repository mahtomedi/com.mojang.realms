package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsButtonProxy;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;

public class RealmsWorldSlotButton extends RealmsButton {
   private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
   private static final String EMPTY_SLOT_LOCATION = "realms:textures/gui/realms/empty_frame.png";
   private final Supplier<RealmsServer> serverDataProvider;
   private final Consumer<String> toolTipSetter;
   private final RealmsWorldSlotButton.Listener listener;
   private final int slotIndex;
   private int animTick;
   private RealmsWorldSlotButton.State state;

   public RealmsWorldSlotButton(
      int x,
      int y,
      int width,
      int height,
      Supplier<RealmsServer> serverDataProvider,
      Consumer<String> toolTipSetter,
      int id,
      int slotIndex,
      RealmsWorldSlotButton.Listener listener
   ) {
      super(id, x, y, width, height, "");
      this.serverDataProvider = serverDataProvider;
      this.slotIndex = slotIndex;
      this.toolTipSetter = toolTipSetter;
      this.listener = listener;
   }

   public void render(int xm, int ym, float a) {
      super.render(xm, ym, a);
   }

   public void tick() {
      ++this.animTick;
      RealmsServer serverData = (RealmsServer)this.serverDataProvider.get();
      if (serverData != null) {
         RealmsWorldOptions slotOptions = (RealmsWorldOptions)serverData.slots.get(this.slotIndex);
         boolean minigame = this.slotIndex == 4;
         boolean currentlyActiveSlot;
         String slotName;
         long imageId;
         String image;
         boolean empty;
         if (minigame) {
            currentlyActiveSlot = serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
            slotName = "Minigame";
            imageId = (long)serverData.minigameId;
            image = serverData.minigameImage;
            empty = serverData.minigameId == -1;
         } else {
            currentlyActiveSlot = serverData.activeSlot == this.slotIndex && !serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
            slotName = slotOptions.getSlotName(this.slotIndex);
            imageId = slotOptions.templateId;
            image = slotOptions.templateImage;
            empty = slotOptions.empty;
         }

         String actionPrompt = null;
         RealmsWorldSlotButton.Action action;
         if (currentlyActiveSlot) {
            boolean inAcceptState = serverData.state == RealmsServer.State.OPEN || serverData.state == RealmsServer.State.CLOSED;
            if (!serverData.expired && inAcceptState) {
               action = RealmsWorldSlotButton.Action.JOIN;
               actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip.active", new Object[0]);
            } else {
               action = RealmsWorldSlotButton.Action.NOTHING;
            }
         } else if (minigame) {
            if (serverData.expired) {
               action = RealmsWorldSlotButton.Action.NOTHING;
            } else {
               action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
               actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip.minigame", new Object[0]);
            }
         } else {
            action = RealmsWorldSlotButton.Action.SWITCH_SLOT;
            actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip", new Object[0]);
         }

         this.state = new RealmsWorldSlotButton.State(currentlyActiveSlot, slotName, imageId, image, empty, minigame, action, actionPrompt);
         String narrationMessage;
         if (action == RealmsWorldSlotButton.Action.NOTHING) {
            narrationMessage = slotName;
         } else if (minigame) {
            if (empty) {
               narrationMessage = actionPrompt;
            } else {
               narrationMessage = actionPrompt + " " + slotName + " " + serverData.minigameName;
            }
         } else {
            narrationMessage = actionPrompt + " " + slotName;
         }

         this.setMessage(narrationMessage);
      }
   }

   public void renderButton(int mouseX, int mouseY, float a) {
      if (this.state != null) {
         RealmsButtonProxy proxy = this.getProxy();
         this.drawSlotFrame(
            proxy.x,
            proxy.y,
            mouseX,
            mouseY,
            this.state.isCurrentlyActiveSlot,
            this.state.slotName,
            this.slotIndex,
            this.state.imageId,
            this.state.image,
            this.state.empty,
            this.state.minigame,
            this.state.action,
            this.state.actionPrompt
         );
      }
   }

   private void drawSlotFrame(
      int x,
      int y,
      int xm,
      int ym,
      boolean currentlyActiveSlot,
      String text,
      int i,
      long imageId,
      @Nullable String image,
      boolean empty,
      boolean minigame,
      RealmsWorldSlotButton.Action action,
      @Nullable String actionPrompt
   ) {
      boolean hoveredOrFocused = this.getProxy().isHovered();
      if (this.getProxy().isMouseOver((double)xm, (double)ym) && actionPrompt != null) {
         this.toolTipSetter.accept(actionPrompt);
      }

      if (minigame) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (empty) {
         Realms.bind("realms:textures/gui/realms/empty_frame.png");
      } else if (image != null && imageId != -1L) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (i == 1) {
         Realms.bind("textures/gui/title/background/panorama_0.png");
      } else if (i == 2) {
         Realms.bind("textures/gui/title/background/panorama_2.png");
      } else if (i == 3) {
         Realms.bind("textures/gui/title/background/panorama_3.png");
      }

      if (currentlyActiveSlot) {
         float c = 0.85F + 0.15F * RealmsMth.cos((float)this.animTick * 0.2F);
         GlStateManager.color4f(c, c, c, 1.0F);
      } else {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      RealmsScreen.blit(x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      Realms.bind("realms:textures/gui/realms/slot_frame.png");
      boolean actionable = hoveredOrFocused && action != RealmsWorldSlotButton.Action.NOTHING;
      if (actionable) {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else if (currentlyActiveSlot) {
         GlStateManager.color4f(0.8F, 0.8F, 0.8F, 1.0F);
      } else {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      RealmsScreen.blit(x, y, 0.0F, 0.0F, 80, 80, 80, 80);
      this.drawCenteredString(text, x + 40, y + 66, 16777215);
   }

   public void onPress() {
      this.listener.onSlotClick(this.slotIndex, this.state.action, this.state.minigame, this.state.empty);
   }

   public static enum Action {
      NOTHING,
      SWITCH_SLOT,
      JOIN;
   }

   public interface Listener {
      void onSlotClick(int var1, @Nonnull RealmsWorldSlotButton.Action var2, boolean var3, boolean var4);
   }

   public static class State {
      final boolean isCurrentlyActiveSlot;
      final String slotName;
      final long imageId;
      public final String image;
      public final boolean empty;
      final boolean minigame;
      public final RealmsWorldSlotButton.Action action;
      final String actionPrompt;

      State(
         boolean isCurrentlyActiveSlot,
         String slotName,
         long imageId,
         @Nullable String image,
         boolean empty,
         boolean minigame,
         @Nonnull RealmsWorldSlotButton.Action action,
         @Nullable String actionPrompt
      ) {
         this.isCurrentlyActiveSlot = isCurrentlyActiveSlot;
         this.slotName = slotName;
         this.imageId = imageId;
         this.image = image;
         this.empty = empty;
         this.minigame = minigame;
         this.action = action;
         this.actionPrompt = actionPrompt;
      }
   }
}
