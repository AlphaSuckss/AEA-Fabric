package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;

public final class ConstantMeta<CTX> implements ConditionType<CTX, ConstantMeta.Cfg> {
   @Override
   public MapCodec<ConstantMeta.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.fieldOf("value").forGetter(ConstantMeta.Cfg::value)).apply(i, ConstantMeta.Cfg::new));
   }

   public boolean test(ConstantMeta.Cfg cfg, CTX ctx) {
      return cfg.value;
   }

   public record Cfg(boolean value) {
   }
}
