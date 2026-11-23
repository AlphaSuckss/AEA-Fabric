package dev.alphasucks.aea.originsport.swap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import io.github.apace100.origins.origin.OriginLayer;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Swappable layers on top of apace's layers. apace's OriginLayer ignores Overgrown's
 * {@code "swappable"} field, so this read-only side loader collects it (plus {@code enabled}) from the
 * same {@code data/<ns>/origin_layers/*.json} files with apace's merge rules (loading_priority order,
 * {@code replace}, later files override). The resulting {@link Topology} (swap layer -> target layer)
 * is kept per side: the server builds it, clients get a copy over {@code aea:origin_swap_topology}.
 *
 * <p>AEA: never stored on apace's OriginLayer objects — in singleplayer the client thread replaces
 * those shared objects with copies read from apace's layer packet.
 */
public final class SwapLayers implements SimpleSynchronousResourceReloadListener {
    private static final String DIRECTORY = "origin_layers";

    /** What the files say about a layer (server side only). */
    public record RawLayer(boolean enabled, SwapConfig swap) {
    }

    private static volatile Map<class_2960, RawLayer> raw = Map.of();
    private static volatile Topology server = Topology.EMPTY;
    private static volatile Topology client = Topology.EMPTY;

    @Override
    public class_2960 getFabricId() {
        return OriginsPortIds.aea("origin_swap_layers");
    }

    @Override
    public Collection<class_2960> getFabricDependencies() {
        return Set.of(new class_2960("origins", "origin_layers"));
    }

    @Override
    public void method_14491(class_3300 manager) {
        Map<class_2960, RawLayer> out = new HashMap<>();
        Map<class_2960, List<class_3298>> found;
        try {
            found = manager.method_41265(DIRECTORY, id -> id.method_12832().endsWith(".json"));
        } catch (Exception e) {
            OriginsPortIds.LOGGER.error("Couldn't list origin layer files for swap data: {}", e.toString());
            return;
        }
        for (Map.Entry<class_2960, List<class_3298>> e : found.entrySet()) {
            class_2960 fileId = e.getKey();
            String path = fileId.method_12832();
            class_2960 layerId = class_2960.method_12829(fileId.method_12836() + ":" + path.substring(DIRECTORY.length() + 1, path.length() - ".json".length()));
            if (layerId == null) {
                continue;
            }
            // apace's OriginLayers#apply: files bucketed by loading_priority; "replace" clears its bucket and drops lower-priority files read after it
            TreeMap<Integer, List<JsonObject>> buckets = new TreeMap<>();
            int minPriority = Integer.MIN_VALUE;
            for (class_3298 resource : e.getValue()) {
                try (Reader reader = resource.method_43039()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (element == null || !element.isJsonObject()) {
                        continue;
                    }
                    JsonObject json = element.getAsJsonObject();
                    boolean replace = json.has("replace") && json.get("replace").isJsonPrimitive() && json.get("replace").getAsBoolean();
                    int priority = json.has("loading_priority") && json.get("loading_priority").isJsonPrimitive() ? json.get("loading_priority").getAsInt() : 0;
                    if (priority >= minPriority) {
                        List<JsonObject> bucket = buckets.computeIfAbsent(priority, p -> new LinkedList<>());
                        if (replace) {
                            bucket.clear();
                            minPriority = priority + 1;
                        }
                        bucket.add(json);
                    }
                } catch (Exception ex) {
                    OriginsPortIds.LOGGER.debug("Couldn't read swap data from layer file {}: {}", fileId, ex.toString());
                }
            }
            boolean first = true;
            boolean enabled = true;
            SwapConfig swap = SwapConfig.DISABLED;
            for (List<JsonObject> bucket : buckets.values()) {
                for (JsonObject json : bucket) {
                    if (first || json.has("enabled")) {
                        enabled = !json.has("enabled") || !json.get("enabled").isJsonPrimitive() || json.get("enabled").getAsBoolean();
                    }
                    if (SwapConfig.has(json)) {
                        SwapConfig parsed = SwapConfig.parse(json.get("swappable"));
                        if (parsed != null) {
                            swap = parsed;
                        } else {
                            OriginsPortIds.LOGGER.warn("Origin layer {} has an invalid \"swappable\" value: {}", layerId, json.get("swappable"));
                        }
                    } else if (first) {
                        swap = SwapConfig.DISABLED;
                    }
                    first = false;
                }
            }
            if (!first) {
                out.put(layerId, new RawLayer(enabled, swap));
            }
        }
        raw = Map.copyOf(out);
        rebuildServer();
    }

    // ------------------------------------------------------------------ topology

    /** Swap layer -> config/target, target -> swap layers, and the default target (first target layer that has swap layers). */
    public record Topology(Map<class_2960, SwapConfig> configs, Map<class_2960, class_2960> targets, Map<class_2960, List<class_2960>> byTarget,
                           @Nullable class_2960 defaultTarget) {
        public static final Topology EMPTY = new Topology(Map.of(), Map.of(), Map.of(), null);

        public boolean isEmpty() {
            return configs.isEmpty();
        }

        public boolean isSwapLayer(@Nullable class_2960 layerId) {
            return layerId != null && configs.containsKey(layerId);
        }

        /** The layer a swap layer swaps into (null if it has none). */
        @Nullable
        public class_2960 targetOf(class_2960 swapLayerId) {
            return targets.get(swapLayerId);
        }

        public List<class_2960> swapLayersFor(class_2960 targetLayerId) {
            List<class_2960> ids = byTarget.get(targetLayerId);
            return ids == null ? List.of() : ids;
        }

        public SwapConfig configFor(class_2960 targetLayerId) {
            List<class_2960> ids = swapLayersFor(targetLayerId);
            return ids.isEmpty() ? SwapConfig.ENABLED : configs.getOrDefault(ids.get(0), SwapConfig.ENABLED);
        }

        /**
         * Overgrown's SwapManager#resolveTarget: no layer -> the default target; a swap layer -> its
         * target; any other existing layer -> itself.
         */
        @Nullable
        public class_2960 resolveTarget(@Nullable class_2960 layerId) {
            if (layerId == null) {
                return defaultTarget;
            }
            if (isSwapLayer(layerId)) {
                return targetOf(layerId);
            }
            return ApaceOrigins.layer(layerId) != null ? layerId : null;
        }

        public void write(class_2540 buf) {
            buf.method_10804(configs.size());
            configs.forEach((id, config) -> {
                buf.method_10812(id);
                config.write(buf);
                class_2960 target = targets.get(id);
                buf.writeBoolean(target != null);
                if (target != null) {
                    buf.method_10812(target);
                }
            });
            buf.writeBoolean(defaultTarget != null);
            if (defaultTarget != null) {
                buf.method_10812(defaultTarget);
            }
        }

        public static Topology read(class_2540 buf) {
            int n = buf.method_10816();
            Map<class_2960, SwapConfig> configs = new LinkedHashMap<>();
            Map<class_2960, class_2960> targets = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                class_2960 id = buf.method_10810();
                configs.put(id, SwapConfig.read(buf));
                if (buf.readBoolean()) {
                    targets.put(id, buf.method_10810());
                }
            }
            class_2960 defaultTarget = buf.readBoolean() ? buf.method_10810() : null;
            return of(configs, targets, defaultTarget);
        }

        static Topology of(Map<class_2960, SwapConfig> configs, Map<class_2960, class_2960> targets, @Nullable class_2960 defaultTarget) {
            Map<class_2960, List<class_2960>> byTarget = new LinkedHashMap<>();
            configs.keySet().forEach(swapLayer -> {
                class_2960 target = targets.get(swapLayer);
                if (target != null) {
                    byTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(swapLayer);
                }
            });
            Map<class_2960, List<class_2960>> frozen = new LinkedHashMap<>();
            byTarget.forEach((k, v) -> frozen.put(k, List.copyOf(v)));
            return new Topology(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(configs)),
                java.util.Collections.unmodifiableMap(new LinkedHashMap<>(targets)), java.util.Collections.unmodifiableMap(frozen), defaultTarget);
        }
    }

    /** Rebuilds the server topology from the side-loaded layer data and apace's current layers. */
    public static void rebuildServer() {
        Map<class_2960, RawLayer> rawNow = raw;
        if (rawNow.isEmpty()) {
            server = Topology.EMPTY;
            return;
        }
        List<OriginLayer> ordered = ApaceOrigins.layersOrdered();
        Set<class_2960> known = new HashSet<>();
        Map<class_2960, SwapConfig> configs = new LinkedHashMap<>();
        List<class_2960> plainEnabled = new ArrayList<>();
        for (OriginLayer layer : ordered) {
            class_2960 id = layer.getIdentifier();
            known.add(id);
            RawLayer info = rawNow.get(id);
            boolean enabled = info != null ? info.enabled() : layer.isEnabled();
            if (info != null && enabled && info.swap().enabled()) {
                configs.put(id, info.swap());
            } else if (enabled) {
                plainEnabled.add(id);
            }
        }
        if (configs.isEmpty()) {
            server = Topology.EMPTY;
            return;
        }
        class_2960 firstPlain = plainEnabled.isEmpty() ? null : plainEnabled.get(0);
        Map<class_2960, class_2960> targets = new LinkedHashMap<>();
        configs.forEach((id, config) -> {
            class_2960 target;
            if (config.targetLayer().isPresent()) {
                target = config.targetLayer().get();
                if (!known.contains(target)) {
                    OriginsPortIds.LOGGER.warn("Swappable layer {} targets unknown layer {}", id, target);
                    target = null;
                } else if (configs.containsKey(target)) {
                    // AEA: a swap layer can't target another swap layer (Overgrown would resolve to it and never find a pool)
                    OriginsPortIds.LOGGER.warn("Swappable layer {} targets {}, which is itself swappable; ignoring it", id, target);
                    target = null;
                }
            } else {
                target = firstPlain;
            }
            if (target != null) {
                targets.put(id, target);
            }
        });
        class_2960 defaultTarget = null;
        for (class_2960 id : plainEnabled) {
            if (targets.containsValue(id)) {
                defaultTarget = id;
                break;
            }
        }
        server = Topology.of(configs, targets, defaultTarget);
        OriginsPortIds.LOGGER.info("Swappable origin layers: {}", server.targets());
    }

    // ------------------------------------------------------------------ per-side access

    public static Topology server() {
        return server;
    }

    public static Topology client() {
        return client;
    }

    public static Topology of(boolean clientSide) {
        return clientSide ? client : server;
    }

    public static void acceptClient(Topology topology) {
        client = topology;
    }

    public static void clearClient() {
        client = Topology.EMPTY;
    }

    public static void clearServer() {
        server = Topology.EMPTY;
        raw = Map.of();
    }

    /**
     * For the OriginLayer mixin: apace's {@code isEnabled()} has no side, but a layer object is the
     * same on both sides in singleplayer, and either side's topology only names real swap layers.
     */
    public static boolean isSwapLayerAnySide(@Nullable class_2960 layerId) {
        return layerId != null && (server.isSwapLayer(layerId) || client.isSwapLayer(layerId));
    }
}
