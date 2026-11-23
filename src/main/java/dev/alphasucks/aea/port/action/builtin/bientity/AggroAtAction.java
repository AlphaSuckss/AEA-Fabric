package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import net.minecraft.class_1657;
import net.minecraft.class_5354;

public final class AggroAtAction implements ActionType<BiEntityCtx, AggroAtAction.Cfg> {
   private static final Expression FOREVER = Expression.constant(-1.0);

   @Override
   public MapCodec<AggroAtAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Expression.INT_OR_EXPR.optionalFieldOf("duration", FOREVER).forGetter(AggroAtAction.Cfg::duration)).apply(i, AggroAtAction.Cfg::new)
      );
   }

   public void run(AggroAtAction.Cfg cfg, BiEntityCtx ctx) {
      if (ctx.target() instanceof class_5354 neutralMob) {
         if (ctx.actor() instanceof class_1657 player) {
            int var7 = cfg.duration.evalInt(player);
            neutralMob.method_29513(player.method_5667());
            neutralMob.method_29514(var7 < 0 ? Integer.MAX_VALUE : var7);
         }
      }
   }

   public record Cfg(Expression duration) {
   }
}
