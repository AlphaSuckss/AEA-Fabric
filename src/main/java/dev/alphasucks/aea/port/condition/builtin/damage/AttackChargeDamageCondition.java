package dev.alphasucks.aea.port.condition.builtin.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.data.AttackStrengthContext;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import net.minecraft.class_1297;

public final class AttackChargeDamageCondition implements ConditionType<DamageCtx, AttackChargeDamageCondition.Cfg> {
   public static final float FULL_CHARGE = 0.9F;

   @Override
   public MapCodec<AttackChargeDamageCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.LESS_EQUAL).forGetter(AttackChargeDamageCondition.Cfg::comparison),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("compare_to", Expression.constant(0.9F)).forGetter(AttackChargeDamageCondition.Cfg::compareTo)
            )
            .apply(i, AttackChargeDamageCondition.Cfg::new)
      );
   }

   public boolean test(AttackChargeDamageCondition.Cfg cfg, DamageCtx ctx) {
      class_1297 attacker = ctx.source().method_5529();
      if (!AttackStrengthContext.has(attacker)) {
         return false;
      }

      float scale = AttackStrengthContext.scaleFor(attacker);
      return cfg.comparison.compare(scale, cfg.compareTo.eval(attacker, scale));
   }

   public record Cfg(Comparison comparison, Expression compareTo) {
   }
}
