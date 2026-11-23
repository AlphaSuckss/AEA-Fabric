package dev.alphasucks.aea.port.compat.icarus;

import dev.cammiescorner.icarus.util.IcarusHelper;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.class_1309;
import net.minecraft.class_1799;

public final class IcarusFieldHooks {
   private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

   private IcarusFieldHooks() {
   }

   public static void install() {
      if (INSTALLED.compareAndSet(false, true)) {
         Predicate<class_1309> previousHasWings = IcarusHelper.hasWings;
         IcarusHelper.hasWings = (Predicate<class_1309>) entity -> WingsAccess.hasWings(entity) || previousHasWings.test(entity);
         Function<class_1309, class_1799> previousEquippedWings = IcarusHelper.getEquippedWings;
         IcarusHelper.getEquippedWings = (Function<class_1309, class_1799>) entity -> {
            WingsPower.Config cfg = WingsAccess.get(entity);
            return cfg != null ? cfg.wingsType().method_7972() : (class_1799)previousEquippedWings.apply(entity);
         };
      }
   }
}
