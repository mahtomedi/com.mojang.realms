package realms;

import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplate;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsConfirmResultListener;
import net.minecraft.realms.RealmsConnect;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class bh {
   private static final Logger a = LogManager.getLogger();

   private static void b(int seconds) {
      try {
         Thread.sleep((long)(seconds * 1000));
      } catch (InterruptedException var2) {
         a.error("", var2);
      }

   }

   public static class a extends t {
      private final RealmsServer b;
      private final ac c;

      public a(RealmsServer realmsServer, ac configureWorldScreen) {
         this.b = realmsServer;
         this.c = configureWorldScreen;
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.configure.world.closing"));
         realms.g client = realms.g.a();

         for(int i = 0; i < 25; ++i) {
            if (this.b()) {
               return;
            }

            try {
               boolean closeResult = client.g(this.b.id);
               if (closeResult) {
                  this.c.a();
                  this.b.state = RealmsServer.b.a;
                  Realms.setScreen(this.c);
                  break;
               }
            } catch (p var4) {
               if (this.b()) {
                  return;
               }

               bh.b(var4.e);
            } catch (Exception var5) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Failed to close server", var5);
               this.a("Failed to close the server");
            }
         }

      }
   }

   public static class b extends t {
      private final long b;
      private final int c;
      private final RealmsScreen d;
      private final String e;

      public b(long worldId, int slot, String downloadName, RealmsScreen lastScreen) {
         this.b = worldId;
         this.c = slot;
         this.d = lastScreen;
         this.e = downloadName;
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.download.preparing"));
         realms.g client = realms.g.a();
         int i = 0;

         while(i < 25) {
            try {
               if (this.b()) {
                  return;
               }

               WorldDownload worldDownload = client.b(this.b, this.c);
               bh.b(1);
               if (this.b()) {
                  return;
               }

               Realms.setScreen(new ag(this.d, worldDownload, this.e));
               return;
            } catch (p var4) {
               if (this.b()) {
                  return;
               }

               bh.b(var4.e);
               ++i;
            } catch (o var5) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't download world data");
               Realms.setScreen(new ah(var5, this.d));
               return;
            } catch (Exception var6) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't download world data", var6);
               this.a(var6.getLocalizedMessage());
               return;
            }
         }

      }
   }

   public static class c extends t {
      private final RealmsServer b;
      private final RealmsScreen c;
      private final boolean d;
      private final RealmsScreen e;

      public c(RealmsServer realmsServer, RealmsScreen returnScreen, RealmsScreen mainScreen, boolean join) {
         this.b = realmsServer;
         this.c = returnScreen;
         this.d = join;
         this.e = mainScreen;
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.configure.world.opening"));
         realms.g client = realms.g.a();

         for(int i = 0; i < 25; ++i) {
            if (this.b()) {
               return;
            }

            try {
               boolean openResult = client.f(this.b.id);
               if (openResult) {
                  if (this.c instanceof ac) {
                     ((ac)this.c).a();
                  }

                  this.b.state = RealmsServer.b.b;
                  if (this.d) {
                     ((realms.b)this.e).a(this.b, this.c);
                  } else {
                     Realms.setScreen(this.c);
                  }
                  break;
               }
            } catch (p var4) {
               if (this.b()) {
                  return;
               }

               bh.b(var4.e);
            } catch (Exception var5) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Failed to open server", var5);
               this.a("Failed to open the server");
            }
         }

      }
   }

   public static class d extends t {
      private final RealmsConnect b;
      private final RealmsServerAddress c;

      public d(RealmsScreen lastScreen, RealmsServerAddress address) {
         this.c = address;
         this.b = new RealmsConnect(lastScreen);
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.connect.connecting"));
         net.minecraft.realms.RealmsServerAddress address = net.minecraft.realms.RealmsServerAddress.parseString(this.c.address);
         this.b.connect(address.getHost(), address.getPort());
      }

      @Override
      public void d() {
         this.b.abort();
         Realms.clearResourcePack();
      }

      @Override
      public void a() {
         this.b.tick();
      }
   }

   public static class e extends t {
      private final RealmsServer b;
      private final RealmsScreen c;
      private final realms.b d;
      private final ReentrantLock e;

      public e(realms.b mainScreen, RealmsScreen lastScreen, RealmsServer server, ReentrantLock connectLock) {
         this.c = lastScreen;
         this.d = mainScreen;
         this.b = server;
         this.e = connectLock;
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.connect.connecting"));
         realms.g client = realms.g.a();
         boolean addressRetrieved = false;
         boolean hasError = false;
         int sleepTime = 5;
         RealmsServerAddress address = null;
         boolean tosNotAccepted = false;
         boolean brokenWorld = false;

         for(int i = 0; i < 40 && !this.b(); ++i) {
            try {
               address = client.c(this.b.id);
               addressRetrieved = true;
            } catch (p var10) {
               sleepTime = var10.e;
            } catch (o var11) {
               if (var11.c == 6002) {
                  tosNotAccepted = true;
               } else if (var11.c == 6006) {
                  brokenWorld = true;
               } else {
                  hasError = true;
                  this.a(var11.toString());
                  bh.a.error("Couldn't connect to world", var11);
               }
               break;
            } catch (IOException var12) {
               bh.a.error("Couldn't parse response connecting to world", var12);
            } catch (Exception var13) {
               hasError = true;
               bh.a.error("Couldn't connect to world", var13);
               this.a(var13.getLocalizedMessage());
               break;
            }

            if (addressRetrieved) {
               break;
            }

            this.a(sleepTime);
         }

         if (tosNotAccepted) {
            Realms.setScreen(new ay(this.c, this.d, this.b));
         } else if (brokenWorld) {
            if (this.b.ownerUUID.equals(Realms.getUUID())) {
               aa brokenWorldScreen = new aa(this.c, this.d, this.b.id);
               if (this.b.worldType.equals(RealmsServer.c.b)) {
                  brokenWorldScreen.a(RealmsScreen.getLocalizedString("mco.brokenworld.minigame.title"));
               }

               Realms.setScreen(brokenWorldScreen);
            } else {
               Realms.setScreen(
                  new ah(
                     RealmsScreen.getLocalizedString("mco.brokenworld.nonowner.title"),
                     RealmsScreen.getLocalizedString("mco.brokenworld.nonowner.error"),
                     this.c
                  )
               );
            }
         } else if (!this.b() && !hasError) {
            if (addressRetrieved) {
               if (address.resourcePackUrl != null && address.resourcePackHash != null) {
                  String line2 = RealmsScreen.getLocalizedString("mco.configure.world.resourcepack.question.line1");
                  String line3 = RealmsScreen.getLocalizedString("mco.configure.world.resourcepack.question.line2");
                  Realms.setScreen(new aj(new ar(this.c, address, this.e), aj.a.b, line2, line3, true, 100));
               } else {
                  ak longRunningMcoTaskScreen = new ak(this.c, new bh.d(this.c, address));
                  longRunningMcoTaskScreen.a();
                  Realms.setScreen(longRunningMcoTaskScreen);
               }
            } else {
               this.a(RealmsScreen.getLocalizedString("mco.errorMessage.connectionFailure"));
            }
         }

      }

      private void a(int sleepTimeSeconds) {
         try {
            Thread.sleep((long)(sleepTimeSeconds * 1000));
         } catch (InterruptedException var3) {
            bh.a.warn(var3.getLocalizedMessage());
         }

      }
   }

   public static class f extends t {
      private final String b;
      private final WorldTemplate c;
      private final int d;
      private final boolean e;
      private final long f;
      private final RealmsScreen g;
      private int h = -1;
      private String i = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");

      public f(long serverId, RealmsScreen lastScreen, WorldTemplate worldTemplate) {
         this.b = null;
         this.c = worldTemplate;
         this.d = -1;
         this.e = true;
         this.f = serverId;
         this.g = lastScreen;
      }

      public f(long serverId, RealmsScreen lastScreen, String seed, int levelType, boolean generateStructures) {
         this.b = seed;
         this.c = null;
         this.d = levelType;
         this.e = generateStructures;
         this.f = serverId;
         this.g = lastScreen;
      }

      public void a(int confirmationId) {
         this.h = confirmationId;
      }

      public void c(String title) {
         this.i = title;
      }

      public void run() {
         realms.g client = realms.g.a();
         this.b(this.i);
         int i = 0;

         while(i < 25) {
            try {
               if (this.b()) {
                  return;
               }

               if (this.c != null) {
                  client.g(this.f, this.c.id);
               } else {
                  client.a(this.f, this.b, this.d, this.e);
               }

               if (this.b()) {
                  return;
               }

               if (this.h == -1) {
                  Realms.setScreen(this.g);
               } else {
                  this.g.confirmResult(true, this.h);
               }

               return;
            } catch (p var4) {
               if (this.b()) {
                  return;
               }

               bh.b(var4.e);
               ++i;
            } catch (Exception var5) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't reset world");
               this.a(var5.toString());
               return;
            }
         }

      }
   }

   public static class g extends t {
      private final Backup b;
      private final long c;
      private final ac d;

      public g(Backup backup, long worldId, ac lastScreen) {
         this.b = backup;
         this.c = worldId;
         this.d = lastScreen;
      }

      public void run() {
         this.b(RealmsScreen.getLocalizedString("mco.backup.restoring"));
         realms.g client = realms.g.a();
         int i = 0;

         while(i < 25) {
            try {
               if (this.b()) {
                  return;
               }

               client.c(this.c, this.b.backupId);
               bh.b(1);
               if (this.b()) {
                  return;
               }

               Realms.setScreen(this.d.b());
               return;
            } catch (p var4) {
               if (this.b()) {
                  return;
               }

               bh.b(var4.e);
               ++i;
            } catch (o var5) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't restore backup", var5);
               Realms.setScreen(new ah(var5, this.d));
               return;
            } catch (Exception var6) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't restore backup", var6);
               this.a(var6.getLocalizedMessage());
               return;
            }
         }

      }
   }

   public static class h extends t {
      private final long b;
      private final WorldTemplate c;
      private final ac d;

      public h(long worldId, WorldTemplate worldTemplate, ac lastScreen) {
         this.b = worldId;
         this.c = worldTemplate;
         this.d = lastScreen;
      }

      public void run() {
         realms.g client = realms.g.a();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.starting.screen.title");
         this.b(title);

         for(int i = 0; i < 25; ++i) {
            try {
               if (this.b()) {
                  return;
               }

               if (client.d(this.b, this.c.id)) {
                  Realms.setScreen(this.d);
                  break;
               }
            } catch (p var5) {
               if (this.b()) {
                  return;
               }

               bh.b(var5.e);
            } catch (Exception var6) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't start mini game!");
               this.a(var6.toString());
            }
         }

      }
   }

   public static class i extends t {
      private final long b;
      private final int c;
      private final RealmsConfirmResultListener d;
      private final int e;

      public i(long worldId, int slot, RealmsConfirmResultListener listener, int confirmId) {
         this.b = worldId;
         this.c = slot;
         this.d = listener;
         this.e = confirmId;
      }

      public void run() {
         realms.g client = realms.g.a();
         String title = RealmsScreen.getLocalizedString("mco.minigame.world.slot.screen.title");
         this.b(title);

         for(int i = 0; i < 25; ++i) {
            try {
               if (this.b()) {
                  return;
               }

               if (client.a(this.b, this.c)) {
                  this.d.confirmResult(true, this.e);
                  break;
               }
            } catch (p var5) {
               if (this.b()) {
                  return;
               }

               bh.b(var5.e);
            } catch (Exception var6) {
               if (this.b()) {
                  return;
               }

               bh.a.error("Couldn't switch world!");
               this.a(var6.toString());
            }
         }

      }
   }

   public static class j extends t {
      private final String b;
      private final String c;
      private final realms.b d;

      public j(String name, String motd, realms.b lastScreen) {
         this.b = name;
         this.c = motd;
         this.d = lastScreen;
      }

      public void run() {
         String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
         this.b(title);
         realms.g client = realms.g.a();

         try {
            RealmsServer server = client.a(this.b, this.c);
            if (server != null) {
               this.d.a(true);
               this.d.g();
               aq resetWorldScreen = new aq(
                  this.d,
                  server,
                  this.d.f(),
                  RealmsScreen.getLocalizedString("mco.selectServer.create"),
                  RealmsScreen.getLocalizedString("mco.create.world.subtitle"),
                  10526880,
                  RealmsScreen.getLocalizedString("mco.create.world.skip")
               );
               resetWorldScreen.a(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
               Realms.setScreen(resetWorldScreen);
            } else {
               this.a(RealmsScreen.getLocalizedString("mco.trial.unavailable"));
            }
         } catch (o var5) {
            bh.a.error("Couldn't create trial");
            this.a(var5.toString());
         } catch (UnsupportedEncodingException var6) {
            bh.a.error("Couldn't create trial");
            this.a(var6.getLocalizedMessage());
         } catch (IOException var7) {
            bh.a.error("Could not parse response creating trial");
            this.a(var7.getLocalizedMessage());
         } catch (Exception var8) {
            bh.a.error("Could not create trial");
            this.a(var8.getLocalizedMessage());
         }

      }
   }

   public static class k extends t {
      private final String b;
      private final String c;
      private final long d;
      private final RealmsScreen e;

      public k(long worldId, String name, String motd, RealmsScreen lastScreen) {
         this.d = worldId;
         this.b = name;
         this.c = motd;
         this.e = lastScreen;
      }

      public void run() {
         String title = RealmsScreen.getLocalizedString("mco.create.world.wait");
         this.b(title);
         realms.g client = realms.g.a();

         try {
            client.a(this.d, this.b, this.c);
            Realms.setScreen(this.e);
         } catch (o var4) {
            bh.a.error("Couldn't create world");
            this.a(var4.toString());
         } catch (UnsupportedEncodingException var5) {
            bh.a.error("Couldn't create world");
            this.a(var5.getLocalizedMessage());
         } catch (IOException var6) {
            bh.a.error("Could not parse response creating world");
            this.a(var6.getLocalizedMessage());
         } catch (Exception var7) {
            bh.a.error("Could not create world");
            this.a(var7.getLocalizedMessage());
         }

      }
   }
}
