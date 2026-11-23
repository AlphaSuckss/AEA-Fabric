package dev.alphasucks.aea.originsport.network;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.origin.OriginTags;
import dev.alphasucks.aea.originsport.storage.StoredData;
import dev.alphasucks.aea.originsport.storage.StoredDataAttachment;
import dev.alphasucks.aea.originsport.swap.SwapData;
import dev.alphasucks.aea.originsport.swap.SwapDataAttachment;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server half of the swap / storage / tag networking (Overgrown's OriginsServerNetwork swap parts).
 * All channels live in the {@code aea} namespace so they never collide with apace's {@code origins:}
 * channels. Packet layouts are shared with the client receivers through the read helpers here.
 */
public final class OriginsPortNetwork {
    /** S2C: swap layer configs, their targets, the default target. */
    public static final class_2960 SYNC_TOPOLOGY = OriginsPortIds.aea("origin_swap_topology");
    /** S2C: subject uuid, active swaps, granted pools. */
    public static final class_2960 SYNC_SWAPS = OriginsPortIds.aea("origin_swap_sync");
    /** S2C (own player): swap layer -> origins its conditions currently offer. */
    public static final class_2960 SYNC_AVAILABLE = OriginsPortIds.aea("origin_swap_available");
    /** S2C: target layer id. */
    public static final class_2960 OPEN_SWAP_SCREEN = OriginsPortIds.aea("origin_swap_open");
    /** S2C: origin id -> tags (the {@code tags} field of origin files, for origin_tag in client worlds). */
    public static final class_2960 SYNC_TAGS = OriginsPortIds.aea("origin_tags_sync");
    /** S2C: subject uuid, stored origins and values. */
    public static final class_2960 SYNC_STORED = OriginsPortIds.aea("origin_stored_sync");
    /** C2S: shift held (return to the main origin). */
    public static final class_2960 SWAP_CYCLE = OriginsPortIds.aea("origin_swap_cycle");
    /** C2S: target layer id, back-to-main flag, origin id. */
    public static final class_2960 SWAP_SELECT = OriginsPortIds.aea("origin_swap_select");

    private OriginsPortNetwork() {
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(SWAP_CYCLE, (server, player, handler, buf, sender) -> {
            boolean toMain = buf.readBoolean();
            server.execute(() -> {
                class_2960 target = SwapManager.resolveTarget(null);
                if (target != null) {
                    SwapManager.cycle(player, target, toMain);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SWAP_SELECT, (server, player, handler, buf, sender) -> {
            class_2960 layerId = buf.method_10810();
            boolean toMain = buf.readBoolean();
            class_2960 originId = buf.method_10810();
            server.execute(() -> {
                class_2960 target = SwapManager.resolveTarget(layerId);
                if (target != null) {
                    // applySwap only accepts origins from the player's pool (or the main origin)
                    SwapManager.applySwap(player, target, toMain ? null : originId);
                }
            });
        });
    }

    // ------------------------------------------------------------------ topology

    public static void sendTopology(class_3222 player) {
        class_2540 buf = PacketByteBufs.create();
        SwapLayers.server().write(buf);
        ServerPlayNetworking.send(player, SYNC_TOPOLOGY, buf);
    }

    public static void broadcastTopology(MinecraftServer server) {
        for (class_3222 player : server.method_3760().method_14571()) {
            sendTopology(player);
        }
    }

    // ------------------------------------------------------------------ swaps

    public static void sendSwapsTo(class_3222 recipient, class_3222 subject) {
        SwapData data = SwapDataAttachment.get(subject);
        class_2540 buf = PacketByteBufs.create();
        buf.method_10797(subject.method_5667());
        writeSwaps(buf, data == null ? Map.of() : data.swapSnapshot(), data == null ? Map.of() : data.poolSnapshot());
        ServerPlayNetworking.send(recipient, SYNC_SWAPS, buf);
    }

    public static void broadcastSwaps(MinecraftServer server, class_3222 subject) {
        for (class_3222 recipient : server.method_3760().method_14571()) {
            sendSwapsTo(recipient, subject);
        }
    }

    public static void writeSwaps(class_2540 buf, Map<class_2960, class_2960> swaps, Map<class_2960, List<class_2960>> pools) {
        buf.method_10804(swaps.size());
        swaps.forEach((layer, origin) -> {
            buf.method_10812(layer);
            buf.method_10812(origin);
        });
        writeIdLists(buf, pools);
    }

    public static Map<class_2960, class_2960> readSwapMap(class_2540 buf) {
        int n = buf.method_10816();
        Map<class_2960, class_2960> swaps = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            swaps.put(buf.method_10810(), buf.method_10810());
        }
        return swaps;
    }

    public static void sendAvailable(class_3222 player, Map<class_2960, List<class_2960>> available) {
        class_2540 buf = PacketByteBufs.create();
        writeIdLists(buf, available);
        ServerPlayNetworking.send(player, SYNC_AVAILABLE, buf);
    }

    // ------------------------------------------------------------------ screen

    public static void openSwapScreen(class_3222 player, class_2960 targetLayerId) {
        // the client's condition-gated options may be up to 10 ticks old; refresh them first
        sendAvailable(player, SwapManager.available(player));
        class_2540 buf = PacketByteBufs.create();
        buf.method_10812(targetLayerId);
        ServerPlayNetworking.send(player, OPEN_SWAP_SCREEN, buf);
    }

    // ------------------------------------------------------------------ tags

    public static void sendTags(class_3222 player) {
        class_2540 buf = PacketByteBufs.create();
        writeIdLists(buf, OriginTags.serverSnapshotForSync());
        ServerPlayNetworking.send(player, SYNC_TAGS, buf);
    }

    public static void broadcastTags(MinecraftServer server) {
        for (class_3222 player : server.method_3760().method_14571()) {
            sendTags(player);
        }
    }

    // ------------------------------------------------------------------ storage

    public static void sendStoredTo(class_3222 recipient, class_3222 subject) {
        StoredData data = StoredDataAttachment.get(subject);
        class_2540 buf = PacketByteBufs.create();
        buf.method_10797(subject.method_5667());
        (data == null ? new StoredData() : data).write(buf);
        ServerPlayNetworking.send(recipient, SYNC_STORED, buf);
    }

    public static void broadcastStored(MinecraftServer server, class_3222 subject) {
        for (class_3222 recipient : server.method_3760().method_14571()) {
            sendStoredTo(recipient, subject);
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Map of id -> list, where the list elements are written as strings (ids or free-form tags). */
    private static void writeIdLists(class_2540 buf, Map<class_2960, ? extends List<?>> map) {
        buf.method_10804(map.size());
        map.forEach((key, values) -> {
            buf.method_10812(key);
            buf.method_10804(values.size());
            for (Object value : values) {
                buf.method_10814(value.toString());
            }
        });
    }

    public static Map<class_2960, List<class_2960>> readIdListMap(class_2540 buf) {
        int n = buf.method_10816();
        Map<class_2960, List<class_2960>> out = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            class_2960 key = buf.method_10810();
            int size = buf.method_10816();
            List<class_2960> ids = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                class_2960 id = class_2960.method_12829(buf.method_19772());
                if (id != null) {
                    ids.add(id);
                }
            }
            out.put(key, ids);
        }
        return out;
    }

    public static Map<class_2960, List<String>> readStringListMap(class_2540 buf) {
        int n = buf.method_10816();
        Map<class_2960, List<String>> out = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            class_2960 key = buf.method_10810();
            int size = buf.method_10816();
            List<String> values = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                values.add(buf.method_19772());
            }
            out.put(key, values);
        }
        return out;
    }
}
