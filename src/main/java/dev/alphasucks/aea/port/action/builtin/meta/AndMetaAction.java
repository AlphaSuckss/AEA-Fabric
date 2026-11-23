package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import java.util.List;
import java.util.function.BiConsumer;

public final class AndMetaAction<CTX, W> implements ActionType<CTX, AndMetaAction.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiConsumer<W, CTX> runner;

   public AndMetaAction(Codec<W> wrapperCodec, BiConsumer<W, CTX> runner) {
      this.wrapperCodec = wrapperCodec;
      this.runner = runner;
   }

   @Override
   public MapCodec<AndMetaAction.Cfg<W>> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.list(this.wrapperCodec).fieldOf("actions").forGetter(AndMetaAction.Cfg::actions)).apply(i, AndMetaAction.Cfg::new)
      );
   }

   public void run(AndMetaAction.Cfg<W> cfg, CTX ctx) {
      for (W w : cfg.actions) {
         this.runner.accept(w, ctx);
      }
   }

   public record Cfg<W>(List<W> actions) {
   }
}
