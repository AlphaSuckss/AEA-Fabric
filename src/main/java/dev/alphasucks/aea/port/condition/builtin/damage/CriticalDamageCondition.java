package dev.alphasucks.aea.port.condition.builtin.damage;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.data.CriticalHitContext;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class CriticalDamageCondition implements ConditionType<DamageCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, DamageCtx ctx) {
      return CriticalHitContext.isCritical(ctx.source().method_5529());
   }
}
