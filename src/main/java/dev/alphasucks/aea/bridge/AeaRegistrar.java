package dev.alphasucks.aea.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.TypedActionRegistry;
import dev.alphasucks.aea.port.alias.AliasDefault;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.TypedConditionRegistry;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.class_2378;
import net.minecraft.class_2960;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Pushes the collected Overgrown registrations into Apoli 2.9.2's registries, after both Apoli
 * and AEA have initialised (entrypoint order between mods is not guaranteed). An id is only
 * registered if nothing else (Apoli, Origins, Sync, other addons) already owns it.
 */
public final class AeaRegistrar {
    private static boolean apoliReady;
    private static boolean originsReady;
    private static boolean aeaReady;
    private static boolean flushed;
    private static final List<String> REGISTERED = new ArrayList<>();
    private static final List<String> SKIPPED = new ArrayList<>();

    private AeaRegistrar() {
    }

    public static synchronized void onApoliInitialized() {
        apoliReady = true;
        tryFlush();
    }

    public static synchronized void onOriginsInitialized() {
        originsReady = true;
        tryFlush();
    }

    public static synchronized void onAeaInitialized() {
        aeaReady = true;
        tryFlush();
    }

    private static void tryFlush() {
        if (!apoliReady || !originsReady || !aeaReady || flushed) {
            return;
        }
        flushed = true;
        flush();
    }

    private static void flush() {
        actions(ActionTypes.ENTITY, ApoliRegistries.ENTITY_ACTION, Bridges::entityAction);
        actions(ActionTypes.BI_ENTITY, ApoliRegistries.BIENTITY_ACTION, Bridges::biEntityAction);
        actions(ActionTypes.BLOCK, ApoliRegistries.BLOCK_ACTION, Bridges::blockAction);
        actions(ActionTypes.ITEM, ApoliRegistries.ITEM_ACTION, Bridges::itemAction);
        conditions(ConditionTypes.ENTITY, ApoliRegistries.ENTITY_CONDITION, Bridges::entityCondition);
        conditions(ConditionTypes.BI_ENTITY, ApoliRegistries.BIENTITY_CONDITION, Bridges::biEntityCondition);
        conditions(ConditionTypes.BLOCK, ApoliRegistries.BLOCK_CONDITION, Bridges::blockCondition);
        conditions(ConditionTypes.ITEM, ApoliRegistries.ITEM_CONDITION, Bridges::itemCondition);
        conditions(ConditionTypes.DAMAGE, ApoliRegistries.DAMAGE_CONDITION, Bridges::damageCondition);
        conditions(ConditionTypes.BIOME, ApoliRegistries.BIOME_CONDITION, Bridges::biomeCondition);
        conditions(ConditionTypes.FLUID, ApoliRegistries.FLUID_CONDITION, Bridges::fluidCondition);
        powers();
        dev.alphasucks.aea.effect.ApoliEffectTypePatch.apply();
        AEA.LOGGER.info("Registered {} ported Overgrown types ({} ids already provided elsewhere were left alone)", REGISTERED.size(), SKIPPED.size());
        AEA.LOGGER.debug("Ported: {}", REGISTERED);
        AEA.LOGGER.debug("Left to Apoli/Origins/Sync: {}", SKIPPED);
    }

    private static boolean taken(class_2378<?> registry, class_2960 id) {
        if (registry.method_10250(id)) {
            SKIPPED.add(id.toString());
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- actions/conditions

    private interface ActionBridge<CTX, F> {
        <C> F create(class_2960 id, ActionType<CTX, C> type, Supplier<MapCodec<C>> codec);
    }

    private interface ConditionBridge<CTX, F> {
        <C> F create(class_2960 id, ConditionType<CTX, C> type, Supplier<MapCodec<C>> codec);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <CTX, F> void actions(TypedActionRegistry<CTX> source, class_2378<F> target, ActionBridgeFn<CTX, F> bridge) {
        for (Map.Entry<class_2960, TypedActionRegistry.Entry<CTX, ?>> e : source.view().entrySet()) {
            ActionType type = e.getValue().type();
            AliasingOptions opts = e.getValue().aliases();
            for (class_2960 id : idsFor(e.getKey(), opts)) {
                if (taken(target, id)) {
                    continue;
                }
                Map<String, String> defaults = opts.typeAliasDefaults().getOrDefault(id, Map.of());
                Supplier<MapCodec> codec = () -> withDefaults(AliasingMapCodec.wrap(type.codec(), opts.fieldAliases()), defaults);
                F factory = (F) bridge.apply(id, type, (Supplier) codec);
                class_2378.method_10230(target, id, factory);
                REGISTERED.add(source.groupName() + "_action " + id);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <CTX, F> void conditions(TypedConditionRegistry<CTX> source, class_2378<F> target, ConditionBridgeFn<CTX, F> bridge) {
        for (Map.Entry<class_2960, TypedConditionRegistry.Entry<CTX, ?>> e : source.view().entrySet()) {
            ConditionType type = e.getValue().type();
            AliasingOptions opts = e.getValue().aliases();
            for (class_2960 id : idsFor(e.getKey(), opts)) {
                if (taken(target, id)) {
                    continue;
                }
                Map<String, String> defaults = opts.typeAliasDefaults().getOrDefault(id, Map.of());
                Supplier<MapCodec> codec = () -> withDefaults(AliasingMapCodec.wrap(type.codec(), opts.fieldAliases()), defaults);
                F factory = (F) bridge.apply(id, type, (Supplier) codec);
                class_2378.method_10230(target, id, factory);
                REGISTERED.add(source.groupName() + "_condition " + id);
            }
        }
    }

    @FunctionalInterface
    public interface ActionBridgeFn<CTX, F> {
        @SuppressWarnings("rawtypes")
        Object apply(class_2960 id, ActionType type, Supplier codec);
    }

    @FunctionalInterface
    public interface ConditionBridgeFn<CTX, F> {
        @SuppressWarnings("rawtypes")
        Object apply(class_2960 id, ConditionType type, Supplier codec);
    }

    private static List<class_2960> idsFor(class_2960 primary, AliasingOptions opts) {
        List<class_2960> ids = new ArrayList<>();
        ids.add(primary);
        for (class_2960 alias : opts.typeAliases()) {
            if (!ids.contains(alias)) {
                ids.add(alias);
            }
        }
        return ids;
    }

    /** Overgrown injects string defaults for type aliases (e.g. a fixed "side"). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static MapCodec withDefaults(MapCodec codec, Map<String, String> defaults) {
        if (defaults.isEmpty()) {
            return codec;
        }
        return dev.alphasucks.aea.port.alias.DefaultInjectingMapCodec.wrap(codec, defaults);
    }

    // ------------------------------------------------------------------------ powers

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void powers() {
        Map<class_2960, List<class_2960>> aliasesOf = new LinkedHashMap<>();
        PowerTypeRegistry.aliasView().forEach((alias, canonical) -> aliasesOf.computeIfAbsent(canonical, k -> new ArrayList<>()).add(alias));
        for (Map.Entry<class_2960, PowerType<?>> e : PowerTypeRegistry.view().entrySet()) {
            class_2960 canonical = e.getKey();
            PowerType type = e.getValue();
            AliasingOptions opts = PowerTypeRegistry.options(canonical);
            List<class_2960> ids = new ArrayList<>();
            ids.add(canonical);
            ids.addAll(aliasesOf.getOrDefault(canonical, List.of()));
            for (class_2960 id : ids) {
                if (taken(ApoliRegistries.POWER_FACTORY, id)) {
                    continue;
                }
                Supplier<MapCodec> codec = () -> AliasingMapCodec.wrap(type.configCodec(), opts.fieldAliases());
                BridgePowerFactory factory = new BridgePowerFactory(id, canonical, type, (Supplier) codec);
                UnaryOperator<JsonObject> pre = powerPreprocessor(id);
                if (pre != null) {
                    ((CodecData<?>) factory.getSerializableData()).setPreprocessor(pre);
                }
                class_2378.method_10230(ApoliRegistries.POWER_FACTORY, id, factory);
                REGISTERED.add("power " + id);
            }
        }
    }

    /** Per-alias field renames and typed defaults (Overgrown applies them to the power JSON before decoding). */
    private static UnaryOperator<JsonObject> powerPreprocessor(class_2960 id) {
        Map<String, String> renames = PowerTypeRegistry.aliasFieldRenames(id);
        List<AliasDefault<?>> defaults = PowerTypeRegistry.aliasDefaults(id);
        if (renames.isEmpty() && defaults.isEmpty()) {
            return null;
        }
        return json -> {
            JsonObject out = json.deepCopy();
            renames.forEach((oldName, newName) -> {
                if (out.has(oldName) && !out.has(newName)) {
                    out.add(newName, out.remove(oldName));
                }
            });
            for (AliasDefault<?> def : defaults) {
                if (!out.has(def.field())) {
                    def.encode(JsonOps.INSTANCE).result().ifPresent(v -> out.add(def.field(), (JsonElement) v));
                }
            }
            return out;
        };
    }

    public static List<String> registered() {
        return List.copyOf(REGISTERED);
    }
}
