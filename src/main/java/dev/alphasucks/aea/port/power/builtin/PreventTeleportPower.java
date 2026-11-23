package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class PreventTeleportPower extends PowerType<PreventTeleportPower.Config> {
   @Override
   public MapCodec<PreventTeleportPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(PreventTeleportPower.Config::entityAction),
               Codec.BOOL.optionalFieldOf("prevent_dimension_change", true).forGetter(PreventTeleportPower.Config::preventDimensionChange)
            )
            .apply(i, PreventTeleportPower.Config::new)
      );
   }

   public record Config(Optional<EntityAction> entityAction, boolean preventDimensionChange) {
   }
}
