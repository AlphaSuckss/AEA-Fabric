package dev.alphasucks.aea.originsport.swap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-player swap state (the swap half of Overgrown's PlayerOriginsImpl): the origin currently
 * swapped in on a layer, and the origins granted to each layer's swap pool. Main origins stay in
 * apace's OriginComponent. Keyed by the (target) layer id.
 */
public final class SwapData {
    public static final Codec<SwapData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(class_2960.field_25139, class_2960.field_25139).optionalFieldOf("active_swaps", Map.of()).forGetter(SwapData::swapSnapshot),
        Codec.unboundedMap(class_2960.field_25139, class_2960.field_25139.listOf()).optionalFieldOf("swap_pool", Map.of()).forGetter(SwapData::poolSnapshot),
        Codec.unboundedMap(class_2960.field_25139, class_2960.field_25139.listOf()).optionalFieldOf("suppressed", Map.of()).forGetter(SwapData::hiddenSnapshot),
        Codec.unboundedMap(class_2960.field_25139, class_2960.field_25139).optionalFieldOf("basis", Map.of()).forGetter(SwapData::basisSnapshot)
    ).apply(i, SwapData::new));

    private final Map<class_2960, class_2960> activeSwaps;
    private final Map<class_2960, Set<class_2960>> pools;
    /** layer -> main-origin powers this layer's swap suppressed (so they can be released even after Sync dropped its copy). */
    private final Map<class_2960, Set<class_2960>> hidden;
    /**
     * AEA: layer -> the main origin the active swap was made over. apace can change a main origin
     * behind our back (orb, choose screen, /origin set); Overgrown ends the swap when the main origin
     * is (re)chosen, so the periodic check compares against this.
     */
    private final Map<class_2960, class_2960> basis;

    public SwapData() {
        this(Map.of(), Map.of(), Map.of(), Map.of());
    }

    public SwapData(Map<class_2960, class_2960> activeSwaps, Map<class_2960, List<class_2960>> pools, Map<class_2960, List<class_2960>> hidden,
                    Map<class_2960, class_2960> basis) {
        this.basis = new HashMap<>(basis);
        this.activeSwaps = new HashMap<>(activeSwaps);
        this.pools = new HashMap<>();
        pools.forEach((layer, ids) -> {
            if (!ids.isEmpty()) {
                this.pools.put(layer, new LinkedHashSet<>(ids));
            }
        });
        this.hidden = new HashMap<>();
        hidden.forEach((layer, ids) -> {
            if (!ids.isEmpty()) {
                this.hidden.put(layer, new LinkedHashSet<>(ids));
            }
        });
    }

    public synchronized Set<class_2960> hiddenOf(class_2960 layerId) {
        Set<class_2960> set = hidden.get(layerId);
        return set == null ? Set.of() : Set.copyOf(set);
    }

    public synchronized void setHidden(class_2960 layerId, Set<class_2960> powers) {
        if (powers.isEmpty()) {
            hidden.remove(layerId);
        } else {
            hidden.put(layerId, new LinkedHashSet<>(powers));
        }
    }

    public synchronized Map<class_2960, List<class_2960>> hiddenSnapshot() {
        Map<class_2960, List<class_2960>> out = new HashMap<>(hidden.size());
        hidden.forEach((k, v) -> out.put(k, List.copyOf(v)));
        return out;
    }

    @Nullable
    public synchronized class_2960 getActiveSwap(class_2960 layerId) {
        return activeSwaps.get(layerId);
    }

    public synchronized void setActiveSwap(class_2960 layerId, @Nullable class_2960 originId) {
        if (originId == null) {
            activeSwaps.remove(layerId);
        } else {
            activeSwaps.put(layerId, originId);
        }
    }

    public synchronized Map<class_2960, class_2960> swapSnapshot() {
        return Map.copyOf(activeSwaps);
    }

    public synchronized boolean grantToPool(class_2960 layerId, class_2960 originId) {
        return pools.computeIfAbsent(layerId, k -> new LinkedHashSet<>()).add(originId);
    }

    public synchronized boolean revokeFromPool(class_2960 layerId, class_2960 originId) {
        Set<class_2960> set = pools.get(layerId);
        if (set == null) {
            return false;
        }
        boolean removed = set.remove(originId);
        if (set.isEmpty()) {
            pools.remove(layerId);
        }
        return removed;
    }

    public synchronized boolean clearPool(class_2960 layerId) {
        return pools.remove(layerId) != null;
    }

    public synchronized List<class_2960> poolOf(class_2960 layerId) {
        Set<class_2960> set = pools.get(layerId);
        return set == null ? List.of() : List.copyOf(set);
    }

    public synchronized Map<class_2960, List<class_2960>> poolSnapshot() {
        Map<class_2960, List<class_2960>> out = new HashMap<>(pools.size());
        pools.forEach((k, v) -> out.put(k, List.copyOf(v)));
        return out;
    }

    @Nullable
    public synchronized class_2960 basisOf(class_2960 layerId) {
        return basis.get(layerId);
    }

    public synchronized void setBasis(class_2960 layerId, @Nullable class_2960 mainOriginId) {
        if (mainOriginId == null) {
            basis.remove(layerId);
        } else {
            basis.put(layerId, mainOriginId);
        }
    }

    public synchronized boolean hasBasis(class_2960 layerId) {
        return basis.containsKey(layerId);
    }

    public synchronized Map<class_2960, class_2960> basisSnapshot() {
        return Map.copyOf(basis);
    }

    public synchronized boolean isEmpty() {
        return activeSwaps.isEmpty() && pools.isEmpty() && hidden.isEmpty();
    }
}
