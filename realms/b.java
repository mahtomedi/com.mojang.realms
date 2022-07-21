package realms;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class b extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private static boolean b;
   private final RateLimiter c;
   private boolean d;
   private static final String[] e = new String[]{
      "realms:textures/gui/realms/images/sand_castle.png",
      "realms:textures/gui/realms/images/factory_floor.png",
      "realms:textures/gui/realms/images/escher_tunnel.png",
      "realms:textures/gui/realms/images/tree_houses.png",
      "realms:textures/gui/realms/images/balloon_trip.png",
      "realms:textures/gui/realms/images/halloween_woods.png",
      "realms:textures/gui/realms/images/flower_mountain.png",
      "realms:textures/gui/realms/images/dornenstein_estate.png",
      "realms:textures/gui/realms/images/desert.png",
      "realms:textures/gui/realms/images/gray.png",
      "realms:textures/gui/realms/images/imperium.png",
      "realms:textures/gui/realms/images/ludo.png",
      "realms:textures/gui/realms/images/makersspleef.png",
      "realms:textures/gui/realms/images/negentropy.png",
      "realms:textures/gui/realms/images/pumpkin_party.png",
      "realms:textures/gui/realms/images/sparrenhout.png",
      "realms:textures/gui/realms/images/spindlewood.png"
   };
   private static final v f = new v();
   private static int g = -1;
   private final RealmsScreen h;
   private volatile realms.b.d i;
   private long j = -1L;
   private RealmsButton k;
   private RealmsButton l;
   private RealmsButton m;
   private RealmsButton n;
   private RealmsButton o;
   private String p;
   private List<RealmsServer> q = Lists.newArrayList();
   private volatile int r;
   private int s;
   private static volatile boolean t;
   private static volatile boolean u;
   private static volatile boolean v;
   private boolean w;
   private boolean x;
   private boolean y;
   private volatile boolean z;
   private volatile boolean A;
   private volatile boolean B;
   private volatile boolean C;
   private volatile String D;
   private int E;
   private int F;
   private boolean G;
   private static RealmsScreen H;
   private static boolean I;
   private List<realms.a> J;
   private int K;
   private ReentrantLock L = new ReentrantLock();
   private boolean M;
   private realms.b.g N;
   private realms.b.c O;
   private realms.b.b P;
   private RealmsButton Q;
   private RealmsButton R;
   private RealmsButton S;

   public b(RealmsScreen lastScreen) {
      this.h = lastScreen;
      this.c = RateLimiter.create(0.016666668F);
   }

   public boolean a() {
      if (this.l() && this.w) {
         if (this.z && !this.A) {
            return true;
         } else {
            for(RealmsServer realmsServer : this.q) {
               if (realmsServer.ownerUUID.equals(Realms.getUUID())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean b() {
      if (!this.l() || !this.w) {
         return false;
      } else if (this.x) {
         return true;
      } else {
         return this.z && !this.A && this.q.isEmpty() ? true : this.q.isEmpty();
      }
   }

   public void init() {
      this.J = Lists.newArrayList(
         new realms.a[]{new realms.a(new char[]{'3', '2', '1', '4', '5', '6'}, () -> b = !b), new realms.a(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
            if (realms.g.a.equals(realms.g.b.b)) {
               this.x();
            } else {
               this.v();
            }
   
         }), new realms.a(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
            if (realms.g.a.equals(realms.g.b.c)) {
               this.x();
            } else {
               this.w();
            }
   
         })}
      );
      if (H != null) {
         Realms.setScreen(H);
      } else {
         this.L = new ReentrantLock();
         if (v && !this.l()) {
            this.u();
         }

         this.s();
         this.t();
         if (!this.d) {
            Realms.setConnectedToRealms(false);
         }

         this.setKeyboardHandlerSendRepeatsToGui(true);
         if (this.l()) {
            f.d();
         }

         this.B = false;
         this.d();
      }
   }

   private boolean l() {
      return u && t;
   }

   public void c() {
      this.buttonsAdd(this.n = new RealmsButton(1, this.width() / 2 - 190, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.configure")) {
         public void onPress() {
            b.this.f(b.this.a(b.this.j));
         }
      });
      this.buttonsAdd(this.k = new RealmsButton(3, this.width() / 2 - 93, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.play")) {
         public void onPress() {
            b.this.p();
         }
      });
      this.buttonsAdd(this.l = new RealmsButton(2, this.width() / 2 + 4, this.height() - 32, 90, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            if (!b.this.y) {
               Realms.setScreen(b.this.h);
            }

         }
      });
      this.buttonsAdd(this.m = new RealmsButton(0, this.width() / 2 + 100, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.expiredRenew")) {
         public void onPress() {
            b.this.q();
         }
      });
      this.buttonsAdd(this.o = new RealmsButton(7, this.width() / 2 - 202, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.leave")) {
         public void onPress() {
            b.this.g(b.this.a(b.this.j));
         }
      });
      this.buttonsAdd(this.O = new realms.b.c());
      this.buttonsAdd(this.P = new realms.b.b());
      this.buttonsAdd(this.N = new realms.b.g());
      this.buttonsAdd(this.S = new realms.b.a());
      this.buttonsAdd(this.Q = new RealmsButton(6, this.width() / 2 + 52, this.C() + 137 - 20, 98, 20, getLocalizedString("mco.selectServer.trial")) {
         public void onPress() {
            b.this.r();
         }
      });
      this.buttonsAdd(this.R = new RealmsButton(5, this.width() / 2 + 52, this.C() + 160 - 20, 98, 20, getLocalizedString("mco.selectServer.buy")) {
         public void onPress() {
            bk.c("https://minecraft.net/realms");
         }
      });
      RealmsServer server = this.a(this.j);
      this.a(server);
   }

   private void a(RealmsServer server) {
      this.k.active(this.b(server) && !this.b());
      this.m.setVisible(this.c(server));
      this.n.setVisible(this.d(server));
      this.o.setVisible(this.e(server));
      boolean trialButton = this.b() && this.z && !this.A;
      this.Q.setVisible(trialButton);
      this.Q.active(trialButton);
      this.R.setVisible(this.b());
      this.S.setVisible(this.b() && this.x);
      this.m.active(!this.b());
      this.n.active(!this.b());
      this.o.active(!this.b());
      this.P.active(true);
      this.O.active(true);
      this.l.active(true);
      this.N.active(!this.b());
   }

   private boolean m() {
      return (!this.b() || this.x) && this.l() && this.w;
   }

   private boolean b(RealmsServer server) {
      return server != null && !server.expired && server.state == RealmsServer.b.b;
   }

   private boolean c(RealmsServer server) {
      return server != null && server.expired && this.h(server);
   }

   private boolean d(RealmsServer server) {
      return server != null && this.h(server);
   }

   private boolean e(RealmsServer server) {
      return server != null && !this.h(server);
   }

   public void d() {
      if (this.l() && this.w) {
         this.c();
      }

      this.i = new realms.b.d();
      if (g != -1) {
         this.i.scroll(g);
      }

      this.addWidget(this.i);
      this.focusOn(this.i);
   }

   public void tick() {
      this.tickButtons();
      this.y = false;
      ++this.s;
      --this.K;
      if (this.K < 0) {
         this.K = 0;
      }

      if (this.l()) {
         f.b();
         if (f.a(v.d.a)) {
            List<RealmsServer> newServers = f.e();
            this.i.clear();
            boolean firstFetchCompleted = !this.w;
            if (firstFetchCompleted) {
               this.w = true;
            }

            if (newServers != null) {
               boolean ownsNonExpiredRealmServer = false;

               for(RealmsServer retrievedServer : newServers) {
                  if (this.i(retrievedServer)) {
                     ownsNonExpiredRealmServer = true;
                  }
               }

               this.q = newServers;
               if (this.a()) {
                  this.i.addEntry(new realms.b.f());
               }

               for(RealmsServer server : this.q) {
                  this.i.addEntry(new realms.b.e(server));
               }

               if (!I && ownsNonExpiredRealmServer) {
                  I = true;
                  this.n();
               }
            }

            if (firstFetchCompleted) {
               this.c();
            }
         }

         if (f.a(v.d.b)) {
            this.r = f.f();
            if (this.r > 0 && this.c.tryAcquire(1)) {
               Realms.narrateNow(getLocalizedString("mco.configure.world.invite.narration", new Object[]{this.r}));
            }
         }

         if (f.a(v.d.c) && !this.A) {
            boolean newStatus = f.g();
            if (newStatus != this.z && this.b()) {
               this.z = newStatus;
               this.B = false;
            } else {
               this.z = newStatus;
            }
         }

         if (f.a(v.d.d)) {
            RealmsServerPlayerLists playerLists = f.h();

            for(RealmsServerPlayerList playerList : playerLists.servers) {
               for(RealmsServer server : this.q) {
                  if (server.id == playerList.serverId) {
                     server.updateServerPing(playerList);
                     break;
                  }
               }
            }
         }

         if (f.a(v.d.e)) {
            this.C = f.i();
            this.D = f.j();
         }

         f.c();
         if (this.b()) {
            ++this.F;
         }

         if (this.N != null) {
            this.N.setVisible(this.m());
         }

      }
   }

   private void a(String url) {
      Realms.setClipboard(url);
      bk.c(url);
   }

   private void n() {
      (new Thread() {
         public void run() {
            List<RegionPingResult> regionPingResultList = realms.e.a();
            realms.g client = realms.g.a();
            PingResult pingResult = new PingResult();
            pingResult.pingResults = regionPingResultList;
            pingResult.worldIds = b.this.o();

            try {
               client.a(pingResult);
            } catch (Throwable var5) {
               realms.b.a.warn("Could not send ping result to Realms: ", var5);
            }

         }
      }).start();
   }

   private List<Long> o() {
      List<Long> ids = new ArrayList();

      for(RealmsServer server : this.q) {
         if (this.i(server)) {
            ids.add(server.id);
         }
      }

      return ids;
   }

   public void removed() {
      this.setKeyboardHandlerSendRepeatsToGui(false);
      this.y();
   }

   public void a(boolean createdTrial) {
      this.A = createdTrial;
   }

   private void p() {
      RealmsServer server = this.a(this.j);
      if (server != null) {
         this.a(server, this);
      }
   }

   private void q() {
      RealmsServer server = this.a(this.j);
      if (server != null) {
         String extensionUrl = "https://account.mojang.com/buy/realms?sid="
            + server.remoteSubscriptionId
            + "&pid="
            + Realms.getUUID()
            + "&ref="
            + (server.expiredTrial ? "expiredTrial" : "expiredRealm");
         this.a(extensionUrl);
      }
   }

   private void r() {
      if (this.z && !this.A) {
         Realms.setScreen(new ag(this));
      }
   }

   private void s() {
      if (!v) {
         v = true;
         (new Thread("MCO Compatability Checker #1") {
               public void run() {
                  realms.g client = realms.g.a();
   
                  try {
                     realms.g.a versionResponse = client.i();
                     if (versionResponse.equals(realms.g.a.b)) {
                        realms.b.H = new ac(b.this.h, true);
                        Realms.setScreen(realms.b.H);
                     } else if (versionResponse.equals(realms.g.a.c)) {
                        realms.b.H = new ac(b.this.h, false);
                        Realms.setScreen(realms.b.H);
                     } else {
                        b.this.u();
                     }
                  } catch (o var3) {
                     realms.b.v = false;
                     realms.b.a.error("Couldn't connect to realms: ", var3.toString());
                     if (var3.a == 401) {
                        realms.b.H = new ai(
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.title"),
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.message"),
                           b.this.h
                        );
                        Realms.setScreen(realms.b.H);
                     } else {
                        Realms.setScreen(new ai(var3, b.this.h));
                     }
                  } catch (IOException var4) {
                     realms.b.v = false;
                     realms.b.a.error("Couldn't connect to realms: ", var4.getMessage());
                     Realms.setScreen(new ai(var4.getMessage(), b.this.h));
                  }
               }
            })
            .start();
      }

   }

   private void t() {
   }

   private void u() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            realms.g client = realms.g.a();

            try {
               Boolean result = client.g();
               if (result) {
                  realms.b.a.info("Realms is available for this user");
                  realms.b.t = true;
               } else {
                  realms.b.a.info("Realms is not available for this user");
                  realms.b.t = false;
                  Realms.setScreen(new an(b.this.h));
               }

               realms.b.u = true;
            } catch (o var3) {
               realms.b.a.error("Couldn't connect to realms: ", var3.toString());
               Realms.setScreen(new ai(var3, b.this.h));
            } catch (IOException var4) {
               realms.b.a.error("Couldn't connect to realms: ", var4.getMessage());
               Realms.setScreen(new ai(var4.getMessage(), b.this.h));
            }

         }
      }).start();
   }

   private void v() {
      if (!realms.g.a.equals(realms.g.b.b)) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               realms.g client = realms.g.a();

               try {
                  Boolean result = client.h();
                  if (result) {
                     realms.g.b();
                     realms.b.a.info("Switched to stage");
                     realms.b.f.d();
                  }
               } catch (o var3) {
                  realms.b.a.error("Couldn't connect to Realms: " + var3);
               } catch (IOException var4) {
                  realms.b.a.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
               }

            }
         }).start();
      }

   }

   private void w() {
      if (!realms.g.a.equals(realms.g.b.c)) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               realms.g client = realms.g.a();

               try {
                  Boolean result = client.h();
                  if (result) {
                     realms.g.d();
                     realms.b.a.info("Switched to local");
                     realms.b.f.d();
                  }
               } catch (o var3) {
                  realms.b.a.error("Couldn't connect to Realms: " + var3);
               } catch (IOException var4) {
                  realms.b.a.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
               }

            }
         }).start();
      }

   }

   private void x() {
      realms.g.c();
      f.d();
   }

   private void y() {
      f.k();
   }

   private void f(RealmsServer selectedServer) {
      if (Realms.getUUID().equals(selectedServer.ownerUUID) || b) {
         this.z();
         cvo minecraft = cvo.u();
         minecraft.execute(() -> minecraft.a(new ad(this, selectedServer.id).getProxy()));
      }

   }

   private void g(RealmsServer selectedServer) {
      if (!Realms.getUUID().equals(selectedServer.ownerUUID)) {
         this.z();
         String line2 = getLocalizedString("mco.configure.world.leave.question.line1");
         String line3 = getLocalizedString("mco.configure.world.leave.question.line2");
         Realms.setScreen(new ak(this, ak.a.b, line2, line3, true, 4));
      }

   }

   private void z() {
      g = this.i.getScroll();
   }

   private RealmsServer a(long id) {
      for(RealmsServer server : this.q) {
         if (server.id == id) {
            return server;
         }
      }

      return null;
   }

   private int b(long serverId) {
      for(int i = 0; i < this.q.size(); ++i) {
         if (((RealmsServer)this.q.get(i)).id == serverId) {
            return i;
         }
      }

      return -1;
   }

   public void confirmResult(boolean result, int id) {
      if (id == 4) {
         if (result) {
            (new Thread("Realms-leave-server") {
               public void run() {
                  try {
                     RealmsServer server = b.this.a(b.this.j);
                     if (server != null) {
                        realms.g client = realms.g.a();
                        client.d(server.id);
                        realms.b.f.a(server);
                        b.this.q.remove(server);
                        b.this.j = -1L;
                        b.this.k.active(false);
                     }
                  } catch (o var3) {
                     realms.b.a.error("Couldn't configure world");
                     Realms.setScreen(new ai(var3, b.this));
                  }

               }
            }).start();
         }

         Realms.setScreen(this);
      }

   }

   public void e() {
      this.j = -1L;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.J.forEach(realms.a::a);
            this.A();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void A() {
      if (this.b() && this.x) {
         this.x = false;
      } else {
         Realms.setScreen(this.h);
      }

   }

   public boolean charTyped(char ch, int mods) {
      this.J.forEach(c -> c.a(ch));
      return true;
   }

   public void render(int xm, int ym, float a) {
      this.M = false;
      this.p = null;
      this.renderBackground();
      this.i.render(xm, ym, a);
      this.a(this.width() / 2 - 50, 7);
      if (realms.g.a.equals(realms.g.b.b)) {
         this.E();
      }

      if (realms.g.a.equals(realms.g.b.c)) {
         this.D();
      }

      if (this.b()) {
         this.b(xm, ym);
      } else {
         if (this.B) {
            this.a(null);
            if (!this.hasWidget(this.i)) {
               this.addWidget(this.i);
            }

            RealmsServer server = this.a(this.j);
            this.k.active(this.b(server));
         }

         this.B = false;
      }

      super.render(xm, ym, a);
      if (this.p != null) {
         this.a(this.p, xm, ym);
      }

      if (this.z && !this.A && this.b()) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         int diamondWidth = 8;
         int diamondHeight = 8;
         int vSprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            vSprite = 8;
         }

         RealmsScreen.blit(this.Q.x() + this.Q.getWidth() - 8 - 4, this.Q.y() + this.Q.getHeight() / 2 - 4, 0.0F, (float)vSprite, 8, 8, 8, 16);
         GlStateManager.popMatrix();
      }

   }

   private void a(int x, int y) {
      RealmsScreen.bind("realms:textures/gui/title/realms.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.scalef(0.5F, 0.5F, 0.5F);
      RealmsScreen.blit(x * 2, y * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
      GlStateManager.popMatrix();
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.a(x, y) && this.x) {
         this.x = false;
         this.y = true;
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   private boolean a(double xm, double ym) {
      int xo = this.B();
      int yo = this.C();
      return xm < (double)(xo - 5) || xm > (double)(xo + 315) || ym < (double)(yo - 5) || ym > (double)(yo + 171);
   }

   private void b(int xm, int ym) {
      int xo = this.B();
      int yo = this.C();
      String popupText = getLocalizedString("mco.selectServer.popup");
      List<String> strings = this.fontSplit(popupText, 100);
      if (!this.B) {
         this.E = 0;
         this.F = 0;
         this.G = true;
         this.a(null);
         if (this.hasWidget(this.i)) {
            this.removeWidget(this.i);
         }

         Realms.narrateNow(popupText);
      }

      if (this.w) {
         this.B = true;
      }

      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.7F);
      GlStateManager.enableBlend();
      RealmsScreen.bind("realms:textures/gui/realms/darken.png");
      GlStateManager.pushMatrix();
      int otherxo = 0;
      int otheryo = 32;
      RealmsScreen.blit(0, 32, 0.0F, 0.0F, this.width(), this.height() - 40 - 32, 310, 166);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RealmsScreen.bind("realms:textures/gui/realms/popup.png");
      GlStateManager.pushMatrix();
      RealmsScreen.blit(xo, yo, 0.0F, 0.0F, 310, 166, 310, 166);
      GlStateManager.popMatrix();
      RealmsScreen.bind(e[this.E]);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(xo + 7, yo + 7, 0.0F, 0.0F, 195, 152, 195, 152);
      GlStateManager.popMatrix();
      if (this.F % 95 < 5) {
         if (!this.G) {
            if (this.E == e.length - 1) {
               this.E = 0;
            } else {
               ++this.E;
            }

            this.G = true;
         }
      } else {
         this.G = false;
      }

      int index = 0;

      for(String s : strings) {
         int var10002 = this.width() / 2 + 52;
         ++index;
         this.drawString(s, var10002, yo + 10 * index - 3, 5000268, false);
      }

   }

   private int B() {
      return (this.width() - 310) / 2;
   }

   private int C() {
      return this.height() / 2 - 80;
   }

   private void a(int xm, int ym, int x, int y, boolean selectedOrHovered, boolean active) {
      int pendingInvitesCount = this.r;
      boolean hovering = this.b((double)xm, (double)ym);
      boolean selectionFrameAnimation = active && selectedOrHovered;
      if (selectionFrameAnimation) {
         float scale = 0.25F + (1.0F + RealmsMth.sin((float)this.s * 0.5F)) * 0.25F;
         int color = 0xFF000000 | (int)(scale * 64.0F) << 16 | (int)(scale * 64.0F) << 8 | (int)(scale * 64.0F) << 0;
         this.fillGradient(x - 2, y - 2, x + 18, y + 18, color, color);
         color = 0xFF000000 | (int)(scale * 255.0F) << 16 | (int)(scale * 255.0F) << 8 | (int)(scale * 255.0F) << 0;
         this.fillGradient(x - 2, y - 2, x + 18, y - 1, color, color);
         this.fillGradient(x - 2, y - 2, x - 1, y + 18, color, color);
         this.fillGradient(x + 17, y - 2, x + 18, y + 18, color, color);
         this.fillGradient(x - 2, y + 17, x + 18, y + 18, color, color);
      }

      RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      boolean grayedOut = active && selectedOrHovered;
      RealmsScreen.blit(x, y - 6, grayedOut ? 16.0F : 0.0F, 0.0F, 15, 25, 31, 25);
      GlStateManager.popMatrix();
      boolean invitationCountAnimation = active && pendingInvitesCount != 0;
      if (invitationCountAnimation) {
         int spritePos = (Math.min(pendingInvitesCount, 6) - 1) * 8;
         int yOff = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.s) * 0.57F), RealmsMth.cos((float)this.s * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x + 4, y + 4 + yOff, (float)spritePos, hovering ? 8.0F : 0.0F, 8, 8, 48, 16);
         GlStateManager.popMatrix();
      }

      int rx = xm + 12;
      boolean showTooltip = active && hovering;
      if (showTooltip) {
         String message = getLocalizedString(pendingInvitesCount == 0 ? "mco.invites.nopending" : "mco.invites.pending");
         int width = this.fontWidth(message);
         this.fillGradient(rx - 3, ym - 3, rx + width + 3, ym + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(message, rx, ym, -1);
      }

   }

   private boolean b(double xm, double ym) {
      int x1 = this.width() / 2 + 50;
      int x2 = this.width() / 2 + 66;
      int y1 = 11;
      int y2 = 23;
      if (this.r != 0) {
         x1 -= 3;
         x2 += 3;
         y1 -= 5;
         y2 += 5;
      }

      return (double)x1 <= xm && xm <= (double)x2 && (double)y1 <= ym && ym <= (double)y2;
   }

   public void a(RealmsServer server, RealmsScreen cancelScreen) {
      if (server != null) {
         try {
            if (!this.L.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.L.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.d = true;
         this.b(server, cancelScreen);
      }

   }

   private void b(RealmsServer server, RealmsScreen cancelScreen) {
      al longRunningMcoTaskScreen = new al(cancelScreen, new bi.e(this, cancelScreen, server, this.L));
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   private boolean h(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID());
   }

   private boolean i(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(Realms.getUUID()) && !serverData.expired;
   }

   private void a(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         this.p = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void a(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      if (this.s % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         if (daysLeft <= 0) {
            this.p = getLocalizedString("mco.selectServer.expires.soon");
         } else if (daysLeft == 1) {
            this.p = getLocalizedString("mco.selectServer.expires.day");
         } else {
            this.p = getLocalizedString("mco.selectServer.expires.days", new Object[]{daysLeft});
         }
      }

   }

   private void b(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         this.p = getLocalizedString("mco.selectServer.open");
      }

   }

   private void c(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         this.p = getLocalizedString("mco.selectServer.closed");
      }

   }

   private void d(int x, int y, int xm, int ym) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/leave_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
      GlStateManager.popMatrix();
      if (hovered) {
         this.p = getLocalizedString("mco.selectServer.leave");
      }

   }

   private void e(int x, int y, int xm, int ym) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 28 && ym >= y && ym <= y + 28 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/configure_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hovered ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
      GlStateManager.popMatrix();
      if (hovered) {
         this.p = getLocalizedString("mco.selectServer.configure");
      }

   }

   protected void a(String msg, int x, int y) {
      if (msg != null) {
         int index = 0;
         int width = 0;

         for(String s : msg.split("\n")) {
            int theWidth = this.fontWidth(s);
            if (theWidth > width) {
               width = theWidth;
            }
         }

         int rx = x - width - 5;
         int ry = y;
         if (rx < 0) {
            rx = x + 12;
         }

         for(String s : msg.split("\n")) {
            this.fillGradient(rx - 3, ry - (index == 0 ? 3 : 0) + index, rx + width + 3, ry + 8 + 3 + index, -1073741824, -1073741824);
            this.fontDrawShadow(s, rx, ry + index, 16777215);
            index += 10;
         }

      }
   }

   private void a(int xm, int ym, int x, int y, boolean hoveredOrFocused) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= y && ym <= y + 20) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/questionmark.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, hoveredOrFocused ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
      GlStateManager.popMatrix();
      if (hovered) {
         this.p = getLocalizedString("mco.selectServer.info");
      }

   }

   private void a(int xm, int ym, boolean unread, int x, int y, boolean selectedOrHovered, boolean active) {
      boolean hovered = false;
      if (xm >= x && xm <= x + 20 && ym >= y && ym <= y + 20) {
         hovered = true;
      }

      RealmsScreen.bind("realms:textures/gui/realms/news_icon.png");
      if (active) {
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);
      }

      GlStateManager.pushMatrix();
      boolean highlighted = active && selectedOrHovered;
      RealmsScreen.blit(x, y, highlighted ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
      GlStateManager.popMatrix();
      if (hovered && active) {
         this.p = getLocalizedString("mco.news");
      }

      if (unread && active) {
         int yOff = hovered ? 0 : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.s) * 0.57F), RealmsMth.cos((float)this.s * 0.35F))) * -6.0F);
         RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(x + 10, y + 2 + yOff, 40.0F, 0.0F, 8, 8, 48, 16);
         GlStateManager.popMatrix();
      }

   }

   private void D() {
      String text = "LOCAL!";
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scalef(1.5F, 1.5F, 1.5F);
      this.drawString("LOCAL!", 0, 0, 8388479);
      GlStateManager.popMatrix();
   }

   private void E() {
      String text = "STAGE!";
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
      GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scalef(1.5F, 1.5F, 1.5F);
      this.drawString("STAGE!", 0, 0, -256);
      GlStateManager.popMatrix();
   }

   public realms.b f() {
      return new realms.b(this.h);
   }

   public void g() {
      if (this.b() && this.x) {
         this.x = false;
      }

   }

   static {
      String version = bd.a();
      if (version != null) {
         a.info("Realms library version == " + version);
      }

   }

   class a extends RealmsButton {
      public a() {
         super(11, b.this.B() + 4, b.this.C() + 4, 12, 12, RealmsScreen.getLocalizedString("mco.selectServer.close"));
      }

      public void tick() {
         super.tick();
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         RealmsScreen.bind("realms:textures/gui/realms/cross_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         RealmsScreen.blit(this.x(), this.y(), 0.0F, this.getProxy().isHovered() ? 12.0F : 0.0F, 12, 12, 12, 24);
         GlStateManager.popMatrix();
         if (this.getProxy().isMouseOver((double)mouseX, (double)mouseY)) {
            b.this.p = this.getProxy().getMessage();
         }

      }

      public void onPress() {
         b.this.A();
      }
   }

   class b extends RealmsButton {
      public b() {
         super(9, b.this.width() - 62, 6, 20, 20, "");
      }

      public void tick() {
         this.setMessage(Realms.getLocalizedString("mco.news", new Object[0]));
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void onPress() {
         if (b.this.D != null) {
            bk.c(b.this.D);
            if (b.this.C) {
               bh.a data = bh.a();
               data.b = false;
               b.this.C = false;
               bh.a(data);
            }

         }
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         b.this.a(mouseX, mouseY, b.this.C, this.x(), this.y(), this.getProxy().isHovered(), this.active());
      }
   }

   class c extends RealmsButton {
      public c() {
         super(8, b.this.width() / 2 + 47, 6, 22, 22, "");
      }

      public void tick() {
         this.setMessage(Realms.getLocalizedString(b.this.r == 0 ? "mco.invites.nopending" : "mco.invites.pending", new Object[0]));
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void onPress() {
         ao pendingInvitationScreen = new ao(b.this.h);
         Realms.setScreen(pendingInvitationScreen);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         b.this.a(mouseX, mouseY, this.x(), this.y(), this.getProxy().isHovered(), this.active());
      }
   }

   class d extends RealmsObjectSelectionList {
      public d() {
         super(b.this.width(), b.this.height(), 32, b.this.height() - 40, 36);
      }

      public boolean isFocused() {
         return b.this.isFocused(this);
      }

      public boolean keyPressed(int eventKey, int scancode, int mods) {
         if (eventKey != 257 && eventKey != 32 && eventKey != 335) {
            return false;
         } else {
            RealmListEntry selected = this.getSelected();
            return selected == null ? super.keyPressed(eventKey, scancode, mods) : selected.mouseClicked(0.0, 0.0, 0);
         }
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && xm < (double)this.getScrollbarPosition() && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int x0 = b.this.i.getRowLeft();
            int x1 = this.getScrollbarPosition();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               b.this.K = b.this.K + 7;
               this.selectItem(slot);
            }

            return true;
         } else {
            return super.mouseClicked(xm, ym, buttonNum);
         }
      }

      public void selectItem(int item) {
         this.setSelected(item);
         if (item != -1) {
            RealmsServer server;
            if (b.this.a()) {
               if (item == 0) {
                  Realms.narrateNow(
                     new String[]{RealmsScreen.getLocalizedString("mco.trial.message.line1"), RealmsScreen.getLocalizedString("mco.trial.message.line2")}
                  );
                  server = null;
               } else {
                  server = (RealmsServer)b.this.q.get(item - 1);
               }
            } else {
               server = (RealmsServer)b.this.q.get(item);
            }

            b.this.a(server);
            if (server == null) {
               b.this.j = -1L;
            } else if (server.state == RealmsServer.b.c) {
               Realms.narrateNow(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized") + RealmsScreen.getLocalizedString("mco.gui.button"));
               b.this.j = -1L;
            } else {
               b.this.j = server.id;
               if (b.this.K >= 10 && b.this.k.active()) {
                  b.this.a(b.this.a(b.this.j), b.this);
               }

               Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{server.name}));
            }
         }
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (b.this.a()) {
            if (slot == 0) {
               b.this.x = true;
               return;
            }

            --slot;
         }

         if (slot < b.this.q.size()) {
            RealmsServer server = (RealmsServer)b.this.q.get(slot);
            if (server != null) {
               if (server.state == RealmsServer.b.c) {
                  b.this.j = -1L;
                  Realms.setScreen(new af(server, b.this));
               } else {
                  b.this.j = server.id;
               }

               if (b.this.p != null && b.this.p.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))) {
                  b.this.j = server.id;
                  b.this.f(server);
               } else if (b.this.p != null && b.this.p.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
                  b.this.j = server.id;
                  b.this.g(server);
               } else if (b.this.h(server) && server.expired && b.this.M) {
                  b.this.q();
               }

            }
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   class e extends RealmListEntry {
      final RealmsServer a;

      public e(RealmsServer serverData) {
         this.a = serverData;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(this.a, rowLeft, rowTop, mouseX, mouseY);
      }

      public boolean mouseClicked(double x, double y, int buttonNum) {
         if (this.a.state == RealmsServer.b.c) {
            b.this.j = -1L;
            Realms.setScreen(new af(this.a, b.this));
         } else {
            b.this.j = this.a.id;
         }

         return true;
      }

      private void a(RealmsServer serverData, int x, int y, int mouseX, int mouseY) {
         this.b(serverData, x + 36, y, mouseX, mouseY);
      }

      private void b(RealmsServer serverData, int x, int y, int mouseX, int mouseY) {
         if (serverData.state == RealmsServer.b.c) {
            RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlphaTest();
            GlStateManager.pushMatrix();
            RealmsScreen.blit(x + 10, y + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            GlStateManager.popMatrix();
            float scale = 0.5F + (1.0F + RealmsMth.sin((float)b.this.s * 0.25F)) * 0.25F;
            int textColor = 0xFF000000 | (int)(127.0F * scale) << 16 | (int)(255.0F * scale) << 8 | (int)(127.0F * scale);
            b.this.drawCenteredString(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"), x + 10 + 40 + 75, y + 12, textColor);
         } else {
            int dx = 225;
            int dy = 2;
            if (serverData.expired) {
               b.this.a(x + 225 - 14, y + 2, mouseX, mouseY);
            } else if (serverData.state == RealmsServer.b.a) {
               b.this.c(x + 225 - 14, y + 2, mouseX, mouseY);
            } else if (b.this.h(serverData) && serverData.daysLeft < 7) {
               b.this.a(x + 225 - 14, y + 2, mouseX, mouseY, serverData.daysLeft);
            } else if (serverData.state == RealmsServer.b.b) {
               b.this.b(x + 225 - 14, y + 2, mouseX, mouseY);
            }

            if (!b.this.h(serverData) && !realms.b.b) {
               b.this.d(x + 225, y + 2, mouseX, mouseY);
            } else {
               b.this.e(x + 225, y + 2, mouseX, mouseY);
            }

            if (!"0".equals(serverData.serverPing.nrOfPlayers)) {
               String coloredNumPlayers = realms.q.h + "" + serverData.serverPing.nrOfPlayers;
               b.this.drawString(coloredNumPlayers, x + 207 - b.this.fontWidth(coloredNumPlayers), y + 3, 8421504);
               if (mouseX >= x + 207 - b.this.fontWidth(coloredNumPlayers)
                  && mouseX <= x + 207
                  && mouseY >= y + 1
                  && mouseY <= y + 10
                  && mouseY < b.this.height() - 40
                  && mouseY > 32
                  && !b.this.b()) {
                  b.this.p = serverData.serverPing.playerList;
               }
            }

            if (b.this.h(serverData) && serverData.expired) {
               boolean hovered = false;
               GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.enableBlend();
               RealmsScreen.bind("minecraft:textures/gui/widgets.png");
               GlStateManager.pushMatrix();
               GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
               String expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredList");
               String expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredRenew");
               if (serverData.expiredTrial) {
                  expirationText = RealmsScreen.getLocalizedString("mco.selectServer.expiredTrial");
                  expirationButtonText = RealmsScreen.getLocalizedString("mco.selectServer.expiredSubscribe");
               }

               int buttonWidth = b.this.fontWidth(expirationButtonText) + 17;
               int buttonHeight = 16;
               int buttonX = x + b.this.fontWidth(expirationText) + 8;
               int buttonY = y + 13;
               if (mouseX >= buttonX
                  && mouseX < buttonX + buttonWidth
                  && mouseY > buttonY
                  && mouseY <= buttonY + 16 & mouseY < b.this.height() - 40
                  && mouseY > 32
                  && !b.this.b()) {
                  hovered = true;
                  b.this.M = true;
               }

               int yImage = hovered ? 2 : 1;
               RealmsScreen.blit(buttonX, buttonY, 0.0F, (float)(46 + yImage * 20), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(buttonX + buttonWidth / 2, buttonY, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(buttonX, buttonY + 8, 0.0F, (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256, 256);
               RealmsScreen.blit(
                  buttonX + buttonWidth / 2, buttonY + 8, (float)(200 - buttonWidth / 2), (float)(46 + yImage * 20 + 12), buttonWidth / 2, 8, 256, 256
               );
               GlStateManager.popMatrix();
               GlStateManager.disableBlend();
               int textHeight = y + 11 + 5;
               int buttonTextColor = hovered ? 16777120 : 16777215;
               b.this.drawString(expirationText, x + 2, textHeight + 1, 15553363);
               b.this.drawCenteredString(expirationButtonText, buttonX + buttonWidth / 2, textHeight + 1, buttonTextColor);
            } else {
               if (serverData.worldType.equals(RealmsServer.c.b)) {
                  int motdColor = 13413468;
                  String miniGameStr = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
                  int mgWidth = b.this.fontWidth(miniGameStr);
                  b.this.drawString(miniGameStr, x + 2, y + 12, 13413468);
                  b.this.drawString(serverData.getMinigameName(), x + 2 + mgWidth, y + 12, 7105644);
               } else {
                  b.this.drawString(serverData.getDescription(), x + 2, y + 12, 7105644);
               }

               if (!b.this.h(serverData)) {
                  b.this.drawString(serverData.owner, x + 2, y + 12 + 11, 5000268);
               }
            }

            b.this.drawString(serverData.getName(), x + 2, y + 1, 16777215);
            bj.a(serverData.ownerUUID, (Runnable)(() -> {
               GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               RealmsScreen.blit(x - 36, y, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
               RealmsScreen.blit(x - 36, y, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            }));
         }
      }
   }

   class f extends RealmListEntry {
      public f() {
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.a(index, rowLeft, rowTop, mouseX, mouseY);
      }

      public boolean mouseClicked(double x, double y, int buttonNum) {
         b.this.x = true;
         return true;
      }

      private void a(int i, int x, int y, int mouseX, int mouseY) {
         int ry = y + 8;
         int index = 0;
         String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
         boolean hovered = false;
         if (x <= mouseX && mouseX <= b.this.i.getScroll() && y <= mouseY && mouseY <= y + 32) {
            hovered = true;
         }

         int textColor = 8388479;
         if (hovered && !b.this.b()) {
            textColor = 6077788;
         }

         for(String s : msg.split("\\\\n")) {
            b.this.drawCenteredString(s, b.this.width() / 2, ry + index, textColor);
            index += 10;
         }

      }
   }

   class g extends RealmsButton {
      public g() {
         super(10, b.this.width() - 37, 6, 20, 20, RealmsScreen.getLocalizedString("mco.selectServer.info"));
      }

      public void tick() {
         super.tick();
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         b.this.a(mouseX, mouseY, this.x(), this.y(), this.getProxy().isHovered());
      }

      public void onPress() {
         b.this.x = !b.this.x;
      }
   }
}
