package dev.alphasucks.aea.port.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_155;

public final class GameVersion {
   private GameVersion() {
   }

   public static String name() {
      return GameVersion.Holder.NAME;
   }

   public static int dataVersion() {
      return GameVersion.Holder.DATA_VERSION;
   }

   public static int compare(String left, String right) {
      List<Object> a = tokenize(left);
      List<Object> b = tokenize(right);
      int n = Math.max(a.size(), b.size());

      for (int i = 0; i < n; i++) {
         Object at = i < a.size() ? a.get(i) : null;
         Object bt = i < b.size() ? b.get(i) : null;
         if (at == null) {
            return bt instanceof String ? 1 : -1;
         }

         if (bt == null) {
            return at instanceof String ? -1 : 1;
         }

         if (at instanceof Long al && bt instanceof Long bl) {
            int cmp = Long.compare(al, bl);
            if (cmp != 0) {
               return cmp;
            }
         } else {
            if (!(at instanceof String as && bt instanceof String bs)) {
               return at instanceof String ? -1 : 1;
            }

            int cmp = as.compareToIgnoreCase(bs);
            if (cmp != 0) {
               return cmp < 0 ? -1 : 1;
            }
         }
      }

      return 0;
   }

   private static List<Object> tokenize(String version) {
      int plus = version.indexOf(43);
      String s = (plus < 0 ? version : version.substring(0, plus)).trim();
      List<Object> out = new ArrayList(4);
      int i = 0;
      int n = s.length();

      while (i < n) {
         char c = s.charAt(i);
         if (c != '.' && c != '-' && c != '_') {
            if (c >= '0' && c <= '9') {
               long value = 0L;

               while (i < n) {
                  char d = s.charAt(i);
                  if (d < '0' || d > '9') {
                     break;
                  }

                  value = value * 10L + (d - '0');
                  i++;
               }

               out.add(value);
            } else {
               int start = i;

               while (true) {
                  if (i < n) {
                     char d = s.charAt(i);
                     if (d != '.' && d != '-' && d != '_' && (d < '0' || d > '9')) {
                        i++;
                        continue;
                     }
                  }

                  out.add(s.substring(start, i));
                  break;
               }
            }
         } else {
            i++;
         }
      }

      return out;
   }

   private static final class Holder {
      static final String NAME = class_155.method_16673().method_48019();
      static final int DATA_VERSION = class_155.method_16673().method_37912().method_38494();
   }
}
