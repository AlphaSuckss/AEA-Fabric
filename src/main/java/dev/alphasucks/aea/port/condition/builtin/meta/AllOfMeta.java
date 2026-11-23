package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import java.util.List;
import java.util.function.BiPredicate;

public final class AllOfMeta<CTX, W> implements ConditionType<CTX, AllOfMeta.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiPredicate<W, CTX> tester;

   public AllOfMeta(Codec<W> wrapperCodec, BiPredicate<W, CTX> tester) {
      this.wrapperCodec = wrapperCodec;
      this.tester = tester;
   }

   @Override
   public MapCodec<AllOfMeta.Cfg<W>> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.list(this.wrapperCodec).fieldOf("conditions").forGetter(AllOfMeta.Cfg::conditions)).apply(i, AllOfMeta.Cfg::new)
      );
   }

   public boolean test(AllOfMeta.Cfg<W> cfg, CTX ctx) {
      for (W w : cfg.conditions) {
         if (!this.tester.test(w, ctx)) {
            return false;
         }
      }

      return true;
   }

   public record Cfg<W>(List<W> conditions) {
   }
}
