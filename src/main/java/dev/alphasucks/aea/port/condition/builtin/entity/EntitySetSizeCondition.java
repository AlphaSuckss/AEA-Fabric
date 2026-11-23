package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import net.minecraft.class_2960;

public final class EntitySetSizeCondition implements ConditionType<EntityCtx, EntitySetSizeCondition.Cfg> {
   @Override
   public MapCodec<EntitySetSizeCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("set").forGetter(EntitySetSizeCondition.Cfg::set),
               Comparison.CODEC.fieldOf("comparison").forGetter(EntitySetSizeCondition.Cfg::comparison),
               Codec.INT.fieldOf("compare_to").forGetter(EntitySetSizeCondition.Cfg::compareTo)
            )
            .apply(i, EntitySetSizeCondition.Cfg::new)
      );
   }

   public boolean test(EntitySetSizeCondition.Cfg cfg, EntityCtx ctx) {
      PowerContainer container = PowerContainer.of(ctx.entity());
      if (container == null || !container.hasPower(cfg.set)) {
         return false;
      }

      if (EntitySetPower.resolveCfg(cfg.set) == null) {
         return false;
      }

      int size = EntitySetPower.size(ctx.entity(), cfg.set);
      return cfg.comparison.compare(size, cfg.compareTo);
   }

   public record Cfg(class_2960 set, Comparison comparison, int compareTo) {
   }
}
