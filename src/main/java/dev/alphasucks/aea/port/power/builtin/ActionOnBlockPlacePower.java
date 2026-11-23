package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.builtin.PreventBlockPlacePower.Dir;
import java.util.List;
import java.util.Optional;

public final class ActionOnBlockPlacePower extends PowerType<ActionOnBlockPlacePower.Config> {
   @Override
   public MapCodec<ActionOnBlockPlacePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnBlockPlacePower.Config::entityAction),
               LoggedOptionalField.of("held_item_action", ItemAction.CODEC).forGetter(ActionOnBlockPlacePower.Config::heldItemAction),
               LoggedOptionalField.of("place_to_action", BlockAction.CODEC).forGetter(ActionOnBlockPlacePower.Config::placeToAction),
               LoggedOptionalField.of("place_on_action", BlockAction.CODEC).forGetter(ActionOnBlockPlacePower.Config::placeOnAction),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ActionOnBlockPlacePower.Config::itemCondition),
               LoggedOptionalField.strict("place_to_condition", BlockCondition.CODEC).forGetter(ActionOnBlockPlacePower.Config::placeToCondition),
               LoggedOptionalField.strict("place_on_condition", BlockCondition.CODEC).forGetter(ActionOnBlockPlacePower.Config::placeOnCondition),
               Dir.LIST_CODEC.optionalFieldOf("directions", Dir.ALL).forGetter(ActionOnBlockPlacePower.Config::directions),
               Hand.LIST_CODEC.optionalFieldOf("hands", Hand.BOTH).forGetter(ActionOnBlockPlacePower.Config::hands),
               ItemStackData.CODEC.optionalFieldOf("result_stack").forGetter(ActionOnBlockPlacePower.Config::resultStack),
               LoggedOptionalField.of("result_item_action", ItemAction.CODEC).forGetter(ActionOnBlockPlacePower.Config::resultItemAction)
            )
            .apply(i, ActionOnBlockPlacePower.Config::new)
      );
   }

   public record Config(
      Optional<EntityAction> entityAction,
      Optional<ItemAction> heldItemAction,
      Optional<BlockAction> placeToAction,
      Optional<BlockAction> placeOnAction,
      Optional<ItemCondition> itemCondition,
      Optional<BlockCondition> placeToCondition,
      Optional<BlockCondition> placeOnCondition,
      List<Dir> directions,
      List<Hand> hands,
      Optional<ItemStackData> resultStack,
      Optional<ItemAction> resultItemAction
   ) {
   }
}
