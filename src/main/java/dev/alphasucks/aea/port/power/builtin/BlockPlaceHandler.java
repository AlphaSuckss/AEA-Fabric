package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PreventBlockPlacePower.Config;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;

public final class BlockPlaceHandler {
   private BlockPlaceHandler() {
   }

   public static boolean isPrevented(
      class_1657 player, class_1937 level, class_1268 hand, class_1799 stack, class_2338 toPos, class_2338 onPos, class_2350 direction
   ) {
      BlockCtx toCtx = new BlockCtx(toPos, level.method_8320(toPos), level, player);
      BlockCtx onCtx = new BlockCtx(onPos, level.method_8320(onPos), level, player);
      boolean[] prevented = new boolean[]{false};
      PowerLookup.forEach(player, ApoliIds.PREVENT_BLOCK_PLACE, Config.class, cfg -> {
         if (BlockInteractionHelper.handMatches(cfg.hands(), hand)) {
            if (BlockInteractionHelper.directionMatches(cfg.directions(), direction)) {
               if (BlockInteractionHelper.itemMatches(cfg.itemCondition(), stack, player)) {
                  if (!cfg.placeToCondition().isPresent() || ((BlockCondition)cfg.placeToCondition().get()).test(toCtx)) {
                     if (!cfg.placeOnCondition().isPresent() || ((BlockCondition)cfg.placeOnCondition().get()).test(onCtx)) {
                        prevented[0] = true;
                        if (!level.method_8608()) {
                           cfg.entityAction().ifPresent(action -> action.run(new EntityCtx(player, level)));
                           cfg.placeToAction().ifPresent(action -> action.run(toCtx));
                           cfg.placeOnAction().ifPresent(action -> action.run(onCtx));
                           BlockInteractionHelper.applyItemResult(player, hand, stack, cfg.heldItemAction(), cfg.resultStack(), cfg.resultItemAction());
                        }
                     }
                  }
               }
            }
         }
      });
      return prevented[0];
   }

   public static void fireAfterPlace(
      class_1657 player, class_1937 level, class_1268 hand, class_1799 stack, class_2338 toPos, class_2338 onPos, class_2350 direction
   ) {
      if (!level.method_8608()) {
         BlockCtx toCtx = new BlockCtx(toPos, level.method_8320(toPos), level, player);
         BlockCtx onCtx = new BlockCtx(onPos, level.method_8320(onPos), level, player);
         PowerLookup.forEach(player, ApoliIds.ACTION_ON_BLOCK_PLACE, dev.alphasucks.aea.port.power.builtin.ActionOnBlockPlacePower.Config.class, cfg -> {
            if (BlockInteractionHelper.handMatches(cfg.hands(), hand)) {
               if (BlockInteractionHelper.directionMatches(cfg.directions(), direction)) {
                  if (BlockInteractionHelper.itemMatches(cfg.itemCondition(), stack, player)) {
                     if (!cfg.placeToCondition().isPresent() || ((BlockCondition)cfg.placeToCondition().get()).test(toCtx)) {
                        if (!cfg.placeOnCondition().isPresent() || ((BlockCondition)cfg.placeOnCondition().get()).test(onCtx)) {
                           cfg.entityAction().ifPresent(action -> action.run(new EntityCtx(player, level)));
                           cfg.placeToAction().ifPresent(action -> action.run(toCtx));
                           cfg.placeOnAction().ifPresent(action -> action.run(onCtx));
                           BlockInteractionHelper.applyItemResult(player, hand, stack, cfg.heldItemAction(), cfg.resultStack(), cfg.resultItemAction());
                        }
                     }
                  }
               }
            }
         });
      }
   }
}
