package dev.alphasucks.aea.port.data.sound;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record WeightedSound(String id, Optional<Float> volume, Optional<Float> pitch, int weight) {
   public static final String MUTE = "minecraft:empty";
   private static final Codec<WeightedSound> RECORD_CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(WeightedSound::id),
            Codec.FLOAT.optionalFieldOf("volume").forGetter(WeightedSound::volume),
            Codec.FLOAT.optionalFieldOf("pitch").forGetter(WeightedSound::pitch),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedSound::weight)
         )
         .apply(i, WeightedSound::new)
   );
   public static final Codec<WeightedSound> CODEC = Codec.either(Codec.STRING, RECORD_CODEC)
      .xmap(
         either -> (WeightedSound)either.map(WeightedSound::of, Function.identity()),
         sound -> sound.volume.isEmpty() && sound.pitch.isEmpty() && sound.weight == 1 ? Either.left(sound.id) : Either.right(sound)
      );
   public static final Codec<List<WeightedSound>> LIST_CODEC = Codec.either(CODEC, CODEC.listOf())
      .xmap(either -> (List)either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left((WeightedSound)list.get(0)) : Either.right(list));

   public WeightedSound {
      if (weight < 1) {
         weight = 1;
      }
   }

   public static WeightedSound of(String id) {
      return new WeightedSound(id, Optional.empty(), Optional.empty(), 1);
   }

   public boolean mutes() {
      return "minecraft:empty".equals(this.id);
   }

   public boolean hasCaptureReference() {
      return this.id.indexOf(36) >= 0;
   }

   public float volumeOr(float original) {
      return (Float)this.volume.orElse(original);
   }

   public float pitchOr(float original) {
      return (Float)this.pitch.orElse(original);
   }

   public WeightedSound withId(String newId) {
      return new WeightedSound(newId, this.volume, this.pitch, this.weight);
   }
}
