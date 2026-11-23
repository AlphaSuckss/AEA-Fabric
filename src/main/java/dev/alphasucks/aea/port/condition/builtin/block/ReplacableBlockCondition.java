package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class ReplacableBlockCondition implements ConditionType<BlockCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, BlockCtx ctx) {
      return ctx.state().method_45474();
   }
}
