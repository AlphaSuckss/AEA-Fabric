package dev.alphasucks.aea.port.data;

import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

public final class CriticalHitContext {
   private static final int NO_ATTACKER = -1;
   public static final long NONE = pack(-1, false);
   private static final ThreadLocal<long[]> CURRENT = ThreadLocal.withInitial(() -> new long[]{NONE});

   private CriticalHitContext() {
   }

   private static long pack(int attackerId, boolean critical) {
      return (long)attackerId << 32 | (critical ? 1L : 0L);
   }

   public static long set(int attackerId, boolean critical) {
      long[] holder = (long[])CURRENT.get();
      long previous = holder[0];
      holder[0] = pack(attackerId, critical);
      return previous;
   }

   public static void restore(long previous) {
      ((long[])CURRENT.get())[0] = previous;
   }

   public static boolean isCritical(@Nullable class_1297 attacker) {
      if (attacker == null) {
         return false;
      }

      long current = ((long[])CURRENT.get())[0];
      return (int)(current >> 32) != attacker.method_5628() ? false : (current & 1L) != 0L;
   }
}
