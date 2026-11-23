package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.mixin.keybinding.KeyMappingClickCountAccessor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_306;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ApoliKeyMappings {
   private static final Map<class_304, ApoliKeyMappings.Sample> SAMPLES = new HashMap();
   private static final Map<class_304, ApoliKeyMappings.KeyState> TICK_CACHE = new HashMap();

   private ApoliKeyMappings() {
   }

   public static void beginTick() {
      TICK_CACHE.clear();
   }

   public static void reset() {
      SAMPLES.clear();
      TICK_CACHE.clear();
   }

   @Nullable
   public static class_304 resolve(String translationKey) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1690 != null) {
         for (class_304 km : mc.field_1690.field_1839) {
            if (km.method_1431().equals(translationKey)) {
               return km;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean consumePress(class_304 km, boolean continuous) {
      ApoliKeyMappings.KeyState st = state(km);
      if (!screenOpen() && !BlockedKeys.blocks(km)) {
         return continuous ? st.down() : st.tap();
      } else {
         return false;
      }
   }

   public static boolean isDown(class_304 km) {
      ApoliKeyMappings.KeyState st = state(km);
      return !screenOpen() && !BlockedKeys.blocks(km) && st.down();
   }

   public static boolean isHeld(class_304 km) {
      return probe(km) == ApoliKeyMappings.Probe.DOWN;
   }

   private static ApoliKeyMappings.KeyState state(class_304 km) {
      ApoliKeyMappings.KeyState st = (ApoliKeyMappings.KeyState)TICK_CACHE.get(km);
      if (st == null) {
         st = computeState(km);
         TICK_CACHE.put(km, st);
      }

      return st;
   }

   private static ApoliKeyMappings.KeyState computeState(class_304 km) {
      ApoliKeyMappings.Sample sample = (ApoliKeyMappings.Sample)SAMPLES.get(km);
      if (sample == null) {
         sample = new ApoliKeyMappings.Sample();
         SAMPLES.put(km, sample);
      }

      boolean wasDown = sample.down;

      boolean down = switch (probe(km)) {
         case DOWN -> true;
         case UP -> false;
         case UNKNOWN -> wasDown;
      };
      sample.down = down;
      int clicks = ((KeyMappingClickCountAccessor)km).apoli$getClickCount();
      boolean clicked = sample.clicks >= 0 && clicks > sample.clicks;
      sample.clicks = clicks;

      while (ForcedKeys.consumeLocalForcedClick(km)) {
         clicked = true;
      }

      boolean screen = screenOpen();
      boolean pressedInScreen = sample.screen && !screen && !clicked;
      sample.screen = screen;
      boolean tap = !wasDown && !pressedInScreen && (down || clicked);
      return new ApoliKeyMappings.KeyState(down, tap);
   }

   private static boolean screenOpen() {
      class_310 mc = class_310.method_1551();
      return mc == null || mc.field_1755 != null;
   }

   private static ApoliKeyMappings.Probe probe(class_304 km) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.method_22683() != null) {
         class_306 key;
         try {
            key = class_3675.method_15981(km.method_1428());
         } catch (Exception e) {
            return vanillaProbe(km);
         }

         int value = key.method_1444();
         if (value < 0) {
            return ApoliKeyMappings.Probe.UP;
         }

         long window = mc.method_22683().method_4490();

         return switch (key.method_1442()) {
            case field_1668 -> class_3675.method_15987(window, value) ? ApoliKeyMappings.Probe.DOWN : ApoliKeyMappings.Probe.UP;
            case field_1672 -> GLFW.glfwGetMouseButton(window, value) == 1 ? ApoliKeyMappings.Probe.DOWN : ApoliKeyMappings.Probe.UP;
            default -> vanillaProbe(km);
         };
      } else {
         return ApoliKeyMappings.Probe.UNKNOWN;
      }
   }

   private static ApoliKeyMappings.Probe vanillaProbe(class_304 km) {
      return km.method_1434() ? ApoliKeyMappings.Probe.DOWN : ApoliKeyMappings.Probe.UP;
   }

   private record KeyState(boolean down, boolean tap) {
   }

   private enum Probe {
      DOWN,
      UP,
      UNKNOWN;
   }

   private static final class Sample {
      boolean down;
      boolean screen;
      int clicks = -1;
   }
}
