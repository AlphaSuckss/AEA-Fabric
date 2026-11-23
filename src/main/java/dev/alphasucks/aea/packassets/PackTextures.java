package dev.alphasucks.aea.packassets;

import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.AeaConfig;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;

/**
 * Data pack textures: PNG files under {@code data/<namespace>/textures/} are sent to every player and registered on
 * their game under {@code <namespace>:textures/...}, so a power can use them without a resource pack.
 */
public final class PackTextures {
    private PackTextures() {
    }

    public static final int MAX_FILE = 2 * 1024 * 1024;
    public static final int MAX_TOTAL = 32 * 1024 * 1024;
    public static final int MAX_COUNT = 1024;
    public static final int MAX_ID_LENGTH = 200;
    public static final int CHUNK = Outbox.CHUNK;

    private record Stored(TextureNetwork.Listed listed, byte[] data) {
    }

    private static volatile Map<class_2960, Stored> stored = Map.of();
    private static volatile int generation;
    /** Total bytes of {@link #stored}. */
    private static volatile long storedBytes;
    private static final Outbox<class_2960> OUTBOX = new Outbox<>("data pack textures", TextureNetwork.CHUNK,
        (buf, gen, id, offset, slice) -> new TextureNetwork.Chunk(gen, id, offset, slice).write(buf));

    public static void init() {
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public class_2960 getFabricId() {
                return new class_2960("aea", "data_pack_textures");
            }

            @Override
            public void method_14491(class_3300 manager) {
                load(manager);
            }
        });
        // Fires when a player joins and after /reload. An empty list tells the client to drop what it had.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> sendManifest(player));
        ServerTickEvents.END_SERVER_TICK.register(server -> OUTBOX.tick(server.method_3760().method_14571(), generation));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.method_32311().method_5667();
            server.execute(() -> OUTBOX.forget(id));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> OUTBOX.clear());
        ServerPlayNetworking.registerGlobalReceiver(TextureNetwork.REQUEST, (server, player, handler, buf, sender) -> {
            TextureNetwork.Request request = TextureNetwork.Request.read(buf);
            server.execute(() -> onRequest(player, request));
        });
    }

    /** Reads every data pack texture into memory. Runs on each data pack load. */
    private static void load(class_3300 manager) {
        Map<class_2960, Stored> found = new TreeMap<>();
        long total = 0;
        if (AeaConfig.packTextures()) {
            Map<class_2960, class_3298> resources = new TreeMap<>(manager.method_14488("textures", id -> id.method_12832().endsWith(".png")));
            for (Map.Entry<class_2960, class_3298> entry : resources.entrySet()) {
                class_2960 id = entry.getKey();
                if (id.toString().length() > MAX_ID_LENGTH) {
                    AEA.LOGGER.warn("[AEA] Data pack texture {} has a name longer than {} characters and is not sent.", id, MAX_ID_LENGTH);
                    continue;
                }
                if (found.size() >= MAX_COUNT) {
                    AEA.LOGGER.warn("[AEA] More than {} data pack textures; the rest are not sent.", MAX_COUNT);
                    break;
                }
                byte[] data;
                try (InputStream in = entry.getValue().method_14482()) {
                    data = in.readNBytes(MAX_FILE + 1);
                } catch (IOException e) {
                    AEA.LOGGER.warn("[AEA] Could not read data pack texture {}: {}", id, e.toString());
                    continue;
                }
                if (data.length == 0) {
                    continue;
                }
                if (data.length > MAX_FILE) {
                    AEA.LOGGER.warn("[AEA] Data pack texture {} is larger than {} KB and is not sent.", id, MAX_FILE / 1024);
                    continue;
                }
                if (total + data.length > MAX_TOTAL) {
                    AEA.LOGGER.warn("[AEA] Data pack textures add up to more than {} MB; {} and the rest are not sent.", MAX_TOTAL / (1024 * 1024), id);
                    break;
                }
                total += data.length;
                found.put(id, new Stored(new TextureNetwork.Listed(id, sha1(data), data.length), data));
            }
        }
        stored = found;
        storedBytes = total;
        generation++;
        if (!found.isEmpty()) {
            AEA.LOGGER.info("[AEA] Loaded {} data pack texture(s), {} KB.", found.size(), total / 1024);
        }
    }

    public static String sha1(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean hex40(String s) {
        if (s.length() != 40) {
            return false;
        }
        for (int i = 0; i < 40; i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    private static void sendManifest(class_3222 player) {
        if (player.field_13987 == null) {
            return;
        }
        List<TextureNetwork.Listed> list = new ArrayList<>(stored.size());
        for (Stored s : stored.values()) {
            list.add(s.listed());
        }
        OUTBOX.reset(player.method_5667());
        class_2540 buf = PacketByteBufs.create();
        new TextureNetwork.Manifest(generation, list).write(buf);
        ServerPlayNetworking.send(player, TextureNetwork.MANIFEST, buf);
    }

    private static void onRequest(class_3222 player, TextureNetwork.Request request) {
        if (request.generation() != generation) {
            return;
        }
        Map<class_2960, byte[]> files = new HashMap<>();
        Map<class_2960, Stored> current = stored;
        for (class_2960 id : request.ids()) {
            Stored s = current.get(id);
            if (s != null) {
                files.put(id, s.data());
            }
        }
        OUTBOX.request(player, generation, request.first(), request.ids(), files, 2 * storedBytes + CHUNK, MAX_COUNT);
    }
}
