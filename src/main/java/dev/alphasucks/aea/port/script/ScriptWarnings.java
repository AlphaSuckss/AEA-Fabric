package dev.alphasucks.aea.port.script;

import dev.alphasucks.aea.port.Apoli;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2960;

final class ScriptWarnings {
   private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

   private ScriptWarnings() {
   }

   static void missing(ApoliScripts.Kind kind, class_2960 id) {
      if (WARNED.add("missing:" + kind + ":" + id)) {
         Apoli.LOGGER
            .warn(
               "[Apoli] No {} script registered for '{}'. Register one from KubeJS (ApoliEvents) or ship data/<namespace>/apoli/scripts/<path>.js.", kind, id
            );
      }
   }

   static void failed(ApoliScripts.Kind kind, class_2960 id, Throwable error) {
      if (WARNED.add("failed:" + kind + ":" + id)) {
         Apoli.LOGGER.error("[Apoli] {} script '{}' threw; it will keep running but this is logged once.", new Object[]{kind, id, error});
      }
   }

   static void reset() {
      WARNED.clear();
   }
}
