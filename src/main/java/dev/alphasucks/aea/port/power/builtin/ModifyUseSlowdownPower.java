package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import org.jetbrains.annotations.Nullable;

public final class ModifyUseSlowdownPower extends PowerType<ModifyUseSlowdownPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_use_slowdown");

   @Override
   public MapCodec<ModifyUseSlowdownPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyUseSlowdownPower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyUseSlowdownPower.Config::modifiers),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyUseSlowdownPower.Config::itemCondition)
            )
            .apply(i, ModifyUseSlowdownPower.Config::new)
      );
   }

   public static float compute(@Nullable class_1309 entity, float base) {
      if (entity == null) {
         return base;
      }

      List<ModifyUseSlowdownPower.Config> configs = PowerLookup.active(entity, CANONICAL, ModifyUseSlowdownPower.Config.class);
      if (configs.isEmpty()) {
         return base;
      }

      class_1799 inUse = entity.method_6030();
      ItemCtx itemCtx = null;
      double value = base;
      int i = 0;

      for (int n = configs.size(); i < n; i++) {
         ModifyUseSlowdownPower.Config cfg = (ModifyUseSlowdownPower.Config)configs.get(i);
         if (cfg.itemCondition().isPresent()) {
            if (itemCtx == null) {
               itemCtx = new ItemCtx(inUse, entity.method_37908(), entity);
            }

            if (!((ItemCondition)cfg.itemCondition().get()).test(itemCtx)) {
               continue;
            }
         }

         List<AttributeModifier> mods = cfg.flattened();
         if (!mods.isEmpty()) {
            value = AttributeModifierHelper.apply(value, mods, entity);
         }
      }

      return (float)class_3532.method_15350(value, 0.0, 1.0);
   }

   public record Config(Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers, Optional<ItemCondition> itemCondition) {
      public List<AttributeModifier> flattened() {
         return AttributeModifierHelper.flatten(this.modifier, this.modifiers);
      }
   }
}
