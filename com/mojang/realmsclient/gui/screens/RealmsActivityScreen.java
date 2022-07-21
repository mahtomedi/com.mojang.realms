package com.mojang.realmsclient.gui.screens;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.ServerActivity;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class RealmsActivityScreen extends RealmsScreen {
   private final RealmsScreen lastScreen;
   private final long serverId;
   private volatile List<RealmsActivityScreen.ActivityRow> activityMap = new ArrayList();
   private RealmsActivityScreen.DetailsList list;
   private int matrixWidth;
   private int matrixHeight;
   private String toolTip;
   private volatile List<RealmsActivityScreen.Day> dayList = new ArrayList();
   private List<RealmsActivityScreen.Color> colors = Arrays.asList(
      new RealmsActivityScreen.Color(79, 243, 29),
      new RealmsActivityScreen.Color(243, 175, 29),
      new RealmsActivityScreen.Color(243, 29, 190),
      new RealmsActivityScreen.Color(29, 165, 243),
      new RealmsActivityScreen.Color(29, 243, 130),
      new RealmsActivityScreen.Color(243, 29, 64),
      new RealmsActivityScreen.Color(29, 74, 243)
   );
   private int colorIndex = 0;
   private long periodInMillis;
   private int fontWidth;
   private int maxKeyWidth = 0;
   private Boolean noActivity = false;
   private static LoadingCache<String, String> activitiesNameCache = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
      public String load(String uuid) throws Exception {
         return Realms.uuidToName(uuid);
      }
   });

   public RealmsActivityScreen(RealmsScreen lastScreen, long serverId) {
      this.lastScreen = lastScreen;
      this.serverId = serverId;
      this.getActivities();
   }

   public void mouseEvent() {
      super.mouseEvent();
      this.list.mouseEvent();
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.matrixWidth = this.width();
      this.matrixHeight = this.height() - 40;
      this.fontWidth = this.fontWidth("A");
      this.list = new RealmsActivityScreen.DetailsList();
      this.buttonsAdd(newButton(1, this.width() / 2 - 100, this.height() - 30, 200, 20, getLocalizedString("gui.back")));
   }

   private RealmsActivityScreen.Color getColor() {
      if (this.colorIndex > this.colors.size() - 1) {
         this.colorIndex = 0;
      }

      RealmsActivityScreen.Color color = (RealmsActivityScreen.Color)this.colors.get(this.colorIndex);
      ++this.colorIndex;
      return color;
   }

   private void getActivities() {
      (new Thread() {
         public void run() {
            RealmsClient client = RealmsClient.createRealmsClient();

            try {
               ServerActivityList activities = client.getActivity(RealmsActivityScreen.this.serverId);
               RealmsActivityScreen.this.activityMap = RealmsActivityScreen.this.convertToActivityMatrix(activities);
               List<RealmsActivityScreen.Day> tempDayList = new ArrayList();

               for(RealmsActivityScreen.ActivityRow row : RealmsActivityScreen.this.activityMap) {
                  for(RealmsActivityScreen.Activity activity : row.activities) {
                     String day = new SimpleDateFormat("dd/MM").format(new Date(activity.start));
                     RealmsActivityScreen.Day the_day = new RealmsActivityScreen.Day(day, activity.start);
                     if (!tempDayList.contains(the_day)) {
                        tempDayList.add(the_day);
                     }
                  }
               }

               Collections.sort(tempDayList, new Comparator<RealmsActivityScreen.Day>() {
                  public int compare(RealmsActivityScreen.Day day1, RealmsActivityScreen.Day day2) {
                     return day1.timestamp.compareTo(day2.timestamp);
                  }
               });
               RealmsActivityScreen.this.dayList = tempDayList;
            } catch (RealmsServiceException var10) {
               var10.printStackTrace();
            }

         }
      }).start();
   }

   private List<RealmsActivityScreen.ActivityRow> convertToActivityMatrix(ServerActivityList serverActivityList) {
      List<RealmsActivityScreen.ActivityRow> activityRows = Lists.newArrayList();
      this.periodInMillis = serverActivityList.periodInMillis;
      long base = System.currentTimeMillis() - serverActivityList.periodInMillis;

      for(ServerActivity sa : serverActivityList.serverActivities) {
         RealmsActivityScreen.ActivityRow activityRow = this.find(sa.profileUuid, activityRows);
         Calendar joinTime = Calendar.getInstance(TimeZone.getDefault());
         joinTime.setTimeInMillis(sa.joinTime);
         Calendar leaveTime = Calendar.getInstance(TimeZone.getDefault());
         leaveTime.setTimeInMillis(sa.leaveTime);
         RealmsActivityScreen.Activity e = new RealmsActivityScreen.Activity(base, joinTime.getTimeInMillis(), leaveTime.getTimeInMillis());
         if (activityRow == null) {
            String name = "";

            try {
               name = (String)activitiesNameCache.get(sa.profileUuid);
            } catch (Exception var13) {
               var13.printStackTrace();
            }

            activityRow = new RealmsActivityScreen.ActivityRow(sa.profileUuid, new ArrayList(), this.getColor(), name, sa.profileUuid);
            activityRow.activities.add(e);
            activityRows.add(activityRow);
         } else {
            activityRow.activities.add(e);
         }
      }

      for(RealmsActivityScreen.ActivityRow row : activityRows) {
         Collections.sort(row.activities);
      }

      this.noActivity = activityRows.size() == 0;
      return activityRows;
   }

   private RealmsActivityScreen.ActivityRow find(String key, List<RealmsActivityScreen.ActivityRow> rows) {
      for(RealmsActivityScreen.ActivityRow row : rows) {
         if (row.key.equals(key)) {
            return row;
         }
      }

      return null;
   }

   public void tick() {
      super.tick();
   }

   public void buttonClicked(RealmsButton button) {
      if (button.id() == 1) {
         Realms.setScreen(this.lastScreen);
      }

   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.renderBackground();
      this.list.render(xm, ym, a);
      this.drawCenteredString(getLocalizedString("mco.activity.title"), this.width() / 2, 10, 16777215);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

      if (this.noActivity) {
         this.drawCenteredString(
            getLocalizedString("mco.activity.noactivity", new Object[]{TimeUnit.DAYS.convert(this.periodInMillis, TimeUnit.MILLISECONDS)}),
            this.width() / 2,
            this.height() / 2 - 20,
            16777215
         );
      }

      super.render(xm, ym, a);
   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int rx = x - 80;
         int ry = y - 12;
         int index = 0;
         int width = 0;

         for(String s : msg.split("\n")) {
            int the_width = this.fontWidth(s);
            if (the_width > width) {
               width = the_width;
            }
         }

         for(String s : msg.split("\n")) {
            this.fillGradient(rx - 3, ry - (index == 0 ? 3 : 0) + index, rx + width + 3, ry + 8 + 3 + index, -1073741824, -1073741824);
            this.fontDrawShadow(s, rx, ry + index, -1);
            index += 10;
         }

      }
   }

   static class Activity implements Comparable<RealmsActivityScreen.Activity> {
      long base;
      long start;
      long end;

      private Activity(long base, long start, long end) {
         this.base = base;
         this.start = start;
         this.end = end;
      }

      public int compareTo(RealmsActivityScreen.Activity o) {
         return (int)(this.start - o.start);
      }

      public int hourIndice() {
         String hour = new SimpleDateFormat("HH").format(new Date(this.start));
         return Integer.parseInt(hour);
      }

      public int minuteIndice() {
         String minute = new SimpleDateFormat("mm").format(new Date(this.start));
         return Integer.parseInt(minute);
      }
   }

   static class ActivityRow {
      String key;
      List<RealmsActivityScreen.Activity> activities;
      RealmsActivityScreen.Color color;
      String name;
      String uuid;

      ActivityRow(String key, List<RealmsActivityScreen.Activity> activities, RealmsActivityScreen.Color color, String name, String uuid) {
         this.key = key;
         this.activities = activities;
         this.color = color;
         this.name = name;
         this.uuid = uuid;
      }
   }

   static class Color {
      int r;
      int g;
      int b;

      Color(int r, int g, int b) {
         this.r = r;
         this.g = g;
         this.b = b;
      }
   }

   static class Day {
      String day;
      Long timestamp;

      Day(String day, Long timestamp) {
         this.day = day;
         this.timestamp = timestamp;
      }

      public boolean equals(Object d) {
         if (!(d instanceof RealmsActivityScreen.Day)) {
            return false;
         } else {
            RealmsActivityScreen.Day that = (RealmsActivityScreen.Day)d;
            return this.day.equals(that.day);
         }
      }
   }

   class DetailsList extends RealmsScrolledSelectionList {
      public DetailsList() {
         super(
            RealmsActivityScreen.this.width(),
            RealmsActivityScreen.this.height(),
            30,
            RealmsActivityScreen.this.height() - 40,
            RealmsActivityScreen.this.fontLineHeight() + 1
         );
      }

      public int getItemCount() {
         return RealmsActivityScreen.this.activityMap.size();
      }

      public void selectItem(int item, boolean doubleClick, int xMouse, int yMouse) {
      }

      public boolean isSelectedItem(int item) {
         return false;
      }

      public void renderBackground() {
      }

      public int getMaxPosition() {
         return this.getItemCount() * (RealmsActivityScreen.this.fontLineHeight() + 1) + 10;
      }

      protected void renderItem(int i, int x, int y, int h, Tezzelator t, int mouseX, int mouseY) {
         if (RealmsActivityScreen.this.activityMap != null && RealmsActivityScreen.this.activityMap.size() > 0) {
            RealmsActivityScreen.this.drawString(((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).name, 20, y, 16777215);
            int keyWidth = RealmsActivityScreen.this.fontWidth * ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).name.length();
            if (keyWidth > RealmsActivityScreen.this.maxKeyWidth) {
               RealmsActivityScreen.this.maxKeyWidth = keyWidth + 5;
            }

            int keyRightPadding = 25;
            int activityPoint = RealmsActivityScreen.this.maxKeyWidth + keyRightPadding;
            int spaceLeft = RealmsActivityScreen.this.matrixWidth - activityPoint - 10;
            int days = RealmsActivityScreen.this.dayList.size() < 1 ? 1 : RealmsActivityScreen.this.dayList.size();
            int dayWidth = spaceLeft / days;
            double hourWidth = (double)dayWidth / 24.0;
            double minuteWidth = hourWidth / 60.0;
            GL11.glDisable(3553);
            t.begin();
            t.color(
               ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.r,
               ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.g,
               ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.b
            );
            t.vertex((double)(activityPoint - 8), (double)(y + 7), 0.0);
            t.vertex((double)(activityPoint - 3), (double)(y + 7), 0.0);
            t.vertex((double)(activityPoint - 3), (double)(y + 2), 0.0);
            t.vertex((double)(activityPoint - 8), (double)(y + 2), 0.0);
            t.end();
            GL11.glEnable(3553);
            GL11.glDisable(3553);
            t.begin();
            t.color(8421504);
            t.vertex((double)activityPoint, (double)(RealmsActivityScreen.this.height() - 40), 0.0);
            t.vertex((double)(activityPoint + 1), (double)(RealmsActivityScreen.this.height() - 40), 0.0);
            t.vertex((double)(activityPoint + 1), (double)(-RealmsActivityScreen.this.height()), 0.0);
            t.vertex((double)activityPoint, (double)(-RealmsActivityScreen.this.height()), 0.0);
            t.end();
            GL11.glEnable(3553);
            int daysIndex = 1;

            for(RealmsActivityScreen.Day day : RealmsActivityScreen.this.dayList) {
               RealmsActivityScreen.this.drawString(
                  day.day,
                  activityPoint + (daysIndex - 1) * dayWidth + (dayWidth - RealmsActivityScreen.this.fontWidth(day.day)) / 2 + 2,
                  RealmsActivityScreen.this.height() - 52,
                  16777215
               );
               GL11.glDisable(3553);
               t.begin();
               t.color(8421504);
               t.vertex((double)(activityPoint + daysIndex * dayWidth), (double)(RealmsActivityScreen.this.height() - 40), 0.0);
               t.vertex((double)(activityPoint + daysIndex * dayWidth + 1), (double)(RealmsActivityScreen.this.height() - 40), 0.0);
               t.vertex((double)(activityPoint + daysIndex * dayWidth + 1), (double)(-RealmsActivityScreen.this.height()), 0.0);
               t.vertex((double)(activityPoint + daysIndex * dayWidth), (double)(-RealmsActivityScreen.this.height()), 0.0);
               t.end();
               GL11.glEnable(3553);

               for(RealmsActivityScreen.Activity activity : ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).activities) {
                  String the_day = new SimpleDateFormat("dd/MM").format(new Date(activity.start));
                  if (the_day.equals(day.day)) {
                     int minute = activity.minuteIndice();
                     int hour = activity.hourIndice();
                     double itemWidth = minuteWidth * (double)TimeUnit.MINUTES.convert(activity.end - activity.start, TimeUnit.MILLISECONDS);
                     if (itemWidth < 3.0) {
                        itemWidth = 3.0;
                     }

                     GL11.glDisable(3553);
                     t.begin();
                     t.color(
                        ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.r,
                        ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.g,
                        ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).color.b
                     );
                     t.vertex(
                        (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth,
                        (double)(y + 7),
                        0.0
                     );
                     t.vertex(
                        (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth + itemWidth,
                        (double)(y + 7),
                        0.0
                     );
                     t.vertex(
                        (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth + itemWidth,
                        (double)(y + 2),
                        0.0
                     );
                     t.vertex(
                        (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth,
                        (double)(y + 2),
                        0.0
                     );
                     t.end();
                     GL11.glEnable(3553);
                     if ((double)this.xm()
                           >= (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth
                        && (double)this.xm()
                           <= (double)(activityPoint + (dayWidth * daysIndex - dayWidth)) + (double)hour * hourWidth + (double)minute * minuteWidth + itemWidth
                        && this.ym() >= y
                        && this.ym() <= y + 10) {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                        Date startDate = new Date(activity.start);
                        Date endDate = new Date(activity.end);
                        int length = (int)Math.ceil((double)TimeUnit.SECONDS.convert(activity.end - activity.start, TimeUnit.MILLISECONDS) / 60.0);
                        if (length < 1) {
                           length = 1;
                        }

                        RealmsActivityScreen.this.toolTip = "["
                           + format.format(startDate)
                           + " - "
                           + format.format(endDate)
                           + "]\n"
                           + length
                           + (length > 1 ? " minutes" : " minute");
                     }
                  }
               }

               ++daysIndex;
            }

            RealmsScreen.bindFace(
               ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).uuid,
               ((RealmsActivityScreen.ActivityRow)RealmsActivityScreen.this.activityMap.get(i)).name
            );
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(10, y, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
            RealmsScreen.blit(10, y, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
         }

      }

      public int getScrollbarPosition() {
         return this.width() - 7;
      }
   }
}
