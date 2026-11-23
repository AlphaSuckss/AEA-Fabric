package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.FoodComponent;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1839;
import net.minecraft.class_2960;
import net.minecraft.class_3542;

public final class EdibleItemPower extends PowerType<EdibleItemPower.Config> {
   private static final class_2960 DEFAULT_EAT_SOUND = new class_2960("minecraft", "entity.generic.eat");
   private static final MapCodec<EdibleItemPower.Config> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(EdibleItemPower.Config::entityAction),
            LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(EdibleItemPower.Config::itemAction),
            LoggedOptionalField.of("result_item_action", ItemAction.CODEC).forGetter(EdibleItemPower.Config::resultItemAction),
            LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(EdibleItemPower.Config::itemCondition),
            FoodComponent.CODEC.fieldOf("food_component").forGetter(EdibleItemPower.Config::foodComponent),
            ItemStackData.CODEC.optionalFieldOf("result_stack").forGetter(EdibleItemPower.Config::resultStack),
            EdibleItemPower.ConsumeAnimation.CODEC
               .optionalFieldOf("consume_animation", EdibleItemPower.ConsumeAnimation.EAT)
               .forGetter(EdibleItemPower.Config::consumeAnimation),
            IdCodecs.ID.optionalFieldOf("consume_sound", DEFAULT_EAT_SOUND).forGetter(EdibleItemPower.Config::consumeSound),
            AttributeModifier.CODEC.optionalFieldOf("consuming_time_modifier").forGetter(EdibleItemPower.Config::consumingTimeModifier),
            Codec.list(AttributeModifier.CODEC).optionalFieldOf("consuming_time_modifiers").forGetter(EdibleItemPower.Config::consumingTimeModifiers)
         )
         .apply(i, EdibleItemPower.Config::new)
   );
   private static final MapCodec<EdibleItemPower.Config> ALIASED = AliasingMapCodec.wrap(
      INNER, Map.of("use_action", "consume_animation", "sound", "consume_sound", "return_stack", "result_stack")
   );

   @Override
   public MapCodec<EdibleItemPower.Config> configCodec() {
      return ALIASED;
   }

   public record Config(
      Optional<EntityAction> entityAction,
      Optional<ItemAction> itemAction,
      Optional<ItemAction> resultItemAction,
      Optional<ItemCondition> itemCondition,
      FoodComponent foodComponent,
      Optional<ItemStackData> resultStack,
      EdibleItemPower.ConsumeAnimation consumeAnimation,
      class_2960 consumeSound,
      Optional<AttributeModifier> consumingTimeModifier,
      Optional<List<AttributeModifier>> consumingTimeModifiers
   ) {
   }

   public enum ConsumeAnimation implements class_3542 {
      EAT("eat", class_1839.field_8950),
      DRINK("drink", class_1839.field_8946);

      public static final Codec<EdibleItemPower.ConsumeAnimation> CODEC = class_3542.method_28140(EdibleItemPower.ConsumeAnimation::values);
      private final String name;
      private final class_1839 vanilla;

      ConsumeAnimation(String n, class_1839 a) {
         this.name = n;
         this.vanilla = a;
      }

      @Override
      public String method_15434() {
         return this.name;
      }

      public class_1839 vanilla() {
         return this.vanilla;
      }
   }
}
