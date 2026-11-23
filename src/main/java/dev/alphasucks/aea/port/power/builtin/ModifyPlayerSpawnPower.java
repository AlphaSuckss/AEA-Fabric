package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiomeCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_3195;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.class_3542;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_7924;

public final class ModifyPlayerSpawnPower extends PowerType<ModifyPlayerSpawnPower.Config> {
   private static final int DEFAULT_SEARCH_RADIUS = 32;
   private static final int MAX_SEARCH_RADIUS = 64;
   private static final Codec<Integer> SEARCH_RADIUS = Codec.INT.flatXmap(ModifyPlayerSpawnPower::checkRadius, DataResult::success);

   @Override
   public MapCodec<ModifyPlayerSpawnPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("dimension").forGetter(ModifyPlayerSpawnPower.Config::dimension),
               IdCodecs.ID.optionalFieldOf("biome").forGetter(ModifyPlayerSpawnPower.Config::biome),
               LoggedOptionalField.of("biome_condition", BiomeCondition.CODEC).forGetter(ModifyPlayerSpawnPower.Config::biomeCondition),
               IdCodecs.ID.optionalFieldOf("structure").forGetter(ModifyPlayerSpawnPower.Config::structure),
               IdCodecs.tagKey(class_7924.field_41246).optionalFieldOf("structure_tag").forGetter(ModifyPlayerSpawnPower.Config::structureTag),
               LoggedOptionalField.of("block_condition", BlockCondition.CODEC).forGetter(ModifyPlayerSpawnPower.Config::blockCondition),
               ModifyPlayerSpawnPower.SpawnStrategy.CODEC
                  .optionalFieldOf("spawn_strategy", ModifyPlayerSpawnPower.SpawnStrategy.DEFAULT)
                  .forGetter(ModifyPlayerSpawnPower.Config::spawnStrategy),
               Codec.FLOAT.optionalFieldOf("dimension_distance_multiplier").forGetter(ModifyPlayerSpawnPower.Config::dimensionDistanceMultiplier),
               SEARCH_RADIUS.optionalFieldOf("search_radius", 32).forGetter(ModifyPlayerSpawnPower.Config::searchRadius)
            )
            .apply(i, ModifyPlayerSpawnPower.Config::new)
      );
   }

   private static DataResult<Integer> checkRadius(int radius) {
      if (radius >= 1 && radius <= 64) {
         return DataResult.success(radius);
      }

      int clamped = class_3532.method_15340(radius, 1, 64);
      return DataResult.error(() -> "search_radius " + radius + " is outside 1 to 64, so " + clamped + " is used", clamped);
   }

   public void onRemoved(class_2960 powerId, ModifyPlayerSpawnPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder.owner() instanceof class_3222 player) {
         if (player.method_26280() != null && player.method_26282()) {
            if (class_5321.method_29179(class_7924.field_41223, cfg.dimension()).equals(player.method_26281())) {
               if (!ModifyPlayerSpawnHandler.anyOtherActive(holder, powerId)) {
                  player.method_26284(class_1937.field_25179, null, 0.0F, false, false);
               }
            }
         }
      }
   }

   public record Config(
      class_2960 dimension,
      Optional<class_2960> biome,
      Optional<BiomeCondition> biomeCondition,
      Optional<class_2960> structure,
      Optional<class_6862<class_3195>> structureTag,
      Optional<BlockCondition> blockCondition,
      ModifyPlayerSpawnPower.SpawnStrategy spawnStrategy,
      Optional<Float> dimensionDistanceMultiplier,
      int searchRadius
   ) {
   }

   public enum SpawnStrategy implements class_3542 {
      DEFAULT("default"),
      CENTER("center");

      public static final Codec<ModifyPlayerSpawnPower.SpawnStrategy> CODEC = class_3542.method_28140(ModifyPlayerSpawnPower.SpawnStrategy::values);
      private final String name;

      SpawnStrategy(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }

      public class_2338 apply(class_2338 worldSpawn, int center, float multiplier) {
         if (this == CENTER) {
            return new class_2338(0, center, 0);
         } else {
            return multiplier == 0.0F
               ? worldSpawn
               : new class_2338((int)(worldSpawn.method_10263() * multiplier), worldSpawn.method_10264(), (int)(worldSpawn.method_10260() * multiplier));
         }
      }
   }
}
