package dev.alphasucks.aea.originsport.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.origin.ImpactLevels;
import dev.alphasucks.aea.originsport.origin.OriginSelection;
import dev.alphasucks.aea.originsport.origin.OriginTags;
import dev.alphasucks.aea.originsport.origin.OriginView;
import dev.alphasucks.aea.originsport.storage.OriginStorage;
import dev.alphasucks.aea.originsport.storage.StoredData;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Overgrown Origins' entity and bi-entity conditions, side-aware (server state or the synced client mirrors). */
public final class OriginConditions {
    private OriginConditions() {
    }

    public static void register() {
        ConditionTypes.ENTITY.register(OriginsPortIds.origins("origin_tag"), new OriginTag());
        ConditionTypes.ENTITY.register(OriginsPortIds.origins("impact"), new Impact());
        ConditionTypes.BI_ENTITY.register(OriginsPortIds.origins("impact"), new ImpactBiEntity());
        ConditionTypes.ENTITY.register(OriginsPortIds.origins("stored_origin"), new StoredOrigin());
        ConditionTypes.ENTITY.register(OriginsPortIds.origins("stored_value"), new StoredValue());
        ConditionTypes.ENTITY.register(OriginsPortIds.origins("swapped"), new Swapped());
    }

    private static boolean client(class_1657 player) {
        return player.method_37908() != null && player.method_37908().method_8608();
    }

    // ------------------------------------------------------------------ origin_tag

    private static final Codec<List<String>> TAGS_CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf()).xmap(
        either -> either.map(List::of, List::copyOf),
        list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list));

    public record OriginTagCfg(List<String> tags, Optional<class_2960> layer, OriginSelection selection) {
        public OriginTagCfg {
            tags = List.copyOf(tags);
        }
    }

    /** {@code origins:origin_tag}: one of the player's origins carries one of the tags (the origin file's {@code tags} field). */
    public static final class OriginTag implements ConditionType<EntityCtx, OriginTagCfg> {
        @Override
        public MapCodec<OriginTagCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                TAGS_CODEC.fieldOf("tag").forGetter(OriginTagCfg::tags),
                class_2960.field_25139.optionalFieldOf("layer").forGetter(OriginTagCfg::layer),
                OriginSelection.CODEC.optionalFieldOf("selection", OriginSelection.MAIN).forGetter(OriginTagCfg::selection)
            ).apply(i, OriginTagCfg::new));
        }

        @Override
        public boolean test(OriginTagCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_1657 player)) {
                return false;
            }
            boolean side = client(player);
            OriginSelection mode = cfg.selection();
            boolean wantsMain = mode == OriginSelection.MAIN || mode == OriginSelection.ALL;
            boolean wantsActive = mode == OriginSelection.ACTIVE || mode == OriginSelection.ALL;
            boolean wantsPool = mode == OriginSelection.POOL || mode == OriginSelection.ALL;
            if (cfg.layer().isPresent()) {
                class_2960 layer = cfg.layer().get();
                if (wantsMain && matches(cfg, OriginView.chosen(player).get(layer), side)) {
                    return true;
                }
                if (wantsActive && matches(cfg, OriginView.activeOn(player, layer), side)) {
                    return true;
                }
                // AEA: Overgrown tested every pool here, ignoring "layer"; now only that layer's pool(s)
                return wantsPool && matchesAny(cfg, SwapManager.grantedPoolOf(player, layer), side);
            }
            if (wantsMain && matchesAny(cfg, OriginView.chosen(player).values(), side)) {
                return true;
            }
            // AEA: "active" means the origins in effect; Overgrown also matched main origins that were swapped out here
            if (wantsActive && matchesAny(cfg, OriginView.active(player).values(), side)) {
                return true;
            }
            return wantsPool && matchesAny(cfg, OriginView.pool(player), side);
        }

        private static boolean matchesAny(OriginTagCfg cfg, Iterable<class_2960> ids, boolean side) {
            for (class_2960 id : ids) {
                if (matches(cfg, id, side)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean matches(OriginTagCfg cfg, @Nullable class_2960 id, boolean side) {
            if (id == null) {
                return false;
            }
            List<String> tags = OriginTags.tags(id, side);
            if (tags.isEmpty()) {
                return false;
            }
            for (String tag : cfg.tags()) {
                if (tags.contains(tag)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------ impact

    public enum Aggregate implements class_3542 {
        MAX("max"),
        MIN("min"),
        SUM("sum"),
        AVERAGE("average");

        public static final Codec<Aggregate> CODEC = class_3542.method_28140(Aggregate::values);
        private final String name;

        Aggregate(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    /** Impact of the player's origins in effect (swapped-in or main) on one layer, or aggregated; -1 for non-players. */
    public static double impactOf(@Nullable class_1297 entity, Optional<class_2960> layer, Aggregate aggregate) {
        if (!(entity instanceof class_1657 player)) {
            return -1.0;
        }
        if (layer.isPresent()) {
            return OriginView.impactOn(player, layer.get());
        }
        // AEA: Overgrown only looked at layers with a main origin, missing a swap onto an empty layer
        Map<class_2960, class_2960> active = OriginView.active(player);
        int count = 0;
        int sum = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (class_2960 originId : active.values()) {
            if (ApaceOrigins.origin(originId) == null) {
                continue;
            }
            int level = ApaceOrigins.impact(originId);
            count++;
            sum += level;
            max = Math.max(max, level);
            min = Math.min(min, level);
        }
        if (count == 0) {
            return 0.0;
        }
        return switch (aggregate) {
            case MAX -> max;
            case MIN -> min;
            case SUM -> sum;
            case AVERAGE -> (double) sum / count;
        };
    }

    public record ImpactCfg(Optional<class_2960> layer, Aggregate aggregate, Comparison comparison, int compareTo) {
    }

    /** {@code origins:impact} (entity). */
    public static final class Impact implements ConditionType<EntityCtx, ImpactCfg> {
        @Override
        public MapCodec<ImpactCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.optionalFieldOf("layer").forGetter(ImpactCfg::layer),
                Aggregate.CODEC.optionalFieldOf("aggregate", Aggregate.MAX).forGetter(ImpactCfg::aggregate),
                Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(ImpactCfg::comparison),
                ImpactLevels.LEVEL_CODEC.fieldOf("compare_to").forGetter(ImpactCfg::compareTo)
            ).apply(i, ImpactCfg::new));
        }

        @Override
        public boolean test(ImpactCfg cfg, EntityCtx ctx) {
            double impact = impactOf(ctx.raw(), cfg.layer(), cfg.aggregate());
            return impact >= 0.0 && cfg.comparison().compare(impact, cfg.compareTo());
        }
    }

    public enum Subject implements class_3542 {
        ACTOR("actor"),
        TARGET("target");

        public static final Codec<Subject> CODEC = class_3542.method_28140(Subject::values);
        private final String name;

        Subject(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    public record ImpactBiCfg(Subject subject, Optional<class_2960> layer, Aggregate aggregate, Comparison comparison, Optional<Integer> compareTo, double offset) {
    }

    /** {@code origins:impact} (bi-entity): one side's impact against a number or the other side's impact (+ offset). */
    public static final class ImpactBiEntity implements ConditionType<BiEntityCtx, ImpactBiCfg> {
        @Override
        public MapCodec<ImpactBiCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Subject.CODEC.optionalFieldOf("subject", Subject.ACTOR).forGetter(ImpactBiCfg::subject),
                class_2960.field_25139.optionalFieldOf("layer").forGetter(ImpactBiCfg::layer),
                Aggregate.CODEC.optionalFieldOf("aggregate", Aggregate.MAX).forGetter(ImpactBiCfg::aggregate),
                Comparison.CODEC.optionalFieldOf("comparison", Comparison.GREATER_EQUAL).forGetter(ImpactBiCfg::comparison),
                ImpactLevels.LEVEL_CODEC.optionalFieldOf("compare_to").forGetter(ImpactBiCfg::compareTo),
                Codec.DOUBLE.optionalFieldOf("offset", 0.0).forGetter(ImpactBiCfg::offset)
            ).apply(i, ImpactBiCfg::new));
        }

        @Override
        public boolean test(ImpactBiCfg cfg, BiEntityCtx ctx) {
            boolean actorIsSubject = cfg.subject() == Subject.ACTOR;
            class_1297 subject = actorIsSubject ? ctx.rawActor() : ctx.rawTarget();
            double left = impactOf(subject, cfg.layer(), cfg.aggregate());
            if (left < 0.0) {
                return false;
            }
            double right;
            if (cfg.compareTo().isPresent()) {
                right = cfg.compareTo().get();
            } else {
                right = impactOf(actorIsSubject ? ctx.rawTarget() : ctx.rawActor(), cfg.layer(), cfg.aggregate());
                if (right < 0.0) {
                    return false;
                }
            }
            return cfg.comparison().compare(left, right + cfg.offset());
        }
    }

    // ------------------------------------------------------------------ storage

    public record StoredOriginCfg(Optional<String> key, Optional<class_2960> origin, Optional<class_2960> layer) {
    }

    /** {@code origins:stored_origin}: an origin is stored (under a key, optionally a specific origin / layer). */
    public static final class StoredOrigin implements ConditionType<EntityCtx, StoredOriginCfg> {
        @Override
        public MapCodec<StoredOriginCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("key").forGetter(StoredOriginCfg::key),
                class_2960.field_25139.optionalFieldOf("origin").forGetter(StoredOriginCfg::origin),
                class_2960.field_25139.optionalFieldOf("layer").forGetter(StoredOriginCfg::layer)
            ).apply(i, StoredOriginCfg::new));
        }

        @Override
        public boolean test(StoredOriginCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_1657 player)) {
                return false;
            }
            StoredData data = OriginStorage.data(player);
            if (data == null) {
                return false;
            }
            if (cfg.key().isPresent()) {
                return matches(data.getOrigin(cfg.key().get()), cfg);
            }
            for (StoredData.StoredOrigin stored : data.originsView().values()) {
                if (matches(stored, cfg)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean matches(@Nullable StoredData.StoredOrigin stored, StoredOriginCfg cfg) {
            if (stored == null) {
                return false;
            }
            if (cfg.origin().isPresent() && !cfg.origin().get().equals(stored.origin())) {
                return false;
            }
            return cfg.layer().isEmpty() || cfg.layer().get().equals(stored.layer());
        }
    }

    public record StoredValueCfg(String key, Optional<String> value) {
    }

    /** {@code origins:stored_value}: a value is stored under the key (optionally equal to {@code value}). */
    public static final class StoredValue implements ConditionType<EntityCtx, StoredValueCfg> {
        @Override
        public MapCodec<StoredValueCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(StoredValueCfg::key),
                Codec.STRING.optionalFieldOf("value").forGetter(StoredValueCfg::value)
            ).apply(i, StoredValueCfg::new));
        }

        @Override
        public boolean test(StoredValueCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_1657 player)) {
                return false;
            }
            String stored = OriginStorage.value(player, cfg.key());
            return stored != null && (cfg.value().isEmpty() || cfg.value().get().equals(stored));
        }
    }

    // ------------------------------------------------------------------ swapped

    public record SwappedCfg(Optional<class_2960> layer, Optional<class_2960> origin) {
    }

    /** {@code origins:swapped}: the player has an origin swapped in (on a layer, optionally a specific origin). */
    public static final class Swapped implements ConditionType<EntityCtx, SwappedCfg> {
        @Override
        public MapCodec<SwappedCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.optionalFieldOf("layer").forGetter(SwappedCfg::layer),
                class_2960.field_25139.optionalFieldOf("origin").forGetter(SwappedCfg::origin)
            ).apply(i, SwappedCfg::new));
        }

        @Override
        public boolean test(SwappedCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_1657 player)) {
                return false;
            }
            // AEA: side-aware; Overgrown read the server-only attachment, so this was always false in client worlds
            class_2960 target = SwapManager.resolveTarget(player, cfg.layer().orElse(null));
            if (target == null) {
                return false;
            }
            class_2960 active = SwapManager.activeSwap(player, target);
            return active != null && (cfg.origin().isEmpty() || cfg.origin().get().equals(active));
        }
    }
}
