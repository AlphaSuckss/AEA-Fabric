package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;

public final class RandomChanceMeta<CTX> implements ConditionType<CTX, RandomChanceMeta.Cfg> {
   @Override
   public MapCodec<RandomChanceMeta.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.FLOAT.fieldOf("chance").forGetter(RandomChanceMeta.Cfg::chance)).apply(i, RandomChanceMeta.Cfg::new)
      );
   }

   public boolean test(RandomChanceMeta.Cfg cfg, CTX ctx) {
      return Math.random() < cfg.chance;
   }

   public record Cfg(float chance) {
   }
}
