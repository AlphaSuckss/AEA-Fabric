package dev.alphasucks.aea.originsport.swap;

import dev.alphasucks.aea.bridge.AeaSuppression;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.origin.OriginView;
import dev.alphasucks.aea.originsport.power.ActionOnSwapPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Port of Overgrown Origins' SwapManager onto apace's Origins 1.10.2.
 *
 * <p>A swappable layer offers origins (its conditioned {@code origins} list plus origins granted to
 * its pool) that can be swapped into a target layer. While swapped, the incoming origin's powers are
 * granted with source {@code <ns>:swap/<target path>} and the target's main origin powers that the
 * incoming origin doesn't share are suppressed with that same source. The main origin itself stays
 * in apace's OriginComponent untouched.
 */
public final class SwapManager {
    private static final int POOL_REFRESH_TICKS = 10;
    /** Server thread only. */
    private static final Map<UUID, Map<class_2960, List<class_2960>>> LAST_AVAILABLE = new HashMap<>();

    private SwapManager() {
    }

    public static class_2960 sourceFor(class_2960 targetLayerId) {
        return new class_2960(targetLayerId.method_12836(), "swap/" + targetLayerId.method_12832());
    }

    private static boolean client(class_1657 player) {
        return player.method_37908() != null && player.method_37908().method_8608();
    }

    private static SwapLayers.Topology topology(class_1657 player) {
        return SwapLayers.of(client(player));
    }

    // ------------------------------------------------------------------ layer resolution

    @Nullable
    public static class_2960 resolveTarget(class_1657 player, @Nullable class_2960 layerId) {
        return topology(player).resolveTarget(layerId);
    }

    @Nullable
    public static class_2960 resolveTarget(@Nullable class_2960 layerId) {
        return SwapLayers.server().resolveTarget(layerId);
    }

    public static boolean isSwapLayer(class_1657 player, @Nullable class_2960 layerId) {
        return topology(player).isSwapLayer(layerId);
    }

    public static List<class_2960> swapLayersFor(class_1657 player, class_2960 targetLayerId) {
        return topology(player).swapLayersFor(targetLayerId);
    }

    public static SwapConfig swapConfigFor(class_1657 player, class_2960 targetLayerId) {
        return topology(player).configFor(targetLayerId);
    }

    // ------------------------------------------------------------------ pools

    /** swap layer -> origins its conditions offer this player right now (server: live; client: last sync, own player only). */
    public static Map<class_2960, List<class_2960>> available(class_1657 player) {
        if (client(player)) {
            return SwapClientState.available(player.method_5667());
        }
        return availableByLayer(player, SwapLayers.server());
    }

    private static Map<class_2960, List<class_2960>> availableByLayer(class_1657 player, SwapLayers.Topology topology) {
        Map<class_2960, List<class_2960>> out = null;
        for (class_2960 swapLayerId : topology.configs().keySet()) {
            List<class_2960> ids = availableOn(player, swapLayerId);
            if (!ids.isEmpty()) {
                if (out == null) {
                    out = new HashMap<>();
                }
                out.put(swapLayerId, List.copyOf(ids));
            }
        }
        return out == null ? Map.of() : out;
    }

    private static List<class_2960> availableOn(class_1657 player, class_2960 swapLayerId) {
        OriginLayer layer = ApaceOrigins.layer(swapLayerId);
        if (layer == null) {
            return List.of();
        }
        try {
            return layer.getOrigins(player);
        } catch (RuntimeException e) {
            // apace evaluates the conditions and reads its origin registry here
            return List.of();
        }
    }

    /** Every origin the player may swap into the target layer (not including its main origin), sorted by name. */
    public static List<class_2960> pool(class_1657 player, class_2960 targetLayerId) {
        return pool(player, targetLayerId, ApaceOrigins.mainOrigin(player, targetLayerId));
    }

    public static List<class_2960> pool(class_1657 player, class_2960 targetLayerId, @Nullable class_2960 main) {
        Map<class_2960, List<class_2960>> granted = OriginView.pools(player);
        Map<class_2960, List<class_2960>> available = available(player);
        Set<class_2960> seen = new LinkedHashSet<>();
        for (class_2960 swapLayerId : swapLayersFor(player, targetLayerId)) {
            collect(seen, available.get(swapLayerId), main);
            collect(seen, granted.get(swapLayerId), main);
        }
        return sorted(seen);
    }

    private static void collect(Set<class_2960> seen, @Nullable Collection<class_2960> ids, @Nullable class_2960 main) {
        if (ids == null) {
            return;
        }
        for (class_2960 id : ids) {
            if (!id.equals(main) && ApaceOrigins.isLive(id)) {
                seen.add(id);
            }
        }
    }

    private static List<class_2960> sorted(Set<class_2960> seen) {
        List<class_2960> out = new ArrayList<>(seen);
        out.sort(Comparator.comparing(SwapManager::sortKey).thenComparing(class_2960::toString));
        return out;
    }

    private static String sortKey(class_2960 originId) {
        Origin origin = ApaceOrigins.origin(originId);
        try {
            return origin == null ? originId.method_12832() : origin.getName().getString();
        } catch (RuntimeException e) {
            return originId.method_12832();
        }
    }

    public static boolean hasPool(class_1657 player, class_2960 targetLayerId) {
        return !pool(player, targetLayerId).isEmpty();
    }

    public static boolean grantToPool(class_3222 player, class_2960 swapLayerId, class_2960 originId) {
        if (!SwapLayers.server().isSwapLayer(swapLayerId) || !ApaceOrigins.isLive(originId)) {
            return false;
        }
        if (!SwapDataAttachment.getOrCreate(player).grantToPool(swapLayerId, originId)) {
            return false;
        }
        broadcast(player);
        return true;
    }

    public static boolean revokeFromPool(class_3222 player, class_2960 swapLayerId, class_2960 originId) {
        SwapData data = SwapDataAttachment.get(player);
        if (data == null || !data.revokeFromPool(swapLayerId, originId)) {
            return false;
        }
        revalidate(player);
        broadcast(player);
        return true;
    }

    /** Revokes from the named swap layer's pool, or from the pools of a target layer's swap layers. */
    public static boolean revokeFromAnyPool(class_3222 player, class_2960 layerId, class_2960 originId) {
        SwapLayers.Topology topology = SwapLayers.server();
        if (topology.isSwapLayer(layerId)) {
            return revokeFromPool(player, layerId, originId);
        }
        for (class_2960 swapLayerId : topology.swapLayersFor(layerId)) {
            if (revokeFromPool(player, swapLayerId, originId)) {
                return true;
            }
        }
        return false;
    }

    /** Origins explicitly granted to a swap layer's pool, or to the pools feeding a target layer. Side-aware. */
    public static List<class_2960> grantedPoolOf(class_1657 player, class_2960 layerId) {
        Map<class_2960, List<class_2960>> pools = OriginView.pools(player);
        SwapLayers.Topology topology = topology(player);
        if (topology.isSwapLayer(layerId)) {
            return pools.getOrDefault(layerId, List.of());
        }
        List<class_2960> out = new ArrayList<>();
        for (class_2960 swapLayerId : topology.swapLayersFor(layerId)) {
            for (class_2960 id : pools.getOrDefault(swapLayerId, List.of())) {
                if (!out.contains(id)) {
                    out.add(id);
                }
            }
        }
        return out;
    }

    /** The swap layer a {@code to_pool} grant on this layer goes to. */
    @Nullable
    public static class_2960 grantTargetFor(class_2960 layerId) {
        SwapLayers.Topology topology = SwapLayers.server();
        if (topology.isSwapLayer(layerId)) {
            return layerId;
        }
        if (ApaceOrigins.layer(layerId) == null) {
            return null;
        }
        List<class_2960> swapLayers = topology.swapLayersFor(layerId);
        return swapLayers.isEmpty() ? null : swapLayers.get(0);
    }

    public static void clearPool(class_3222 player, class_2960 swapLayerId) {
        SwapData data = SwapDataAttachment.get(player);
        if (data != null && data.clearPool(swapLayerId)) {
            revalidate(player);
            broadcast(player);
        }
    }

    public static void broadcast(class_3222 player) {
        MinecraftServer server = player.method_5682();
        if (server != null) {
            OriginsPortNetwork.broadcastSwaps(server, player);
        }
    }

    // ------------------------------------------------------------------ active swaps

    @Nullable
    public static class_2960 activeSwap(class_1657 player, class_2960 targetLayerId) {
        return OriginView.activeSwap(player, targetLayerId);
    }

    @Nullable
    public static class_2960 activeOrigin(class_1657 player, class_2960 targetLayerId) {
        return OriginView.activeOn(player, targetLayerId);
    }

    /**
     * Swaps {@code incomingId} into the target layer ({@code null} = back to the main origin).
     * Only origins from the player's pool for that layer are accepted.
     */
    public static boolean applySwap(class_3222 player, class_2960 targetLayerId, @Nullable class_2960 incomingId) {
        if (ApaceOrigins.layer(targetLayerId) == null || SwapLayers.server().isSwapLayer(targetLayerId)) {
            return false;
        }
        SwapData data = SwapDataAttachment.getOrCreate(player);
        class_2960 mainId = ApaceOrigins.mainOrigin(player, targetLayerId);
        class_2960 outgoingId = data.getActiveSwap(targetLayerId);
        if (outgoingId == null) {
            outgoingId = mainId;
        }
        if (incomingId != null && incomingId.equals(mainId)) {
            incomingId = null;
        }
        class_2960 resolvedIncoming = incomingId == null ? mainId : incomingId;
        if (resolvedIncoming == null) {
            // AEA: Overgrown refuses to go "back to main" when the layer has no main origin, which
            // left such swaps stuck (cycle's wrap and shift-to-main both failed). Clearing is allowed.
            if (data.getActiveSwap(targetLayerId) == null) {
                return false;
            }
        } else {
            if (resolvedIncoming.equals(outgoingId)) {
                return false;
            }
            if (incomingId != null && !pool(player, targetLayerId).contains(incomingId)) {
                return false;
            }
            if (ApaceOrigins.origin(resolvedIncoming) == null) {
                return false;
            }
        }
        if (PowerHolderComponent.KEY.getNullable(player) == null) {
            return false;
        }
        data.setActiveSwap(targetLayerId, incomingId);
        data.setBasis(targetLayerId, incomingId == null ? null : basisId(mainId));
        reconcile(player, data, targetLayerId);
        ActionOnSwapPower.fire(player, targetLayerId, outgoingId, resolvedIncoming == null ? ApaceOrigins.EMPTY_ID : resolvedIncoming);
        Origin incoming = ApaceOrigins.origin(resolvedIncoming);
        if (incoming != null && ChoseOriginCriterion.INSTANCE != null) {
            ChoseOriginCriterion.INSTANCE.trigger(player, incoming);
        }
        broadcast(player);
        return true;
    }

    private static class_2960 basisId(@Nullable class_2960 mainId) {
        return mainId == null ? ApaceOrigins.EMPTY_ID : mainId;
    }

    public static boolean cycle(class_3222 player, class_2960 targetLayerId, boolean shift) {
        SwapConfig config = swapConfigFor(player, targetLayerId);
        if (shift && config.shiftReturnsToMain()) {
            return applySwap(player, targetLayerId, null);
        }
        List<class_2960> options = pool(player, targetLayerId);
        if (options.isEmpty()) {
            return false;
        }
        class_2960 current = activeSwap(player, targetLayerId);
        if (current == null) {
            return applySwap(player, targetLayerId, options.get(0));
        }
        int index = options.indexOf(current);
        if (index < 0) {
            return applySwap(player, targetLayerId, options.get(0));
        }
        if (index + 1 < options.size()) {
            return applySwap(player, targetLayerId, options.get(index + 1));
        }
        return applySwap(player, targetLayerId, config.wrapToMain() ? null : options.get(0));
    }

    public static boolean random(class_3222 player, class_2960 targetLayerId, boolean includeMain) {
        List<class_2960> options = new ArrayList<>(pool(player, targetLayerId));
        class_2960 current = activeSwap(player, targetLayerId);
        options.remove(current);
        int bound = options.size() + (includeMain && current != null ? 1 : 0);
        if (bound <= 0) {
            return false;
        }
        int pick = player.method_6051().method_43048(bound);
        return applySwap(player, targetLayerId, pick < options.size() ? options.get(pick) : null);
    }

    public static void resetToMain(class_3222 player, class_2960 targetLayerId) {
        SwapData data = SwapDataAttachment.get(player);
        if (data != null && data.getActiveSwap(targetLayerId) != null) {
            applySwap(player, targetLayerId, null);
        }
    }

    /** Ends the layer's swap without firing action_on_swap (Overgrown does this when a main origin is chosen or removed). */
    public static void revokeSwapGrants(class_3222 player, class_2960 targetLayerId) {
        SwapData data = SwapDataAttachment.get(player);
        if (data == null) {
            // nothing was ever swapped or suppressed for this player
            return;
        }
        boolean wasSwapped = data.getActiveSwap(targetLayerId) != null;
        boolean hadHidden = !data.hiddenOf(targetLayerId).isEmpty();
        if (!wasSwapped && !hadHidden && SwapLayers.server().swapLayersFor(targetLayerId).isEmpty()) {
            return;
        }
        data.setActiveSwap(targetLayerId, null);
        data.setBasis(targetLayerId, null);
        boolean changed = reconcile(player, data, targetLayerId);
        if (wasSwapped || changed) {
            broadcast(player);
        }
    }

    public static void revokeAllSwaps(class_3222 player) {
        SwapData data = SwapDataAttachment.get(player);
        if (data != null) {
            for (class_2960 targetLayerId : List.copyOf(data.swapSnapshot().keySet())) {
                revokeSwapGrants(player, targetLayerId);
            }
        }
    }

    /**
     * Makes the player's powers match the layer's swap state: the swapped-in origin's powers held
     * with the swap source, the main origin's other powers suppressed with it, and nothing stale.
     */
    private static boolean reconcile(class_3222 player, SwapData data, class_2960 targetLayerId) {
        class_2960 source = sourceFor(targetLayerId);
        class_2960 activeId = data.getActiveSwap(targetLayerId);
        Origin active = ApaceOrigins.origin(activeId);
        boolean changed = false;
        if (activeId != null && active == null) {
            data.setActiveSwap(targetLayerId, null);
            data.setBasis(targetLayerId, null);
            changed = true;
        }
        PowerHolderComponent powers = PowerHolderComponent.KEY.getNullable(player);
        if (powers == null) {
            return changed;
        }
        boolean powersChanged = false;
        // compared by id: Apoli also sources an apoli:multiple power's sub-powers, and may hand out references
        Set<class_2960> wantedIds = ApaceOrigins.powerIds(active);
        for (PowerType<?> type : List.copyOf(powers.getPowersFromSource(source))) {
            if (type != null && !wantedIds.contains(type.getIdentifier())) {
                powers.removePower(type, source);
                powersChanged = true;
            }
        }
        if (active != null) {
            for (PowerType<?> type : active.getPowerTypes()) {
                if (type != null && !powers.hasPower(type, source) && powers.addPower(type, source)) {
                    powersChanged = true;
                }
            }
        }
        // main-origin powers the swapped-in origin doesn't share are suppressed while swapped
        Set<class_2960> hidden = new HashSet<>();
        if (active != null) {
            Origin main = ApaceOrigins.origin(ApaceOrigins.mainOrigin(player, targetLayerId));
            if (main != null) {
                for (class_2960 id : ApaceOrigins.powerIds(main)) {
                    if (!wantedIds.contains(id)) {
                        hidden.add(id);
                    }
                }
            }
        }
        Set<class_2960> previous = new HashSet<>(data.hiddenOf(targetLayerId));
        previous.addAll(AeaSuppression.suppressed(player));
        for (class_2960 id : previous) {
            if (!hidden.contains(id) && AeaSuppression.sourcesOf(player, id).contains(source) && AeaSuppression.unsuppress(player, id, source)) {
                changed = true;
            }
        }
        for (class_2960 id : hidden) {
            // always called: re-mirrors into Sync's manager, which drops everything on death
            if (AeaSuppression.suppress(player, id, source)) {
                changed = true;
            }
        }
        data.setHidden(targetLayerId, hidden);
        if (powersChanged) {
            powers.sync();
        }
        return changed || powersChanged;
    }

    /** Re-applies every swap (login, respawn, reload): re-grants swap powers and re-suppresses hidden ones. */
    public static void reconcileAll(class_3222 player) {
        SwapData data = SwapDataAttachment.get(player);
        if (data == null || data.isEmpty() && SwapLayers.server().isEmpty()) {
            return;
        }
        Set<class_2960> targets = new LinkedHashSet<>(data.swapSnapshot().keySet());
        targets.addAll(data.hiddenSnapshot().keySet());
        targets.addAll(SwapLayers.server().byTarget().keySet());
        for (class_2960 targetLayerId : targets) {
            reconcile(player, data, targetLayerId);
        }
    }

    /** Ends swaps whose layer, pool membership or main origin no longer allows them. */
    public static void revalidate(class_3222 player) {
        SwapData data = SwapDataAttachment.get(player);
        if (data == null) {
            return;
        }
        SwapLayers.Topology topology = SwapLayers.server();
        for (class_2960 targetLayerId : List.copyOf(data.swapSnapshot().keySet())) {
            class_2960 active = data.getActiveSwap(targetLayerId);
            if (active == null) {
                continue;
            }
            OriginLayer target = ApaceOrigins.layer(targetLayerId);
            if (target == null || !target.isEnabled() || topology.isSwapLayer(targetLayerId) || !holdsSwapLayer(player, targetLayerId)
                || !pool(player, targetLayerId).contains(active) || mainChanged(player, data, targetLayerId)) {
                revokeSwapGrants(player, targetLayerId);
            }
        }
    }

    /** AEA: apace changed the main origin under an active swap (Overgrown's chooseOrigin ends the swap). */
    private static boolean mainChanged(class_3222 player, SwapData data, class_2960 targetLayerId) {
        class_2960 now = basisId(ApaceOrigins.mainOrigin(player, targetLayerId));
        if (!data.hasBasis(targetLayerId)) {
            data.setBasis(targetLayerId, now);
            return false;
        }
        return !now.equals(data.basisOf(targetLayerId));
    }

    public static boolean holdsSwapLayer(class_1657 player, class_2960 targetLayerId) {
        Map<class_2960, List<class_2960>> granted = OriginView.pools(player);
        Map<class_2960, List<class_2960>> available = available(player);
        for (class_2960 swapLayerId : swapLayersFor(player, targetLayerId)) {
            List<class_2960> offered = available.get(swapLayerId);
            if (offered != null && !offered.isEmpty()) {
                return true;
            }
            List<class_2960> explicit = granted.get(swapLayerId);
            if (explicit != null && !explicit.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ ticking

    public static void tickPools(MinecraftServer server) {
        SwapLayers.Topology topology = SwapLayers.server();
        if (topology.isEmpty()) {
            if (!LAST_AVAILABLE.isEmpty()) {
                LAST_AVAILABLE.clear();
            }
            return;
        }
        int tick = server.method_3780();
        for (class_3222 player : server.method_3760().method_14571()) {
            if (Math.floorMod(tick + player.method_5628(), POOL_REFRESH_TICKS) == 0) {
                try {
                    refresh(player, topology);
                } catch (RuntimeException e) {
                    OriginsPortIds.LOGGER.debug("Swap refresh for {} failed: {}", player.method_5477().getString(), e.toString());
                }
            }
        }
    }

    private static void refresh(class_3222 player, SwapLayers.Topology topology) {
        demoteSwapLayerOrigins(player, topology);
        Map<class_2960, List<class_2960>> now = availableByLayer(player, topology);
        boolean availableChanged = !now.equals(LAST_AVAILABLE.getOrDefault(player.method_5667(), Map.of()));
        if (availableChanged) {
            LAST_AVAILABLE.put(player.method_5667(), now);
            OriginsPortNetwork.sendAvailable(player, now);
        }
        SwapData data = SwapDataAttachment.get(player);
        if (data != null && !data.swapSnapshot().isEmpty()) {
            revalidate(player);
        }
    }

    /**
     * AEA (from Overgrown's demoteSwappableLayerRecord): apace's own {@code /origin set} or choose
     * screen can still put a main origin on a swap layer; move it into that layer's pool instead.
     */
    private static void demoteSwapLayerOrigins(class_3222 player, SwapLayers.Topology topology) {
        OriginComponent component = ApaceOrigins.component(player);
        if (component == null) {
            return;
        }
        for (class_2960 swapLayerId : topology.configs().keySet()) {
            class_2960 held = ApaceOrigins.mainOrigin(player, swapLayerId);
            if (held != null && ApaceOrigins.remove(player, swapLayerId)) {
                grantToPool(player, swapLayerId, held);
                OriginsPortIds.LOGGER.info("{} had {} as a main origin on swappable layer {}; moved it into that layer's swap pool.",
                    player.method_5477().getString(), held, swapLayerId);
            }
        }
    }

    public static void forgetAvailable(class_1657 player) {
        LAST_AVAILABLE.remove(player.method_5667());
    }

    public static void clearServer() {
        LAST_AVAILABLE.clear();
    }
}
