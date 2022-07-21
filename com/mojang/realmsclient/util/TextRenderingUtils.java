package com.mojang.realmsclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TextRenderingUtils {
   private TextRenderingUtils() {
   }

   static List<String> lineBreak(String text) {
      return Arrays.asList(text.split("\\n"));
   }

   public static List<TextRenderingUtils.Line> decompose(String text, TextRenderingUtils.LineSegment... links) {
      return decompose(text, Arrays.asList(links));
   }

   private static List<TextRenderingUtils.Line> decompose(String text, List<TextRenderingUtils.LineSegment> links) {
      List<String> brokenLines = lineBreak(text);
      return insertLinks(brokenLines, links);
   }

   private static List<TextRenderingUtils.Line> insertLinks(List<String> lines, List<TextRenderingUtils.LineSegment> links) {
      int linkCount = 0;
      ArrayList<TextRenderingUtils.Line> processedLines = new ArrayList();

      for(String line : lines) {
         List<TextRenderingUtils.LineSegment> segments = new ArrayList();

         for(String part : split(line, "%link")) {
            if (part.equals("%link")) {
               segments.add(links.get(linkCount++));
            } else {
               segments.add(TextRenderingUtils.LineSegment.text(part));
            }
         }

         processedLines.add(new TextRenderingUtils.Line(segments));
      }

      return processedLines;
   }

   public static List<String> split(String line, String delimiter) {
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

   public static class Line {
      public final List<TextRenderingUtils.LineSegment> segments;

      Line(TextRenderingUtils.LineSegment... segments) {
         this(Arrays.asList(segments));
      }

      Line(List<TextRenderingUtils.LineSegment> segments) {
         this.segments = segments;
      }

      public String toString() {
         return "Line{segments=" + this.segments + '}';
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            TextRenderingUtils.Line line = (TextRenderingUtils.Line)o;
            return Objects.equals(this.segments, line.segments);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.segments});
      }
   }

   public static class LineSegment {
      final String fullText;
      final String linkTitle;
      final String linkUrl;

      private LineSegment(String fullText) {
         this.fullText = fullText;
         this.linkTitle = null;
         this.linkUrl = null;
      }

      private LineSegment(String fullText, String linkTitle, String linkUrl) {
         this.fullText = fullText;
         this.linkTitle = linkTitle;
         this.linkUrl = linkUrl;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            TextRenderingUtils.LineSegment segment = (TextRenderingUtils.LineSegment)o;
            return Objects.equals(this.fullText, segment.fullText)
               && Objects.equals(this.linkTitle, segment.linkTitle)
               && Objects.equals(this.linkUrl, segment.linkUrl);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.fullText, this.linkTitle, this.linkUrl});
      }

      public String toString() {
         return "Segment{fullText='" + this.fullText + '\'' + ", linkTitle='" + this.linkTitle + '\'' + ", linkUrl='" + this.linkUrl + '\'' + '}';
      }

      public String renderedText() {
         return this.isLink() ? this.linkTitle : this.fullText;
      }

      public boolean isLink() {
         return this.linkTitle != null;
      }

      public String getLinkUrl() {
         if (!this.isLink()) {
            throw new IllegalStateException("Not a link: " + this);
         } else {
            return this.linkUrl;
         }
      }

      public static TextRenderingUtils.LineSegment link(String linkTitle, String linkUrl) {
         return new TextRenderingUtils.LineSegment(null, linkTitle, linkUrl);
      }

      static TextRenderingUtils.LineSegment text(String fullText) {
         return new TextRenderingUtils.LineSegment(fullText);
      }
   }
}
