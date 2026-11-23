package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import net.minecraft.class_1297;

public final class PreventTeleportHandler {
   private static boolean firing;
   private static int lastEntityId = -1;
   private static long lastTick = Long.MIN_VALUE;

   private PreventTeleportHandler() {
   }

   public static boolean prevented(class_1297 entity, boolean dimensionChange) {
      if (entity == null || entity.method_37908().method_8608()) {
         return false;
      }

      if (!PowerLookup.hasActive(entity, ApoliIds.PREVENT_TELEPORT)) {
         return false;
      }

      long tick = entity.method_37908().method_8510();
      boolean notify = !firing && (lastEntityId != entity.method_5628() || lastTick != tick);
      boolean[] blocked = new boolean[]{false};
      if (notify) {
         firing = true;
      }

      try {
         PowerLookup.forEach(entity, ApoliIds.PREVENT_TELEPORT, PreventTeleportPower.Config.class, config -> {
            if (!dimensionChange || config.preventDimensionChange()) {
               blocked[0] = true;
               if (notify) {
                  config.entityAction().ifPresent(action -> action.run(new EntityCtx(entity, entity.method_37908())));
               }
            }
         });
      } finally {
         if (notify) {
            firing = false;
         }
      }

      if (blocked[0] && notify) {
         lastEntityId = entity.method_5628();
         lastTick = tick;
      }

      return blocked[0];
   }
}
