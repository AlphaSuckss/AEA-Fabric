package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.data.Expression;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

public final class RandomChanceMetaAction<CTX, W> implements ActionType<CTX, RandomChanceMetaAction.Cfg<W>> {
   private final Codec<W> wrapperCodec;
   private final BiConsumer<W, CTX> runner;
   private final Function<CTX, @Nullable class_1297> entityGetter;
   private final Function<CTX, @Nullable class_1937> levelGetter;

   public RandomChanceMetaAction(
      Codec<W> wrapperCodec, BiConsumer<W, CTX> runner, Function<CTX, @Nullable class_1297> entityGetter, Function<CTX, @Nullable class_1937> levelGetter
   ) {
      this.wrapperCodec = wrapperCodec;
      this.runner = runner;
      this.entityGetter = entityGetter;
      this.levelGetter = levelGetter;
   }

   @Override
   public MapCodec<RandomChanceMetaAction.Cfg<W>> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Expression.FLOAT_OR_EXPR.fieldOf("chance").forGetter(RandomChanceMetaAction.Cfg::chance),
                  this.wrapperCodec.fieldOf("action").forGetter(RandomChanceMetaAction.Cfg::action),
                  this.wrapperCodec.optionalFieldOf("fail_action").forGetter(RandomChanceMetaAction.Cfg::failAction)
               )
               .apply(i, RandomChanceMetaAction.Cfg::new)
         ),
         Map.of("success_action", "action")
      );
   }

   public void run(RandomChanceMetaAction.Cfg<W> cfg, CTX ctx) {
      class_1297 entity = (class_1297)this.entityGetter.apply(ctx);
      double chance = entity != null ? cfg.chance.eval(entity) : cfg.chance.eval((class_1937)this.levelGetter.apply(ctx));
      if (ThreadLocalRandom.current().nextDouble() < chance) {
         this.runner.accept(cfg.action, ctx);
      } else {
         cfg.failAction.ifPresent(a -> this.runner.accept(a, ctx));
      }
   }

   public record Cfg<W>(Expression chance, W action, Optional<W> failAction) {
   }
}
