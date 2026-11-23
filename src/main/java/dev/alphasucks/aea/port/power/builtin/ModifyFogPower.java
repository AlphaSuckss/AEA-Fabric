package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class ModifyFogPower extends PowerType<ModifyFogPower.Config> {
   @Override
   public MapCodec<ModifyFogPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.FLOAT_OR_EXPR.optionalFieldOf("s").forGetter(ModifyFogPower.Config::s),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("v").forGetter(ModifyFogPower.Config::v),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("r").forGetter(ModifyFogPower.Config::r),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("g").forGetter(ModifyFogPower.Config::g),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("b").forGetter(ModifyFogPower.Config::b),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("fade_in", Expression.constant(0.0)).forGetter(ModifyFogPower.Config::fadeIn),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("fade_out", Expression.constant(0.0)).forGetter(ModifyFogPower.Config::fadeOut),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyFogPower.Config::priority)
            )
            .apply(i, ModifyFogPower.Config::new)
      );
   }

   public record Config(
      Optional<Expression> s,
      Optional<Expression> v,
      Optional<Expression> r,
      Optional<Expression> g,
      Optional<Expression> b,
      Expression fadeIn,
      Expression fadeOut,
      int priority
   ) {
   }
}
