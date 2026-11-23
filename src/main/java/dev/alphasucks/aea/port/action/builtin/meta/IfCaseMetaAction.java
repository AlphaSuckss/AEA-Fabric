package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class IfCaseMetaAction<CTX, COND, ACTION> implements ActionType<CTX, IfCaseMetaAction.Cfg<COND, ACTION>> {
   private final Codec<COND> condCodec;
   private final Codec<ACTION> actionCodec;
   private final BiPredicate<COND, CTX> condTester;
   private final BiConsumer<ACTION, CTX> actionRunner;

   public IfCaseMetaAction(Codec<COND> condCodec, Codec<ACTION> actionCodec, BiPredicate<COND, CTX> condTester, BiConsumer<ACTION, CTX> actionRunner) {
      this.condCodec = condCodec;
      this.actionCodec = actionCodec;
      this.condTester = condTester;
      this.actionRunner = actionRunner;
   }

   @Override
   public MapCodec<IfCaseMetaAction.Cfg<COND, ACTION>> codec() {
      Codec<IfCaseMetaAction.Case<COND, ACTION>> caseCodec = RecordCodecBuilder.create(
         i -> i.group(
               this.condCodec.fieldOf("condition").forGetter(IfCaseMetaAction.Case::condition),
               this.actionCodec.fieldOf("action").forGetter(IfCaseMetaAction.Case::action)
            )
            .apply(i, IfCaseMetaAction.Case::new)
      );
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.list(caseCodec).fieldOf("cases").forGetter(IfCaseMetaAction.Cfg::cases)).apply(i, IfCaseMetaAction.Cfg::new)
         ),
         Map.of("actions", "cases")
      );
   }

   public void run(IfCaseMetaAction.Cfg<COND, ACTION> cfg, CTX ctx) {
      List<IfCaseMetaAction.Case<COND, ACTION>> cases = cfg.cases;
      int size = cases.size();
      boolean[] matched = new boolean[size];

      for (int i = 0; i < size; i++) {
         matched[i] = this.condTester.test(((IfCaseMetaAction.Case<COND, ACTION>)cases.get(i)).condition, ctx);
      }

      for (int i = 0; i < size; i++) {
         if (matched[i]) {
            this.actionRunner.accept(((IfCaseMetaAction.Case<COND, ACTION>)cases.get(i)).action, ctx);
         }
      }
   }

   public record Case<COND, ACTION>(COND condition, ACTION action) {
   }

   public record Cfg<COND, ACTION>(List<IfCaseMetaAction.Case<COND, ACTION>> cases) {
   }
}
