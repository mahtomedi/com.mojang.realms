package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsButtonProxy;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;

public class w extends RealmsButton {
   private final Supplier<RealmsServer> a;
   private final Consumer<String> b;
   private final w.b c;
   private final int d;
   private int e;
   private w.c f;

   public w(int x, int y, int width, int height, Supplier<RealmsServer> serverDataProvider, Consumer<String> toolTipSetter, int id, int slotIndex, w.b listener) {
      super(id, x, y, width, height, "");
      this.a = serverDataProvider;
      this.d = slotIndex;
      this.b = toolTipSetter;
      this.c = listener;
   }

   public void render(int xm, int ym, float a) {
      super.render(xm, ym, a);
   }

   public void tick() {
      ++this.e;
      RealmsServer serverData = (RealmsServer)this.a.get();
      if (serverData != null) {
         RealmsWorldOptions slotOptions = (RealmsWorldOptions)serverData.slots.get(this.d);
         boolean minigame = this.d == 4;
         boolean currentlyActiveSlot;
         String slotName;
         long imageId;
         String image;
         boolean empty;
         if (minigame) {
            currentlyActiveSlot = serverData.worldType.equals(RealmsServer.c.b);
            slotName = "Minigame";
            imageId = (long)serverData.minigameId;
            image = serverData.minigameImage;
            empty = serverData.minigameId == -1;
         } else {
            currentlyActiveSlot = serverData.activeSlot == this.d && !serverData.worldType.equals(RealmsServer.c.b);
            slotName = slotOptions.getSlotName(this.d);
            imageId = slotOptions.templateId;
            image = slotOptions.templateImage;
            empty = slotOptions.empty;
         }

         String actionPrompt = null;
         w.a action;
         if (currentlyActiveSlot) {
            boolean inAcceptState = serverData.state == RealmsServer.b.b || serverData.state == RealmsServer.b.a;
            if (!serverData.expired && inAcceptState) {
               action = w.a.c;
               actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip.active", new Object[0]);
            } else {
               action = w.a.a;
            }
         } else if (minigame) {
            if (serverData.expired) {
               action = w.a.a;
            } else {
               action = w.a.b;
               actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip.minigame", new Object[0]);
            }
         } else {
            action = w.a.b;
            actionPrompt = Realms.getLocalizedString("mco.configure.world.slot.tooltip", new Object[0]);
         }

         this.f = new w.c(currentlyActiveSlot, slotName, imageId, image, empty, minigame, action, actionPrompt);
         String narrationMessage;
         if (action == w.a.a) {
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
      if (this.f != null) {
         RealmsButtonProxy proxy = this.getProxy();
         this.a(proxy.x, proxy.y, mouseX, mouseY, this.f.a, this.f.b, this.d, this.f.c, this.f.d, this.f.e, this.f.f, this.f.g, this.f.h);
      }
   }

   private void a(
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
      w.a action,
      @Nullable String actionPrompt
   ) {
      boolean hoveredOrFocused = this.getProxy().isHovered();
      if (this.getProxy().isMouseOver((double)xm, (double)ym) && actionPrompt != null) {
         this.b.accept(actionPrompt);
      }

      if (minigame) {
         bj.a(String.valueOf(imageId), image);
      } else if (empty) {
         Realms.bind("realms:textures/gui/realms/empty_frame.png");
      } else if (image != null && imageId != -1L) {
         bj.a(String.valueOf(imageId), image);
      } else if (i == 1) {
         Realms.bind("textures/gui/title/background/panorama_0.png");
      } else if (i == 2) {
         Realms.bind("textures/gui/title/background/panorama_2.png");
      } else if (i == 3) {
         Realms.bind("textures/gui/title/background/panorama_3.png");
      }

      if (currentlyActiveSlot) {
         float c = 0.85F + 0.15F * RealmsMth.cos((float)this.e * 0.2F);
         GlStateManager.color4f(c, c, c, 1.0F);
      } else {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      RealmsScreen.blit(x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      Realms.bind("realms:textures/gui/realms/slot_frame.png");
      boolean actionable = hoveredOrFocused && action != w.a.a;
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
      this.c.a(this.d, this.f.g, this.f.f, this.f.e);
   }

   public static enum a {
      a,
      b,
      c;
   }

   public interface b {
      void a(int var1, @Nonnull w.a var2, boolean var3, boolean var4);
   }

   public static class c {
      final boolean a;
      final String b;
      final long c;
      public final String d;
      public final boolean e;
      final boolean f;
      public final w.a g;
      final String h;

      c(
         boolean isCurrentlyActiveSlot,
         String slotName,
         long imageId,
         @Nullable String image,
         boolean empty,
         boolean minigame,
         @Nonnull w.a action,
         @Nullable String actionPrompt
      ) {
         this.a = isCurrentlyActiveSlot;
         this.b = slotName;
         this.c = imageId;
         this.d = image;
         this.e = empty;
         this.f = minigame;
         this.g = action;
         this.h = actionPrompt;
      }
   }
}
