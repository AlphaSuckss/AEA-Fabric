package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class NothingMetaCondition<CTX> implements ConditionType<CTX, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, CTX ctx) {
      return true;
   }
}
