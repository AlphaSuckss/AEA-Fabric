package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.power.ApoliPowers;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2960;

public final class FunctionWarnings {
   private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();
   private static volatile int generation = -1;

   private FunctionWarnings() {
   }

   public static boolean first(class_2960 function, String kind) {
      int current = ApoliPowers.generation();
      if (current != generation) {
         generation = current;
         SEEN.clear();
      }

      return SEEN.add(kind + "\u0000" + function);
   }
}
