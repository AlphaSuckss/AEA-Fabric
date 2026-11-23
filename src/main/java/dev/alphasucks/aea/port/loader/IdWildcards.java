package dev.alphasucks.aea.port.loader;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.class_2960;

public final class IdWildcards {
   public static final String SELF_ID = "*:*";
   private static final String SELF_NAMESPACE = "*:";
   private static final String TAGGED_SELF_NAMESPACE = "#*:";

   private IdWildcards() {
   }

   public static <T> Dynamic<T> apply(Dynamic<T> data, class_2960 self) {
      return walk(data, self.method_12836(), self.method_12836() + ":" + self.method_12832());
   }

   public static JsonElement apply(JsonElement json, class_2960 self) {
      return (JsonElement)apply(new Dynamic(JsonOps.INSTANCE, json), self).getValue();
   }

   public static String apply(String value, class_2960 self) {
      return expand(value, self.method_12836(), self.method_12836() + ":" + self.method_12832());
   }

   private static <T> Dynamic<T> walk(Dynamic<T> data, String namespace, String fullId) {
      Map<Dynamic<T>, Dynamic<T>> entries = (Map<Dynamic<T>, Dynamic<T>>)data.getMapValues().result().orElse(null);
      if (entries == null) {
         Stream<Dynamic<T>> list = (Stream<Dynamic<T>>)data.asStreamOpt().result().orElse(null);
         if (list != null) {
            return data.createList(list.map(element -> walk(element, namespace, fullId)));
         }

         String raw = (String)data.asString().result().orElse(null);
         if (raw == null) {
            return data;
         }

         String expanded = expand(raw, namespace, fullId);
         return expanded.equals(raw) ? data : data.createString(expanded);
      } else {
         Map<Dynamic<T>, Dynamic<T>> rebuilt = new LinkedHashMap(entries.size());

         for (Entry<Dynamic<T>, Dynamic<T>> entry : entries.entrySet()) {
            rebuilt.put((Dynamic)entry.getKey(), walk((Dynamic<T>)entry.getValue(), namespace, fullId));
         }

         return data.createMap(rebuilt);
      }
   }

   private static String expand(String value, String namespace, String fullId) {
      if (value.indexOf(42) < 0) {
         return value;
      } else {
         String out = value.contains("*:*") ? value.replace("*:*", fullId) : value;
         if (out.startsWith("*:")) {
            return namespace + ":" + out.substring("*:".length());
         } else {
            return out.startsWith("#*:") ? "#" + namespace + ":" + out.substring("#*:".length()) : expandInline(out, namespace);
         }
      }
   }

   private static String expandInline(String value, String namespace) {
      int at = value.indexOf("*:");
      if (at < 0) {
         return value;
      }

      StringBuilder out = null;
      int copied = 0;

      while (at >= 0) {
         if (standsAlone(value, at)) {
            if (out == null) {
               out = new StringBuilder(value.length() + namespace.length());
            }

            out.append(value, copied, at).append(namespace).append(':');
            copied = at + "*:".length();
         }

         at = value.indexOf("*:", at + 1);
      }

      return out == null ? value : out.append(value, copied, value.length()).toString();
   }

   private static boolean standsAlone(String value, int at) {
      int after = at + "*:".length();
      if (after >= value.length() || !isPathChar(value.charAt(after))) {
         return false;
      }

      if (at == 0) {
         return true;
      }

      char before = value.charAt(at - 1);
      return !Character.isLetterOrDigit(before) && before != '_' && before != '.' && before != ':';
   }

   private static boolean isPathChar(char c) {
      return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '/' || c == '-';
   }
}
