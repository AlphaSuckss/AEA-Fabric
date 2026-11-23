package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower.Config;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2960;

public final class StoredPowerCondition implements ConditionType<EntityCtx, StoredPowerCondition.Cfg> {
   @Override
   public MapCodec<StoredPowerCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("storage", IdCodecs.ID).forGetter(StoredPowerCondition.Cfg::storage),
               LoggedOptionalField.strict("power", IdCodecs.ID).forGetter(StoredPowerCondition.Cfg::power),
               LoggedOptionalField.of("tags", SingleOrList.of(Codec.STRING), List.of()).forGetter(StoredPowerCondition.Cfg::tags),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(StoredPowerCondition.Cfg::comparison),
               Codec.INT.optionalFieldOf("compare_to", 1).forGetter(StoredPowerCondition.Cfg::compareTo)
            )
            .apply(i, StoredPowerCondition.Cfg::new)
      );
   }

   public boolean test(StoredPowerCondition.Cfg cfg, EntityCtx ctx) {
      PowerContainer container = PowerContainer.of(ctx.raw());
      if (container == null || container.isEmpty()) {
         return cfg.comparison().compare(0.0, cfg.compareTo());
      }

      if (container.powersOfType(ApoliIds.POWER_STORAGE).isEmpty()) {
         return cfg.comparison().compare(0.0, cfg.compareTo());
      }

      int[] matches = new int[1];
      PowerLookup.forEachEntry(
         ctx.raw(),
         ApoliIds.POWER_STORAGE,
         Config.class,
         (storageId, storage) -> {
            if (!cfg.storage().isPresent() || ((class_2960)cfg.storage().get()).equals(storageId)) {
               List<class_2960> powers = PowerStoragePower.live(container, storageId);

               for (int i = 0; i < powers.size(); i++) {
                  class_2960 id = (class_2960)powers.get(i);
                  if ((!cfg.power().isPresent() || ((class_2960)cfg.power().get()).equals(id))
                     && (cfg.tags().isEmpty() || ApoliPowers.hasAnyTag(id, cfg.tags()))) {
                     matches[0]++;
                  }
               }
            }
         }
      );
      return cfg.comparison().compare(matches[0], cfg.compareTo());
   }

   public record Cfg(Optional<class_2960> storage, Optional<class_2960> power, List<String> tags, Comparison comparison, int compareTo) {
   }
}
