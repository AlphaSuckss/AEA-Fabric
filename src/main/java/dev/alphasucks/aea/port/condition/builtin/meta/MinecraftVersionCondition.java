package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.util.GameVersion;
import java.util.Optional;
import java.util.OptionalInt;

public final class MinecraftVersionCondition<CTX> implements ConditionType<CTX, MinecraftVersionCondition.Cfg> {
   @Override
   public MapCodec<MinecraftVersionCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("version").forGetter(MinecraftVersionCondition.Cfg::version),
               Codec.INT.optionalFieldOf("data_version").forGetter(MinecraftVersionCondition.Cfg::dataVersion),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(MinecraftVersionCondition.Cfg::comparison)
            )
            .apply(i, MinecraftVersionCondition.Cfg::new)
      );
   }

   public boolean test(MinecraftVersionCondition.Cfg cfg, CTX ctx) {
      return cfg.result;
   }

   public static final class Cfg {
      private final Optional<String> version;
      private final OptionalInt dataVersion;
      private final Comparison comparison;
      private final boolean result;

      public Cfg(Optional<String> version, Optional<Integer> dataVersion, Comparison comparison) {
         this.version = version;
         this.dataVersion = dataVersion.isPresent() ? OptionalInt.of((Integer)dataVersion.get()) : OptionalInt.empty();
         this.comparison = comparison;
         this.result = evaluate(version, this.dataVersion, comparison);
      }

      public Optional<String> version() {
         return this.version;
      }

      public Optional<Integer> dataVersion() {
         return this.dataVersion.isPresent() ? Optional.of(this.dataVersion.getAsInt()) : Optional.empty();
      }

      public Comparison comparison() {
         return this.comparison;
      }

      private static boolean evaluate(Optional<String> version, OptionalInt dataVersion, Comparison comparison) {
         if (version.isPresent() && !comparison.compare(GameVersion.compare(GameVersion.name(), (String)version.get()), 0.0)) {
            return false;
         } else {
            return dataVersion.isPresent() && !comparison.compare(GameVersion.dataVersion(), dataVersion.getAsInt())
               ? false
               : version.isPresent() || dataVersion.isPresent();
         }
      }
   }
}
