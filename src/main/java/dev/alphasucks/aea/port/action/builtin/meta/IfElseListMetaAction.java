package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class IfElseListMetaAction<CTX, COND, ACTION> implements ActionType<CTX, IfElseListMetaAction.Cfg<COND, ACTION>> {
   private final Codec<COND> condCodec;
   private final Codec<ACTION> actionCodec;
   private final BiPredicate<COND, CTX> condTester;
   private final BiConsumer<ACTION, CTX> actionRunner;

   public IfElseListMetaAction(Codec<COND> condCodec, Codec<ACTION> actionCodec, BiPredicate<COND, CTX> condTester, BiConsumer<ACTION, CTX> actionRunner) {
      this.condCodec = condCodec;
      this.actionCodec = actionCodec;
      this.condTester = condTester;
      this.actionRunner = actionRunner;
   }

   @Override
   public MapCodec<IfElseListMetaAction.Cfg<COND, ACTION>> codec() {
      Codec<IfElseListMetaAction.Branch<COND, ACTION>> branchCodec = RecordCodecBuilder.create(
         i -> i.group(
               this.condCodec.fieldOf("condition").forGetter(IfElseListMetaAction.Branch::condition),
               this.actionCodec.fieldOf("action").forGetter(IfElseListMetaAction.Branch::action)
            )
            .apply(i, IfElseListMetaAction.Branch::new)
      );
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.list(branchCodec).fieldOf("actions").forGetter(IfElseListMetaAction.Cfg::actions)).apply(i, IfElseListMetaAction.Cfg::new)
      );
   }

   public void run(IfElseListMetaAction.Cfg<COND, ACTION> cfg, CTX ctx) {
      List<IfElseListMetaAction.Branch<COND, ACTION>> branches = cfg.actions;
      int i = 0;

      for (int size = branches.size(); i < size; i++) {
         IfElseListMetaAction.Branch<COND, ACTION> branch = (IfElseListMetaAction.Branch<COND, ACTION>)branches.get(i);
         if (this.condTester.test(branch.condition, ctx)) {
            this.actionRunner.accept(branch.action, ctx);
            return;
         }
      }
   }

   public record Branch<COND, ACTION>(COND condition, ACTION action) {
   }

   public record Cfg<COND, ACTION>(List<IfElseListMetaAction.Branch<COND, ACTION>> actions) {
   }
}
