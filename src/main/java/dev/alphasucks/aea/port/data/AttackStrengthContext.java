package dev.alphasucks.aea.port.data;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

public final class AttackStrengthContext {
   private static final int NO_ATTACKER = -1;
   public static final long NONE = pack(-1, 0.0F);
   private static final ThreadLocal<long[]> CURRENT = ThreadLocal.withInitial(() -> new long[]{NONE});

   private AttackStrengthContext() {
   }

   private static long pack(int attackerId, float scale) {
      return (long)attackerId << 32 | Float.floatToRawIntBits(scale) & 4294967295L;
   }

   public static long set(int attackerId, float scale) {
      long[] holder = (long[])CURRENT.get();
      long previous = holder[0];
      holder[0] = pack(attackerId, scale);
      return previous;
   }

   public static void restore(long previous) {
      ((long[])CURRENT.get())[0] = previous;
   }

   public static boolean has(@Nullable class_1297 attacker) {
      return attacker == null ? false : (int)(((long[])CURRENT.get())[0] >> 32) == attacker.method_5628();
   }

   public static float scaleFor(@Nullable class_1297 attacker) {
      if (attacker == null) {
         return 0.0F;
      }

      long current = ((long[])CURRENT.get())[0];
      return (int)(current >> 32) != attacker.method_5628() ? 0.0F : Float.intBitsToFloat((int)current);
   }

   public static float charge(@Nullable class_1297 entity) {
      if (entity instanceof class_1657 player) {
         long current = ((long[])CURRENT.get())[0];
         return (int)(current >> 32) == player.method_5628() ? Float.intBitsToFloat((int)current) : player.method_7261(0.5F);
      } else {
         return 0.0F;
      }
   }
}
