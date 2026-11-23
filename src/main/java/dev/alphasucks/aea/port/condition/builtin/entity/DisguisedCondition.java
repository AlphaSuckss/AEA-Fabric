package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class DisguisedCondition implements ConditionType<EntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, EntityCtx ctx) {
      return DisguiseManager.isDisguised(ctx.raw());
   }
}
