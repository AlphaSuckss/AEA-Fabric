package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.jetbrains.annotations.Nullable;

public final class ModifyEnchantabilityPower extends PowerType<ModifyEnchantabilityPower.Config> {
   @Nullable
   private static class_1657 enchanter;

   @Override
   public MapCodec<ModifyEnchantabilityPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyEnchantabilityPower.Config::itemCondition),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyEnchantabilityPower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyEnchantabilityPower.Config::modifiers)
            )
            .apply(i, (condition, modifier, modifiers) -> new ModifyEnchantabilityPower.Config(condition, modifier, modifiers))
      );
   }

   public static void begin(@Nullable class_1657 player) {
      enchanter = player;
   }

   public static void end() {
      enchanter = null;
   }

   public static int modify(class_1799 stack, int enchantability) {
      class_1657 player = enchanter;
      if (player != null && enchantability > 0 && PowerLookup.holdsType(player, ApoliIds.MODIFY_ENCHANTABILITY)) {
         ItemCtx ctx = new ItemCtx(stack, player.method_37908(), player);
         List<AttributeModifier> mods = new ArrayList(2);
         PowerLookup.forEach(player, ApoliIds.MODIFY_ENCHANTABILITY, ModifyEnchantabilityPower.Config.class, cfg -> {
            if (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(ctx)) {
               mods.addAll(cfg.combined());
            }
         });
         return mods.isEmpty()
            ? enchantability
            : (int)Math.round(AttributeModifierHelper.apply(enchantability, AttributeModifierHelper.ensureSorted(mods), player));
      } else {
         return enchantability;
      }
   }

   public record Config(
      Optional<ItemCondition> itemCondition,
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      List<AttributeModifier> combined
   ) {
      public Config(Optional<ItemCondition> itemCondition, Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers) {
         this(itemCondition, modifier, modifiers, AttributeModifierHelper.flatten(modifier, modifiers));
      }
   }
}
