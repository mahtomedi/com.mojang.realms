package realms;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.ServerActivity;
import com.mojang.realmsclient.dto.ServerActivityList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class y extends RealmsScreen {
   private static final Logger a = LogManager.getLogger();
   private final RealmsScreen b;
   private final RealmsServer c;
   private volatile List<y.c> d = new ArrayList();
   private y.f e;
   private int f;
   private String g;
   private volatile List<y.e> h = new ArrayList();
   private final List<y.d> i = Arrays.asList(
      new y.d(79, 243, 29),
      new y.d(243, 175, 29),
      new y.d(243, 29, 190),
      new y.d(29, 165, 243),
      new y.d(29, 243, 130),
      new y.d(243, 29, 64),
      new y.d(29, 74, 243)
   );
   private int j;
   private long k;
   private int l;
   private Boolean m = false;
   private int n;
   private int o;
   private double p;
   private double q;
   private final int r = 0;

   public y(RealmsScreen lastScreen, RealmsServer serverData) {
      this.b = lastScreen;
      this.c = serverData;
      this.b();
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.f = this.width();
      this.e = new y.f();
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, this.height() - 30, 200, 20, getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(y.this.b);
         }
      });
      this.addWidget(this.e);
   }

   private y.d a() {
      if (this.j > this.i.size() - 1) {
         this.j = 0;
      }

      return (y.d)this.i.get(this.j++);
   }

   private void b() {
      (new Thread() {
         public void run() {
            g client = realms.g.a();

            try {
               ServerActivityList activities = client.b(y.this.c.id);
               y.this.d = y.this.a(activities);
               List<y.e> tempDayList = new ArrayList();

               for(y.c row : y.this.d) {
                  for(y.a activity : row.b) {
                     String day = new SimpleDateFormat("dd/MM").format(new Date(activity.b));
                     y.e theDay = new y.e(day, activity.b);
                     if (!tempDayList.contains(theDay)) {
                        tempDayList.add(theDay);
                     }
                  }
               }

               Collections.sort(tempDayList);

               for(y.c row : y.this.d) {
                  for(y.a activity : row.b) {
                     String day = new SimpleDateFormat("dd/MM").format(new Date(activity.b));
                     y.e theDay = new y.e(day, activity.b);
                     activity.d = tempDayList.indexOf(theDay) + 1;
                  }
               }

               y.this.h = tempDayList;
            } catch (o var10) {
               var10.printStackTrace();
            }

         }
      }).start();
   }

   private List<y.c> a(ServerActivityList serverActivityList) {
      List<y.c> activityRows = Lists.newArrayList();
      this.k = serverActivityList.periodInMillis;
      long base = System.currentTimeMillis() - serverActivityList.periodInMillis;

      for(ServerActivity sa : serverActivityList.serverActivities) {
         y.c activityRow = this.a(sa.profileUuid, activityRows);
         Calendar joinTime = Calendar.getInstance(TimeZone.getDefault());
         joinTime.setTimeInMillis(sa.joinTime);
         Calendar leaveTime = Calendar.getInstance(TimeZone.getDefault());
         leaveTime.setTimeInMillis(sa.leaveTime);
         y.a e = new y.a(base, joinTime.getTimeInMillis(), leaveTime.getTimeInMillis());
         if (activityRow == null) {
            String name = "";

            try {
               name = bk.a(sa.profileUuid);
            } catch (Exception var13) {
               a.error("Could not get name for " + sa.profileUuid, var13);
               continue;
            }

            activityRow = new y.c(sa.profileUuid, new ArrayList(), name, sa.profileUuid);
            activityRow.b.add(e);
            activityRows.add(activityRow);
         } else {
            activityRow.b.add(e);
         }
      }

      Collections.sort(activityRows);

      for(y.c row : activityRows) {
         row.c = this.a();
         Collections.sort(row.b);
      }

      this.m = activityRows.size() == 0;
      return activityRows;
   }

   private y.c a(String key, List<y.c> rows) {
      for(y.c row : rows) {
         if (row.a.equals(key)) {
            return row;
         }
      }

      return null;
   }

   public void tick() {
      super.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         Realms.setScreen(this.b);
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void render(int xm, int ym, float a) {
      this.g = null;
      this.renderBackground();

      for(y.c row : this.d) {
         int keyWidth = this.fontWidth(row.d) + 2;
         if (keyWidth > this.l) {
            this.l = keyWidth + 10;
         }
      }

      int keyRightPadding = 25;
      this.n = this.l + 25;
      int spaceLeft = this.f - this.n - 10;
      int days = this.h.size() < 1 ? 1 : this.h.size();
      this.o = spaceLeft / days;
      this.p = (double)this.o / 24.0;
      this.q = this.p / 60.0;
      if (this.e != null) {
         this.e.render(xm, ym, a);
      }

      if (this.d != null && this.d.size() > 0) {
         Tezzelator t = Tezzelator.instance;
         GlStateManager.disableTexture();
         t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
         t.vertex((double)this.n, (double)(this.height() - 40), 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)(this.n + 1), (double)(this.height() - 40), 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)(this.n + 1), 30.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.vertex((double)this.n, 30.0, 0.0).color(128, 128, 128, 255).endVertex();
         t.end();
         GlStateManager.enableTexture();

         for(y.e day : this.h) {
            int daysIndex = this.h.indexOf(day) + 1;
            this.drawString(day.a, this.n + (daysIndex - 1) * this.o + (this.o - this.fontWidth(day.a)) / 2 + 2, this.height() - 52, 16777215);
            GlStateManager.disableTexture();
            t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
            t.vertex((double)(this.n + daysIndex * this.o), (double)(this.height() - 40), 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex((double)(this.n + daysIndex * this.o + 1), (double)(this.height() - 40), 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex((double)(this.n + daysIndex * this.o + 1), 30.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex((double)(this.n + daysIndex * this.o), 30.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.end();
            GlStateManager.enableTexture();
         }
      }

      super.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.activity.title"), this.width() / 2, 10, 16777215);
      if (this.g != null) {
         this.a(this.g, xm, ym);
      }

      if (this.m) {
         this.drawCenteredString(
            getLocalizedString("mco.activity.noactivity", new Object[]{TimeUnit.DAYS.convert(this.k, TimeUnit.MILLISECONDS)}),
            this.width() / 2,
            this.height() / 2 - 20,
            16777215
         );
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
            this.fontDrawShadow(s, rx, ry + index, -1);
            index += 10;
         }

      }
   }

   static class a implements Comparable<y.a> {
      long a;
      long b;
      long c;
      int d;

      private a(long base, long start, long end) {
         this.a = base;
         this.b = start;
         this.c = end;
      }

      public int a(y.a o) {
         return (int)(this.b - o.b);
      }

      public int a() {
         String hour = new SimpleDateFormat("HH").format(new Date(this.b));
         return Integer.parseInt(hour);
      }

      public int b() {
         String minute = new SimpleDateFormat("mm").format(new Date(this.b));
         return Integer.parseInt(minute);
      }
   }

   static class b {
      double a;
      double b;
      String c;

      private b(double start, double width, String tooltip) {
         this.a = start;
         this.b = width;
         this.c = tooltip;
      }
   }

   static class c implements Comparable<y.c> {
      String a;
      List<y.a> b;
      y.d c;
      String d;
      String e;

      public int a(y.c o) {
         return this.d.compareTo(o.d);
      }

      c(String key, List<y.a> activities, String name, String uuid) {
         this.a = key;
         this.b = activities;
         this.d = name;
         this.e = uuid;
      }
   }

   static class d {
      int a;
      int b;
      int c;

      d(int r, int g, int b) {
         this.a = r;
         this.b = g;
         this.c = b;
      }
   }

   static class e implements Comparable<y.e> {
      String a;
      Long b;

      public int a(y.e o) {
         return this.b.compareTo(o.b);
      }

      e(String day, Long timestamp) {
         this.a = day;
         this.b = timestamp;
      }

      public boolean equals(Object d) {
         if (!(d instanceof y.e)) {
            return false;
         } else {
            y.e that = (y.e)d;
            return this.a.equals(that.a);
         }
      }
   }

   class f extends RealmsScrolledSelectionList {
      public f() {
         super(y.this.width(), y.this.height(), 30, y.this.height() - 40, y.this.fontLineHeight() + 1);
      }

      public int getItemCount() {
         return y.this.d.size();
      }

      public boolean isSelectedItem(int item) {
         return false;
      }

      public int getMaxPosition() {
         return this.getItemCount() * (y.this.fontLineHeight() + 1) + 15;
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         if (y.this.d != null && y.this.d.size() > i) {
            y.c row = (y.c)y.this.d.get(i);
            y.this.drawString(row.d, 20, y, ((y.c)y.this.d.get(i)).e.equals(Realms.getUUID()) ? 8388479 : 16777215);
            int r = row.c.a;
            int g = row.c.b;
            int b = row.c.c;
            GlStateManager.disableTexture();
            t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
            t.vertex((double)(y.this.n - 8), (double)y + 6.5, 0.0).color(r, g, b, 255).endVertex();
            t.vertex((double)(y.this.n - 3), (double)y + 6.5, 0.0).color(r, g, b, 255).endVertex();
            t.vertex((double)(y.this.n - 3), (double)y + 1.5, 0.0).color(r, g, b, 255).endVertex();
            t.vertex((double)(y.this.n - 8), (double)y + 1.5, 0.0).color(r, g, b, 255).endVertex();
            t.end();
            GlStateManager.enableTexture();
            List<y.b> toRender = new ArrayList();

            for(y.a activity : row.b) {
               int minute = activity.b();
               int hour = activity.a();
               double itemWidth = y.this.q * (double)TimeUnit.MINUTES.convert(activity.c - activity.b, TimeUnit.MILLISECONDS);
               if (itemWidth < 3.0) {
                  itemWidth = 3.0;
               }

               double pos = (double)(y.this.n + (y.this.o * activity.d - y.this.o)) + (double)hour * y.this.p + (double)minute * y.this.q;
               SimpleDateFormat format = new SimpleDateFormat("HH:mm");
               Date startDate = new Date(activity.b);
               Date endDate = new Date(activity.c);
               int length = (int)Math.ceil((double)TimeUnit.SECONDS.convert(activity.c - activity.b, TimeUnit.MILLISECONDS) / 60.0);
               if (length < 1) {
                  length = 1;
               }

               String tooltip = "[" + format.format(startDate) + " - " + format.format(endDate) + "] " + length + (length > 1 ? " minutes" : " minute");
               boolean exists = false;

               for(y.b render : toRender) {
                  if (render.a + render.b >= pos - 0.5) {
                     double overlap = render.a + render.b - pos;
                     double padding = Math.max(0.0, pos - (render.a + render.b));
                     render.b = render.b - Math.max(0.0, overlap) + itemWidth + padding;
                     render.c = render.c + "\n" + tooltip;
                     exists = true;
                     break;
                  }
               }

               if (!exists) {
                  toRender.add(new y.b(pos, itemWidth, tooltip));
               }
            }

            for(y.b render : toRender) {
               GlStateManager.disableTexture();
               t.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
               t.vertex(render.a, (double)y + 6.5, 0.0).color(r, g, b, 255).endVertex();
               t.vertex(render.a + render.b, (double)y + 6.5, 0.0).color(r, g, b, 255).endVertex();
               t.vertex(render.a + render.b, (double)y + 1.5, 0.0).color(r, g, b, 255).endVertex();
               t.vertex(render.a, (double)y + 1.5, 0.0).color(r, g, b, 255).endVertex();
               t.end();
               GlStateManager.enableTexture();
               if ((double)mouseX >= render.a
                  && (double)mouseX <= render.a + render.b
                  && (double)mouseY >= (double)y + 1.5
                  && (double)mouseY <= (double)y + 6.5) {
                  y.this.g = render.c.trim();
               }
            }

            RealmsScreen.bind("realms:textures/gui/realms/user_icon.png");
            bj.a(((y.c)y.this.d.get(i)).e, (Runnable)(() -> {
               GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               RealmsScreen.blit(10, y, 8.0F, 8.0F, 8, 8, 8, 8, 64, 64);
               RealmsScreen.blit(10, y, 40.0F, 8.0F, 8, 8, 8, 8, 64, 64);
            }));
         }

      }

      public int getScrollbarPosition() {
         return this.width() - 7;
      }
   }
}
