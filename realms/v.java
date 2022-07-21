package realms;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class v {
   private static final Logger a = LogManager.getLogger();
   private final ScheduledExecutorService b = Executors.newScheduledThreadPool(3);
   private volatile boolean c = true;
   private final v.c d = new v.c();
   private final v.b e = new v.b();
   private final v.e f = new v.e();
   private final v.a g = new v.a();
   private final v.f h = new v.f();
   private final Set<RealmsServer> i = Sets.newHashSet();
   private List<RealmsServer> j = Lists.newArrayList();
   private RealmsServerPlayerLists k;
   private int l;
   private boolean m;
   private boolean n;
   private String o;
   private ScheduledFuture<?> p;
   private ScheduledFuture<?> q;
   private ScheduledFuture<?> r;
   private ScheduledFuture<?> s;
   private ScheduledFuture<?> t;
   private final Map<v.d, Boolean> u = new ConcurrentHashMap(v.d.values().length);

   public boolean a() {
      return this.c;
   }

   public synchronized void b() {
      if (this.c) {
         this.c = false;
         this.n();
         this.m();
      }

   }

   public synchronized void a(List<v.d> tasks) {
      if (this.c) {
         this.c = false;
         this.n();

         for(v.d task : tasks) {
            this.u.put(task, false);
            switch(task) {
               case a:
                  this.p = this.b.scheduleAtFixedRate(this.d, 0L, 60L, TimeUnit.SECONDS);
                  break;
               case b:
                  this.q = this.b.scheduleAtFixedRate(this.e, 0L, 10L, TimeUnit.SECONDS);
                  break;
               case c:
                  this.r = this.b.scheduleAtFixedRate(this.f, 0L, 60L, TimeUnit.SECONDS);
                  break;
               case d:
                  this.s = this.b.scheduleAtFixedRate(this.g, 0L, 10L, TimeUnit.SECONDS);
                  break;
               case e:
                  this.t = this.b.scheduleAtFixedRate(this.h, 0L, 300L, TimeUnit.SECONDS);
            }
         }
      }

   }

   public boolean a(v.d task) {
      Boolean result = (Boolean)this.u.get(task);
      return result == null ? false : result;
   }

   public void c() {
      for(v.d task : this.u.keySet()) {
         this.u.put(task, false);
      }

   }

   public synchronized void d() {
      this.k();
      this.b();
   }

   public synchronized List<RealmsServer> e() {
      return Lists.newArrayList(this.j);
   }

   public synchronized int f() {
      return this.l;
   }

   public synchronized boolean g() {
      return this.m;
   }

   public synchronized RealmsServerPlayerLists h() {
      return this.k;
   }

   public synchronized boolean i() {
      return this.n;
   }

   public synchronized String j() {
      return this.o;
   }

   public synchronized void k() {
      this.c = true;
      this.n();
   }

   private void m() {
      for(v.d task : v.d.values()) {
         this.u.put(task, false);
      }

      this.p = this.b.scheduleAtFixedRate(this.d, 0L, 60L, TimeUnit.SECONDS);
      this.q = this.b.scheduleAtFixedRate(this.e, 0L, 10L, TimeUnit.SECONDS);
      this.r = this.b.scheduleAtFixedRate(this.f, 0L, 60L, TimeUnit.SECONDS);
      this.s = this.b.scheduleAtFixedRate(this.g, 0L, 10L, TimeUnit.SECONDS);
      this.t = this.b.scheduleAtFixedRate(this.h, 0L, 300L, TimeUnit.SECONDS);
   }

   private void n() {
      try {
         if (this.p != null) {
            this.p.cancel(false);
         }

         if (this.q != null) {
            this.q.cancel(false);
         }

         if (this.r != null) {
            this.r.cancel(false);
         }

         if (this.s != null) {
            this.s.cancel(false);
         }

         if (this.t != null) {
            this.t.cancel(false);
         }
      } catch (Exception var2) {
         a.error("Failed to cancel Realms tasks", var2);
      }

   }

   private synchronized void b(List<RealmsServer> newServers) {
      int removedCnt = 0;

      for(RealmsServer server : this.i) {
         if (newServers.remove(server)) {
            ++removedCnt;
         }
      }

      if (removedCnt == 0) {
         this.i.clear();
      }

      this.j = newServers;
   }

   public synchronized void a(RealmsServer server) {
      this.j.remove(server);
      this.i.add(server);
   }

   private void c(List<RealmsServer> servers) {
      Collections.sort(servers, new RealmsServer.a(Realms.getName()));
   }

   private boolean o() {
      return !this.c;
   }

   class a implements Runnable {
      private a() {
      }

      public void run() {
         if (v.this.o()) {
            this.a();
         }

      }

      private void a() {
         try {
            g client = realms.g.a();
            if (client != null) {
               v.this.k = client.f();
               v.this.u.put(v.d.d, true);
            }
         } catch (Exception var2) {
            v.a.error("Couldn't get live stats", var2);
         }

      }
   }

   class b implements Runnable {
      private b() {
      }

      public void run() {
         if (v.this.o()) {
            this.a();
         }

      }

      private void a() {
         try {
            g client = realms.g.a();
            if (client != null) {
               v.this.l = client.j();
               v.this.u.put(v.d.b, true);
            }
         } catch (Exception var2) {
            v.a.error("Couldn't get pending invite count", var2);
         }

      }
   }

   class c implements Runnable {
      private c() {
      }

      public void run() {
         if (v.this.o()) {
            this.a();
         }

      }

      private void a() {
         try {
            g client = realms.g.a();
            if (client != null) {
               List<RealmsServer> servers = client.e().servers;
               if (servers != null) {
                  v.this.c(servers);
                  v.this.b(servers);
                  v.this.u.put(v.d.a, true);
               } else {
                  v.a.warn("Realms server list was null or empty");
               }
            }
         } catch (Exception var3) {
            v.this.u.put(v.d.a, true);
            v.a.error("Couldn't get server list", var3);
         }

      }
   }

   public static enum d {
      a,
      b,
      c,
      d,
      e;
   }

   class e implements Runnable {
      private e() {
      }

      public void run() {
         if (v.this.o()) {
            this.a();
         }

      }

      private void a() {
         try {
            g client = realms.g.a();
            if (client != null) {
               v.this.m = client.n();
               v.this.u.put(v.d.c, true);
            }
         } catch (Exception var2) {
            v.a.error("Couldn't get trial availability", var2);
         }

      }
   }

   class f implements Runnable {
      private f() {
      }

      public void run() {
         if (v.this.o()) {
            this.a();
         }

      }

      private void a() {
         try {
            g client = realms.g.a();
            if (client != null) {
               RealmsNews fetchedNews = null;

               try {
                  fetchedNews = client.m();
               } catch (Exception var5) {
               }

               bg.a data = bg.a();
               if (fetchedNews != null) {
                  String fetchedNewsLink = fetchedNews.newsLink;
                  if (fetchedNewsLink != null && !fetchedNewsLink.equals(data.a)) {
                     data.b = true;
                     data.a = fetchedNewsLink;
                     bg.a(data);
                  }
               }

               v.this.n = data.b;
               v.this.o = data.a;
               v.this.u.put(v.d.e, true);
            }
         } catch (Exception var6) {
            v.a.error("Couldn't get unread news", var6);
         }

      }
   }
}
