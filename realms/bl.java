package realms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class bl {
   private bl() {
   }

   static List<String> a(String text) {
      return Arrays.asList(text.split("\\n"));
   }

   public static List<bl.a> a(String text, bl.b... links) {
      return a(text, Arrays.asList(links));
   }

   private static List<bl.a> a(String text, List<bl.b> links) {
      List<String> brokenLines = a(text);
      return a(brokenLines, links);
   }

   private static List<bl.a> a(List<String> lines, List<bl.b> links) {
      int linkCount = 0;
      ArrayList<bl.a> processedLines = new ArrayList();

      for(String line : lines) {
         List<bl.b> segments = new ArrayList();

         for(String part : a(line, "%link")) {
            if (part.equals("%link")) {
               segments.add(links.get(linkCount++));
            } else {
               segments.add(bl.b.a(part));
            }
         }

         processedLines.add(new bl.a(segments));
      }

      return processedLines;
   }

   public static List<String> a(String line, String delimiter) {
      if (delimiter.isEmpty()) {
         throw new IllegalArgumentException("Delimiter cannot be the empty string");
      } else {
         List<String> parts = new ArrayList();

         int searchStart;
         int matchIndex;
         for(searchStart = 0; (matchIndex = line.indexOf(delimiter, searchStart)) != -1; searchStart = matchIndex + delimiter.length()) {
            if (matchIndex > searchStart) {
               parts.add(line.substring(searchStart, matchIndex));
            }

            parts.add(delimiter);
         }

         if (searchStart < line.length()) {
            parts.add(line.substring(searchStart));
         }

         return parts;
      }
   }

   public static class a {
      public final List<bl.b> a;

      a(bl.b... segments) {
         this(Arrays.asList(segments));
      }

      a(List<bl.b> segments) {
         this.a = segments;
      }

      public String toString() {
         return "Line{segments=" + this.a + '}';
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            bl.a line = (bl.a)o;
            return Objects.equals(this.a, line.a);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.a});
      }
   }

   public static class b {
      final String a;
      final String b;
      final String c;

      private b(String fullText) {
         this.a = fullText;
         this.b = null;
         this.c = null;
      }

      private b(String fullText, String linkTitle, String linkUrl) {
         this.a = fullText;
         this.b = linkTitle;
         this.c = linkUrl;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            bl.b segment = (bl.b)o;
            return Objects.equals(this.a, segment.a) && Objects.equals(this.b, segment.b) && Objects.equals(this.c, segment.c);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.a, this.b, this.c});
      }

      public String toString() {
         return "Segment{fullText='" + this.a + '\'' + ", linkTitle='" + this.b + '\'' + ", linkUrl='" + this.c + '\'' + '}';
      }

      public String a() {
         return this.b() ? this.b : this.a;
      }

      public boolean b() {
         return this.b != null;
      }

      public String c() {
         if (!this.b()) {
            throw new IllegalStateException("Not a link: " + this);
         } else {
            return this.c;
         }
      }

      public static bl.b a(String linkTitle, String linkUrl) {
         return new bl.b(null, linkTitle, linkUrl);
      }

      static bl.b a(String fullText) {
         return new bl.b(fullText);
      }
   }
}
