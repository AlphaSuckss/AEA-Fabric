package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1747;

public final class IsBlockItemCondition implements ConditionType<ItemCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, ItemCtx ctx) {
      return ctx.stack() != null && ctx.stack().method_7909() instanceof class_1747;
   }
}
