package dev.alphasucks.aea.originsport.origin;

import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.swap.SwapClientState;
import dev.alphasucks.aea.originsport.swap.SwapData;
import dev.alphasucks.aea.originsport.swap.SwapDataAttachment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Side-aware read view of a player's origins: main origins from apace's (synced) OriginComponent,
 * swaps and pools from the AEA attachment on the server or its client mirror in client worlds.
 */
public final class OriginView {
    private OriginView() {
    }

    private static boolean client(class_1657 player) {
        return player.method_37908() != null && player.method_37908().method_8608();
    }

    /** layer -> main origin (empty layers left out). */
    public static Map<class_2960, class_2960> chosen(class_1657 player) {
        return ApaceOrigins.chosen(player);
    }

    /** layer -> origin currently swapped in. */
    public static Map<class_2960, class_2960> swaps(class_1657 player) {
        if (client(player)) {
            return SwapClientState.swaps(player.method_5667());
        }
        SwapData data = SwapDataAttachment.get(player);
        return data == null ? Map.of() : data.swapSnapshot();
    }

    /** layer -> origins granted to that layer's swap pool (unfiltered). */
    public static Map<class_2960, List<class_2960>> pools(class_1657 player) {
        if (client(player)) {
            return SwapClientState.pools(player.method_5667());
        }
        SwapData data = SwapDataAttachment.get(player);
        return data == null ? Map.of() : data.poolSnapshot();
    }

    public static List<class_2960> poolOf(class_1657 player, class_2960 layerId) {
        List<class_2960> ids = pools(player).get(layerId);
        return ids == null ? List.of() : ids;
    }

    /** Every pooled origin across all layers, deduplicated. */
    public static List<class_2960> pool(class_1657 player) {
        Set<class_2960> out = new LinkedHashSet<>();
        for (List<class_2960> ids : pools(player).values()) {
            out.addAll(ids);
        }
        return new ArrayList<>(out);
    }

    @Nullable
    public static class_2960 activeSwap(class_1657 player, class_2960 layerId) {
        return swaps(player).get(layerId);
    }

    /** The origin in effect on a layer: the swapped-in one, else the main one (null for none). */
    @Nullable
    public static class_2960 activeOn(class_1657 player, class_2960 layerId) {
        class_2960 swapped = activeSwap(player, layerId);
        return swapped != null ? swapped : ApaceOrigins.mainOrigin(player, layerId);
    }

    /** layer -> origin in effect, for every layer with a main origin or a swap. */
    public static Map<class_2960, class_2960> active(class_1657 player) {
        Map<class_2960, class_2960> out = new LinkedHashMap<>(chosen(player));
        out.putAll(swaps(player));
        return out;
    }

    /** Holds the origin as a main origin or as the swapped-in origin on any layer. */
    public static boolean holds(class_1657 player, class_2960 originId) {
        return chosen(player).containsValue(originId) || swaps(player).containsValue(originId);
    }

    public static boolean inPool(class_1657 player, class_2960 originId) {
        for (List<class_2960> ids : pools(player).values()) {
            if (ids.contains(originId)) {
                return true;
            }
        }
        return false;
    }

    public static int impactOn(class_1657 player, class_2960 layerId) {
        class_2960 active = activeOn(player, layerId);
        return active == null ? 0 : ApaceOrigins.impact(active);
    }

    public static int layerCount(class_1657 player) {
        return chosen(player).size();
    }

    @Nullable
    public static class_1657 playerOf(@Nullable class_1297 entity) {
        return entity instanceof class_1657 player ? player : null;
    }
}
