package realms;

import com.google.common.collect.Lists;
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
   private boolean c;
   private static final String[] d = new String[]{
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
   private static final v e = new v();
   private static int f = -1;
   private final RealmsScreen g;
   private volatile realms.b.d h;
   private long i = -1L;
   private RealmsButton j;
   private RealmsButton k;
   private RealmsButton l;
   private RealmsButton m;
   private RealmsButton n;
   private String o;
   private List<RealmsServer> p = Lists.newArrayList();
   private volatile int q;
   private int r;
   private static volatile boolean s;
   private static volatile boolean t;
   private static volatile boolean u;
   private boolean v;
   private boolean w;
   private boolean x;
   private volatile boolean y;
   private volatile boolean z;
   private volatile boolean A;
   private volatile boolean B;
   private volatile String C;
   private int D;
   private int E;
   private boolean F;
   private static RealmsScreen G;
   private static boolean H;
   private List<realms.a> I;
   private int J;
   private ReentrantLock K = new ReentrantLock();
   private boolean L;
   private realms.b.g M;
   private realms.b.c N;
   private realms.b.b O;
   private RealmsButton P;
   private RealmsButton Q;
   private RealmsButton R;

   public b(RealmsScreen lastScreen) {
      this.g = lastScreen;
   }

   public boolean a() {
      if (this.l() && this.v) {
         if (this.y && !this.z) {
            return true;
         } else {
            for(RealmsServer realmsServer : this.p) {
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
      if (!this.l() || !this.v) {
         return false;
      } else if (this.w) {
         return true;
      } else {
         return this.y && !this.z && this.p.isEmpty() ? true : this.p.isEmpty();
      }
   }

   public void init() {
      this.I = Lists.newArrayList(
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
      if (G != null) {
         Realms.setScreen(G);
      } else {
         this.K = new ReentrantLock();
         if (u && !this.l()) {
            this.u();
         }

         this.s();
         this.t();
         if (!this.c) {
            Realms.setConnectedToRealms(false);
         }

         this.setKeyboardHandlerSendRepeatsToGui(true);
         if (this.l()) {
            e.d();
         }

         this.A = false;
         this.d();
      }
   }

   private boolean l() {
      return t && s;
   }

   public void c() {
      this.buttonsAdd(this.m = new RealmsButton(1, this.width() / 2 - 202, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.configure")) {
         public void onPress() {
            b.this.f(b.this.a(b.this.i));
         }
      });
      this.buttonsAdd(this.j = new RealmsButton(3, this.width() / 2 - 98, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.play")) {
         public void onPress() {
            b.this.p();
         }
      });
      this.buttonsAdd(this.k = new RealmsButton(2, this.width() / 2 + 6, this.height() - 32, 98, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            if (!b.this.x) {
               Realms.setScreen(b.this.g);
            }

         }
      });
      this.buttonsAdd(this.l = new RealmsButton(0, this.width() / 2 + 110, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.expiredRenew")) {
         public void onPress() {
            b.this.q();
         }
      });
      this.buttonsAdd(this.n = new RealmsButton(7, this.width() / 2 - 202, this.height() - 32, 98, 20, getLocalizedString("mco.selectServer.leave")) {
         public void onPress() {
            b.this.g(b.this.a(b.this.i));
         }
      });
      this.buttonsAdd(this.N = new realms.b.c());
      this.buttonsAdd(this.O = new realms.b.b());
      this.buttonsAdd(this.M = new realms.b.g());
      this.buttonsAdd(this.R = new realms.b.a());
      this.buttonsAdd(this.P = new RealmsButton(6, this.width() / 2 + 52, this.C() + 137 - 20, 98, 20, getLocalizedString("mco.selectServer.trial")) {
         public void onPress() {
            b.this.r();
         }
      });
      this.buttonsAdd(this.Q = new RealmsButton(5, this.width() / 2 + 52, this.C() + 160 - 20, 98, 20, getLocalizedString("mco.selectServer.buy")) {
         public void onPress() {
            bj.c("https://minecraft.net/realms");
         }
      });
      RealmsServer server = this.a(this.i);
      this.a(server);
   }

   private void a(RealmsServer server) {
      this.j.active(this.b(server) && !this.b());
      this.l.setVisible(this.c(server));
      this.m.setVisible(this.d(server));
      this.n.setVisible(this.e(server));
      this.P.setVisible(this.b() && this.y && !this.z);
      this.Q.setVisible(this.b());
      this.R.setVisible(this.b() && this.w);
      this.l.active(!this.b());
      this.m.active(!this.b());
      this.n.active(!this.b());
      this.P.active(!this.b());
      this.O.active(true);
      this.N.active(true);
      this.k.active(true);
      this.M.active(!this.b());
   }

   private boolean m() {
      return (!this.b() || this.w) && this.l() && this.v;
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
      if (this.l() && this.v) {
         this.c();
      }

      this.h = new realms.b.d();
      this.h.setLeftPos(-15);
      if (f != -1) {
         this.h.scroll(f);
      }

      this.addWidget(this.h);
      this.focusOn(this.h);
   }

   public void tick() {
      this.tickButtons();
      this.x = false;
      ++this.r;
      --this.J;
      if (this.J < 0) {
         this.J = 0;
      }

      if (this.l()) {
         e.b();
         if (e.a(v.d.a)) {
            List<RealmsServer> newServers = e.e();
            if (newServers != null) {
               boolean ownsNonExpiredRealmServer = false;

               for(RealmsServer retrievedServer : newServers) {
                  if (this.i(retrievedServer)) {
                     ownsNonExpiredRealmServer = true;
                  }
               }

               if (this.a()) {
                  this.h.addEntry(new realms.b.f());
               }

               this.p = newServers;

               for(RealmsServer server : this.p) {
                  this.h.addEntry(new realms.b.e(server));
               }

               if (!H && ownsNonExpiredRealmServer) {
                  H = true;
                  this.n();
               }
            }

            if (!this.v) {
               this.v = true;
               this.c();
            }
         }

         if (e.a(v.d.b)) {
            this.q = e.f();
         }

         if (e.a(v.d.c) && !this.z) {
            boolean newStatus = e.g();
            if (newStatus != this.y && this.b()) {
               this.y = newStatus;
               this.A = false;
            } else {
               this.y = newStatus;
            }
         }

         if (e.a(v.d.d)) {
            RealmsServerPlayerLists playerLists = e.h();

            for(RealmsServerPlayerList playerList : playerLists.servers) {
               for(RealmsServer server : this.p) {
                  if (server.id == playerList.serverId) {
                     server.updateServerPing(playerList);
                     break;
                  }
               }
            }
         }

         if (e.a(v.d.e)) {
            this.B = e.i();
            this.C = e.j();
         }

         e.c();
         if (this.b()) {
            ++this.E;
         }

         if (this.M != null) {
            this.M.setVisible(this.m());
         }

      }
   }

   private void a(String url) {
      Realms.setClipboard(url);
      bj.c(url);
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

      for(RealmsServer server : this.p) {
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
      this.z = createdTrial;
   }

   private void p() {
      RealmsServer server = this.a(this.i);
      if (server != null) {
         this.a(server, this);
      }
   }

   private void q() {
      RealmsServer server = this.a(this.i);
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
      if (this.y && !this.z) {
         Realms.setScreen(new af(this));
      }
   }

   private void s() {
      if (!u) {
         u = true;
         (new Thread("MCO Compatability Checker #1") {
               public void run() {
                  realms.g client = realms.g.a();
   
                  try {
                     realms.g.a versionResponse = client.i();
                     if (versionResponse.equals(realms.g.a.b)) {
                        realms.b.G = new ab(b.this.g, true);
                        Realms.setScreen(realms.b.G);
                     } else if (versionResponse.equals(realms.g.a.c)) {
                        realms.b.G = new ab(b.this.g, false);
                        Realms.setScreen(realms.b.G);
                     } else {
                        b.this.u();
                     }
                  } catch (o var3) {
                     realms.b.u = false;
                     realms.b.a.error("Couldn't connect to realms: ", var3.toString());
                     if (var3.a == 401) {
                        realms.b.G = new ah(
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.title"),
                           RealmsScreen.getLocalizedString("mco.error.invalid.session.message"),
                           b.this.g
                        );
                        Realms.setScreen(realms.b.G);
                     } else {
                        Realms.setScreen(new ah(var3, b.this.g));
                     }
                  } catch (IOException var4) {
                     realms.b.u = false;
                     realms.b.a.error("Couldn't connect to realms: ", var4.getMessage());
                     Realms.setScreen(new ah(var4.getMessage(), b.this.g));
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
                  realms.b.s = true;
               } else {
                  realms.b.a.info("Realms is not available for this user");
                  realms.b.s = false;
                  Realms.setScreen(new am(b.this.g));
               }

               realms.b.t = true;
            } catch (o var3) {
               realms.b.a.error("Couldn't connect to realms: ", var3.toString());
               Realms.setScreen(new ah(var3, b.this.g));
            } catch (IOException var4) {
               realms.b.a.error("Couldn't connect to realms: ", var4.getMessage());
               Realms.setScreen(new ah(var4.getMessage(), b.this.g));
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
                     realms.b.e.d();
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
                     realms.b.e.d();
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
      e.d();
   }

   private void y() {
      e.k();
   }

   private void f(RealmsServer selectedServer) {
      if (Realms.getUUID().equals(selectedServer.ownerUUID) || b) {
         this.z();
         cva minecraft = cva.v();
         minecraft.execute(() -> minecraft.a(new ac(this, selectedServer.id).getProxy()));
      }

   }

   private void g(RealmsServer selectedServer) {
      if (!Realms.getUUID().equals(selectedServer.ownerUUID)) {
         this.z();
         String line2 = getLocalizedString("mco.configure.world.leave.question.line1");
         String line3 = getLocalizedString("mco.configure.world.leave.question.line2");
         Realms.setScreen(new aj(this, aj.a.b, line2, line3, true, 4));
      }

   }

   private void z() {
      f = this.h.getScroll();
   }

   private RealmsServer a(long id) {
      for(RealmsServer server : this.p) {
         if (server.id == id) {
            return server;
         }
      }

      return null;
   }

   private int b(long serverId) {
      for(int i = 0; i < this.p.size(); ++i) {
         if (((RealmsServer)this.p.get(i)).id == serverId) {
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
                     RealmsServer server = b.this.a(b.this.i);
                     if (server != null) {
                        realms.g client = realms.g.a();
                        client.d(server.id);
                        realms.b.e.a(server);
                        b.this.p.remove(server);
                        b.this.i = -1L;
                        b.this.j.active(false);
                     }
                  } catch (o var3) {
                     realms.b.a.error("Couldn't configure world");
                     Realms.setScreen(new ah(var3, b.this));
                  }

               }
            }).start();
         }

         Realms.setScreen(this);
      }

   }

   public void e() {
      this.i = -1L;
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
         case 256:
            this.I.forEach(realms.a::a);
            this.A();
            return true;
         default:
            return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void A() {
      if (this.b() && this.w) {
         this.w = false;
      } else {
         Realms.setScreen(this.g);
      }

   }

   public boolean charTyped(char ch, int mods) {
      this.I.forEach(c -> c.a(ch));
      return true;
   }

   public void render(int xm, int ym, float a) {
      this.L = false;
      this.o = null;
      this.renderBackground();
      this.h.render(xm, ym, a);
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
         if (this.A) {
            this.a(null);
            if (!this.hasWidget(this.h)) {
               this.addWidget(this.h);
            }

            RealmsServer server = this.a(this.i);
            this.j.active(this.b(server));
         }

         this.A = false;
      }

      super.render(xm, ym, a);
      if (this.o != null) {
         this.a(this.o, xm, ym);
      }

      if (this.y && !this.z && this.b()) {
         RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.pushMatrix();
         int ySprite = 0;
         if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
            ySprite = 8;
         }

         int yo = this.height() / 2 - 83 - 3;
         int buttonHeight = yo + 147 - 20;
         RealmsScreen.blit(this.width() / 2 + 52 + 83, buttonHeight - 4, 0.0F, (float)ySprite, 8, 8, 8, 16);
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
      if (this.a(x, y) && this.w) {
         this.w = false;
         this.x = true;
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
      if (!this.A) {
         this.D = 0;
         this.E = 0;
         this.F = true;
         this.a(null);
         if (this.hasWidget(this.h)) {
            this.removeWidget(this.h);
         }

         Realms.narrateNow(popupText);
      }

      if (this.v) {
         this.A = true;
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
      RealmsScreen.bind(d[this.D]);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(xo + 7, yo + 7, 0.0F, 0.0F, 195, 152, 195, 152);
      GlStateManager.popMatrix();
      if (this.E % 95 < 5) {
         if (!this.F) {
            if (this.D == d.length - 1) {
               this.D = 0;
            } else {
               ++this.D;
            }

            this.F = true;
         }
      } else {
         this.F = false;
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
      int pendingInvitesCount = this.q;
      boolean hovering = this.b((double)xm, (double)ym);
      boolean selectionFrameAnimation = active && selectedOrHovered;
      if (selectionFrameAnimation) {
         float scale = 0.25F + (1.0F + RealmsMth.sin((float)this.r * 0.5F)) * 0.25F;
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
         int yOff = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.r) * 0.57F), RealmsMth.cos((float)this.r * 0.35F))) * -6.0F);
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
      if (this.q != 0) {
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
            if (!this.K.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.K.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.c = true;
         this.b(server, cancelScreen);
      }

   }

   private void b(RealmsServer server, RealmsScreen cancelScreen) {
      ak longRunningMcoTaskScreen = new ak(cancelScreen, new bh.e(this, cancelScreen, server, this.K));
      longRunningMcoTaskScreen.a();
      Realms.setScreen(longRunningMcoTaskScreen);
   }

   public void a(int item) {
      if (this.a()) {
         if (item == 0) {
            this.a(null);
            return;
         }

         --item;
      }

      if (item < this.p.size()) {
         RealmsServer server = (RealmsServer)this.p.get(item);
         if (server.state == RealmsServer.b.c) {
            this.a(null);
            this.i = -1L;
         } else {
            this.i = server.id;
            this.a(server);
            if (this.J >= 10 && this.j.active()) {
               this.a(this.a(this.i), this);
            }

         }
      }
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
         this.o = getLocalizedString("mco.selectServer.expired");
      }

   }

   private void a(int x, int y, int xm, int ym, int daysLeft) {
      RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      if (this.r % 20 < 10) {
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         RealmsScreen.blit(x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         if (daysLeft <= 0) {
            this.o = getLocalizedString("mco.selectServer.expires.soon");
         } else if (daysLeft == 1) {
            this.o = getLocalizedString("mco.selectServer.expires.day");
         } else {
            this.o = getLocalizedString("mco.selectServer.expires.days", new Object[]{daysLeft});
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
         this.o = getLocalizedString("mco.selectServer.open");
      }

   }

   private void c(int x, int y, int xm, int ym) {
      RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      RealmsScreen.blit(x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      GlStateManager.popMatrix();
      if (xm >= x && xm <= x + 9 && ym >= y && ym <= y + 27 && ym < this.height() - 40 && ym > 32 && !this.b()) {
         this.o = getLocalizedString("mco.selectServer.closed");
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
         this.o = getLocalizedString("mco.selectServer.leave");
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
         this.o = getLocalizedString("mco.selectServer.configure");
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
         this.o = getLocalizedString("mco.selectServer.info");
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
         this.o = getLocalizedString("mco.news");
      }

      if (unread && active) {
         int yOff = hovered ? 0 : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.r) * 0.57F), RealmsMth.cos((float)this.r * 0.35F))) * -6.0F);
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
      return new realms.b(this.g);
   }

   public void g() {
      if (this.b() && this.w) {
         this.w = false;
      }

   }

   static {
      String version = bc.a();
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
            b.this.o = this.getProxy().getMessage();
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
         if (b.this.C != null) {
            bj.c(b.this.C);
            if (b.this.B) {
               bg.a data = bg.a();
               data.b = false;
               b.this.B = false;
               bg.a(data);
            }

         }
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         b.this.a(mouseX, mouseY, b.this.B, this.x(), this.y(), this.getProxy().isHovered(), this.active());
      }
   }

   class c extends RealmsButton {
      public c() {
         super(8, b.this.width() / 2 + 47, 6, 22, 22, "");
      }

      public void tick() {
         this.setMessage(Realms.getLocalizedString(b.this.q == 0 ? "mco.invites.nopending" : "mco.invites.pending", new Object[0]));
      }

      public void render(int xm, int ym, float a) {
         super.render(xm, ym, a);
      }

      public void onPress() {
         an pendingInvitationScreen = new an(b.this.g);
         Realms.setScreen(pendingInvitationScreen);
      }

      public void renderButton(int mouseX, int mouseY, float a) {
         b.this.a(mouseX, mouseY, this.x(), this.y(), this.getProxy().isHovered(), this.active());
      }
   }

   class d extends RealmsObjectSelectionList {
      public d() {
         super(b.this.width() + 15, b.this.height(), 32, b.this.height() - 40, 36);
      }

      public int getRowWidth() {
         return (int)((double)this.width() * 0.6);
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
            int x0 = this.width() / 2 - 160;
            int x1 = this.getScrollbarPosition();
            int clickSlotPos = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int slot = clickSlotPos / this.itemHeight();
            if (xm >= (double)x0 && xm <= (double)x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
               this.itemClicked(clickSlotPos, slot, xm, ym, this.width());
               b.this.J = b.this.J + 7;
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
            if (b.this.a()) {
               if (item == 0) {
                  String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + RealmsScreen.getLocalizedString("mco.trial.message.line2");
                  Realms.narrateNow(msg);
                  b.this.a(item);
               } else {
                  --item;
               }
            }

            if (item >= b.this.p.size()) {
               return;
            }

            RealmsServer server = (RealmsServer)b.this.p.get(item);
            if (server == null) {
               return;
            }

            if (server.state == RealmsServer.b.c) {
               Realms.narrateNow(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"));
            } else {
               Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", new Object[]{((RealmsServer)b.this.p.get(item)).name}));
            }
         }

         b.this.a(item);
         int newIndex = item;
         if (b.this.a()) {
            if (item == 0) {
               b.this.a(null);
               return;
            }

            newIndex = item - 1;
         }

         if (newIndex < b.this.p.size()) {
            RealmsServer server = (RealmsServer)b.this.p.get(newIndex);
            if (server != null) {
               if (server.state == RealmsServer.b.c) {
                  b.this.a(null);
               } else {
                  b.this.a((RealmsServer)b.this.p.get(newIndex));
               }

            }
         }
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (b.this.a()) {
            if (slot == 0) {
               b.this.w = true;
               return;
            }

            --slot;
         }

         if (slot < b.this.p.size()) {
            RealmsServer server = (RealmsServer)b.this.p.get(slot);
            if (server != null) {
               if (server.state == RealmsServer.b.c) {
                  b.this.i = -1L;
                  Realms.setScreen(new ae(server, b.this));
               } else {
                  b.this.i = server.id;
               }

               if (b.this.o != null && b.this.o.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))) {
                  b.this.i = server.id;
                  b.this.f(server);
               } else if (b.this.o != null && b.this.o.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
                  b.this.i = server.id;
                  b.this.g(server);
               } else if (b.this.h(server) && server.expired && b.this.L) {
                  b.this.q();
               }

            }
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
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
            b.this.i = -1L;
            Realms.setScreen(new ae(this.a, b.this));
         } else {
            b.this.i = this.a.id;
         }

         return true;
      }

      private void a(RealmsServer serverData, int x, int y, int mouseX, int mouseY) {
         if (serverData.state == RealmsServer.b.c) {
            RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlphaTest();
            GlStateManager.pushMatrix();
            RealmsScreen.blit(x + 10, y + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            GlStateManager.popMatrix();
            float scale = 0.5F + (1.0F + RealmsMth.sin((float)b.this.r * 0.25F)) * 0.25F;
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
                  b.this.o = serverData.serverPing.playerList;
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
                  b.this.L = true;
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
            bi.a(serverData.ownerUUID, (Runnable)(() -> {
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
         b.this.w = true;
         return true;
      }

      private void a(int i, int x, int y, int mouseX, int mouseY) {
         int ry = y + 8;
         int index = 0;
         String msg = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
         boolean hovered = false;
         if (x <= mouseX && mouseX <= b.this.h.getScroll() && y <= mouseY && mouseY <= y + 32) {
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
         b.this.w = !b.this.w;
      }
   }
}
