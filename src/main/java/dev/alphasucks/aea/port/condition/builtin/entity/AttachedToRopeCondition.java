package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.rope.Rope;
import dev.alphasucks.aea.port.rope.RopeManager;
import java.util.Optional;
import net.minecraft.class_1297;

public final class AttachedToRopeCondition implements ConditionType<EntityCtx, AttachedToRopeCondition.Cfg> {
   @Override
   public MapCodec<AttachedToRopeCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("slot").forGetter(AttachedToRopeCondition.Cfg::slot),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(AttachedToRopeCondition.Cfg::comparison),
               Codec.INT.optionalFieldOf("compare_to", 1).forGetter(AttachedToRopeCondition.Cfg::compareTo)
            )
            .apply(i, AttachedToRopeCondition.Cfg::new)
      );
   }

   public boolean test(AttachedToRopeCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity == null) {
         return false;
      }

      int count = 0;

      for (Rope rope : RopeManager.forEntity(entity.method_5667())) {
         if (!cfg.slot.isPresent() || ((String)cfg.slot.get()).equals(rope.slot)) {
            count++;
         }
      }

      return cfg.comparison.compare(count, cfg.compareTo);
   }

   public record Cfg(Optional<String> slot, Comparison comparison, int compareTo) {
   }
}
