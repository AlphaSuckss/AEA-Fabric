package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class PreventDeathPower extends PowerType<PreventDeathPower.Config> {
   @Override
   public MapCodec<PreventDeathPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(PreventDeathPower.Config::damageCondition),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(PreventDeathPower.Config::entityAction)
            )
            .apply(i, PreventDeathPower.Config::new)
      );
   }

   public record Config(Optional<DamageCondition> damageCondition, Optional<EntityAction> entityAction) {
   }
}
