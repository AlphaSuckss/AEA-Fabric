package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import net.minecraft.class_2338;

public final class OffsetBlockMetaCondition implements ConditionType<BlockCtx, OffsetBlockMetaCondition.Cfg> {
   @Override
   public MapCodec<OffsetBlockMetaCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               BlockCondition.CODEC.fieldOf("condition").forGetter(OffsetBlockMetaCondition.Cfg::condition),
               Codec.INT.optionalFieldOf("x", 0).forGetter(OffsetBlockMetaCondition.Cfg::x),
               Codec.INT.optionalFieldOf("y", 0).forGetter(OffsetBlockMetaCondition.Cfg::y),
               Codec.INT.optionalFieldOf("z", 0).forGetter(OffsetBlockMetaCondition.Cfg::z)
            )
            .apply(i, OffsetBlockMetaCondition.Cfg::new)
      );
   }

   public boolean test(OffsetBlockMetaCondition.Cfg cfg, BlockCtx ctx) {
      class_2338 offset = ctx.pos().method_10069(cfg.x, cfg.y, cfg.z);
      return cfg.condition.test(ctx.at(offset, ctx.level().method_8320(offset)));
   }

   public record Cfg(BlockCondition condition, int x, int y, int z) {
   }
}
