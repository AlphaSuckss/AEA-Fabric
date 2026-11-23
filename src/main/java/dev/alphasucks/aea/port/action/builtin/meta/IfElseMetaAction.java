package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class IfElseMetaAction<CTX, COND, ACTION> implements ActionType<CTX, IfElseMetaAction.Cfg<COND, ACTION>> {
   private final Codec<COND> condCodec;
   private final Codec<ACTION> actionCodec;
   private final BiPredicate<COND, CTX> condTester;
   private final BiConsumer<ACTION, CTX> actionRunner;

   public IfElseMetaAction(Codec<COND> condCodec, Codec<ACTION> actionCodec, BiPredicate<COND, CTX> condTester, BiConsumer<ACTION, CTX> actionRunner) {
      this.condCodec = condCodec;
      this.actionCodec = actionCodec;
      this.condTester = condTester;
      this.actionRunner = actionRunner;
   }

   @Override
   public MapCodec<IfElseMetaAction.Cfg<COND, ACTION>> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               this.condCodec.fieldOf("condition").forGetter(IfElseMetaAction.Cfg::condition),
               this.actionCodec.fieldOf("if_action").forGetter(IfElseMetaAction.Cfg::ifAction),
               this.actionCodec.optionalFieldOf("else_action").forGetter(IfElseMetaAction.Cfg::elseAction)
            )
            .apply(i, IfElseMetaAction.Cfg::new)
      );
   }

   public void run(IfElseMetaAction.Cfg<COND, ACTION> cfg, CTX ctx) {
      if (this.condTester.test(cfg.condition, ctx)) {
         this.actionRunner.accept(cfg.ifAction, ctx);
      } else {
         cfg.elseAction.ifPresent(a -> this.actionRunner.accept(a, ctx));
      }
   }

   public record Cfg<COND, ACTION>(COND condition, ACTION ifAction, Optional<ACTION> elseAction) {
   }
}
