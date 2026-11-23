package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.entity.GrabManager;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class GrabbedBiEntityCondition implements ConditionType<BiEntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, BiEntityCtx ctx) {
      return ctx.actor() != null && ctx.target() != null && !ctx.level().method_8608()
         ? GrabManager.isGrabbedBy(ctx.target().method_5667(), ctx.actor().method_5667())
         : false;
   }
}
