package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeUsage;
import dev.alphasucks.aea.port.power.PowerTypeUsage.Handle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1536;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_4140;
import net.minecraft.class_47;
import net.minecraft.class_4836;
import org.jetbrains.annotations.Nullable;

public final class ReplaceLootTablePower extends PowerType<ReplaceLootTablePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("replace_loot_table");
   public static final class_2960 REPLACED_TABLE = Apoli.id("replaced_loot_table");
   public static final Handle HELD = PowerTypeUsage.handle(CANONICAL);
   private static final Comparator<ReplaceLootTablePower.Config> BY_PRIORITY = Comparator.comparingInt(ReplaceLootTablePower.Config::priority);
   private static final ThreadLocal<List<ReplaceLootTablePower.Config>> CONFIGS = ThreadLocal.withInitial(() -> new ArrayList(2));
   private static final Set<class_2960> WARNED = ConcurrentHashMap.newKeySet();
   private static final Codec<String> PATTERN = Codec.STRING.comapFlatMap(ReplaceLootTablePower::validPattern, pattern -> pattern);

   @Override
   public MapCodec<ReplaceLootTablePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.unboundedMap(PATTERN, IdCodecs.ID).fieldOf("replace").forGetter(ReplaceLootTablePower.Config::replace),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ReplaceLootTablePower.Config::bientityCondition),
               LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(ReplaceLootTablePower.Config::blockCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ReplaceLootTablePower.Config::itemCondition),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(ReplaceLootTablePower.Config::priority)
            )
            .apply(i, ReplaceLootTablePower.Config::new)
      );
   }

   private static DataResult<String> validPattern(String regex) {
      try {
         Pattern.compile(regex);
         return DataResult.success(regex);
      } catch (PatternSyntaxException e) {
         return DataResult.error(() -> "Invalid regular expression \"" + regex + "\": " + e.getDescription());
      }
   }

   public static void replacementsFor(class_47 context, String tableId, List<class_2960> out) {
      class_1297 target = context.method_296(class_181.field_1226);
      class_1297 holder = holderOf(context, target);
      if (holder != null) {
         List<ReplaceLootTablePower.Config> configs = (List<ReplaceLootTablePower.Config>)CONFIGS.get();
         configs.clear();
         PowerLookup.collect(holder, CANONICAL, ReplaceLootTablePower.Config.class, configs);
         if (!configs.isEmpty()) {
            if (configs.size() > 1) {
               configs.sort(BY_PRIORITY);
            }

            class_3218 level = context.method_299();
            class_1799 tool = context.method_296(class_181.field_1229);
            class_243 origin = context.method_296(class_181.field_24424);
            class_2338 pos = origin == null ? null : class_2338.method_49638(origin);
            class_2680 state = context.method_296(class_181.field_1224);

            for (int i = 0; i < configs.size(); i++) {
               ReplaceLootTablePower.Config cfg = (ReplaceLootTablePower.Config)configs.get(i);
               class_2960 table = cfg.replacementFor(tableId);
               if (table != null && applies(cfg, holder, target, level, tool == null ? class_1799.field_8037 : tool, pos, state)) {
                  out.add(table);
               }
            }

            configs.clear();
         }
      }
   }

   public static void warnMissing(class_2960 table) {
      if (WARNED.add(table)) {
         Apoli.LOGGER.warn("[Apoli] apoli:replace_loot_table points at loot table \"{}\", which does not exist; it is skipped.", table);
      }
   }

   @Nullable
   private static class_1297 holderOf(class_47 context, @Nullable class_1297 target) {
      if (target instanceof class_1536 hook) {
         return hook.method_24921();
      } else {
         class_1282 source = context.method_296(class_181.field_1231);
         if (source != null) {
            return source.method_5529() != null ? source.method_5529() : target;
         } else if (target instanceof class_4836 piglin) {
            Optional<class_1657> player = piglin.method_18868().method_18904(class_4140.field_18444);
            return (class_1297)(player.isPresent() ? (class_1297)player.get() : piglin);
         } else {
            return target;
         }
      }
   }

   private static boolean applies(
      ReplaceLootTablePower.Config cfg,
      class_1297 holder,
      @Nullable class_1297 target,
      class_3218 level,
      class_1799 tool,
      @Nullable class_2338 pos,
      @Nullable class_2680 state
   ) {
      if (cfg.itemCondition().isPresent()
         && !((ItemCondition)cfg.itemCondition().get()).test(new ItemCtx(tool, level, holder instanceof class_1309 living ? living : null))) {
         return false;
      }

      if (cfg.blockCondition().isPresent()) {
         if (pos == null) {
            return false;
         }

         class_2680 blockState = state != null ? state : level.method_8320(pos);
         if (!((BlockCondition)cfg.blockCondition().get()).test(new BlockCtx(pos, blockState, level, holder))) {
            return false;
         }
      }

      return cfg.bientityCondition().isEmpty() || ((BiEntityCondition)cfg.bientityCondition().get()).test(BiEntityCtx.of(holder, target, level));
   }

   public record Config(
      Map<String, class_2960> replace,
      Optional<BiEntityCondition> bientityCondition,
      Optional<BlockCondition> blockCondition,
      Optional<ItemCondition> itemCondition,
      int priority,
      List<ReplaceLootTablePower.Replacement> replacements
   ) {
      public Config(
         Map<String, class_2960> replace,
         Optional<BiEntityCondition> bientityCondition,
         Optional<BlockCondition> blockCondition,
         Optional<ItemCondition> itemCondition,
         int priority
      ) {
         this(replace, bientityCondition, blockCondition, itemCondition, priority, compile(replace));
      }

      private static List<ReplaceLootTablePower.Replacement> compile(Map<String, class_2960> raw) {
         List<ReplaceLootTablePower.Replacement> out = new ArrayList(raw.size());
         raw.forEach((regex, table) -> out.add(new ReplaceLootTablePower.Replacement(Pattern.compile(regex), regex, table)));
         return List.copyOf(out);
      }

      @Nullable
      public class_2960 replacementFor(String tableId) {
         for (int i = 0; i < this.replacements.size(); i++) {
            ReplaceLootTablePower.Replacement replacement = (ReplaceLootTablePower.Replacement)this.replacements.get(i);
            if (replacement.matches(tableId)) {
               return replacement.table();
            }
         }

         return null;
      }
   }

   public record Replacement(Pattern pattern, String literal, class_2960 table) {
      public boolean matches(String id) {
         return this.literal.equals(id) || this.pattern.matcher(id).matches();
      }
   }
}
