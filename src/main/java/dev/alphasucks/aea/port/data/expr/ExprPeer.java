package dev.alphasucks.aea.port.data.expr;

import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

public final class ExprPeer {
   public static final int ACTOR = 0;
   public static final int TARGET = 1;
   private static final ThreadLocal<class_1297[]> CURRENT = ThreadLocal.withInitial(() -> new class_1297[2]);

   private ExprPeer() {
   }

   public static class_1297[] frame() {
      return (class_1297[])CURRENT.get();
   }

   @Nullable
   public static class_1297 actor() {
      return ((class_1297[])CURRENT.get())[0];
   }

   @Nullable
   public static class_1297 target() {
      return ((class_1297[])CURRENT.get())[1];
   }
}
