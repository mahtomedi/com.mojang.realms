package realms;

import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class e {
   public static List<RegionPingResult> a(e.a... regions) {
      for(e.a region : regions) {
         a(region.j);
      }

      List<RegionPingResult> results = new ArrayList();

      for(e.a region : regions) {
         results.add(new RegionPingResult(region.i, a(region.j)));
      }

      Collections.sort(results, new Comparator<RegionPingResult>() {
         public int a(RegionPingResult o1, RegionPingResult o2) {
            return o1.ping() - o2.ping();
         }
      });
      return results;
   }

   private static int a(String host) {
      int timeout = 700;
      long sum = 0L;
      Socket socket = null;

      for(int i = 0; i < 5; ++i) {
         try {
            SocketAddress sockAddr = new InetSocketAddress(host, 80);
            socket = new Socket();
            long t1 = b();
            socket.connect(sockAddr, 700);
            sum += b() - t1;
         } catch (Exception var12) {
            sum += 700L;
         } finally {
            a(socket);
         }
      }

      return (int)((double)sum / 5.0);
   }

   private static void a(Socket socket) {
      try {
         if (socket != null) {
            socket.close();
         }
      } catch (Throwable var2) {
      }

   }

   private static long b() {
      return System.currentTimeMillis();
   }

   public static List<RegionPingResult> a() {
      return a(e.a.values());
   }

   static enum a {
      a("us-east-1", "ec2.us-east-1.amazonaws.com"),
      b("us-west-2", "ec2.us-west-2.amazonaws.com"),
      c("us-west-1", "ec2.us-west-1.amazonaws.com"),
      d("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
      e("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
      f("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
      g("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
      h("sa-east-1", "ec2.sa-east-1.amazonaws.com");

      private final String i;
      private final String j;

      private a(String name, String endpoint) {
         this.i = name;
         this.j = endpoint;
      }
   }
}
