package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import java.util.Optional;
import net.minecraft.class_1297;

public final class TickRateCondition implements ConditionType<EntityCtx, TickRateCondition.Cfg> {
   @Override
   public MapCodec<TickRateCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TickScope.CODEC.optionalFieldOf("scope", TickScope.ENTITY).forGetter(TickRateCondition.Cfg::scope),
               Comparison.CODEC.optionalFieldOf("comparison").forGetter(TickRateCondition.Cfg::comparison),
               Expression.INT_OR_EXPR.optionalFieldOf("compare_to").forGetter(TickRateCondition.Cfg::compareTo),
               Codec.BOOL.optionalFieldOf("frozen").forGetter(TickRateCondition.Cfg::frozen)
            )
            .apply(i, TickRateCondition.Cfg::new)
      );
   }

   public boolean test(TickRateCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity == null) {
         return false;
      }

      int rate = TickRates.effectiveRate(entity, cfg.scope);
      if (cfg.frozen.isPresent() && (Boolean)cfg.frozen.get() != (rate == 0)) {
         return false;
      }

      if (cfg.comparison.isEmpty()) {
         return true;
      }

      double compareTo = (Double)cfg.compareTo.map(expression -> expression.eval(entity)).orElse(0.0);
      return ((Comparison)cfg.comparison.get()).compare(rate, compareTo);
   }

   public record Cfg(TickScope scope, Optional<Comparison> comparison, Optional<Expression> compareTo, Optional<Boolean> frozen) {
   }
}
