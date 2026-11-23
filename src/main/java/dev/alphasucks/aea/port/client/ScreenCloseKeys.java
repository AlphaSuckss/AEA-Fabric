package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.client.radial.RadialMenuScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_490;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ScreenCloseKeys {
   private static final int OPEN_WINDOW_TICKS = 30;
   @Nullable
   private static class_304 expected;
   private static int expectedTicks;
   @Nullable
   private static class_437 screen;
   @Nullable
   private static class_304 key;
   private static boolean armed;

   private ScreenCloseKeys() {
   }

   public static void expect(class_304 opener) {
      expected = opener;
      expectedTicks = 30;
   }

   public static void carryOver() {
      if (screen != null && key != null) {
         expect(key);
      }
   }

   public static void tick(class_310 mc) {
      class_437 current = mc.field_1755;
      if (screen != null && current != screen) {
         screen = null;
         key = null;
      }

      if (expected != null) {
         if (screen == null && current != null && bindable(current)) {
            screen = current;
            key = expected;
            armed = !ApoliKeyMappings.isHeld(expected);
            expected = null;
         } else if (--expectedTicks <= 0) {
            expected = null;
         }
      }

      if (screen != null && !armed && key != null && !ApoliKeyMappings.isHeld(key)) {
         armed = true;
      }
   }

   public static boolean closes(class_437 target, int keyCode, int scanCode) {
      return target == screen && armed && key != null && key.method_1417(keyCode, scanCode);
   }

   public static boolean isSlotKey(class_315 options, int keyCode, int scanCode) {
      for (class_304 hotbar : options.field_1852) {
         if (hotbar.method_1417(keyCode, scanCode)) {
            return true;
         }
      }

      return options.field_1831.method_1417(keyCode, scanCode)
         || options.field_1869.method_1417(keyCode, scanCode)
         || options.field_1871.method_1417(keyCode, scanCode);
   }

   public static void clear() {
      expected = null;
      screen = null;
      key = null;
      armed = false;
   }

   private static boolean bindable(class_437 candidate) {
      return candidate instanceof RadialMenuScreen
         ? true
         : candidate instanceof class_465 && !(candidate instanceof class_490) && !(candidate instanceof class_481);
   }
}
