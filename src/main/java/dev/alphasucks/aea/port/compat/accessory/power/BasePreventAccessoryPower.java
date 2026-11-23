package dev.alphasucks.aea.port.compat.accessory.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public abstract class BasePreventAccessoryPower extends PowerType<BasePreventAccessoryPower.Config> {
   protected static MapCodec<BasePreventAccessoryPower.Config> baseCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AccessorySlot.LIST.optionalFieldOf("slots", List.of()).forGetter(BasePreventAccessoryPower.Config::slots),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(BasePreventAccessoryPower.Config::itemCondition),
               Codec.BOOL.optionalFieldOf("allow_in_creative", true).forGetter(BasePreventAccessoryPower.Config::allowInCreative)
            )
            .apply(i, BasePreventAccessoryPower.Config::new)
      );
   }

   public static boolean isPrevented(class_1309 entity, class_2960 canonical, AccessorySlotRef ref, class_1799 stack) {
      boolean creative = entity instanceof class_1657 p && p.method_31549().field_7477;
      ItemCtx itemCtx = new ItemCtx(stack, entity.method_37908(), entity);
      boolean[] prevented = new boolean[]{false};
      PowerLookup.forEach(entity, canonical, BasePreventAccessoryPower.Config.class, cfg -> {
         if (!prevented[0]) {
            if (!creative || !cfg.allowInCreative()) {
               if (AccessorySlot.matchesAny(cfg.slots(), ref)) {
                  if (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(itemCtx)) {
                     prevented[0] = true;
                  }
               }
            }
         }
      });
      return prevented[0];
   }

   public record Config(List<AccessorySlot> slots, Optional<ItemCondition> itemCondition, boolean allowInCreative) {
   }
}
