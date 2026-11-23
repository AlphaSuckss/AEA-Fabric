package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.tick.TickRateApply;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import dev.alphasucks.aea.port.tick.TickState;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class TickRateAction implements ActionType<EntityCtx, TickRateAction.Cfg> {
   @Override
   public MapCodec<TickRateAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TickScope.CODEC.optionalFieldOf("scope", TickScope.ENTITY).forGetter(TickRateAction.Cfg::scope),
               Expression.INT_OR_EXPR.optionalFieldOf("rate").forGetter(TickRateAction.Cfg::rate),
               Codec.BOOL.optionalFieldOf("frozen").forGetter(TickRateAction.Cfg::frozen),
               Expression.INT_OR_EXPR.optionalFieldOf("step").forGetter(TickRateAction.Cfg::step),
               Expression.INT_OR_EXPR.optionalFieldOf("sprint").forGetter(TickRateAction.Cfg::sprint),
               Codec.BOOL.optionalFieldOf("reset", false).forGetter(TickRateAction.Cfg::reset),
               Codec.INT.optionalFieldOf("duration", 0).forGetter(TickRateAction.Cfg::duration)
            )
            .apply(i, TickRateAction.Cfg::new)
      );
   }

   public void run(TickRateAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && ctx.level() instanceof class_3218 level) {
         MinecraftServer server = level.method_8503();
         if (server != null) {
            if (cfg.scope == TickScope.SERVER) {
               TickRateApply.applyServer(server, cfg.rate, cfg.frozen, cfg.step, cfg.sprint, entity, cfg.reset, cfg.duration);
            } else if (cfg.reset) {
               TickRateApply.reset(cfg.scope, entity);
            } else {
               TickState state = TickRates.stateFor(entity, cfg.scope);
               TickRateApply.apply(state, server, cfg.rate, cfg.frozen, cfg.step, cfg.sprint, entity, cfg.duration);
               TickRates.prune(cfg.scope, entity);
            }
         }
      }
   }

   public record Cfg(
      TickScope scope, Optional<Expression> rate, Optional<Boolean> frozen, Optional<Expression> step, Optional<Expression> sprint, boolean reset, int duration
   ) {
   }
}
