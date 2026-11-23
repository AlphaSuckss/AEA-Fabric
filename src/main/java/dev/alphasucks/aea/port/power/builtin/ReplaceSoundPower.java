package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.data.sound.SoundReplacements;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public abstract class ReplaceSoundPower extends PowerType<ReplaceSoundPower.Config> {
   private static final MapCodec<ReplaceSoundPower.Config> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            SoundReplacements.CODEC.fieldOf("sounds").forGetter(ReplaceSoundPower.Config::sounds),
            Codec.BOOL.optionalFieldOf("replace", true).forGetter(ReplaceSoundPower.Config::replace),
            LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ReplaceSoundPower.Config::entityAction),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ReplaceSoundPower.Config::priority)
         )
         .apply(i, ReplaceSoundPower.Config::new)
   );

   @Override
   public MapCodec<ReplaceSoundPower.Config> configCodec() {
      return CODEC;
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public record Config(SoundReplacements sounds, boolean replace, Optional<EntityAction> entityAction, int priority) {
   }
}
