package dev.alphasucks.aea.port.power;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.bridge.AeaPowerJson;
import dev.alphasucks.aea.bridge.BridgePowerFactory;
import dev.alphasucks.aea.bridge.CodecData;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.skill.SkillInfo;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AEA: Overgrown's global power table, rebuilt lazily from Apoli 2.9.2's
 * {@link io.github.apace100.apoli.power.PowerTypeRegistry} whenever it changes.
 */
public final class ApoliPowers {
    private static final AtomicInteger GENERATION = new AtomicInteger();
    private static volatile int builtFor = -1;
    private static volatile Map<class_2960, Power> POWERS = Map.of();
    private static volatile Set<class_2960> TYPES_IN_USE = Set.of();
    private static volatile Map<String, List<class_2960>> BY_TAG = Map.of();

    private ApoliPowers() {
    }

    /** Called (via mixin) whenever Apoli's power registry changes. */
    public static void invalidate() {
        GENERATION.incrementAndGet();
    }

    public static int generation() {
        return GENERATION.get();
    }

    private static void ensure() {
        int gen = GENERATION.get();
        if (builtFor == gen) {
            return;
        }
        synchronized (ApoliPowers.class) {
            if (builtFor == gen) {
                return;
            }
            // AEA: in singleplayer Apoli's client power sync clears and refills the shared registry on the
            // render thread while the server thread reads it, so copy it first and retry on a concurrent change.
            List<Map.Entry<class_2960, io.github.apace100.apoli.power.PowerType>> snapshot = snapshot();
            if (snapshot == null) {
                return;
            }
            Map<class_2960, Power> powers = new HashMap<>();
            Set<class_2960> types = new HashSet<>();
            Map<String, List<class_2960>> byTag = new HashMap<>();
            for (Map.Entry<class_2960, io.github.apace100.apoli.power.PowerType> e : snapshot) {
                Power p;
                try {
                    p = build(e.getKey(), e.getValue());
                } catch (RuntimeException ex) {
                    continue;
                }
                if (p == null) {
                    continue;
                }
                powers.put(e.getKey(), p);
                types.add(p.typeId());
                for (String tag : p.tags()) {
                    byTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(e.getKey());
                }
            }
            byTag.replaceAll((k, v) -> List.copyOf(v));
            POWERS = powers;
            TYPES_IN_USE = types;
            BY_TAG = byTag;
            builtFor = gen;
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private static List<Map.Entry<class_2960, io.github.apace100.apoli.power.PowerType>> snapshot() {
        for (int attempt = 0; attempt < 16; attempt++) {
            try {
                List<Map.Entry<class_2960, io.github.apace100.apoli.power.PowerType>> copy = new ArrayList<>(io.github.apace100.apoli.power.PowerTypeRegistry.size() + 16);
                for (Map.Entry<class_2960, io.github.apace100.apoli.power.PowerType> e : io.github.apace100.apoli.power.PowerTypeRegistry.entries()) {
                    if (e != null && e.getKey() != null && e.getValue() != null) {
                        copy.add(new java.util.AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue()));
                    }
                }
                return copy;
            } catch (java.util.ConcurrentModificationException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
                Thread.onSpinWait();
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static Power build(class_2960 id, io.github.apace100.apoli.power.PowerType<?> type) {
        PowerFactory.Instance factoryInstance = type.getFactory();
        if (factoryInstance == null) {
            return null;
        }
        SerializableData.Instance data = factoryInstance.getDataInstance();
        PowerFactory<?> factory = factoryInstance.getFactory();
        class_2960 typeId;
        Object cfg = null;
        JsonObject json = null;
        if (factory instanceof BridgePowerFactory<?> bridge) {
            typeId = bridge.canonicalTypeId();
            cfg = data.get(CodecData.CFG);
            if (cfg == null) {
                return null; // its config failed to sync to this client: treat the power as absent
            }
            JsonElement j = data.get(CodecData.JSON);
            json = j != null && j.isJsonObject() ? j.getAsJsonObject() : null;
        } else {
            typeId = factory.getSerializerId();
        }
        Optional<EntityCondition> condition = Optional.empty();
        try {
            if (data.isPresent("condition")) {
                Object c = data.get("condition");
                if (c instanceof ConditionFactory.Instance inst) {
                    JsonElement src = json != null && json.has("condition") ? json.get("condition") : null;
                    condition = Optional.of(new EntityCondition((ConditionFactory<class_1297>.Instance) inst, src));
                }
            }
        } catch (RuntimeException ignored) {
        }
        List<String> tags = AeaPowerJson.tags(id);
        Optional<SkillInfo> skill = Optional.empty();
        JsonObject full = json != null ? json : AeaPowerJson.json(id);
        if (full != null && full.has("skill")) {
            skill = SkillInfo.CODEC.parse(JsonOps.INSTANCE, full.get("skill")).result();
        }
        Optional<class_2561> name = Optional.of(type.getName());
        Optional<class_2561> description = Optional.of(type.getDescription());
        return new Power(typeId, name, description, condition, type.isHidden(), tags, skill, cfg);
    }

    @Nullable
    public static Power get(class_2960 id) {
        ensure();
        return POWERS.get(id);
    }

    public static Map<class_2960, Power> view() {
        ensure();
        return java.util.Collections.unmodifiableMap(POWERS);
    }

    public static boolean isSubPower(class_2960 id) {
        return AeaPowerJson.isSubPower(id);
    }

    public static Set<class_2960> grantableIds() {
        ensure();
        Set<class_2960> out = new HashSet<>(POWERS.keySet());
        out.removeIf(AeaPowerJson::isSubPower);
        return out;
    }

    public static List<class_2960> withTag(String tag) {
        ensure();
        return BY_TAG.getOrDefault(tag, List.of());
    }

    public static boolean anyTagged() {
        ensure();
        return !BY_TAG.isEmpty();
    }

    public static boolean hasAnyTag(class_2960 id, List<String> tags) {
        Power power = get(id);
        if (power == null || power.tags().isEmpty()) {
            return false;
        }
        for (String tag : tags) {
            if (power.hasTag(tag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyOfType(class_2960 canonicalTypeId) {
        ensure();
        return TYPES_IN_USE.contains(canonicalTypeId);
    }

    public static void replaceAll(Map<class_2960, Power> ignored) {
        invalidate();
    }
}
