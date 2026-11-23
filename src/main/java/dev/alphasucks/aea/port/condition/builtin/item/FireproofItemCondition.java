package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class FireproofItemCondition implements ConditionType<ItemCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, ItemCtx ctx) {
      return ctx.stack().method_7909().method_24358();
   }
}
