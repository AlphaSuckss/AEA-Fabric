package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.tick.TickRateApply;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import dev.alphasucks.aea.port.tick.TickState;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class TickRateBlockAction implements ActionType<BlockCtx, TickRateBlockAction.Cfg> {
   @Override
   public MapCodec<TickRateBlockAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TickScope.CODEC.optionalFieldOf("scope", TickScope.CHUNK).forGetter(TickRateBlockAction.Cfg::scope),
               Expression.INT_OR_EXPR.optionalFieldOf("rate").forGetter(TickRateBlockAction.Cfg::rate),
               Codec.BOOL.optionalFieldOf("frozen").forGetter(TickRateBlockAction.Cfg::frozen),
               Expression.INT_OR_EXPR.optionalFieldOf("step").forGetter(TickRateBlockAction.Cfg::step),
               Expression.INT_OR_EXPR.optionalFieldOf("sprint").forGetter(TickRateBlockAction.Cfg::sprint),
               Codec.BOOL.optionalFieldOf("reset", false).forGetter(TickRateBlockAction.Cfg::reset),
               Codec.INT.optionalFieldOf("duration", 0).forGetter(TickRateBlockAction.Cfg::duration)
            )
            .apply(i, TickRateBlockAction.Cfg::new)
      );
   }

   public void run(TickRateBlockAction.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 level) {
         MinecraftServer server = level.method_8503();
         if (server != null) {
            long chunkPos = class_1923.method_37232(ctx.pos());
            if (cfg.scope == TickScope.SERVER) {
               TickRateApply.applyServer(server, cfg.rate, cfg.frozen, cfg.step, cfg.sprint, ctx.actor(), cfg.reset, cfg.duration);
            } else if (cfg.reset) {
               if (cfg.scope == TickScope.DIMENSION) {
                  TickRates.clearDimension(level.method_27983());
               } else {
                  TickRateApply.resetChunk(level.method_27983(), chunkPos);
               }
            } else {
               TickState state = cfg.scope == TickScope.DIMENSION
                  ? TickRates.dimensionState(level.method_27983())
                  : TickRates.chunkState(level.method_27983(), chunkPos);
               TickRateApply.apply(state, server, cfg.rate, cfg.frozen, cfg.step, cfg.sprint, ctx.actor(), cfg.duration);
               if (state.rate() < 0 && !state.frozen() && state.stepTicks() == 0 && state.sprintTicks() == 0) {
                  if (cfg.scope == TickScope.DIMENSION) {
                     TickRates.clearDimension(level.method_27983());
                  } else {
                     TickRateApply.resetChunk(level.method_27983(), chunkPos);
                  }
               }
            }
         }
      }
   }

   public record Cfg(
      TickScope scope, Optional<Expression> rate, Optional<Boolean> frozen, Optional<Expression> step, Optional<Expression> sprint, boolean reset, int duration
   ) {
   }
}
