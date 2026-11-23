package dev.alphasucks.aea.originsport.swap;

import net.minecraft.class_2960;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side mirror of every player's swap state, filled by the {@code aea:origin_swap_sync}
 * packet, plus the local player's condition-gated swap options ({@code aea:origin_swap_available}).
 * Plain data with no client-only types, so common code (conditions evaluated in client worlds) can
 * read it; only the client receivers write it.
 */
public final class SwapClientState {
    private static final Map<UUID, Map<class_2960, class_2960>> SWAPS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<class_2960, List<class_2960>>> POOLS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<class_2960, List<class_2960>>> AVAILABLE = new ConcurrentHashMap<>();

    private SwapClientState() {
    }

    public static void set(UUID player, Map<class_2960, class_2960> swaps, Map<class_2960, List<class_2960>> pools) {
        SWAPS.put(player, Map.copyOf(swaps));
        POOLS.put(player, Map.copyOf(pools));
    }

    public static void setAvailable(UUID player, Map<class_2960, List<class_2960>> available) {
        AVAILABLE.put(player, Map.copyOf(available));
    }

    public static Map<class_2960, class_2960> swaps(UUID player) {
        return SWAPS.getOrDefault(player, Map.of());
    }

    public static Map<class_2960, List<class_2960>> pools(UUID player) {
        return POOLS.getOrDefault(player, Map.of());
    }

    /** swap layer -> origins its conditions currently offer (only known for the local player). */
    public static Map<class_2960, List<class_2960>> available(UUID player) {
        return AVAILABLE.getOrDefault(player, Map.of());
    }

    public static void clear() {
        SWAPS.clear();
        POOLS.clear();
        AVAILABLE.clear();
    }
}
