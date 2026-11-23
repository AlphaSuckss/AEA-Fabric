package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.PowerTypes;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;

public final class ActionOnUseHandler {
   private static final Map<UUID, ActionOnUseHandler.Recent> RECENT = new ConcurrentHashMap();

   private ActionOnUseHandler() {
   }

   public static class_1269 fire(class_1657 actor, class_1297 target, class_1268 hand) {
      class_1269 outcome = fireSide(actor, actor, target, hand, false);
      return outcome != class_1269.field_5811 ? outcome : fireSide(target, actor, target, hand, true);
   }

   public static class_1269 fireOncePerTick(class_1657 actor, class_1297 target, class_1268 hand) {
      long now = actor.method_37908().method_8510();
      UUID uuid = actor.method_5667();
      ActionOnUseHandler.Recent prev = (ActionOnUseHandler.Recent)RECENT.get(uuid);
      if (prev != null && prev.gameTime() == now && prev.entity() == target.method_5628() && prev.hand() == hand.ordinal()) {
         return prev.result();
      }

      class_1269 result = fire(actor, target, hand);
      RECENT.put(uuid, new ActionOnUseHandler.Recent(now, target.method_5628(), hand.ordinal(), result));
      return result;
   }

   private static class_1269 fireSide(class_1297 holder, class_1657 actor, class_1297 target, class_1268 hand, boolean targetUsedSide) {
      PowerContainer container = PowerContainer.of(holder);
      if (container == null) {
         return class_1269.field_5811;
      }

      class_2960 onUseId = Apoli.id("action_on_use");

      for (class_2960 powerId : container.allPowers()) {
         Power power = ApoliPowers.get(powerId);
         if (power != null
            && !container.isSuppressed(powerId)
            && onUseId.equals(PowerTypeRegistry.resolveId(power.typeId()))
            && power.config() instanceof ActionOnUsePower.Config cfg
            && cfg.targetUsed() == targetUsedSide
            && (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(new EntityCtx(holder, holder.method_37908())))) {
            class_1269 result = PowerTypes.ACTION_ON_USE.tryFire(cfg, actor, target, hand);
            if (result != class_1269.field_5811) {
               return result;
            }
         }
      }

      return class_1269.field_5811;
   }

   private record Recent(long gameTime, int entity, int hand, class_1269 result) {
   }
}
