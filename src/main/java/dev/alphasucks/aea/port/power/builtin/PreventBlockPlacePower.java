package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
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
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2350;
import net.minecraft.class_3542;

public final class PreventBlockPlacePower extends PowerType<PreventBlockPlacePower.Config> {
   @Override
   public MapCodec<PreventBlockPlacePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(PreventBlockPlacePower.Config::entityAction),
               LoggedOptionalField.of("held_item_action", ItemAction.CODEC).forGetter(PreventBlockPlacePower.Config::heldItemAction),
               LoggedOptionalField.of("place_to_action", BlockAction.CODEC).forGetter(PreventBlockPlacePower.Config::placeToAction),
               LoggedOptionalField.of("place_on_action", BlockAction.CODEC).forGetter(PreventBlockPlacePower.Config::placeOnAction),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(PreventBlockPlacePower.Config::itemCondition),
               LoggedOptionalField.strict("place_to_condition", BlockCondition.CODEC).forGetter(PreventBlockPlacePower.Config::placeToCondition),
               LoggedOptionalField.strict("place_on_condition", BlockCondition.CODEC).forGetter(PreventBlockPlacePower.Config::placeOnCondition),
               PreventBlockPlacePower.Dir.LIST_CODEC
                  .optionalFieldOf("directions", PreventBlockPlacePower.Dir.ALL)
                  .forGetter(PreventBlockPlacePower.Config::directions),
               Hand.LIST_CODEC.optionalFieldOf("hands", Hand.BOTH).forGetter(PreventBlockPlacePower.Config::hands),
               ItemStackData.CODEC.optionalFieldOf("result_stack").forGetter(PreventBlockPlacePower.Config::resultStack),
               LoggedOptionalField.of("result_item_action", ItemAction.CODEC).forGetter(PreventBlockPlacePower.Config::resultItemAction)
            )
            .apply(i, PreventBlockPlacePower.Config::new)
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
      List<PreventBlockPlacePower.Dir> directions,
      List<Hand> hands,
      Optional<ItemStackData> resultStack,
      Optional<ItemAction> resultItemAction
   ) {
   }

   public enum Dir implements class_3542 {
      UP("up", class_2350.field_11036),
      DOWN("down", class_2350.field_11033),
      NORTH("north", class_2350.field_11043),
      SOUTH("south", class_2350.field_11035),
      EAST("east", class_2350.field_11034),
      WEST("west", class_2350.field_11039);

      public static final Codec<PreventBlockPlacePower.Dir> CODEC = class_3542.method_28140(PreventBlockPlacePower.Dir::values);
      public static final Codec<List<PreventBlockPlacePower.Dir>> LIST_CODEC = Codec.list(CODEC);
      public static final List<PreventBlockPlacePower.Dir> ALL = List.of(values());
      private final String name;
      private final class_2350 vanilla;

      Dir(String n, class_2350 v) {
         this.name = n;
         this.vanilla = v;
      }

      public class_2350 vanilla() {
         return this.vanilla;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
