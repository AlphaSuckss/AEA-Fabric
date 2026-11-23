package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import java.util.Optional;
import net.minecraft.class_2960;

public final class EntitySetMembershipCondition implements ConditionType<EntityCtx, EntitySetMembershipCondition.Cfg> {
   @Override
   public MapCodec<EntitySetMembershipCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("set", IdCodecs.ID).forGetter(EntitySetMembershipCondition.Cfg::set),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(EntitySetMembershipCondition.Cfg::comparison),
               Expression.INT_OR_EXPR.optionalFieldOf("compare_to", Expression.constant(1.0)).forGetter(EntitySetMembershipCondition.Cfg::compareTo)
            )
            .apply(i, EntitySetMembershipCondition.Cfg::new)
      );
   }

   public boolean test(EntitySetMembershipCondition.Cfg cfg, EntityCtx ctx) {
      if (ctx.level().method_8608()) {
         return false;
      }

      int count = EntitySetPower.membershipCount(ctx.entity(), (class_2960)cfg.set.orElse(null));
      return cfg.comparison.compare(count, cfg.compareTo.evalInt(ctx.entity()));
   }

   public record Cfg(Optional<class_2960> set, Comparison comparison, Expression compareTo) {
   }
}
