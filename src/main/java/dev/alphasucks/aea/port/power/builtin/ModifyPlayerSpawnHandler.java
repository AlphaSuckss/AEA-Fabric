package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Pair;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.condition.BiomeCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.class_124;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3195;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.class_5275;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_6885;
import net.minecraft.class_7924;
import net.minecraft.class_2338.class_2339;
import org.jetbrains.annotations.Nullable;

public final class ModifyPlayerSpawnHandler {
   private static final int BIOME_SEARCH_RADIUS = 6400;
   private static final int BIOME_HORIZONTAL_STEP = 32;
   private static final int BIOME_VERTICAL_STEP = 64;
   private static final int STRUCTURE_SEARCH_RADIUS = 100;
   private static final long FAILED_SCAN_RETRY_TICKS = 6000L;
   private static final Map<ModifyPlayerSpawnPower.Config, ModifyPlayerSpawnHandler.Search> SEARCHES = new IdentityHashMap();

   private ModifyPlayerSpawnHandler() {
   }

   public static void clearCache() {
      SEARCHES.clear();
   }

   @Nullable
   public static ModifyPlayerSpawnPower.Config firstActive(class_3222 player) {
      return PowerLookup.firstActive(player, ApoliIds.MODIFY_PLAYER_SPAWN, ModifyPlayerSpawnPower.Config.class);
   }

   public static boolean anyOtherActive(PowerContainer holder, class_2960 ignored) {
      List<class_2960> powers = holder.powersOfType(ApoliIds.MODIFY_PLAYER_SPAWN);

      for (int i = 0; i < powers.size(); i++) {
         class_2960 id = (class_2960)powers.get(i);
         if (!id.equals(ignored) && !holder.isSuppressed(id)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static class_2338 respawnPosition(class_3222 player, @Nullable class_2338 original) {
      ModifyPlayerSpawnPower.Config cfg = firstActive(player);
      if (cfg == null) {
         return original;
      }

      if (original != null && !isObstructed(player, original)) {
         return original;
      }

      ModifyPlayerSpawnHandler.Spawn spawn = resolve(player, cfg);
      if (spawn == null) {
         return original;
      }

      class_2338 pos = spawn.blockPos();
      player.method_26284(spawn.level().method_27983(), pos, player.method_36454(), true, false);
      return pos;
   }

   private static boolean isObstructed(class_3222 player, class_2338 original) {
      class_3218 level = player.field_13995.method_3847(player.method_26281());
      return level == null ? true : class_1657.method_26091(level, original, player.method_30631(), player.method_26282(), true).isEmpty();
   }

   public static void teleportToModifiedSpawn(class_3222 player) {
      ModifyPlayerSpawnPower.Config cfg = firstActive(player);
      if (cfg != null) {
         if (player.method_26280() == null || !dimension(cfg).equals(player.method_26281())) {
            ModifyPlayerSpawnHandler.Spawn spawn = resolve(player, cfg);
            if (spawn != null) {
               class_243 at = spawn.position();
               player.method_26284(spawn.level().method_27983(), spawn.blockPos(), player.method_36454(), true, false);
               player.method_14251(spawn.level(), at.field_1352, at.field_1351, at.field_1350, player.method_36454(), player.method_36455());
            }
         }
      }
   }

   private static class_5321<class_1937> dimension(ModifyPlayerSpawnPower.Config cfg) {
      return class_5321.method_29179(class_7924.field_41223, cfg.dimension());
   }

   @Nullable
   private static ModifyPlayerSpawnHandler.Spawn resolve(class_3222 player, ModifyPlayerSpawnPower.Config cfg) {
      class_3218 target = player.field_13995.method_3847(dimension(cfg));
      if (target == null) {
         Apoli.LOGGER.warn("[Apoli] apoli:modify_player_spawn refers to unknown dimension '{}'.", cfg.dimension());
         return null;
      } else {
         int center = target.method_8597().comp_653() / 2;
         class_2338 origin = cfg.spawnStrategy().apply(player.method_51469().method_43126(), center, (Float)cfg.dimensionDistanceMultiplier().orElse(1.0F));
         ModifyPlayerSpawnHandler.Search search = search(target, cfg, origin);
         if (search.start == null) {
            complain(player, cfg, search.missing);
            return null;
         } else {
            long now = target.method_8510();
            if (now < search.retryScanAt) {
               return null;
            } else {
               class_243 spawn = findValidSpawn(target, search.start, center, cfg);
               if (spawn == null) {
                  search.retryScanAt = now + 6000L;
                  Apoli.LOGGER
                     .warn(
                        "[Apoli] apoli:modify_player_spawn could not find a safe spot for {} within {} blocks of {} in '{}'.",
                        new Object[]{player.method_5477().getString(), cfg.searchRadius(), search.start.method_23854(), cfg.dimension()}
                     );
                  return null;
               } else {
                  return new ModifyPlayerSpawnHandler.Spawn(target, spawn);
               }
            }
         }
      }
   }

   private static ModifyPlayerSpawnHandler.Search search(class_3218 target, ModifyPlayerSpawnPower.Config cfg, class_2338 origin) {
      ModifyPlayerSpawnHandler.Search cached = (ModifyPlayerSpawnHandler.Search)SEARCHES.get(cfg);
      if (cached != null && cached.level == target && cached.origin.equals(origin)) {
         return cached;
      }

      ModifyPlayerSpawnHandler.Search fresh = locate(target, cfg, origin);
      SEARCHES.put(cfg, fresh);
      return fresh;
   }

   private static ModifyPlayerSpawnHandler.Search locate(class_3218 target, ModifyPlayerSpawnPower.Config cfg, class_2338 origin) {
      class_2338 start = origin;
      if (cfg.biome().isPresent() || cfg.biomeCondition().isPresent()) {
         Pair<class_2338, class_6880<class_1959>> biome = target.method_42108(biomes(target, cfg, origin), origin, 6400, 32, 64);
         if (biome == null) {
            return new ModifyPlayerSpawnHandler.Search(target, origin, null, biomeName(cfg));
         }

         start = (class_2338)biome.getFirst();
      }

      if (cfg.structure().isPresent() || cfg.structureTag().isPresent()) {
         Pair<class_2338, class_6880<class_3195>> structure = target.method_14178()
            .method_12129()
            .method_12103(target, structures(target, cfg), start, 100, false);
         if (structure == null) {
            return new ModifyPlayerSpawnHandler.Search(target, origin, null, structureName(cfg));
         }

         start = (class_2338)structure.getFirst();
      }

      return new ModifyPlayerSpawnHandler.Search(target, origin, start, null);
   }

   private static Predicate<class_6880<class_1959>> biomes(class_3218 target, ModifyPlayerSpawnPower.Config cfg, class_2338 origin) {
      class_5321<class_1959> key = (class_5321<class_1959>)cfg.biome().map(id -> class_5321.method_29179(class_7924.field_41236, id)).orElse(null);
      BiomeCondition condition = (BiomeCondition)cfg.biomeCondition().orElse(null);
      return holder -> key != null && holder.method_40225(key) || condition != null && condition.test(new BiomeCtx(holder, origin, target));
   }

   private static class_6885<class_3195> structures(class_3218 target, ModifyPlayerSpawnPower.Config cfg) {
      class_2378<class_3195> registry = target.method_30349().method_30530(class_7924.field_41246);
      List<class_6880<class_3195>> holders = new ArrayList();
      cfg.structure().flatMap(id -> registry.method_40264(class_5321.method_29179(class_7924.field_41246, id))).ifPresent(holders::add);
      cfg.structureTag().flatMap(registry::method_40266).ifPresent(tag -> {
         for (class_6880<class_3195> holder : tag) {
            if (!holders.contains(holder)) {
               holders.add(holder);
            }
         }
      });
      return class_6885.<class_3195>method_40242(holders);
   }

   private static String biomeName(ModifyPlayerSpawnPower.Config cfg) {
      return either(
         (String)cfg.biome().map(id -> "biome \"" + id + "\"").orElse(null), cfg.biomeCondition().isPresent() ? "a biome matching its biome_condition" : null
      );
   }

   private static String structureName(ModifyPlayerSpawnPower.Config cfg) {
      return either(
         (String)cfg.structure().map(id -> "structure \"" + id + "\"").orElse(null),
         (String)cfg.structureTag().map(tag -> "a structure in \"#" + tag.comp_327() + "\"").orElse(null)
      );
   }

   private static String either(@Nullable String first, @Nullable String second) {
      if (first == null) {
         return second;
      } else {
         return second == null ? first : first + " or " + second;
      }
   }

   private static void complain(class_3222 player, ModifyPlayerSpawnPower.Config cfg, String what) {
      Apoli.LOGGER
         .warn(
            "[Apoli] apoli:modify_player_spawn could not set {}'s spawn at {}: none found in '{}'.",
            new Object[]{player.method_5477().getString(), what, cfg.dimension()}
         );
      player.method_43496(
         class_2561.method_43470("Couldn't set your spawn point at %s — none could be found in \"%s\".".formatted(new Object[]{what, cfg.dimension()}))
            .method_27695(class_124.field_1056, class_124.field_1080)
      );
   }

   @Nullable
   private static class_243 findValidSpawn(class_3218 level, class_2338 start, int preferredY, ModifyPlayerSpawnPower.Config cfg) {
      int minY = level.method_31607() + 1;
      int maxY = Math.min(level.method_31600(), level.method_31607() + level.method_8597().comp_653()) - 2;
      int startY = class_3532.method_15340(preferredY, minY, maxY);
      BlockCondition condition = (BlockCondition)cfg.blockCondition().orElse(null);
      class_2339 cursor = new class_2339();

      for (class_2338 column : class_2338.method_30512(start, cfg.searchRadius(), class_2350.field_11034, class_2350.field_11035)) {
         class_243 found = findInColumn(level, cursor, column.method_10263(), column.method_10260(), startY, minY, maxY, condition);
         if (found != null) {
            return found;
         }
      }

      return null;
   }

   @Nullable
   private static class_243 findInColumn(class_3218 level, class_2339 cursor, int x, int z, int startY, int minY, int maxY, @Nullable BlockCondition condition) {
      for (int offset = 0; offset <= maxY - minY; offset++) {
         int down = startY - offset;
         int up = startY + offset;
         if (down < minY && up > maxY) {
            break;
         }

         if (down >= minY) {
            class_243 found = safeSpot(level, cursor.method_10103(x, down, z), condition);
            if (found != null) {
               return found;
            }
         }

         if (offset > 0 && up <= maxY) {
            class_243 found = safeSpot(level, cursor.method_10103(x, up, z), condition);
            if (found != null) {
               return found;
            }
         }
      }

      return null;
   }

   @Nullable
   private static class_243 safeSpot(class_3218 level, class_2339 cursor, @Nullable BlockCondition condition) {
      class_243 found = class_5275.method_30769(class_1299.field_6097, level, cursor, true);
      if (found != null && condition != null) {
         class_2338 pos = cursor.method_10062();
         return condition.test(new BlockCtx(pos, level.method_8320(pos), level)) ? found : null;
      } else {
         return found;
      }
   }

   private static final class Search {
      private final class_3218 level;
      private final class_2338 origin;
      @Nullable
      private final class_2338 start;
      @Nullable
      private final String missing;
      private long retryScanAt;

      private Search(class_3218 level, class_2338 origin, @Nullable class_2338 start, @Nullable String missing) {
         this.level = level;
         this.origin = origin;
         this.start = start;
         this.missing = missing;
      }
   }

   private record Spawn(class_3218 level, class_243 position) {
      class_2338 blockPos() {
         return class_2338.method_49638(this.position);
      }
   }
}
