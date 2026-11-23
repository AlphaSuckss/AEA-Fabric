package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.tick.TickRateVanilla;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.server.MinecraftServer;

public final class TickRateBlockCondition implements ConditionType<BlockCtx, TickRateBlockCondition.Cfg> {
   @Override
   public MapCodec<TickRateBlockCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TickScope.CODEC.optionalFieldOf("scope", TickScope.CHUNK).forGetter(TickRateBlockCondition.Cfg::scope),
               Comparison.CODEC.optionalFieldOf("comparison").forGetter(TickRateBlockCondition.Cfg::comparison),
               Expression.INT_OR_EXPR.optionalFieldOf("compare_to").forGetter(TickRateBlockCondition.Cfg::compareTo),
               Codec.BOOL.optionalFieldOf("frozen").forGetter(TickRateBlockCondition.Cfg::frozen)
            )
            .apply(i, TickRateBlockCondition.Cfg::new)
      );
   }

   public boolean test(TickRateBlockCondition.Cfg cfg, BlockCtx ctx) {
      MinecraftServer server = ctx.level().method_8503();
      int base = TickRateVanilla.rate(server);
      int rate = cfg.scope == TickScope.DIMENSION
         ? TickRates.effectiveDimensionRate(ctx.level(), base)
         : TickRates.effectiveChunkRate(ctx.level(), class_1923.method_37232(ctx.pos()), base);
      if (cfg.frozen.isPresent() && (Boolean)cfg.frozen.get() != (rate == 0)) {
         return false;
      }

      if (cfg.comparison.isEmpty()) {
         return true;
      }

      double compareTo = (Double)cfg.compareTo.map(expression -> expression.eval(ctx.actor())).orElse(0.0);
      return ((Comparison)cfg.comparison.get()).compare(rate, compareTo);
   }

   public record Cfg(TickScope scope, Optional<Comparison> comparison, Optional<Expression> compareTo, Optional<Boolean> frozen) {
   }
}
