package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Stat;
import net.minecraft.class_3222;
import net.minecraft.class_3445;

public final class StatCondition implements ConditionType<EntityCtx, StatCondition.Cfg> {
   @Override
   public MapCodec<StatCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Stat.CODEC.fieldOf("stat").forGetter(StatCondition.Cfg::stat),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(StatCondition.Cfg::comparison),
               Codec.INT.fieldOf("compare_to").forGetter(StatCondition.Cfg::compareTo)
            )
            .apply(i, StatCondition.Cfg::new)
      );
   }

   public boolean test(StatCondition.Cfg cfg, EntityCtx ctx) {
      if (ctx.raw() instanceof class_3222 player) {
         class_3445<?> stat = cfg.stat.resolve();
         return stat == null ? false : cfg.comparison.compare(player.method_14248().method_15025(stat), cfg.compareTo);
      } else {
         return false;
      }
   }

   public record Cfg(Stat stat, Comparison comparison, int compareTo) {
   }
}
