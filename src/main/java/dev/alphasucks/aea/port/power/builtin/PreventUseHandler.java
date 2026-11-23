package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerTypes;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public final class PreventUseHandler {
   private static final Map<UUID, PreventUseHandler.Claim> CLAIMED = new ConcurrentHashMap();

   private PreventUseHandler() {
   }

   public static boolean isPrevented(class_1657 actor, class_1297 target, class_1268 hand) {
      if (actor != null && target != null) {
         boolean runActions = !actor.method_37908().method_8608() && claim(actor, target, hand);
         return prevented(actor, actor, target, hand, false, runActions) || prevented(target, actor, target, hand, true, runActions);
      } else {
         return false;
      }
   }

   private static boolean claim(class_1657 actor, class_1297 target, class_1268 hand) {
      PreventUseHandler.Claim next = new PreventUseHandler.Claim(actor.method_37908().method_8510(), target.method_5628(), hand.ordinal());
      return !next.equals(CLAIMED.put(actor.method_5667(), next));
   }

   private static boolean prevented(class_1297 holder, class_1657 actor, class_1297 target, class_1268 hand, boolean targetUsedSide, boolean runActions) {
      boolean[] blocked = new boolean[]{false};
      PowerLookup.forEach(holder, ApoliIds.PREVENT_USE, PreventUsePower.Config.class, cfg -> {
         if (!blocked[0] && cfg.targetUsed() == targetUsedSide) {
            blocked[0] = PowerTypes.PREVENT_USE.tryPrevent(cfg, actor, target, hand, runActions);
         }
      });
      return blocked[0];
   }

   private record Claim(long gameTime, int entity, int hand) {
   }
}
