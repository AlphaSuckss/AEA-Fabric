package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class ModifyEnchantmentLevelPower extends PowerType<ModifyEnchantmentLevelPower.Config> {
   @Override
   public MapCodec<ModifyEnchantmentLevelPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("enchantment").forGetter(ModifyEnchantmentLevelPower.Config::enchantment),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyEnchantmentLevelPower.Config::itemCondition),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyEnchantmentLevelPower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyEnchantmentLevelPower.Config::modifiers)
            )
            .apply(i, ModifyEnchantmentLevelPower.Config::new)
      );
   }

   public record Config(
      class_2960 enchantment,
      Optional<ItemCondition> itemCondition,
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      List<AttributeModifier> allModifiers
   ) {
      public Config(
         class_2960 enchantment, Optional<ItemCondition> itemCondition, Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers
      ) {
         this(enchantment, itemCondition, modifier, modifiers, AttributeModifierHelper.flatten(modifier, modifiers));
      }

      public boolean appliesTo(class_1799 stack, class_1309 holder) {
         return this.itemCondition.isEmpty() || ((ItemCondition)this.itemCondition.get()).test(new ItemCtx(stack, holder.method_37908(), holder));
      }
   }
}
