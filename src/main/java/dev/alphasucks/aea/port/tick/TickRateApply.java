package dev.alphasucks.aea.port.tick;

import dev.alphasucks.aea.port.data.Expression;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;

public final class TickRateApply {
   private TickRateApply() {
   }

   public static void apply(
      TickState state,
      MinecraftServer server,
      Optional<Expression> rate,
      Optional<Boolean> frozen,
      Optional<Expression> step,
      Optional<Expression> sprint,
      class_1297 actor,
      int duration
   ) {
      if (rate.isPresent()) {
         state.setRate(Math.max(0, ((Expression)rate.get()).evalInt(actor)));
      }

      frozen.ifPresent(state::setFrozen);
      if (step.isPresent()) {
         state.setStepTicks(Math.max(0, ((Expression)step.get()).evalInt(actor)));
      }

      if (sprint.isPresent()) {
         state.setSprintTicks(Math.max(0, ((Expression)sprint.get()).evalInt(actor)));
      }

      state.setExpiry(duration <= 0 ? -1L : server.method_3780() + duration);
   }

   public static void applyServer(
      MinecraftServer server,
      Optional<Expression> rate,
      Optional<Boolean> frozen,
      Optional<Expression> step,
      Optional<Expression> sprint,
      class_1297 actor,
      boolean reset,
      int duration
   ) {
      if (reset) {
         TickRates.releaseServer(server);
         TickRateVanilla.sprint(server, 0);
         TickRateVanilla.step(server, 0);
      } else {
         if (rate.isPresent() || frozen.isPresent()) {
            TickRates.requestServer(
               server,
               (Float)rate.map(expression -> (float)expression.eval(actor)).orElse(TickRateVanilla.exactRate(server)),
               (Boolean)frozen.orElse(TickRateVanilla.frozen(server)),
               duration
            );
         }

         step.ifPresent(expression -> TickRateVanilla.step(server, expression.evalInt(actor)));
         sprint.ifPresent(expression -> TickRateVanilla.sprint(server, expression.evalInt(actor)));
      }
   }

   public static void reset(TickScope scope, class_1297 entity) {
      switch (scope) {
         case ENTITY:
            TickRates.clearEntity(entity);
            break;
         case CHUNK:
            TickRates.clearChunk(entity.method_37908().method_27983(), entity.method_31476().method_8324());
            break;
         case DIMENSION:
            TickRates.clearDimension(entity.method_37908().method_27983());
            break;
         case SERVER:
            MinecraftServer server = entity.method_5682();
            if (server != null) {
               TickRates.releaseServer(server);
            }
      }
   }

   public static void resetChunk(class_5321<class_1937> dimension, long chunkPos) {
      TickRates.clearChunk(dimension, chunkPos);
   }
}
