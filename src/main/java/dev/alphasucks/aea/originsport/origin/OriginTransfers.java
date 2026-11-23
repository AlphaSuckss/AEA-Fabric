package dev.alphasucks.aea.originsport.origin;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * The parts of Overgrown's OriginManager that its storage/transfer actions need, on top of apace's
 * OriginComponent (via {@link ApaceOrigins}) and the swap pools (via {@link SwapManager}).
 */
public final class OriginTransfers {
    private OriginTransfers() {
    }

    /** Overgrown's chooseOrigin: a swap layer gets the origin in its pool, any other layer as its main origin. */
    public static boolean chooseOrigin(class_3222 player, class_2960 layerId, class_2960 originId) {
        if (ApaceOrigins.origin(originId) == null) {
            OriginsPortIds.LOGGER.warn("Can't give {} unknown origin {}", player.method_5477().getString(), originId);
            return false;
        }
        if (SwapLayers.server().isSwapLayer(layerId)) {
            return SwapManager.grantToPool(player, layerId, originId);
        }
        return ApaceOrigins.choose(player, layerId, originId);
    }

    /** Overgrown's removeOrigin: a swap layer's pool is cleared, any other layer loses its main origin (and swap). */
    public static void removeOrigin(class_3222 player, class_2960 layerId) {
        if (SwapLayers.server().isSwapLayer(layerId)) {
            SwapManager.clearPool(player, layerId);
        } else {
            ApaceOrigins.remove(player, layerId);
        }
    }

    public static boolean isKnownLayer(class_2960 layerId) {
        return ApaceOrigins.layer(layerId) != null || SwapLayers.server().isSwapLayer(layerId);
    }

    private static OriginSelection effectiveSelection(class_2960 fromLayer, @Nullable OriginSelection selection) {
        if (selection != null) {
            return selection;
        }
        return SwapLayers.server().isSwapLayer(fromLayer) ? OriginSelection.POOL : OriginSelection.MAIN;
    }

    public static List<class_2960> selectableOrigins(class_3222 donor, class_2960 fromLayer, @Nullable OriginSelection selection) {
        OriginSelection mode = effectiveSelection(fromLayer, selection);
        List<class_2960> out = new ArrayList<>();
        if (mode == OriginSelection.MAIN || mode == OriginSelection.ALL) {
            addLive(out, ApaceOrigins.mainOrigin(donor, fromLayer));
        }
        if (mode == OriginSelection.ACTIVE) {
            class_2960 targetLayer = SwapLayers.server().isSwapLayer(fromLayer) ? SwapManager.resolveTarget(fromLayer) : fromLayer;
            if (targetLayer != null) {
                addLive(out, SwapManager.activeOrigin(donor, targetLayer));
            }
        }
        if (mode == OriginSelection.POOL || mode == OriginSelection.ALL) {
            for (class_2960 id : SwapManager.grantedPoolOf(donor, fromLayer)) {
                addLive(out, id);
            }
        }
        return out;
    }

    private static void addLive(List<class_2960> out, @Nullable class_2960 originId) {
        if (ApaceOrigins.isLive(originId) && !out.contains(originId)) {
            out.add(originId);
        }
    }

    /**
     * Moves (or copies) origins from the donor's {@code fromLayer} to the recipient's {@code toLayer}.
     * Resource/cooldown values of the moved origin's powers follow the origin.
     */
    public static boolean transferOrigin(class_3222 donor, class_3222 recipient, class_2960 fromLayer, class_2960 toLayer, boolean copy,
                                         @Nullable OriginSelection selection, @Nullable class_2960 explicitOrigin, boolean random) {
        if (!isKnownLayer(toLayer)) {
            return false;
        }
        boolean toPool = SwapLayers.server().isSwapLayer(toLayer);
        List<class_2960> candidates = explicitOrigin != null ? List.of(explicitOrigin) : selectableOrigins(donor, fromLayer, selection);
        if (candidates.isEmpty()) {
            return false;
        }
        List<class_2960> moving;
        if (explicitOrigin == null && effectiveSelection(fromLayer, selection) == OriginSelection.ALL) {
            moving = candidates;
        } else if (random && candidates.size() > 1) {
            moving = List.of(candidates.get(donor.method_6051().method_43048(candidates.size())));
        } else {
            moving = List.of(candidates.get(0));
        }
        boolean any = false;
        for (class_2960 originId : moving) {
            if (!ApaceOrigins.isLive(originId)) {
                continue;
            }
            boolean given = toPool ? SwapManager.grantToPool(recipient, toLayer, originId) : ApaceOrigins.choose(recipient, toLayer, originId);
            if (!given) {
                // AEA: Overgrown took the origin from the donor even when the recipient's main layer refused it
                continue;
            }
            any = true;
            if (donor != recipient) {
                copyResources(donor, recipient, originId);
            }
            if (!copy && (donor != recipient || !fromLayer.equals(toLayer))) {
                takeFromDonor(donor, fromLayer, originId);
            }
        }
        return any;
    }

    private static void copyResources(class_3222 donor, class_3222 recipient, class_2960 originId) {
        Origin origin = ApaceOrigins.origin(originId);
        PowerContainerImpl source = PowerContainerImpl.of(donor);
        PowerContainerImpl target = PowerContainerImpl.of(recipient);
        if (origin == null || source == null || target == null) {
            return;
        }
        for (class_2960 powerId : ApaceOrigins.powerIds(origin)) {
            if (!source.hasPower(powerId) || !target.hasPower(powerId)) {
                continue;
            }
            OptionalInt single = source.getAuxInt(powerId);
            if (single.isPresent()) {
                target.setAuxInt(powerId, single.getAsInt());
            }
            int[] table = source.getAuxInts(powerId);
            if (table != null) {
                target.setAuxInts(powerId, table.clone());
            }
        }
    }

    private static boolean takeFromDonor(class_3222 donor, class_2960 fromLayer, class_2960 originId) {
        if (originId.equals(ApaceOrigins.mainOrigin(donor, fromLayer))) {
            removeOrigin(donor, fromLayer);
            return true;
        }
        return SwapManager.revokeFromAnyPool(donor, fromLayer, originId);
    }
}
