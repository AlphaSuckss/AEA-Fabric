package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public final class PreventItemUseHandler {
   private PreventItemUseHandler() {
   }

   public static boolean isBlocked(class_1657 user, class_1799 stack, class_1937 level) {
      if (stack.method_7960()) {
         return false;
      }

      ItemCtx ctx = new ItemCtx(stack, level, user);
      boolean[] block = new boolean[]{false};
      PowerLookup.forEach(user, Apoli.id("prevent_item_use"), PreventItemUsePower.Config.class, cfg -> {
         if (!block[0]) {
            if (cfg.itemCondition().isEmpty() || ((ItemCondition)cfg.itemCondition().get()).test(ctx)) {
               block[0] = true;
            }
         }
      });
      return block[0];
   }
}
