package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.DelayedActionQueue;
import dev.alphasucks.aea.port.data.Expression;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.class_1297;

public final class LoopMetaAction<CTX, W> implements ActionType<CTX, LoopMetaAction.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiConsumer<W, CTX> runner;
   private final Predicate<CTX> aliveCheck;
   private final Function<CTX, class_1297> subject;
   private static final Expression ONCE = Expression.constant(1.0);
   private static final int MAX_SYNC_ITERATIONS = 4096;

   public LoopMetaAction(Codec<W> wrapperCodec, BiConsumer<W, CTX> runner, Predicate<CTX> aliveCheck, Function<CTX, class_1297> subject) {
      this.wrapperCodec = wrapperCodec;
      this.runner = runner;
      this.aliveCheck = aliveCheck;
      this.subject = subject;
   }

   @Override
   public MapCodec<LoopMetaAction.Cfg<W>> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.INT_OR_EXPR.optionalFieldOf("value", ONCE).forGetter(LoopMetaAction.Cfg::value),
               Expression.INT_OR_EXPR.optionalFieldOf("ticks", ONCE).forGetter(LoopMetaAction.Cfg::ticks),
               this.wrapperCodec.optionalFieldOf("before_action").forGetter(LoopMetaAction.Cfg::beforeAction),
               this.wrapperCodec.optionalFieldOf("action").forGetter(LoopMetaAction.Cfg::action),
               this.wrapperCodec.optionalFieldOf("after_action").forGetter(LoopMetaAction.Cfg::afterAction)
            )
            .apply(i, LoopMetaAction.Cfg::new)
      );
   }

   public void run(LoopMetaAction.Cfg<W> cfg, CTX ctx) {
      class_1297 entity = (class_1297)this.subject.apply(ctx);
      int iterations = cfg.value.evalInt(entity);
      if (iterations > 0) {
         int interval = cfg.ticks.evalInt(entity);
         cfg.beforeAction.ifPresent(a -> this.runner.accept(a, ctx));
         if (cfg.action.isEmpty()) {
            cfg.afterAction.ifPresent(a -> this.runner.accept(a, ctx));
         } else {
            W body = (W)cfg.action.get();
            if (interval > 0) {
               this.runner.accept(body, ctx);
               if (iterations == 1) {
                  cfg.afterAction.ifPresent(a -> this.runner.accept(a, ctx));
               } else {
                  this.arm(iterations - 1, interval, body, cfg.afterAction, ctx);
               }
            } else {
               int count = Math.min(iterations, 4096);

               for (int i = 0; i < count; i++) {
                  this.runner.accept(body, ctx);
               }

               cfg.afterAction.ifPresent(a -> this.runner.accept(a, ctx));
            }
         }
      }
   }

   private void arm(int remaining, int interval, W body, Optional<W> afterAction, CTX ctx) {
      DelayedActionQueue.schedule(interval, () -> this.aliveCheck.test(ctx), () -> {
         this.runner.accept(body, ctx);
         if (remaining > 1) {
            this.arm(remaining - 1, interval, body, afterAction, ctx);
         } else {
            afterAction.ifPresent(a -> this.runner.accept(a, ctx));
         }
      });
   }

   public record Cfg<W>(Expression value, Expression ticks, Optional<W> beforeAction, Optional<W> action, Optional<W> afterAction) {
   }
}
