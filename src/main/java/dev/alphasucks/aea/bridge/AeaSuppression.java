package dev.alphasucks.aea.bridge;

import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Source-tracked power suppression (Overgrown's semantics). The effective set is mirrored
 * into Sync's {@link SuppressedPowerManager}, whose mixin makes Apoli's own powers report
 * inactive too, so suppressing works on every power type, not just ported ones.
 */
public final class AeaSuppression {
    /** entity -> power -> sources suppressing it */
    private static final Map<UUID, Map<class_2960, Set<class_2960>>> BY_ENTITY = new ConcurrentHashMap<>();

    private AeaSuppression() {
    }

    public static synchronized boolean suppress(class_1297 entity, class_2960 power, class_2960 source) {
        Map<class_2960, Set<class_2960>> map = BY_ENTITY.computeIfAbsent(entity.method_5667(), k -> new HashMap<>());
        boolean added = map.computeIfAbsent(power, k -> new HashSet<>()).add(source);
        SuppressedPowerManager.suppress(entity.method_5667(), power);
        return added;
    }

    public static synchronized boolean unsuppress(class_1297 entity, class_2960 power, class_2960 source) {
        Map<class_2960, Set<class_2960>> map = BY_ENTITY.get(entity.method_5667());
        if (map == null) {
            return false;
        }
        Set<class_2960> sources = map.get(power);
        if (sources == null || !sources.remove(source)) {
            return false;
        }
        if (sources.isEmpty()) {
            map.remove(power);
            SuppressedPowerManager.liberate(entity.method_5667(), power);
        }
        if (map.isEmpty()) {
            BY_ENTITY.remove(entity.method_5667());
        }
        return true;
    }

    public static boolean isSuppressed(class_1297 entity, class_2960 power) {
        return SuppressedPowerManager.isSuppressed(entity.method_5667(), power);
    }

    public static Set<class_2960> suppressed(class_1297 entity) {
        return SuppressedPowerManager.getSuppressedPowers(entity.method_5667());
    }

    public static synchronized Set<class_2960> sourcesOf(class_1297 entity, class_2960 power) {
        Map<class_2960, Set<class_2960>> map = BY_ENTITY.get(entity.method_5667());
        if (map == null || !map.containsKey(power)) {
            return Set.of();
        }
        return Collections.unmodifiableSet(new HashSet<>(map.get(power)));
    }

    public static void forget(UUID entity) {
        BY_ENTITY.remove(entity);
    }
}
