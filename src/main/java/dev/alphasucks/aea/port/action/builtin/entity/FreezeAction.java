package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import java.util.Optional;
import net.minecraft.class_1297;

public final class FreezeAction implements ActionType<EntityCtx, FreezeAction.Cfg> {
   public static final int PERMANENT = 1000000000;
   private static final int DRAIN_PER_TICK = 2;

   @Override
   public MapCodec<FreezeAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.strict("duration", Expression.INT_OR_EXPR).forGetter(FreezeAction.Cfg::duration)).apply(i, FreezeAction.Cfg::new)
      );
   }

   public void run(FreezeAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (cfg.duration.isEmpty()) {
         entity.method_32317(entity.method_32315() + 2);
      } else {
         int ticks = ((Expression)cfg.duration.get()).evalInt(entity);
         entity.method_32317(ticks < 0 ? 1000000000 : ticks);
      }
   }

   public record Cfg(Optional<Expression> duration) {
   }
}
