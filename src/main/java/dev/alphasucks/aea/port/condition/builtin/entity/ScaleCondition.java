package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypeCodec;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.minecraft.class_1297;

public final class ScaleCondition implements ConditionType<EntityCtx, ScaleCondition.Cfg> {
   @Override
   public MapCodec<ScaleCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ScaleTypeCodec.CODEC.optionalFieldOf("scale_type", ScaleTypes.BASE).forGetter(ScaleCondition.Cfg::type),
               Comparison.CODEC.fieldOf("comparison").forGetter(ScaleCondition.Cfg::comparison),
               Expression.DOUBLE_OR_EXPR.fieldOf("compare_to").forGetter(ScaleCondition.Cfg::compareTo)
            )
            .apply(i, ScaleCondition.Cfg::new)
      );
   }

   public boolean test(ScaleCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      return entity == null ? false : cfg.comparison.compare(Scales.value(entity, cfg.type), cfg.compareTo.eval(entity));
   }

   public record Cfg(ScaleType type, Comparison comparison, Expression compareTo) {
   }
}
