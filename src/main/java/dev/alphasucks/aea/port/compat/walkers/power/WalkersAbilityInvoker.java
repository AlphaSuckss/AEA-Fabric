package dev.alphasucks.aea.port.compat.walkers.power;

import dev.alphasucks.aea.port.compat.walkers.WalkersBridge;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public final class WalkersAbilityInvoker {
   private static final MethodHandle USE_ABILITY;

   private WalkersAbilityInvoker() {
   }

   public static void invoke(class_1297 entity) {
      if (USE_ABILITY != null && entity instanceof class_1657 player) {
         try {
            USE_ABILITY.invoke(player);
         } catch (Throwable var3) {
         }
      }
   }

   static {
      MethodHandle handle = null;
      if (WalkersBridge.present()) {
         try {
            handle = MethodHandles.publicLookup()
               .findStatic(Class.forName("tocraft.walkers.api.PlayerAbilities"), "useAbility", MethodType.methodType(void.class, class_1657.class));
         } catch (Throwable var2) {
         }
      }

      USE_ABILITY = handle;
   }
}
