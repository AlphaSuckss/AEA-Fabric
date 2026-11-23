package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.entity.TeleportHelper;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class TeleportAction implements ActionType<EntityCtx, TeleportAction.Cfg> {
   @Override
   public MapCodec<TeleportAction.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("x", Expression.constant(0.0)).forGetter(TeleportAction.Cfg::x),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("y", Expression.constant(0.0)).forGetter(TeleportAction.Cfg::y),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("z", Expression.constant(0.0)).forGetter(TeleportAction.Cfg::z),
                  Codec.BOOL.optionalFieldOf("relative", true).forGetter(TeleportAction.Cfg::relative),
                  Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(TeleportAction.Cfg::space),
                  IdCodecs.ID.optionalFieldOf("dimension").forGetter(TeleportAction.Cfg::dimension),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("yaw").forGetter(TeleportAction.Cfg::yaw),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("pitch").forGetter(TeleportAction.Cfg::pitch),
                  LoggedOptionalField.strict("landing_block_condition", BlockCondition.CODEC).forGetter(TeleportAction.Cfg::landingBlockCondition),
                  LoggedOptionalField.strict("landing_condition", EntityCondition.CODEC).forGetter(TeleportAction.Cfg::landingCondition),
                  Codec.BOOL.optionalFieldOf("loaded_chunks_only", true).forGetter(TeleportAction.Cfg::loadedChunksOnly),
                  LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(TeleportAction.Cfg::successAction),
                  LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(TeleportAction.Cfg::failAction)
               )
               .apply(i, TeleportAction.Cfg::new)
         ),
         Map.of("relative_to_position", "relative")
      );
   }

   public void run(TeleportAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null) {
         class_3218 level = TeleportHelper.level(entity, cfg.dimension);
         if (level == null) {
            cfg.failAction.ifPresent(a -> a.run(ctx));
         } else {
            class_243 raw = new class_243(cfg.x.eval(entity), cfg.y.eval(entity), cfg.z.eval(entity));
            class_243 destination = cfg.relative ? entity.method_19538().method_1019(cfg.space.toGlobal(entity, raw)) : raw;
            if (cfg.loadedChunksOnly && !TeleportHelper.chunkLoaded(level, destination.field_1352, destination.field_1350)) {
               cfg.failAction.ifPresent(a -> a.run(ctx));
            } else if (!TeleportHelper.landingAllowed(
               entity, level, destination.field_1352, destination.field_1351, destination.field_1350, cfg.landingBlockCondition, cfg.landingCondition
            )) {
               cfg.failAction.ifPresent(a -> a.run(ctx));
            } else {
               float yaw = (Float)cfg.yaw.map(e -> (float)e.eval(entity)).orElse(entity.method_36454());
               float pitch = (Float)cfg.pitch.map(e -> (float)e.eval(entity)).orElse(entity.method_36455());
               class_1297 moved = TeleportHelper.teleport(entity, level, destination.field_1352, destination.field_1351, destination.field_1350, yaw, pitch);
               if (moved == null) {
                  cfg.failAction.ifPresent(a -> a.run(ctx));
               } else {
                  cfg.successAction.ifPresent(a -> a.run(new EntityCtx(moved, moved.method_37908())));
               }
            }
         }
      }
   }

   public record Cfg(
      Expression x,
      Expression y,
      Expression z,
      boolean relative,
      Space space,
      Optional<class_2960> dimension,
      Optional<Expression> yaw,
      Optional<Expression> pitch,
      Optional<BlockCondition> landingBlockCondition,
      Optional<EntityCondition> landingCondition,
      boolean loadedChunksOnly,
      Optional<EntityAction> successAction,
      Optional<EntityAction> failAction
   ) {
   }
}
