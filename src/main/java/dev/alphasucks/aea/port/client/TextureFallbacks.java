package dev.alphasucks.aea.port.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class TextureFallbacks {
   private static final Map<class_2960, Boolean> PRESENT = new HashMap();

   private TextureFallbacks() {
   }

   public static boolean exists(class_2960 texture) {
      Boolean present = (Boolean)PRESENT.get(texture);
      if (present == null) {
         present = class_310.method_1551().method_1478().method_14486(texture).isPresent();
         PRESENT.put(texture, present);
      }

      return present;
   }

   public static class_2960 first(class_2960 texture, List<class_2960> fallbacks) {
      if (!fallbacks.isEmpty() && !exists(texture)) {
         int i = 0;

         for (int n = fallbacks.size(); i < n; i++) {
            class_2960 fallback = (class_2960)fallbacks.get(i);
            if (exists(fallback)) {
               return fallback;
            }
         }

         return texture;
      } else {
         return texture;
      }
   }

   public static void clear() {
      PRESENT.clear();
   }
}
