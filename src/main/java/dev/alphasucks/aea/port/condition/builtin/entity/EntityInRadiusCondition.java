package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Shape;
import dev.alphasucks.aea.port.dev.DevParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_3218;

public final class EntityInRadiusCondition implements ConditionType<EntityCtx, EntityInRadiusCondition.Cfg> {
   @Override
   public MapCodec<EntityInRadiusCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(EntityInRadiusCondition.Cfg::bientityCondition),
               Codec.FLOAT.fieldOf("radius").forGetter(EntityInRadiusCondition.Cfg::radius),
               Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(EntityInRadiusCondition.Cfg::shape),
               Shape.HOLLOW_THICKNESS.forGetter(EntityInRadiusCondition.Cfg::hollowThickness),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(EntityInRadiusCondition.Cfg::comparison),
               Expression.INT_OR_EXPR.optionalFieldOf("compare_to", Expression.constant(1.0)).forGetter(EntityInRadiusCondition.Cfg::compareTo)
            )
            .apply(i, EntityInRadiusCondition.Cfg::new)
      );
   }

   public boolean test(EntityInRadiusCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 self = ctx.entity();
      double r = cfg.radius;
      if (DevParticles.due(ctx.level(), self, cfg) && ctx.level() instanceof class_3218 devLevel) {
         DevParticles.outlineCondition(devLevel, self.method_19538(), cfg.shape, r, r, r);
      }

      class_238 box = new class_238(
         self.method_23317() - r, self.method_23318() - r, self.method_23321() - r, self.method_23317() + r, self.method_23318() + r, self.method_23321() + r
      );
      List<class_1297> nearby = ctx.level().method_8335(self, box);
      boolean filtered = cfg.bientityCondition.isPresent();
      int threshold = cfg.compareTo.evalInt(self);
      double thickness = cfg.shape.thickness(cfg.hollowThickness, self);
      int count = 0;

      for (class_1297 candidate : nearby) {
         if (cfg.shape
               .contains(
                  candidate.method_23317() - self.method_23317(),
                  candidate.method_23318() - self.method_23318(),
                  candidate.method_23321() - self.method_23321(),
                  r,
                  r,
                  r,
                  thickness
               )
            && (!filtered || ((BiEntityCondition)cfg.bientityCondition.get()).test(new BiEntityCtx(self, candidate, ctx.level())))) {
            if (++count > threshold) {
               break;
            }
         }
      }

      return cfg.comparison.compare(count, threshold);
   }

   public record Cfg(
      Optional<BiEntityCondition> bientityCondition, float radius, Shape shape, Expression hollowThickness, Comparison comparison, Expression compareTo
   ) {
   }
}
