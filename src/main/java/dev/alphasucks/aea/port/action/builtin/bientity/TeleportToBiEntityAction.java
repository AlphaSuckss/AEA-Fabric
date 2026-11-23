package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.entity.TeleportHelper;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3218;

public final class TeleportToBiEntityAction implements ActionType<BiEntityCtx, TeleportToBiEntityAction.Cfg> {
   private final boolean swapByDefault;

   public TeleportToBiEntityAction(boolean swapByDefault) {
      this.swapByDefault = swapByDefault;
   }

   @Override
   public MapCodec<TeleportToBiEntityAction.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Codec.BOOL.optionalFieldOf("swap", this.swapByDefault).forGetter(TeleportToBiEntityAction.Cfg::swap),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("x", Expression.constant(0.0)).forGetter(TeleportToBiEntityAction.Cfg::x),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("y", Expression.constant(0.0)).forGetter(TeleportToBiEntityAction.Cfg::y),
                  Expression.FLOAT_OR_EXPR.optionalFieldOf("z", Expression.constant(0.0)).forGetter(TeleportToBiEntityAction.Cfg::z),
                  Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(TeleportToBiEntityAction.Cfg::space),
                  LoggedOptionalField.strict("landing_block_condition", BlockCondition.CODEC).forGetter(TeleportToBiEntityAction.Cfg::landingBlockCondition),
                  LoggedOptionalField.strict("landing_condition", EntityCondition.CODEC).forGetter(TeleportToBiEntityAction.Cfg::landingCondition),
                  Codec.BOOL.optionalFieldOf("loaded_chunks_only", true).forGetter(TeleportToBiEntityAction.Cfg::loadedChunksOnly),
                  LoggedOptionalField.of("success_action", BiEntityAction.CODEC).forGetter(TeleportToBiEntityAction.Cfg::successAction),
                  LoggedOptionalField.of("fail_action", BiEntityAction.CODEC).forGetter(TeleportToBiEntityAction.Cfg::failAction)
               )
               .apply(i, TeleportToBiEntityAction.Cfg::new)
         ),
         Map.of("swap_position", "swap")
      );
   }

   public void run(TeleportToBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 target = ctx.target();
      if (actor != null && target != null && actor != target) {
         if (target.method_37908() instanceof class_3218 targetLevel) {
            if (actor.method_37908() instanceof class_3218 actorLevel) {
               class_243 var14 = cfg.space.toGlobal(target, new class_243(cfg.x.eval(actor), cfg.y.eval(actor), cfg.z.eval(actor)));
               class_243 destination = target.method_19538().method_1019(var14);
               class_243 origin = actor.method_19538();
               if (cfg.loadedChunksOnly && !TeleportHelper.chunkLoaded(targetLevel, destination.field_1352, destination.field_1350)) {
                  cfg.failAction.ifPresent(a -> a.run(ctx));
               } else if (!TeleportHelper.landingAllowed(
                  actor, targetLevel, destination.field_1352, destination.field_1351, destination.field_1350, cfg.landingBlockCondition, cfg.landingCondition
               )) {
                  cfg.failAction.ifPresent(a -> a.run(ctx));
               } else {
                  class_1297 movedActor = TeleportHelper.teleport(actor, targetLevel, destination.field_1352, destination.field_1351, destination.field_1350);
                  if (movedActor == null) {
                     cfg.failAction.ifPresent(a -> a.run(ctx));
                  } else {
                     class_1297 movedTarget = target;
                     if (cfg.swap) {
                        class_1297 swapped = TeleportHelper.teleport(target, actorLevel, origin.field_1352, origin.field_1351, origin.field_1350);
                        if (swapped != null) {
                           movedTarget = swapped;
                        }
                     }

                     class_1297 finalTarget = movedTarget;
                     cfg.successAction.ifPresent(a -> a.run(new BiEntityCtx(movedActor, finalTarget, movedActor.method_37908())));
                  }
               }
            }
         }
      }
   }

   public record Cfg(
      boolean swap,
      Expression x,
      Expression y,
      Expression z,
      Space space,
      Optional<BlockCondition> landingBlockCondition,
      Optional<EntityCondition> landingCondition,
      boolean loadedChunksOnly,
      Optional<BiEntityAction> successAction,
      Optional<BiEntityAction> failAction
   ) {
   }
}
