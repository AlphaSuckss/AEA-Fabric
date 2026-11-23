package dev.alphasucks.aea.port.action.builtin.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Expression;
import net.minecraft.class_1657;
import net.minecraft.class_1799;

public final class CooldownItemAction implements ActionType<ItemCtx, CooldownItemAction.Cfg> {
   @Override
   public MapCodec<CooldownItemAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Expression.INT_OR_EXPR.fieldOf("ticks").forGetter(CooldownItemAction.Cfg::ticks)).apply(i, CooldownItemAction.Cfg::new)
      );
   }

   public void run(CooldownItemAction.Cfg cfg, ItemCtx ctx) {
      if (ctx.holder() instanceof class_1657 player) {
         class_1799 stack = ctx.stack();
         if (stack != null && !stack.method_7960()) {
            int ticks = cfg.ticks.evalInt(player);
            if (ticks > 0) {
               player.method_7357().method_7906(stack.method_7909(), ticks);
            }
         }
      }
   }

   public record Cfg(Expression ticks) {
   }
}
