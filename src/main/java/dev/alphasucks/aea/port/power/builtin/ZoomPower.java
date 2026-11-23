package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.PowerType;
import net.minecraft.class_2960;

public final class ZoomPower extends PowerType<ZoomPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("zoom");

   @Override
   public MapCodec<ZoomPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.FLOAT_OR_EXPR.optionalFieldOf("zoom", Expression.constant(4.0)).forGetter(ZoomPower.Config::zoom),
               Codec.BOOL.optionalFieldOf("scale_sensitivity", true).forGetter(ZoomPower.Config::scaleSensitivity),
               Codec.BOOL.optionalFieldOf("cinematic", false).forGetter(ZoomPower.Config::cinematic),
               Codec.BOOL.optionalFieldOf("hide_hand", false).forGetter(ZoomPower.Config::hideHand),
               Codec.floatRange(0.01F, 1.0F).optionalFieldOf("speed", 0.5F).forGetter(ZoomPower.Config::speed)
            )
            .apply(i, ZoomPower.Config::new)
      );
   }

   @Override
   public boolean resolvesForClient() {
      return true;
   }

   public record Config(Expression zoom, boolean scaleSensitivity, boolean cinematic, boolean hideHand, float speed) {
   }
}
