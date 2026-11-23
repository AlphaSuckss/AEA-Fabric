package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import java.util.List;
import java.util.function.BiConsumer;

public final class ChoiceMetaAction<CTX, W> implements ActionType<CTX, ChoiceMetaAction.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiConsumer<W, CTX> runner;

   public ChoiceMetaAction(Codec<W> wrapperCodec, BiConsumer<W, CTX> runner) {
      this.wrapperCodec = wrapperCodec;
      this.runner = runner;
   }

   @Override
   public MapCodec<ChoiceMetaAction.Cfg<W>> codec() {
      Codec<ChoiceMetaAction.Weighted<W>> weightedCodec = RecordCodecBuilder.create(
         i -> i.group(
               Codec.INT.fieldOf("weight").forGetter(ChoiceMetaAction.Weighted::weight),
               this.wrapperCodec.fieldOf("element").forGetter(ChoiceMetaAction.Weighted::element)
            )
            .apply(i, ChoiceMetaAction.Weighted::new)
      );
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.list(weightedCodec).fieldOf("actions").forGetter(ChoiceMetaAction.Cfg::actions)).apply(i, ChoiceMetaAction.Cfg::new)
      );
   }

   public void run(ChoiceMetaAction.Cfg<W> cfg, CTX ctx) {
      int total = 0;

      for (ChoiceMetaAction.Weighted<W> w : cfg.actions) {
         total += Math.max(0, w.weight);
      }

      if (total > 0) {
         int roll = (int)Math.floor(Math.random() * total);

         for (ChoiceMetaAction.Weighted<W> w : cfg.actions) {
            roll -= Math.max(0, w.weight);
            if (roll < 0) {
               this.runner.accept(w.element, ctx);
               return;
            }
         }
      }
   }

   public record Cfg<W>(List<ChoiceMetaAction.Weighted<W>> actions) {
   }

   public record Weighted<W>(int weight, W element) {
   }
}
