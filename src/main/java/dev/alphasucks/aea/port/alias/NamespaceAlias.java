package dev.alphasucks.aea.port.alias;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;

public final class NamespaceAlias {
   private static final Map<String, String> ALIASES = new HashMap();

   private NamespaceAlias() {
   }

   public static void addAlias(String from, String to) {
      String existing = (String)ALIASES.putIfAbsent(from, to);
      if (existing != null && !existing.equals(to)) {
         throw new IllegalStateException("Conflicting namespace alias: " + from + " -> " + existing + " vs " + to);
      }
   }

   public static boolean hasAlias(String namespace) {
      return ALIASES.containsKey(namespace);
   }

   public static class_2960 resolve(class_2960 id) {
      String target = (String)ALIASES.get(id.method_12836());
      return target == null ? id : new class_2960(target, id.method_12832());
   }

   public static Map<String, String> view() {
      return Collections.unmodifiableMap(ALIASES);
   }
}
