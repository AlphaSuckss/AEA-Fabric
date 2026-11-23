package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttackStrengthContext;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import net.minecraft.class_1657;

public final class AttackChargeCondition implements ConditionType<EntityCtx, AttackChargeCondition.Cfg> {
   @Override
   public MapCodec<AttackChargeCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.LESS_EQUAL).forGetter(AttackChargeCondition.Cfg::comparison),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("compare_to", Expression.constant(0.9F)).forGetter(AttackChargeCondition.Cfg::compareTo)
            )
            .apply(i, AttackChargeCondition.Cfg::new)
      );
   }

   public boolean test(AttackChargeCondition.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_1657 player) {
         float var5 = AttackStrengthContext.charge(player);
         return cfg.comparison.compare(var5, cfg.compareTo.eval(player, var5));
      } else {
         return false;
      }
   }

   public record Cfg(Comparison comparison, Expression compareTo) {
   }
}
