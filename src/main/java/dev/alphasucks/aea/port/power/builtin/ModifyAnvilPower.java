package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
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
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.jetbrains.annotations.Nullable;

public final class ModifyAnvilPower extends PowerType<ModifyAnvilPower.Config> {
   @Override
   public MapCodec<ModifyAnvilPower.Config> configCodec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyAnvilPower.Config::itemCondition),
                  AttributeModifier.LIST_OR_SINGLE
                     .optionalFieldOf("repair_material_modifier", List.of())
                     .forGetter(ModifyAnvilPower.Config::repairMaterialModifiers),
                  AttributeModifier.LIST_OR_SINGLE
                     .optionalFieldOf("combine_bonus_modifier", List.of())
                     .forGetter(ModifyAnvilPower.Config::combineBonusModifiers)
               )
               .apply(i, ModifyAnvilPower.Config::new)
         ),
         Map.of("repair_material_modifiers", "repair_material_modifier", "combine_bonus_modifiers", "combine_bonus_modifier")
      );
   }

   public static int repairMaterial(@Nullable class_1657 player, class_1799 stack, int original) {
      List<AttributeModifier> mods = collect(player, stack, true);
      return mods.isEmpty() ? original : Math.max(1, (int)Math.round(AttributeModifierHelper.apply(original, mods, player)));
   }

   public static int combineBonus(@Nullable class_1657 player, class_1799 stack, int original) {
      List<AttributeModifier> mods = collect(player, stack, false);
      return mods.isEmpty() ? original : Math.max(0, (int)Math.round(AttributeModifierHelper.apply(original, mods, player)));
   }

   private static List<AttributeModifier> collect(@Nullable class_1657 player, class_1799 stack, boolean material) {
      if (player != null && !stack.method_7960() && PowerLookup.holdsType(player, ApoliIds.MODIFY_ANVIL)) {
         ItemCtx ctx = new ItemCtx(stack, player.method_37908(), player);
         List<AttributeModifier> mods = new ArrayList(2);
         PowerLookup.forEach(player, ApoliIds.MODIFY_ANVIL, ModifyAnvilPower.Config.class, cfg -> {
            if (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(ctx)) {
               mods.addAll(material ? cfg.repairMaterialModifiers() : cfg.combineBonusModifiers());
            }
         });
         return AttributeModifierHelper.ensureSorted(mods);
      } else {
         return List.of();
      }
   }

   public record Config(Optional<ItemCondition> itemCondition, List<AttributeModifier> repairMaterialModifiers, List<AttributeModifier> combineBonusModifiers) {
   }
}
