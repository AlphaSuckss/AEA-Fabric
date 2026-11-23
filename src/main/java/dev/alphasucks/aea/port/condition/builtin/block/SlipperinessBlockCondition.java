package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Comparison;

public final class SlipperinessBlockCondition implements ConditionType<BlockCtx, SlipperinessBlockCondition.Cfg> {
   @Override
   public MapCodec<SlipperinessBlockCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.fieldOf("comparison").forGetter(SlipperinessBlockCondition.Cfg::comparison),
               Codec.FLOAT.fieldOf("compare_to").forGetter(SlipperinessBlockCondition.Cfg::compareTo)
            )
            .apply(i, SlipperinessBlockCondition.Cfg::new)
      );
   }

   public boolean test(SlipperinessBlockCondition.Cfg cfg, BlockCtx ctx) {
      return cfg.comparison.compare(ctx.state().method_26204().method_9499(), cfg.compareTo);
   }

   public record Cfg(Comparison comparison, float compareTo) {
   }
}
