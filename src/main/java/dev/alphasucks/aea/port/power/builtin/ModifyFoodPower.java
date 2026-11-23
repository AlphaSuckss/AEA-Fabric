package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModifyFoodPower extends PowerType<ModifyFoodPower.Config> {
   private static final MapCodec<ModifyFoodPower.Config> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ModifyFoodPower.Config::entityAction),
            LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(ModifyFoodPower.Config::itemAction),
            LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyFoodPower.Config::itemCondition),
            AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("food_modifiers").forGetter(ModifyFoodPower.Config::foodModifiers),
            AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("saturation_modifiers").forGetter(ModifyFoodPower.Config::saturationModifiers),
            AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("eat_ticks_modifiers").forGetter(ModifyFoodPower.Config::eatTicksModifiers),
            ItemStackData.CODEC.optionalFieldOf("replace_stack").forGetter(ModifyFoodPower.Config::replaceStack),
            Codec.BOOL.optionalFieldOf("prevent_effects", false).forGetter(ModifyFoodPower.Config::preventEffects),
            Codec.BOOL.optionalFieldOf("always_edible", false).forGetter(ModifyFoodPower.Config::alwaysEdible)
         )
         .apply(i, ModifyFoodPower.Config::new)
   );
   private static final MapCodec<ModifyFoodPower.Config> ALIASED = AliasingMapCodec.wrap(
      INNER,
      Map.of(
         "food_modifier",
         "food_modifiers",
         "saturation_modifier",
         "saturation_modifiers",
         "eat_ticks_modifier",
         "eat_ticks_modifiers",
         "can_always_eat",
         "always_edible"
      )
   );

   @Override
   public MapCodec<ModifyFoodPower.Config> configCodec() {
      return ALIASED;
   }

   public record Config(
      Optional<EntityAction> entityAction,
      Optional<ItemAction> itemAction,
      Optional<ItemCondition> itemCondition,
      Optional<List<AttributeModifier>> foodModifiers,
      Optional<List<AttributeModifier>> saturationModifiers,
      Optional<List<AttributeModifier>> eatTicksModifiers,
      Optional<ItemStackData> replaceStack,
      boolean preventEffects,
      boolean alwaysEdible
   ) {
   }
}
