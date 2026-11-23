package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_2609;

public final class FuelItemCondition implements ConditionType<ItemCtx, FuelItemCondition.Cfg> {
   @Override
   public MapCodec<FuelItemCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER).forGetter(FuelItemCondition.Cfg::comparison),
               Codec.INT.optionalFieldOf("compare_to", 0).forGetter(FuelItemCondition.Cfg::compareTo)
            )
            .apply(i, FuelItemCondition.Cfg::new)
      );
   }

   public boolean test(FuelItemCondition.Cfg cfg, ItemCtx ctx) {
      int burnTime = (Integer)class_2609.method_11196().getOrDefault(ctx.stack().method_7909(), 0);
      return cfg.comparison.compare(burnTime, cfg.compareTo);
   }

   public record Cfg(Comparison comparison, int compareTo) {
   }
}
