package dev.alphasucks.aea.port.keybind;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ApoliKeybinds {
   private static volatile Map<class_2960, Keybind> LOADED = Map.of();

   private ApoliKeybinds() {
   }

   public static void replaceAll(Map<class_2960, Keybind> next) {
      LOADED = Map.copyOf(next);
   }

   public static Map<class_2960, Keybind> view() {
      return Collections.unmodifiableMap(LOADED);
   }

   @Nullable
   public static Keybind get(class_2960 id) {
      return (Keybind)LOADED.get(id);
   }

   public static Map<class_2960, Keybind> snapshot() {
      return new LinkedHashMap(LOADED);
   }
}
