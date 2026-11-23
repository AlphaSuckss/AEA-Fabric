package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record ModelAnimation(List<ModelAnimation.Entry> entries) {
   private static final Set<class_2960> WARNED = new HashSet();
   public static final Codec<ModelAnimation> CODEC = Codec.either(ModelAnimation.Entry.CODEC, ModelAnimation.Entry.LIST_CODEC)
      .xmap(
         either -> (ModelAnimation)either.map(e -> new ModelAnimation(List.of(e)), ModelAnimation::new),
         animation -> animation.entries.size() == 1 ? Either.left((ModelAnimation.Entry)animation.entries.get(0)) : Either.right(animation.entries)
      );

   @Nullable
   public ModelAnimation.Entry select(class_1297 entity) {
      EntityCtx ctx = null;
      int i = 0;

      ModelAnimation.Entry entry;
      while (true) {
         if (i >= this.entries.size()) {
            return null;
         }

         entry = (ModelAnimation.Entry)this.entries.get(i);
         if (!entry.condition.isPresent()) {
            break;
         }

         if (ctx == null) {
            ctx = new EntityCtx(entity, entity.method_37908());
         }

         if (passes(entry, ctx)) {
            break;
         }

         i++;
      }

      return entry;
   }

   private static boolean passes(ModelAnimation.Entry entry, EntityCtx ctx) {
      try {
         return ((EntityCondition)entry.condition.get()).test(ctx);
      } catch (Throwable t) {
         if (WARNED.add(entry.animation)) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] A condition on an 'animations' entry for '{}' threw while the model was being drawn, so that entry is being skipped. Conditions that need the server — predicate, command, scoreboard, advancement, stat — cannot be evaluated in a render layer.",
                  entry.animation,
                  t
               );
         }

         return false;
      }
   }

   public record Entry(class_2960 animation, Optional<String> name, Optional<EntityCondition> condition, float speed, Optional<ModelAnimation.LoopMode> loop) {
      public static final Codec<ModelAnimation.Entry> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               IdCodecs.ID.fieldOf("animation").forGetter(ModelAnimation.Entry::animation),
               Codec.STRING.optionalFieldOf("name").forGetter(ModelAnimation.Entry::name),
               LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(ModelAnimation.Entry::condition),
               Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(ModelAnimation.Entry::speed),
               ModelAnimation.LoopMode.CODEC.optionalFieldOf("loop").forGetter(ModelAnimation.Entry::loop)
            )
            .apply(i, ModelAnimation.Entry::new)
      );
      public static final Codec<List<ModelAnimation.Entry>> LIST_CODEC = new Codec<List<ModelAnimation.Entry>>() {
         public <T> DataResult<Pair<List<ModelAnimation.Entry>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getStream(input)
               .map(
                  stream -> {
                     List<ModelAnimation.Entry> out = new ArrayList();
                     stream.forEach(
                        element -> ModelAnimation.Entry.CODEC
                           .parse(ops, element)
                           .resultOrPartial(
                              error -> Apoli.LOGGER.warn("[Apoli] Skipping one entry of an 'animations' list — {}. Every other entry still loads.", error)
                           )
                           .ifPresent(out::add)
                     );
                     return Pair.of(List.copyOf(out), input);
                  }
               );
         }

         public <T> DataResult<T> encode(List<ModelAnimation.Entry> input, DynamicOps<T> ops, T prefix) {
            return ModelAnimation.Entry.CODEC.listOf().encode(input, ops, prefix);
         }
      };

      public int key() {
         int result = this.animation.hashCode();
         return 31 * result + (Integer)this.name.map(String::hashCode).orElse(0);
      }
   }

   public enum LoopMode {
      ONCE,
      HOLD,
      LOOP;

      public static final Codec<ModelAnimation.LoopMode> CODEC = Codec.either(Codec.BOOL, Codec.STRING)
         .comapFlatMap(
            either -> (DataResult)either.map(flag -> DataResult.success(flag ? LOOP : ONCE), ModelAnimation.LoopMode::byName),
            mode -> mode == HOLD ? Either.right("hold_on_last_frame") : Either.left(mode == LOOP)
         );

      private static DataResult<ModelAnimation.LoopMode> byName(String raw) {
         return switch (raw.toLowerCase(Locale.ROOT)) {
            case "true", "loop" -> DataResult.success(LOOP);
            case "false", "once", "play_once" -> DataResult.success(ONCE);
            case "hold", "hold_on_last_frame" -> DataResult.success(HOLD);
            default -> DataResult.error(() -> "'" + raw + "' is not a loop mode. Use true, false, or \"hold_on_last_frame\" to freeze on the final keyframe.");
         };
      }
   }
}
