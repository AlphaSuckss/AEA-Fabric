package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.bridge.BridgedPower;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AEA: client-side power view. Overgrown kept its own synced copy; here everything is read from
 * Apoli 2.9.2's synced component on the client entity. Only key cooldowns are tracked locally.
 */
public final class ClientPowerState {
    private static final Map<class_2960, Integer> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<class_2960, TypeHolders> HOLDERS = new ConcurrentHashMap<>();

    private ClientPowerState() {
    }

    @Nullable
    private static class_1297 entity(int id) {
        class_310 mc = class_310.method_1551();
        return mc.field_1687 == null ? null : mc.field_1687.method_8469(id);
    }

    private static int localId() {
        class_310 mc = class_310.method_1551();
        return mc.field_1724 == null ? -1 : mc.field_1724.method_5628();
    }

    public static TypeHolders holders(class_2960 canonicalType) {
        return HOLDERS.computeIfAbsent(canonicalType, TypeHolders::new);
    }

    public static void removeEntity(int entityId) {
    }

    @Nullable
    public static PowerContainer containerFor(class_1297 entity) {
        return PowerContainerImpl.of(entity);
    }

    public static Map<class_2960, int[]> tablesFor(int entityId) {
        PowerContainerImpl c = PowerContainerImpl.of(entity(entityId));
        if (c == null) {
            return Map.of();
        }
        Map<class_2960, int[]> out = new HashMap<>();
        for (io.github.apace100.apoli.power.Power p : c.component().getPowers()) {
            if (p instanceof BridgedPower<?> b && b.auxInts() != null) {
                out.put(b.powerId(), b.auxInts());
            }
        }
        return out;
    }

    @Nullable
    public static class_2487 powerInventory(class_2960 powerId) {
        return null;
    }

    public static void clear() {
        COOLDOWNS.clear();
    }

    public static Set<class_2960> suppressedFor(int entityId) {
        class_1297 e = entity(entityId);
        return e == null ? Set.of() : SuppressedPowerManager.getSuppressedPowers(e.method_5667());
    }

    public static Map<class_2960, Set<class_2960>> powersFor(int entityId) {
        class_1297 e = entity(entityId);
        PowerContainerImpl c = PowerContainerImpl.of(e);
        if (c == null) {
            return Map.of();
        }
        Map<class_2960, Set<class_2960>> out = new LinkedHashMap<>();
        PowerHolderComponent comp = c.component();
        for (io.github.apace100.apoli.power.PowerType<?> t : comp.getPowerTypes(true)) {
            out.put(t.getIdentifier(), new HashSet<>(comp.getSources(t)));
        }
        return out;
    }

    public static Map<class_2960, Integer> auxFor(int entityId) {
        PowerContainerImpl c = PowerContainerImpl.of(entity(entityId));
        if (c == null) {
            return Map.of();
        }
        Map<class_2960, Integer> out = new HashMap<>();
        for (class_2960 id : c.allPowers()) {
            OptionalInt v = c.getAuxInt(id);
            if (v.isPresent()) {
                out.put(id, v.getAsInt());
            }
        }
        return out;
    }

    public static Set<class_2960> localPowers() {
        return powersFor(localId()).keySet();
    }

    public static Set<class_2960> localSourcesOf(class_2960 power) {
        Set<class_2960> s = powersFor(localId()).get(power);
        return s == null ? Set.of() : s;
    }

    public static Set<class_2960> localAllSources() {
        Set<class_2960> out = new HashSet<>();
        powersFor(localId()).values().forEach(out::addAll);
        return out;
    }

    public static Map<class_2960, Integer> localAuxInt() {
        return auxFor(localId());
    }

    public static OptionalInt getAuxInt(class_2960 powerId) {
        Integer v = localAuxInt().get(powerId);
        return v == null ? OptionalInt.empty() : OptionalInt.of(v);
    }

    public static void setCooldown(class_2960 power, int ticks) {
        if (ticks <= 0) {
            COOLDOWNS.remove(power);
        } else {
            COOLDOWNS.put(power, ticks);
        }
    }

    public static int getCooldown(class_2960 power) {
        return COOLDOWNS.getOrDefault(power, 0);
    }

    public static void tickCooldowns() {
        COOLDOWNS.replaceAll((k, v) -> Math.max(0, v - 1));
        COOLDOWNS.values().removeIf(v -> v == 0);
    }

    @Nullable
    public static Power getLocalPower(class_2960 id) {
        return ApoliPowers.get(id);
    }

    public static final class TypeHolders {
        private final class_2960 type;

        private TypeHolders(class_2960 type) {
            this.type = type;
        }

        public boolean contains(int entityId) {
            if (!ApoliPowers.anyOfType(type)) {
                return false;
            }
            PowerContainerImpl c = PowerContainerImpl.of(entity(entityId));
            return c != null && !c.powersOfType(type).isEmpty();
        }
    }
}
