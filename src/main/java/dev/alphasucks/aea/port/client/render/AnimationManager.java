package dev.alphasucks.aea.port.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.model.BedrockAnimation;
import dev.alphasucks.aea.port.client.model.BedrockAnimationParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.jetbrains.annotations.Nullable;

public final class AnimationManager implements SimpleSynchronousResourceReloadListener {
   public static final AnimationManager INSTANCE = new AnimationManager();
   private static final String PREFIX = "animations";
   private static final String[] SUFFIXES = new String[]{".animation.json", ".json"};
   private static final Map<class_2960, Map<String, BedrockAnimation>> FILES = new HashMap();
   private static final Map<class_2960, Set<String>> WARNED = new HashMap();

   private AnimationManager() {
   }

   public class_2960 getFabricId() {
      return Apoli.id("custom_model_animations");
   }

   public void method_14491(class_3300 manager) {
      FILES.clear();
      WARNED.clear();
      AnimationPlayer.clearWarnings();
      int loaded = 0;
      Map<class_2960, class_3298> found = manager.method_14488("animations", location -> location.method_12832().endsWith(".json"));

      for (Entry<class_2960, class_3298> entry : found.entrySet()) {
         class_2960 file = (class_2960)entry.getKey();
         class_2960 id = strip(file);
         if (id != null && !FILES.containsKey(id)) {
            try {
               InputStream stream = ((class_3298)entry.getValue()).method_14482();

               label54: {
                  try {
                     JsonElement raw = JsonParser.parseReader(new InputStreamReader(stream));
                     Map<String, BedrockAnimation> parsed = BedrockAnimationParser.parse(id, new Dynamic(JsonOps.INSTANCE, raw));
                     if (parsed.isEmpty()) {
                        break label54;
                     }

                     FILES.put(id, parsed);
                     loaded += parsed.size();
                  } catch (Throwable var12) {
                     if (stream != null) {
                        try {
                           stream.close();
                        } catch (Throwable var11) {
                           var12.addSuppressed(var11);
                        }
                     }

                     throw var12;
                  }

                  if (stream != null) {
                     stream.close();
                  }
                  continue;
               }

               if (stream != null) {
                  stream.close();
               }
            } catch (Exception e) {
               Apoli.LOGGER.error("[Apoli] Failed to load animation file {}: {}", id, e.getMessage());
            }
         }
      }

      Apoli.LOGGER.info("[Apoli] Loaded {} custom model animation(s) from {} file(s).", loaded, FILES.size());
   }

   @Nullable
   private static class_2960 strip(class_2960 file) {
      String path = file.method_12832();
      if (!path.startsWith("animations/")) {
         return null;
      }

      String trimmed = path.substring("animations".length() + 1);

      for (String suffix : SUFFIXES) {
         if (trimmed.endsWith(suffix)) {
            return new class_2960(file.method_12836(), trimmed.substring(0, trimmed.length() - suffix.length()));
         }
      }

      return null;
   }

   @Nullable
   public static BedrockAnimation get(class_2960 file, @Nullable String name) {
      Map<String, BedrockAnimation> animations = (Map<String, BedrockAnimation>)FILES.get(file);
      if (animations == null || animations.isEmpty()) {
         warnMissing(file, name, null);
         return null;
      }

      if (name == null) {
         return (BedrockAnimation)animations.values().iterator().next();
      }

      BedrockAnimation found = (BedrockAnimation)animations.get(name);
      if (found == null) {
         warnMissing(file, name, animations);
      }

      return found;
   }

   private static void warnMissing(class_2960 file, @Nullable String name, @Nullable Map<String, BedrockAnimation> available) {
      Set<String> seen = (Set<String>)WARNED.computeIfAbsent(file, key -> new HashSet(2));
      if (seen.add(name == null ? "" : name)) {
         if (available == null) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] No animation file '{}' is loaded, so nothing will play. It is expected at assets/{}/animations/{}.animation.json inside an enabled resource pack.",
                  new Object[]{file, file.method_12836(), file.method_12832()}
               );
         } else {
            Apoli.LOGGER
               .warn("[Apoli] Animation file '{}' has no clip called '{}', so nothing will play. It contains: {}", new Object[]{file, name, available.keySet()});
         }
      }
   }
}
