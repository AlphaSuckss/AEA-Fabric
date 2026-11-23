package dev.alphasucks.aea.originsport.storage;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.origin.OriginTransfers;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Port of Overgrown's OriginStorage: stored origins/values on a player, and {@code [key]} placeholders. */
public final class OriginStorage {
    public static final class_2960 DEFAULT_LAYER = OriginsPortIds.origins("origin");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\[([^\\[\\]]+)]");

    private OriginStorage() {
    }

    /** Side-aware: the attachment on the server, the synced mirror in client worlds. */
    @Nullable
    public static StoredData data(class_1657 holder) {
        if (holder.method_37908() != null && holder.method_37908().method_8608()) {
            return StoredClientState.get(holder.method_5667());
        }
        return StoredDataAttachment.get(holder);
    }

    private static void changed(class_1657 holder) {
        if (holder instanceof class_3222 player) {
            MinecraftServer server = player.method_5682();
            if (server != null) {
                OriginsPortNetwork.broadcastStored(server, player);
            }
        }
    }

    /** Stores the source's main origin on the layer. */
    public static boolean storeOrigin(class_1657 holder, String key, class_1657 source, class_2960 layerId) {
        class_2960 main = ApaceOrigins.mainOrigin(source, layerId);
        return main != null && storeOrigin(holder, key, layerId, main);
    }

    public static boolean storeOrigin(class_1657 holder, String key, class_2960 layerId, @Nullable class_2960 originId) {
        if (ApaceOrigins.isEmpty(originId)) {
            return false;
        }
        StoredDataAttachment.getOrCreate(holder).setOrigin(key, new StoredData.StoredOrigin(layerId, originId));
        changed(holder);
        return true;
    }

    public static boolean applyStoredOrigin(class_3222 holder, String key, @Nullable class_2960 layerOverride, boolean clear) {
        StoredData data = StoredDataAttachment.get(holder);
        if (data == null) {
            return false;
        }
        StoredData.StoredOrigin stored = data.getOrigin(key);
        if (stored == null) {
            return false;
        }
        class_2960 layerId = layerOverride != null ? layerOverride : stored.layer();
        boolean layerKnown = ApaceOrigins.layer(layerId) != null || SwapLayers.server().isSwapLayer(layerId);
        if (!layerKnown || ApaceOrigins.origin(stored.origin()) == null) {
            return false;
        }
        OriginTransfers.chooseOrigin(holder, layerId, stored.origin());
        if (clear) {
            data.removeOrigin(key);
            changed(holder);
        }
        return true;
    }

    public static void storeValue(class_1657 holder, String key, String value) {
        StoredDataAttachment.getOrCreate(holder).setValue(key, value);
        changed(holder);
    }

    public static boolean clear(class_1657 holder, @Nullable String key) {
        StoredData data = StoredDataAttachment.get(holder);
        if (data == null) {
            return false;
        }
        if (key == null) {
            if (data.isEmpty()) {
                return false;
            }
            data.clear();
            changed(holder);
            return true;
        }
        boolean removedOrigin = data.removeOrigin(key);
        boolean removedValue = data.removeValue(key);
        if (removedOrigin || removedValue) {
            changed(holder);
            return true;
        }
        return false;
    }

    @Nullable
    public static StoredData.StoredOrigin origin(class_1657 holder, String key) {
        StoredData data = data(holder);
        return data == null ? null : data.getOrigin(key);
    }

    @Nullable
    public static String value(class_1657 holder, String key) {
        StoredData data = data(holder);
        return data == null ? null : data.getValue(key);
    }

    public static boolean has(class_1657 holder, String key) {
        StoredData data = data(holder);
        return data != null && data.has(key);
    }

    /** {@code key} -> stored origin id or value; {@code key.name} -> the stored origin's display name. */
    @Nullable
    public static String lookup(class_1657 holder, String key) {
        StoredData data = data(holder);
        if (data == null) {
            return null;
        }
        boolean wantsName = key.endsWith(".name");
        String baseKey = wantsName ? key.substring(0, key.length() - ".name".length()) : key;
        StoredData.StoredOrigin stored = data.getOrigin(baseKey);
        if (stored != null) {
            if (!wantsName) {
                return stored.origin().toString();
            }
            Origin origin = ApaceOrigins.origin(stored.origin());
            return origin == null ? null : origin.getName().getString();
        }
        // AEA: Overgrown returned null for "<value key>.name"; a value whose own key ends in ".name" still resolves
        if (wantsName) {
            return data.getValue(key);
        }
        return data.getValue(baseKey);
    }

    /** Replaces every {@code [key]} with its stored value; null if any placeholder is unknown. */
    @Nullable
    public static String resolve(class_1657 holder, String template) {
        if (template.indexOf('[') < 0) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String replacement = lookup(holder, matcher.group(1));
            if (replacement == null) {
                return null;
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
