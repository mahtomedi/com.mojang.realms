package realms;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum q {
   a('0'),
   b('1'),
   c('2'),
   d('3'),
   e('4'),
   f('5'),
   g('6'),
   h('7'),
   i('8'),
   j('9'),
   k('a'),
   l('b'),
   m('c'),
   n('d'),
   o('e'),
   p('f'),
   q('k', true),
   r('l', true),
   s('m', true),
   t('n', true),
   u('o', true),
   v('r');

   private static final Map<Character, q> w = (Map<Character, q>)Arrays.stream(values()).collect(Collectors.toMap(q::a, f -> f));
   private static final Map<String, q> x = (Map<String, q>)Arrays.stream(values()).collect(Collectors.toMap(q::d, f -> f));
   private static final Pattern y = Pattern.compile("(?i)§[0-9A-FK-OR]");
   private final char z;
   private final boolean A;
   private final String B;

   private q(char code) {
      this(code, false);
   }

   private q(char code, boolean isFormat) {
      this.z = code;
      this.A = isFormat;
      this.B = "§" + code;
   }

   public char a() {
      return this.z;
   }

   public boolean b() {
      return this.A;
   }

   public boolean c() {
      return !this.A && this != v;
   }

   public String d() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public String toString() {
      return this.B;
   }

   public static String a(String input) {
      return input == null ? null : y.matcher(input).replaceAll("");
   }

   public static q a(char code) {
      return (q)w.get(code);
   }

   public static q b(String name) {
      return name == null ? null : (q)x.get(name.toLowerCase(Locale.ROOT));
   }

   public static Collection<String> a(boolean getColors, boolean getFormats) {
      List<String> result = Lists.newArrayList();

      for(q format : values()) {
         if ((!format.c() || getColors) && (!format.b() || getFormats)) {
            result.add(format.d());
         }
      }

      return result;
   }
}
