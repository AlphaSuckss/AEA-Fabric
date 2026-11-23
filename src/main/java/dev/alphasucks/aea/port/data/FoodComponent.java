package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import net.minecraft.class_4174;
import net.minecraft.class_4174.class_4175;
import org.jetbrains.annotations.Nullable;

public record FoodComponent(
   int hunger,
   float saturation,
   boolean meat,
   boolean alwaysEdible,
   boolean snack,
   Optional<Float> eatSeconds,
   Optional<FoodComponent.FoodEffect> effect,
   Optional<List<FoodComponent.FoodEffect>> effects
) {
   public static final float DEFAULT_EAT_SECONDS = 1.6F;
   public static final float SNACK_EAT_SECONDS = 0.8F;
   private static final MapCodec<FoodComponent> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.INT.fieldOf("hunger").forGetter(FoodComponent::hunger),
            Codec.FLOAT.fieldOf("saturation").forGetter(FoodComponent::saturation),
            Codec.BOOL.optionalFieldOf("meat", false).forGetter(FoodComponent::meat),
            Codec.BOOL.optionalFieldOf("always_edible", false).forGetter(FoodComponent::alwaysEdible),
            Codec.BOOL.optionalFieldOf("snack", false).forGetter(FoodComponent::snack),
            Codec.FLOAT.optionalFieldOf("eat_seconds").forGetter(FoodComponent::eatSeconds),
            FoodComponent.FoodEffect.CODEC.optionalFieldOf("effect").forGetter(FoodComponent::effect),
            Codec.list(FoodComponent.FoodEffect.CODEC).optionalFieldOf("effects").forGetter(FoodComponent::effects)
         )
         .apply(i, FoodComponent::new)
   );
   public static final Codec<FoodComponent> CODEC = AliasingMapCodec.wrap(INNER, Map.of("nutrition", "hunger", "can_always_eat", "always_edible")).codec();

   public float resolvedEatSeconds() {
      return (Float)this.eatSeconds.orElse(this.snack ? 0.8F : 1.6F);
   }

   public int eatDurationTicks() {
      return Math.max(1, (int)(this.resolvedEatSeconds() * 20.0F));
   }

   public class_4174 build(@Nullable class_1309 eater) {
      class_4175 b = new class_4175().method_19238(this.hunger).method_19237(this.saturation);
      if (this.meat) {
         b.method_19236();
      }

      if (this.alwaysEdible) {
         b.method_19240();
      }

      if (this.resolvedEatSeconds() <= 0.8F) {
         b.method_19241();
      }

      this.effect.ifPresent(entry -> b.method_19239(entry.spec().resolve(eater), entry.chance()));
      this.effects.ifPresent(list -> list.forEach(entry -> b.method_19239(entry.spec().resolve(eater), entry.chance())));
      return b.method_19242();
   }

   public List<Pair<class_1293, Float>> allEffectsWithProbability(@Nullable class_1309 eater) {
      ArrayList<Pair<class_1293, Float>> out = new ArrayList();
      this.effect.ifPresent(entry -> out.add(Pair.of(entry.spec().resolve(eater), entry.chance())));
      this.effects.ifPresent(list -> list.forEach(entry -> out.add(Pair.of(entry.spec().resolve(eater), entry.chance()))));
      return out;
   }

   public record FoodEffect(EffectSpec spec, float chance) {
      private static final Codec<FoodComponent.FoodEffect> FLAT = RecordCodecBuilder.create(
         i -> i.group(
               EffectSpec.MAP_CODEC.forGetter(FoodComponent.FoodEffect::spec),
               Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(FoodComponent.FoodEffect::chance)
            )
            .apply(i, FoodComponent.FoodEffect::new)
      );
      private static final Codec<FoodComponent.FoodEffect> NESTED = RecordCodecBuilder.create(
         i -> i.group(
               EffectSpec.CODEC.fieldOf("effect").forGetter(FoodComponent.FoodEffect::spec),
               Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(FoodComponent.FoodEffect::chance)
            )
            .apply(i, FoodComponent.FoodEffect::new)
      );
      public static final Codec<FoodComponent.FoodEffect> CODEC = Codec.either(FLAT, NESTED)
         .xmap(either -> (FoodComponent.FoodEffect)either.map(Function.identity(), Function.identity()), Either::left);
   }
}
