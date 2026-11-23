package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class ActionOnItemPickupPower extends PowerType<ActionOnItemPickupPower.Config> {
   @Override
   public MapCodec<ActionOnItemPickupPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnItemPickupPower.Config::bientityAction),
               LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(ActionOnItemPickupPower.Config::itemAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnItemPickupPower.Config::bientityCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ActionOnItemPickupPower.Config::itemCondition),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(ActionOnItemPickupPower.Config::priority)
            )
            .apply(i, ActionOnItemPickupPower.Config::new)
      );
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<ItemAction> itemAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<ItemCondition> itemCondition,
      int priority
   ) {
   }
}
