package dev.alphasucks.aea.originsport.apace;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapter over apace's Origins 1.10.2 data model, standing in for Overgrown's OriginManager /
 * OriginRegistry / OriginLayers / PlayerOriginsAttachment. Main origins live in apace's
 * {@link OriginComponent} (synced to clients by apace), and an origin's powers are granted with the
 * origin id as power source, exactly as apace's own {@code /origin set} does.
 */
public final class ApaceOrigins {
    /** {@code origins:empty}, apace's "no origin" marker. Kept literal so this class doesn't force apace's Origin class init. */
    public static final class_2960 EMPTY_ID = new class_2960("origins", "empty");

    private ApaceOrigins() {
    }

    // ------------------------------------------------------------------ lookups

    @Nullable
    public static OriginComponent component(@Nullable class_1297 entity) {
        if (!(entity instanceof class_1657 player)) {
            return null;
        }
        try {
            return ModComponents.ORIGIN.getNullable(player);
        } catch (RuntimeException e) {
            // components aren't attached yet while the entity is being constructed
            return null;
        }
    }

    /**
     * Null-safe {@link OriginLayers#getLayer} (apace's throws for unknown ids). AEA: also swallows
     * the odd failure from reading apace's static layer map while the singleplayer client refills it.
     */
    @Nullable
    public static OriginLayer layer(@Nullable class_2960 id) {
        if (id == null) {
            return null;
        }
        try {
            return OriginLayers.getLayer(id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** A copy of apace's layers (retried if the map changes under us), sorted by order then id. */
    public static List<OriginLayer> layersOrdered() {
        List<OriginLayer> out = new ArrayList<>();
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                out = new ArrayList<>(OriginLayers.getLayers());
                break;
            } catch (RuntimeException e) {
                out = new ArrayList<>();
            }
        }
        out.removeIf(java.util.Objects::isNull);
        out.sort(Comparator.<OriginLayer>naturalOrder().thenComparing(l -> String.valueOf(l.getIdentifier())));
        return out;
    }

    /** Null-safe {@link OriginRegistry#get} (apace's throws for unknown ids). */
    @Nullable
    public static Origin origin(@Nullable class_2960 id) {
        if (id == null) {
            return null;
        }
        try {
            return OriginRegistry.contains(id) ? OriginRegistry.get(id) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean isEmpty(@Nullable class_2960 originId) {
        return originId == null || EMPTY_ID.equals(originId);
    }

    /** An origin id that exists and isn't {@code origins:empty}. */
    public static boolean isLive(@Nullable class_2960 originId) {
        return !isEmpty(originId) && origin(originId) != null;
    }

    /** apace-enabled layers in order (swap layers report disabled through the OriginLayer mixin). */
    public static List<OriginLayer> enabledOrdered() {
        List<OriginLayer> out = layersOrdered();
        out.removeIf(layer -> !layer.isEnabled());
        return out;
    }

    public static boolean layerEnabled(@Nullable class_2960 layerId) {
        OriginLayer layer = layer(layerId);
        return layer != null && layer.isEnabled();
    }

    /** layer id -> main origin id, empty layers left out. Works on both sides (apace syncs the component). */
    public static Map<class_2960, class_2960> chosen(@Nullable class_1657 player) {
        OriginComponent component = component(player);
        if (component == null) {
            return Map.of();
        }
        Map<class_2960, class_2960> out = new LinkedHashMap<>();
        for (Map.Entry<OriginLayer, Origin> e : component.getOrigins().entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            class_2960 originId = e.getValue().getIdentifier();
            if (!isEmpty(originId)) {
                out.put(e.getKey().getIdentifier(), originId);
            }
        }
        return out;
    }

    /** The main (chosen) origin on a layer, or null for none / {@code origins:empty}. */
    @Nullable
    public static class_2960 mainOrigin(@Nullable class_1657 player, @Nullable class_2960 layerId) {
        OriginComponent component = component(player);
        if (component == null || layerId == null) {
            return null;
        }
        for (Map.Entry<OriginLayer, Origin> e : component.getOrigins().entrySet()) {
            if (e.getKey() != null && e.getValue() != null && layerId.equals(e.getKey().getIdentifier())) {
                class_2960 originId = e.getValue().getIdentifier();
                return isEmpty(originId) ? null : originId;
            }
        }
        return null;
    }

    public static int impact(@Nullable class_2960 originId) {
        Origin origin = origin(originId);
        return origin == null || origin.getImpact() == null ? 0 : origin.getImpact().getImpactValue();
    }

    public static class_2561 name(class_2960 originId) {
        Origin origin = origin(originId);
        return origin == null ? class_2561.method_43470(originId.toString()) : origin.getName();
    }

    /** The power ids an origin grants, including the sub-powers of {@code apoli:multiple} powers. */
    public static Set<class_2960> powerIds(@Nullable Origin origin) {
        Set<class_2960> out = new LinkedHashSet<>();
        if (origin != null) {
            for (PowerType<?> type : origin.getPowerTypes()) {
                addExpanded(out, type);
            }
        }
        return out;
    }

    public static void addExpanded(Set<class_2960> out, @Nullable PowerType<?> type) {
        if (type == null || type.getIdentifier() == null) {
            return;
        }
        out.add(type.getIdentifier());
        if (type instanceof MultiplePowerType<?> multiple) {
            out.addAll(multiple.getSubPowers());
        }
    }

    /** Whether apace will keep this origin on this layer across a relog (its NBT reader drops origins a layer doesn't list unless they're special). */
    public static boolean layerAccepts(OriginLayer layer, Origin origin) {
        return isEmpty(origin.getIdentifier()) || origin.isSpecial() || layer.contains(origin);
    }

    // ------------------------------------------------------------------ mutation

    /**
     * Sets a main origin the way apace's {@code /origin set} does ({@link OriginComponent#setOrigin}
     * grants the new origin's powers with its id as source and drops the old origin's), then lets
     * apace auto-choose newly settled layers and fires apace's "chosen" callbacks.
     */
    public static boolean choose(class_3222 player, class_2960 layerId, class_2960 originId) {
        OriginLayer layer = layer(layerId);
        Origin origin = origin(originId);
        OriginComponent component = component(player);
        if (layer == null || origin == null || component == null) {
            OriginsPortIds.LOGGER.warn("Can't give {} origin {} on layer {}: unknown layer or origin", player.method_5477().getString(), originId, layerId);
            return false;
        }
        if (!layerAccepts(layer, origin)) {
            // apace's own /origin set refuses this too: PlayerOriginComponent#readFromNbt would wipe it on the next login.
            OriginsPortIds.LOGGER.warn("Not giving {} origin {} on layer {}: the layer doesn't list it and it isn't special, so Origins would drop it on relog",
                player.method_5477().getString(), originId, layerId);
            return false;
        }
        Origin previous = component.getOrigin(layer);
        if (previous != null && originId.equals(previous.getIdentifier())) {
            return true;
        }
        SwapManager.revokeSwapGrants(player, layerId);
        boolean hadOriginBefore = component.hadOriginBefore();
        boolean hadAllOrigins = component.hasAllOrigins();
        component.setOrigin(layer, origin);
        regrantHeld(player, component);
        component.checkAutoChoosingLayers(player, false);
        component.sync();
        if (component.hasAllOrigins() && !hadAllOrigins) {
            OriginComponent.onChosen(player, hadOriginBefore);
        } else {
            OriginComponent.partialOnChosen(player, hadOriginBefore, origin);
        }
        return true;
    }

    /** Clears a main origin (apace's "empty" origin), dropping its powers. */
    public static boolean remove(class_3222 player, class_2960 layerId) {
        OriginLayer layer = layer(layerId);
        OriginComponent component = component(player);
        if (layer == null || component == null) {
            return false;
        }
        // AEA: Overgrown's removeOrigin ends the layer's swap even when no main origin is held
        SwapManager.revokeSwapGrants(player, layerId);
        if (!component.hasOrigin(layer)) {
            return false;
        }
        component.setOrigin(layer, Origin.EMPTY);
        regrantHeld(player, component);
        component.checkAutoChoosingLayers(player, false);
        component.sync();
        return true;
    }

    /**
     * apace's setOrigin drops every power sourced by the replaced origin, even when the same origin is
     * still held on another layer; this puts those back.
     */
    public static void regrantHeld(class_3222 player, OriginComponent component) {
        PowerHolderComponent powers = PowerHolderComponent.KEY.getNullable(player);
        if (powers == null) {
            return;
        }
        boolean changed = false;
        for (Origin origin : List.copyOf(component.getOrigins().values())) {
            if (origin == null || isEmpty(origin.getIdentifier())) {
                continue;
            }
            for (PowerType<?> type : origin.getPowerTypes()) {
                if (type != null && !powers.hasPower(type, origin.getIdentifier())) {
                    changed |= powers.addPower(type, origin.getIdentifier());
                }
            }
        }
        if (changed) {
            powers.sync();
        }
    }
}
