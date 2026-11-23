package dev.alphasucks.aea.port.compat.accessory.condition.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import java.util.List;
import net.minecraft.class_1309;

public final class AccessoryEquippedCountCondition implements ConditionType<EntityCtx, AccessoryEquippedCountCondition.Cfg> {
   @Override
   public MapCodec<AccessoryEquippedCountCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Comparison.CODEC.fieldOf("comparison").forGetter(AccessoryEquippedCountCondition.Cfg::comparison),
               Codec.INT.fieldOf("compare_to").forGetter(AccessoryEquippedCountCondition.Cfg::compareTo),
               AccessorySlot.LIST.optionalFieldOf("slots", List.of()).forGetter(AccessoryEquippedCountCondition.Cfg::slots)
            )
            .apply(i, AccessoryEquippedCountCondition.Cfg::new)
      );
   }

   public boolean test(AccessoryEquippedCountCondition.Cfg cfg, EntityCtx ctx) {
      class_1309 e = ctx.living();
      if (e == null) {
         return false;
      }

      int count = Accessories.equipped(e, cfg.slots()).size();
      return cfg.comparison().compare(count, cfg.compareTo());
   }

   public record Cfg(Comparison comparison, int compareTo, List<AccessorySlot> slots) {
   }
}
