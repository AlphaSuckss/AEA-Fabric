package dev.alphasucks.aea.port.condition.builtin.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class SourceDamageCondition implements ConditionType<DamageCtx, SourceDamageCondition.Cfg> {
   @Override
   public MapCodec<SourceDamageCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("power").forGetter(SourceDamageCondition.Cfg::power),
               IdCodecs.ID.optionalFieldOf("source").forGetter(SourceDamageCondition.Cfg::source)
            )
            .apply(i, SourceDamageCondition.Cfg::new)
      );
   }

   public boolean test(SourceDamageCondition.Cfg cfg, DamageCtx ctx) {
      class_2960 causedBy = PowerCause.powerId();
      if (causedBy == null) {
         return false;
      }

      if (cfg.power.isPresent() && !((class_2960)cfg.power.get()).equals(causedBy)) {
         return false;
      }

      if (cfg.source.isEmpty()) {
         return true;
      }

      class_1297 holder = PowerCause.holder();
      if (holder == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(holder);
      return container != null && container.sourcesOf(causedBy).contains(cfg.source.get());
   }

   public record Cfg(Optional<class_2960> power, Optional<class_2960> source) {
      public Cfg {
         PowerCause.armAttribution();
      }
   }
}
