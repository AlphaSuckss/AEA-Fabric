package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.util.GameVersion;
import java.util.List;
import java.util.Optional;

public final class ModLoadedCondition<CTX> implements ConditionType<CTX, ModLoadedCondition.Cfg> {
   @Override
   public MapCodec<ModLoadedCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("mod").forGetter(ModLoadedCondition.Cfg::mod),
               Codec.STRING.listOf().optionalFieldOf("mods", List.of()).forGetter(ModLoadedCondition.Cfg::mods),
               Codec.STRING.optionalFieldOf("version").forGetter(ModLoadedCondition.Cfg::version),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(ModLoadedCondition.Cfg::comparison)
            )
            .apply(i, ModLoadedCondition.Cfg::new)
      );
   }

   public boolean test(ModLoadedCondition.Cfg cfg, CTX ctx) {
      return cfg.result;
   }

   public static final class Cfg {
      private final Optional<String> mod;
      private final List<String> mods;
      private final Optional<String> version;
      private final Comparison comparison;
      private final boolean result;

      public Cfg(Optional<String> mod, List<String> mods, Optional<String> version, Comparison comparison) {
         this.mod = mod;
         this.mods = List.copyOf(mods);
         this.version = version;
         this.comparison = comparison;
         this.result = evaluate(mod, this.mods, version, comparison);
      }

      public Optional<String> mod() {
         return this.mod;
      }

      public List<String> mods() {
         return this.mods;
      }

      public Optional<String> version() {
         return this.version;
      }

      public Comparison comparison() {
         return this.comparison;
      }

      private static boolean evaluate(Optional<String> mod, List<String> mods, Optional<String> version, Comparison comparison) {
         if (mod.isPresent() && !matches((String)mod.get(), version, comparison)) {
            return false;
         }

         for (int i = 0; i < mods.size(); i++) {
            if (!matches((String)mods.get(i), version, comparison)) {
               return false;
            }
         }

         return mod.isPresent() || !mods.isEmpty();
      }

      private static boolean matches(String id, Optional<String> version, Comparison comparison) {
         if (!ModCompat.isLoaded(id)) {
            return false;
         }

         if (version.isEmpty()) {
            return true;
         }

         String actual = ModCompat.versionOf(id);
         return actual == null ? false : comparison.compare(GameVersion.compare(actual, (String)version.get()), 0.0);
      }
   }
}
