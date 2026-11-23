package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.alias.AliasDefault;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AEA: registry of the ported (Overgrown) power types, keyed by canonical type id, plus the
 * alias ids that point at them and the per-alias defaults/renames. Apoli 2.9.2's factory
 * registry is the real one; {@link dev.alphasucks.aea.bridge.AeaRegistrar} fills it from here.
 */
public final class PowerTypeRegistry {
    private static final Map<class_2960, PowerType<?>> BY_ID = new LinkedHashMap<>();
    private static final Map<class_2960, AliasingOptions> OPTIONS = new HashMap<>();
    private static final Map<class_2960, class_2960> ALIASES = new LinkedHashMap<>();
    private static final Map<class_2960, List<AliasDefault<?>>> ALIAS_DEFAULTS = new HashMap<>();
    private static final Map<class_2960, Map<String, String>> ALIAS_RENAMES = new HashMap<>();

    private PowerTypeRegistry() {
    }

    public static <T extends PowerType<?>> T register(class_2960 id, T type) {
        return register(id, type, AliasingOptions.NONE);
    }

    public static <T extends PowerType<?>> T register(class_2960 id, T type, AliasingOptions opts) {
        if (BY_ID.containsKey(id)) {
            throw new IllegalStateException("Duplicate ported power type: " + id);
        }
        BY_ID.put(id, type);
        OPTIONS.put(id, opts);
        for (class_2960 alias : opts.typeAliases()) {
            ALIASES.put(alias, id);
        }
        return type;
    }

    public static void registerAliasDefaults(class_2960 aliasId, AliasDefault<?>... defaults) {
        ALIAS_DEFAULTS.computeIfAbsent(aliasId, k -> new ArrayList<>()).addAll(List.of(defaults));
    }

    public static List<AliasDefault<?>> aliasDefaults(class_2960 aliasId) {
        return ALIAS_DEFAULTS.getOrDefault(aliasId, List.of());
    }

    public static void registerAliasFieldRenames(class_2960 aliasId, Map<String, String> oldToNew) {
        ALIAS_RENAMES.computeIfAbsent(aliasId, k -> new HashMap<>()).putAll(oldToNew);
    }

    public static Map<String, String> aliasFieldRenames(class_2960 aliasId) {
        return ALIAS_RENAMES.getOrDefault(aliasId, Map.of());
    }

    public static AliasingOptions options(class_2960 id) {
        return OPTIONS.getOrDefault(id, AliasingOptions.NONE);
    }

    public static void alias(class_2960 alias, class_2960 canonical) {
        if (!alias.equals(canonical)) {
            ALIASES.put(alias, canonical);
        }
    }

    public static Map<class_2960, class_2960> aliasView() {
        return Map.copyOf(ALIASES);
    }

    public static class_2960 resolveId(class_2960 id) {
        return ALIASES.getOrDefault(id, id);
    }

    @Nullable
    public static PowerType<?> get(class_2960 id) {
        return BY_ID.get(resolveId(id));
    }

    public static Map<class_2960, PowerType<?>> view() {
        return Map.copyOf(BY_ID);
    }

    public static PowerType<?> unwrap(@Nullable PowerType<?> type) {
        return type;
    }
}
