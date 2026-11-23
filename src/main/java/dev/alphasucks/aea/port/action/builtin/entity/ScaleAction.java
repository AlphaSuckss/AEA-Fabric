package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.scale.ScaleEasing;
import dev.alphasucks.aea.port.scale.ScaleOperation;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypeCodec;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import java.util.List;
import net.minecraft.class_1297;

public final class ScaleAction implements ActionType<EntityCtx, ScaleAction.Cfg> {
   @Override
   public MapCodec<ScaleAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ScaleTypeCodec.LIST_OR_SINGLE.optionalFieldOf("scale_types", List.of(ScaleTypes.BASE)).forGetter(ScaleAction.Cfg::types),
               ScaleOperation.CODEC.optionalFieldOf("operation", ScaleOperation.SET).forGetter(ScaleAction.Cfg::operation),
               Expression.DOUBLE_OR_EXPR.optionalFieldOf("scale", Expression.constant(1.0)).forGetter(ScaleAction.Cfg::scale),
               Expression.INT_OR_EXPR.optionalFieldOf("ticks", Expression.constant(0.0)).forGetter(ScaleAction.Cfg::ticks),
               ScaleEasing.CODEC.optionalFieldOf("easing", ScaleEasing.LINEAR).forGetter(ScaleAction.Cfg::easing)
            )
            .apply(i, ScaleAction.Cfg::new)
      );
   }

   public void run(ScaleAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && !entity.method_37908().method_8608()) {
         float argument = (float)cfg.scale.eval(entity);
         int ticks = Math.max(0, cfg.ticks.evalInt(entity));
         Scales.setAll(entity, cfg.types, cfg.operation, argument, ticks, cfg.easing);
      }
   }

   public record Cfg(List<ScaleType> types, ScaleOperation operation, Expression scale, Expression ticks, ScaleEasing easing) {
   }
}
