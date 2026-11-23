package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;

public final class ModifyBlockStuckSpeedHandler {
   private ModifyBlockStuckSpeedHandler() {
   }

   public static class_243 modify(class_1297 entity, class_2680 state, class_243 original) {
      PowerContainer container = PowerContainer.of(entity);
      if (container == null || container.isEmpty()) {
         return original;
      }

      if (container.powersOfType(ApoliIds.MODIFY_BLOCK_STUCK_SPEED).isEmpty()) {
         return original;
      }

      BlockCtx ctx = new BlockCtx(entity.method_24515(), state, entity.method_37908());
      class_243[] result = new class_243[]{null};
      PowerLookup.forEach(
         entity,
         ApoliIds.MODIFY_BLOCK_STUCK_SPEED,
         ModifyBlockStuckSpeedPower.Config.class,
         cfg -> {
            if (!cfg.blockCondition().isPresent() || ((BlockCondition)cfg.blockCondition().get()).test(ctx)) {
               Vector m = cfg.multiplier();
               class_243 current = result[0];
               result[0] = current == null
                  ? new class_243(m.x(), m.y(), m.z())
                  : new class_243(Math.max(current.field_1352, m.x()), Math.max(current.field_1351, m.y()), Math.max(current.field_1350, m.z()));
            }
         }
      );
      return result[0] == null ? original : result[0];
   }

   public static boolean fullyFree(class_243 multiplier) {
      return multiplier.field_1352 >= 1.0 && multiplier.field_1351 >= 1.0 && multiplier.field_1350 >= 1.0;
   }

   public static float modifySpeedFactor(class_1297 entity, float original) {
      if (original >= 1.0F) {
         return original;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container == null || container.isEmpty()) {
         return original;
      }

      if (container.powersOfType(ApoliIds.MODIFY_BLOCK_STUCK_SPEED).isEmpty()) {
         return original;
      }

      class_2338 pos = entity.method_24515();
      class_2680 state = entity.method_37908().method_8320(pos);
      class_2680 below = entity.method_37908().method_8320(pos.method_10074());
      BlockCtx atFeet = new BlockCtx(pos, state, entity.method_37908());
      BlockCtx underneath = new BlockCtx(pos.method_10074(), below, entity.method_37908());
      float[] result = new float[]{original};
      PowerLookup.forEach(
         entity,
         ApoliIds.MODIFY_BLOCK_STUCK_SPEED,
         ModifyBlockStuckSpeedPower.Config.class,
         cfg -> {
            if (!cfg.blockCondition().isPresent()
               || ((BlockCondition)cfg.blockCondition().get()).test(atFeet)
               || ((BlockCondition)cfg.blockCondition().get()).test(underneath)) {
               float m = Math.max(cfg.multiplier().x(), Math.max(cfg.multiplier().y(), cfg.multiplier().z()));
               if (m > result[0]) {
                  result[0] = m;
               }
            }
         }
      );
      return Math.min(result[0], 1.0F);
   }
}
