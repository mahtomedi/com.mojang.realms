package realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ab extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final realms.b c;
   private RealmsServer d;
   private final long e;
   private String f = getLocalizedString("mco.brokenworld.title");
   private final String g = getLocalizedString("mco.brokenworld.message.line1") + "\\n" + getLocalizedString("mco.brokenworld.message.line2");
   private int h;
   private int i;
   private final int j = 80;
   private final int k = 5;
   private static final List<Integer> l = Arrays.asList(1, 2, 3);
   private static final List<Integer> m = Arrays.asList(4, 5, 6);
   private static final List<Integer> n = Arrays.asList(7, 8, 9);
   private static final List<Integer> o = Arrays.asList(10, 11, 12);
   private final List<Integer> p = new ArrayList();
   private int q;

   public ab(RealmsScreen lastScreen, realms.b mainScreen, long serverId) {
      this.b = lastScreen;
      this.c = mainScreen;
      this.e = serverId;
   }

   public void a(String title) {
      this.f = title;
   }

   public void init() {
      this.h = this.width() / 2 - 150;
      this.i = this.width() / 2 + 190;
      this.buttonsAdd(new RealmsButton(0, this.i - 80 + 8, u.a(13) - 5, 70, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            ab.this.e();
         }
      });
      if (this.d == null) {
         this.a(this.e);
      } else {
         this.a();
      }

      this.setKeyboardHandlerSendRepeatsToGui(true);
   }

   public void a() {
      for(Entry<Integer, RealmsWorldOptions> entry : this.d.slots.entrySet()) {
         RealmsWorldOptions slot = (RealmsWorldOptions)entry.getValue();
         boolean canPlay = entry.getKey() != this.d.activeSlot || this.d.worldType.equals(RealmsServer.c.b);
         RealmsButton downloadButton;
         if (canPlay) {
            downloadButton = new ab.b(l.get(entry.getKey() - 1), this.a(entry.getKey()), getLocalizedString("mco.brokenworld.play"));
         } else {
            downloadButton = new ab.a(n.get(entry.getKey() - 1), this.a(entry.getKey()), getLocalizedString("mco.brokenworld.download"));
         }

         if (this.p.contains(entry.getKey())) {
            downloadButton.active(false);
            downloadButton.setMessage(getLocalizedString("mco.brokenworld.downloaded"));
         }

         this.buttonsAdd(downloadButton);
         this.buttonsAdd(new RealmsButton(m.get(entry.getKey() - 1), this.a(entry.getKey()), u.a(10), 80, 20, getLocalizedString("mco.brokenworld.reset")) {
            public void onPress() {
               int slot = ab.m.indexOf(this.id()) + 1;
               ar realmsResetWorldScreen = new ar(ab.this, ab.this.d, ab.this);
               if (slot != ab.this.d.activeSlot || ab.this.d.worldType.equals(RealmsServer.c.b)) {
                  realmsResetWorldScreen.b(slot);
               }

               realmsResetWorldScreen.a(14);
               Realms.setScreen(realmsResetWorldScreen);
            }
         });
      }

   }

   public void tick() {
      ++this.q;
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      super.render(xm, ym, a);
      this.drawCenteredString(this.f, this.width() / 2, 17, 16777215);
      String[] lines = this.g.split("\\\\n");

      for(int i = 0; i < lines.length; ++i) {
         this.drawCenteredString(lines[i], this.width() / 2, u.a(-1) + 3 + i * 12, 10526880);
      }

      if (this.d != null) {
         for(Entry<Integer, RealmsWorldOptions> entry : this.d.slots.entrySet()) {
            if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
               this.a(
                  this.a(entry.getKey()),
                  u.a(1) + 5,
                  xm,
                  ym,
                  this.d.activeSlot == entry.getKey() && !this.f(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  ((RealmsWorldOptions)entry.getValue()).templateId,
                  ((RealmsWorldOptions)entry.getValue()).templateImage,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            } else {
               this.a(
                  this.a(entry.getKey()),
                  u.a(1) + 5,
                  xm,
                  ym,
                  this.d.activeSlot == entry.getKey() && !this.f(),
                  ((RealmsWorldOptions)entry.getValue()).getSlotName(entry.getKey()),
                  entry.getKey(),
                  -1L,
                  null,
                  ((RealmsWorldOptions)entry.getValue()).empty
               );
            }
         }

      }
   }

   private int a(int i) {
      return this.h + (i - 1) * 110;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.e();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void e() {
      Realms.setScreen(this.b);
   }

   private void a(final long worldId) {
      (new Thread() {
         public void run() {
            g client = realms.g.a();

            try {
               ab.this.d = client.a(worldId);
               ab.this.a();
            } catch (o var3) {
               ab.a.error("Couldn't get own world");
               Realms.setScreen(new ai(var3.getMessage(), ab.this.b));
            } catch (IOException var4) {
               ab.a.error("Couldn't parse response getting own world");
            }

         }
      }).start();
   }

   public void confirmResult(boolean result, int id) {
      if (!result) {
         Realms.setScreen(this);
      } else {
         if (id != 13 && id != 14) {
            if (n.contains(id)) {
               this.b(n.indexOf(id) + 1);
            } else if (o.contains(id)) {
               this.p.add(o.indexOf(id) + 1);
               this.childrenClear();
               this.a();
            }
         } else {
            (new Thread() {
               public void run() {
                  g client = realms.g.a();
                  if (ab.this.d.state.equals(RealmsServer.b.a)) {
                     bi.c openServerTask = new bi.c(ab.this.d, ab.this, ab.this.b, true);
                     al openWorldLongRunningTaskScreen = new al(ab.this, openServerTask);
                     openWorldLongRunningTaskScreen.a();
                     Realms.setScreen(openWorldLongRunningTaskScreen);
                  } else {
                     try {
                        ab.this.c.f().a(client.a(ab.this.e), ab.this);
                     } catch (o var4) {
                        ab.a.error("Couldn't get own world");
                        Realms.setScreen(ab.this.b);
                     } catch (IOException var5) {
                        ab.a.error("Couldn't parse response getting own world");
                        Realms.setScreen(ab.this.b);
                     }
                  }

               }
            }).start();
         }

      }
   }

   private void b(int slotId) {
      g client = realms.g.a();

      try {
         WorldDownload worldDownload = client.b(this.d.id, slotId);
         ah downloadScreen = new ah(this, worldDownload, this.d.name + " (" + ((RealmsWorldOptions)this.d.slots.get(slotId)).getSlotName(slotId) + ")");
         downloadScreen.a(o.get(slotId - 1));
         Realms.setScreen(downloadScreen);
      } catch (o var5) {
         a.error("Couldn't download world data");
         Realms.setScreen(new ai(var5, this));
      }

   }

   private boolean f() {
      return this.d != null && this.d.worldType.equals(RealmsServer.c.b);
   }

   private void a(int x, int y, int xm, int ym, boolean active, String text, int i, long imageId, String image, boolean empty) {
      if (empty) {
         bind("realms:textures/gui/realms/empty_frame.png");
      } else if (image != null && imageId != -1L) {
         bj.a(String.valueOf(imageId), image);
      } else if (i == 1) {
         bind("textures/gui/title/background/panorama_0.png");
      } else if (i == 2) {
         bind("textures/gui/title/background/panorama_2.png");
      } else if (i == 3) {
         bind("textures/gui/title/background/panorama_3.png");
      } else {
         bj.a(String.valueOf(this.d.minigameId), this.d.minigameImage);
      }

      if (!active) {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      } else if (active) {
         float c = 0.9F + 0.1F * RealmsMth.cos((float)this.q * 0.2F);
         GlStateManager.color4f(c, c, c, 1.0F);
      }

      RealmsScreen.blit(x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      bind("realms:textures/gui/realms/slot_frame.png");
      if (active) {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         GlStateManager.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      RealmsScreen.blit(x, y, 0.0F, 0.0F, 80, 80, 80, 80);
      this.drawCenteredString(text, x + 40, y + 66, 16777215);
   }

   private void c(int id) {
      bi.i switchSlotTask = new bi.i(this.d.id, id, this, 13);
      al longRunningMcoTaskScreen = new al(this.b, switchSlotTask);
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   class a extends RealmsButton {
      public a(int id, int x, String msg) {
         super(id, x, u.a(8), 80, 20, msg);
      }

      public void onPress() {
         String line2 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
         String line3 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
         Realms.setScreen(new ak(ab.this, ak.a.b, line2, line3, true, this.id()));
      }
   }

   class b extends RealmsButton {
      public b(int id, int x, String msg) {
         super(id, x, u.a(8), 80, 20, msg);
      }

      public void onPress() {
         int slot = ab.l.indexOf(this.id()) + 1;
         if (((RealmsWorldOptions)ab.this.d.slots.get(slot)).empty) {
            ar resetWorldScreen = new ar(
               ab.this,
               ab.this.d,
               ab.this,
               RealmsScreen.getLocalizedString("mco.configure.world.switch.slot"),
               RealmsScreen.getLocalizedString("mco.configure.world.switch.slot.subtitle"),
               10526880,
               RealmsScreen.getLocalizedString("gui.cancel")
            );
            resetWorldScreen.b(slot);
            resetWorldScreen.a(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
            resetWorldScreen.a(14);
            Realms.setScreen(resetWorldScreen);
         } else {
            ab.this.c(slot);
         }

      }
   }
}
