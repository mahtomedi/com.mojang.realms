package com.mojang.realmsclient.client;

import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ping {
   public static List<RegionPingResult> ping(Ping.Region... regions) {
      for(Ping.Region region : regions) {
         ping(region.endpoint);
      }

      List<RegionPingResult> results = new ArrayList();

      for(Ping.Region region : regions) {
         results.add(new RegionPingResult(region.name, ping(region.endpoint)));
      }

      Collections.sort(results, new Comparator<RegionPingResult>() {
         public int compare(RegionPingResult o1, RegionPingResult o2) {
            return o1.ping() - o2.ping();
         }
      });
      return results;
   }

   private static int ping(String host) {
      long t = -now();

      for(int i = 0; i < 10; ++i) {
         try {
            SocketAddress sockaddr = new InetSocketAddress(host, 80);
            Socket s = new Socket();
            s.connect(sockaddr, 700);
         } catch (Exception var6) {
         }
      }

      t += now();
      return (int)((double)t / 10.0);
   }

   private static long now() {
      return System.currentTimeMillis();
   }

   public static List<RegionPingResult> pingAllRegions() {
      return ping(Ping.Region.values());
   }

   static enum Region {
      US_EAST_1("us-east-1", "ec2.us-east-1.amazonaws.com"),
      US_WEST_2("us-west-2", "ec2.us-west-2.amazonaws.com"),
      US_WEST_1("us-west-1", "ec2.us-west-1.amazonaws.com"),
      EU_WEST_1("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
      AP_SOUTHEAST_1("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
      AP_SOUTHEAST_2("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
      AP_NORTHEAST_1("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
      SA_EAST_1("sa-east-1", "ec2.sa-east-1.amazonaws.com");

      private final String name;
      private final String endpoint;

      private Region(String name, String endpoint) {
         this.name = name;
         this.endpoint = endpoint;
      }
   }
}
