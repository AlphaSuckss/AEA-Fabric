package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.DelayedActionQueue;
import dev.alphasucks.aea.port.data.Expression;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.class_1297;

public final class DelayMetaAction<CTX, W> implements ActionType<CTX, DelayMetaAction.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiConsumer<W, CTX> runner;
   private final Predicate<CTX> aliveCheck;
   private final Function<CTX, class_1297> subject;

   public DelayMetaAction(Codec<W> wrapperCodec, BiConsumer<W, CTX> runner, Predicate<CTX> aliveCheck, Function<CTX, class_1297> subject) {
      this.wrapperCodec = wrapperCodec;
      this.runner = runner;
      this.aliveCheck = aliveCheck;
      this.subject = subject;
   }

   @Override
   public MapCodec<DelayMetaAction.Cfg<W>> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.INT_OR_EXPR.fieldOf("ticks").forGetter(DelayMetaAction.Cfg::ticks),
               this.wrapperCodec.fieldOf("action").forGetter(DelayMetaAction.Cfg::action)
            )
            .apply(i, DelayMetaAction.Cfg::new)
      );
   }

   public void run(DelayMetaAction.Cfg<W> cfg, CTX ctx) {
      int ticks = cfg.ticks.evalInt((class_1297)this.subject.apply(ctx));
      if (ticks <= 0) {
         this.runner.accept(cfg.action, ctx);
      } else {
         DelayedActionQueue.schedule(ticks, () -> this.aliveCheck.test(ctx), () -> this.runner.accept(cfg.action, ctx));
      }
   }

   public record Cfg<W>(Expression ticks, W action) {
   }
}
