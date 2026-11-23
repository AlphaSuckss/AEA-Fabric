package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;

public final class ModifyBouncinessHandler {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.MODIFY_BOUNCINESS);
   private static final int MATCHED = 1;
   private static final int DAMAGE = 2;
   private static final int PREVENTABLE = 4;

   private ModifyBouncinessHandler() {
   }

   public static boolean has(class_1309 entity) {
      if (!IN_USE.inUse()) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() ? !container.powersOfType(ApoliIds.MODIFY_BOUNCINESS).isEmpty() : false;
   }

   public static double modify(class_1309 entity, double original, BlockCtx block) {
      List<AttributeModifier> mods = new ArrayList();
      PowerLookup.forEach(entity, ApoliIds.MODIFY_BOUNCINESS, ModifyBouncinessPower.Config.class, cfg -> {
         if (cfg.blockCondition().isEmpty() || ((BlockCondition)cfg.blockCondition().get()).test(block)) {
            cfg.modifier().ifPresent(mods::add);
            cfg.modifiers().ifPresent(mods::addAll);
         }
      });
      return mods.isEmpty() ? 0.0 : Math.max(0.0, AttributeModifierHelper.apply(original, AttributeModifierHelper.ensureSorted(mods), entity));
   }

   public static boolean preventsFallDamage(class_1309 entity, BlockCtx block) {
      int flags = flags(entity, block);
      return (flags & 1) != 0 && (flags & 2) == 0;
   }

   public static boolean preventable(class_1309 entity, BlockCtx block) {
      return (flags(entity, block) & 4) != 0;
   }

   private static int flags(class_1309 entity, BlockCtx block) {
      int[] flags = new int[1];
      PowerLookup.forEach(entity, ApoliIds.MODIFY_BOUNCINESS, ModifyBouncinessPower.Config.class, cfg -> {
         if (!cfg.blockCondition().isPresent() || ((BlockCondition)cfg.blockCondition().get()).test(block)) {
            flags[0] |= 1;
            if (cfg.damage()) {
               flags[0] |= 2;
            }

            if (cfg.preventable()) {
               flags[0] |= 4;
            }
         }
      });
      return flags[0];
   }
}
