package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.power.builtin.ModifyCursorSpeedPower;
import dev.alphasucks.aea.port.power.builtin.ModifyCursorSpeedPower.Multipliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class CursorSpeedState {
   private static Multipliers current = Multipliers.NONE;
   private static boolean active;

   private CursorSpeedState() {
   }

   public static boolean appliesTo(class_1297 entity) {
      return active && entity == class_310.method_1551().field_1724;
   }

   public static void tick(class_310 mc) {
      if (mc.field_1724 == null) {
         reset();
      } else {
         Multipliers next = ModifyCursorSpeedPower.compute(mc.field_1724);
         current = next;
         active = !next.isIdentity();
      }
   }

   public static void reset() {
      current = Multipliers.NONE;
      active = false;
   }

   public static boolean isActive() {
      return active;
   }

   public static double horizontal() {
      return current.x();
   }

   public static double vertical() {
      return current.y();
   }
}
