package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.entity.GrabManager;
import net.minecraft.class_1297;

public final class GrabAction implements ActionType<BiEntityCtx, GrabAction.Cfg> {
   private static final Expression FOREVER = Expression.constant(-1.0);
   private static final Expression DEFAULT_DISTANCE = Expression.constant(2.0);

   @Override
   public MapCodec<GrabAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.INT_OR_EXPR.optionalFieldOf("duration", FOREVER).forGetter(GrabAction.Cfg::duration),
               Expression.DOUBLE_OR_EXPR.optionalFieldOf("distance", DEFAULT_DISTANCE).forGetter(GrabAction.Cfg::distance),
               Codec.BOOL.optionalFieldOf("disable_grabber", false).forGetter(GrabAction.Cfg::disableGrabber),
               Codec.BOOL.optionalFieldOf("disable_grabbed", false).forGetter(GrabAction.Cfg::disableGrabbed),
               Codec.BOOL.optionalFieldOf("horizontal_only", false).forGetter(GrabAction.Cfg::horizontalOnly),
               Codec.BOOL.optionalFieldOf("vertical_only", false).forGetter(GrabAction.Cfg::verticalOnly)
            )
            .apply(i, GrabAction.Cfg::new)
      );
   }

   public void run(GrabAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 target = ctx.target();
      if (actor != null && target != null && !actor.method_37908().method_8608()) {
         GrabManager.start(
            actor, target, cfg.duration().evalInt(actor), cfg.distance(), cfg.disableGrabber(), cfg.disableGrabbed(), cfg.horizontalOnly(), cfg.verticalOnly()
         );
      }
   }

   public record Cfg(Expression duration, Expression distance, boolean disableGrabber, boolean disableGrabbed, boolean horizontalOnly, boolean verticalOnly) {
   }
}
