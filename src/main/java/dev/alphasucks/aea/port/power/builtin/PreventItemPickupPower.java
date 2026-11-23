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

public final class PreventItemPickupPower extends PowerType<PreventItemPickupPower.Config> {
   @Override
   public MapCodec<PreventItemPickupPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action_thrower", BiEntityAction.CODEC).forGetter(PreventItemPickupPower.Config::bientityActionThrower),
               LoggedOptionalField.of("bientity_action_item", BiEntityAction.CODEC).forGetter(PreventItemPickupPower.Config::bientityActionItem),
               LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(PreventItemPickupPower.Config::itemAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(PreventItemPickupPower.Config::bientityCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(PreventItemPickupPower.Config::itemCondition),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(PreventItemPickupPower.Config::priority)
            )
            .apply(i, PreventItemPickupPower.Config::new)
      );
   }

   public record Config(
      Optional<BiEntityAction> bientityActionThrower,
      Optional<BiEntityAction> bientityActionItem,
      Optional<ItemAction> itemAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<ItemCondition> itemCondition,
      int priority
   ) {
   }
}
