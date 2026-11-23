package dev.alphasucks.aea.port.script;

import dev.alphasucks.aea.port.Apoli;
import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4080;

public final class ScriptLoader extends class_4080<Map<class_2960, String>> {
   private static final String DIRECTORY = "apoli/scripts";
   private static final String SUFFIX = ".js";
   private static final int MAX_SOURCE_BYTES = 524288;

   // the original class carried javac bridges from class_4080's intermediary names to prepare/apply
   @Override
   protected Map<class_2960, String> method_18789(class_3300 manager, class_3695 profiler) {
      return this.prepare(manager, profiler);
   }

   @Override
   protected void method_18788(Map<class_2960, String> data, class_3300 manager, class_3695 profiler) {
      this.apply(data, manager, profiler);
   }

   protected Map<class_2960, String> prepare(class_3300 manager, class_3695 profiler) {
      Map<class_2960, String> sources = new LinkedHashMap();
      if (!ApoliScriptConfig.get().allowDataPackScripts()) {
         return sources;
      }

      for (Entry<class_2960, class_3298> entry : manager.method_14488("apoli/scripts", path -> path.method_12832().endsWith(".js")).entrySet()) {
         class_2960 file = (class_2960)entry.getKey();

         try {
            BufferedReader reader = ((class_3298)entry.getValue()).method_43039();

            label52: {
               try {
                  String source = ((StringBuilder)reader.lines().reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)).toString();
                  if (source.length() <= 524288) {
                     sources.put(scriptId(file), source);
                     break label52;
                  }

                  Apoli.LOGGER.error("[Apoli] Script {} is larger than {} bytes and was skipped", file, 524288);
               } catch (Throwable var11) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (reader != null) {
                  reader.close();
               }
               continue;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (Exception e) {
            Apoli.LOGGER.error("[Apoli] Could not read script {}", file, e);
         }
      }

      return sources;
   }

   protected void apply(Map<class_2960, String> sources, class_3300 manager, class_3695 profiler) {
      ScriptBackend backend = ApoliScripts.backend();
      if (!sources.isEmpty()) {
         if (backend != null && backend.available()) {
            backend.beginReload();

            for (Entry<class_2960, String> entry : sources.entrySet()) {
               try {
                  backend.load((class_2960)entry.getKey(), (String)entry.getValue());
               } catch (Throwable t) {
                  Apoli.LOGGER.error("[Apoli] Script {} failed to load", entry.getKey(), t);
               }
            }

            backend.endReload();
            Apoli.LOGGER.info("[Apoli] Loaded {} data pack script(s) via {}", sources.size(), backend.name());
         } else {
            Apoli.LOGGER
               .warn("[Apoli] {} data pack script(s) found but no script backend is installed. Install KubeJS to run apoli:script files.", sources.size());
         }
      }
   }

   private static class_2960 scriptId(class_2960 file) {
      String path = file.method_12832();
      String trimmed = path.substring("apoli/scripts".length() + 1);
      return new class_2960(file.method_12836(), trimmed);
   }
}
