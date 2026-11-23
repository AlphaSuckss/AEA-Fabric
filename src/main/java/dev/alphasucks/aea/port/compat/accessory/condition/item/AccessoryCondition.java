package dev.alphasucks.aea.port.compat.accessory.condition.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;

public final class AccessoryCondition implements ConditionType<ItemCtx, AccessoryCondition.Cfg> {
   @Override
   public MapCodec<AccessoryCondition.Cfg> codec() {
      return MapCodec.unit(new AccessoryCondition.Cfg());
   }

   public boolean test(AccessoryCondition.Cfg cfg, ItemCtx ctx) {
      return Accessories.isAccessory(ctx.stack(), ctx.level());
   }

   public record Cfg() {
   }
}
