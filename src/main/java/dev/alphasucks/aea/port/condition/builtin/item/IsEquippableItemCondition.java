package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import java.util.Optional;
import net.minecraft.class_5151;

public final class IsEquippableItemCondition implements ConditionType<ItemCtx, IsEquippableItemCondition.Cfg> {
   @Override
   public MapCodec<IsEquippableItemCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(EquipmentSlot.CODEC.optionalFieldOf("equipment_slot").forGetter(IsEquippableItemCondition.Cfg::equipmentSlot))
            .apply(i, IsEquippableItemCondition.Cfg::new)
      );
   }

   public boolean test(IsEquippableItemCondition.Cfg cfg, ItemCtx ctx) {
      class_5151 equipable = class_5151.method_48957(ctx.stack());
      if (equipable == null) {
         return false;
      } else {
         return cfg.equipmentSlot.isEmpty() ? true : equipable.method_7685() == ((EquipmentSlot)cfg.equipmentSlot.get()).vanilla();
      }
   }

   public record Cfg(Optional<EquipmentSlot> equipmentSlot) {
   }
}
