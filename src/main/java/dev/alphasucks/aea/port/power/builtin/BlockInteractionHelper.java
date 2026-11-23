package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.builtin.PreventBlockPlacePower.Dir;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2350;

public final class BlockInteractionHelper {
   private BlockInteractionHelper() {
   }

   public static boolean handMatches(List<Hand> hands, class_1268 hand) {
      if (hands.size() == Hand.BOTH.size()) {
         return true;
      }

      for (int i = 0; i < hands.size(); i++) {
         if (((Hand)hands.get(i)).vanilla() == hand) {
            return true;
         }
      }

      return false;
   }

   public static boolean directionMatches(List<Dir> dirs, class_2350 direction) {
      if (direction != null && dirs.size() != Dir.ALL.size()) {
         for (int i = 0; i < dirs.size(); i++) {
            if (((Dir)dirs.get(i)).vanilla() == direction) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean itemMatches(Optional<ItemCondition> condition, class_1799 stack, class_1657 player) {
      return condition.isEmpty() || ((ItemCondition)condition.get()).test(new ItemCtx(stack, player.method_37908(), player));
   }

   public static void applyItemResult(
      class_1657 player,
      class_1268 hand,
      class_1799 used,
      Optional<ItemAction> heldItemAction,
      Optional<ItemStackData> resultStack,
      Optional<ItemAction> resultItemAction
   ) {
      if (heldItemAction.isPresent()) {
         ((ItemAction)heldItemAction.get()).run(new ItemCtx(used, player.method_37908(), player));
      }

      if (!resultStack.isEmpty()) {
         class_1799[] result = new class_1799[]{((ItemStackData)resultStack.get()).stack().method_7972()};
         if (resultItemAction.isPresent()) {
            ((ItemAction)resultItemAction.get()).run(new ItemCtx(result[0], player.method_37908(), player, replaced -> result[0] = replaced));
         }

         giveResult(player, hand, result[0]);
      }
   }

   public static void giveResult(class_1657 player, class_1268 hand, class_1799 result) {
      if (!result.method_7960()) {
         if (player.method_5998(hand).method_7960()) {
            player.method_6122(hand, result);
         } else if (!player.method_31548().method_7394(result)) {
            player.method_7328(result, false);
         }
      }
   }
}
