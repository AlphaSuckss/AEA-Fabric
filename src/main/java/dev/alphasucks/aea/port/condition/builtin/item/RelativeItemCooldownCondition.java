package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_1657;
import net.minecraft.class_1799;

public final class RelativeItemCooldownCondition implements ConditionType<ItemCtx, RelativeItemCooldownCondition.Cfg> {
   @Override
   public MapCodec<RelativeItemCooldownCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.fieldOf("comparison").forGetter(RelativeItemCooldownCondition.Cfg::comparison),
               Codec.FLOAT.fieldOf("compare_to").forGetter(RelativeItemCooldownCondition.Cfg::compareTo)
            )
            .apply(i, RelativeItemCooldownCondition.Cfg::new)
      );
   }

   public boolean test(RelativeItemCooldownCondition.Cfg cfg, ItemCtx ctx) {
      class_1799 stack = ctx.stack();
      if (stack != null && !stack.method_7960()) {
         return ctx.holder() instanceof class_1657 player
            ? cfg.comparison.compare(player.method_7357().method_7905(stack.method_7909(), 0.0F), cfg.compareTo)
            : false;
      } else {
         return false;
      }
   }

   public record Cfg(Comparison comparison, float compareTo) {
   }
}
