package dev.alphasucks.aea.packassets.client;

import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.packassets.PackTextures;
import dev.alphasucks.aea.packassets.TextureNetwork;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1047;
import net.minecraft.class_156;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/**
 * Client side of data pack textures. Files the server lists are taken from a disk cache when the same file was
 * downloaded before, and requested otherwise. A texture a resource pack already provides is left to the resource pack.
 */
@Environment(EnvType.CLIENT)
public final class ClientPackTextures {
    private ClientPackTextures() {
    }

    private static final int MAX_DIMENSION = 4096;
    /** Most pixels all registered textures may add up to (about 190 MB of video memory). */
    private static final long MAX_PIXELS = 48L * 1024 * 1024;
    private static long pixels;
    private static final Map<class_2960, Long> PIXELS_OF = new HashMap<>();

    /** A download in progress. */
    private static final class Incoming {
        final TextureNetwork.Listed listed;
        /** Allocated when the first chunk arrives. */
        byte[] data;
        int received;

        Incoming(TextureNetwork.Listed listed) {
            this.listed = listed;
        }
    }

    private static int generation = -1;
    /** Textures this class registered, by id, with the hash of the file each came from. */
    private static final Map<class_2960, String> REGISTERED = new HashMap<>();
    private static final Map<class_2960, String> WANTED = new HashMap<>();
    private static final Map<class_2960, Incoming> INCOMING = new HashMap<>();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(TextureNetwork.MANIFEST, (client, handler, buf, sender) -> {
            TextureNetwork.Manifest manifest = TextureNetwork.Manifest.read(buf);
            client.execute(() -> onManifest(manifest));
        });
        ClientPlayNetworking.registerGlobalReceiver(TextureNetwork.CHUNK, (client, handler, buf, sender) -> {
            TextureNetwork.Chunk chunk = TextureNetwork.Chunk.read(buf);
            client.execute(() -> onChunk(chunk));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(ClientPackTextures::clear));
    }

    private static Path cacheFile(String hash) {
        return FabricLoader.getInstance().getGameDir().resolve("aea-texture-cache").resolve(hash + ".png");
    }

    private static boolean acceptable(TextureNetwork.Listed t) {
        String path = t.id().method_12832();
        return path.startsWith("textures/") && path.endsWith(".png") && t.size() > 0 && t.size() <= PackTextures.MAX_FILE
            && t.id().toString().length() <= PackTextures.MAX_ID_LENGTH && PackTextures.hex40(t.hash());
    }

    private static void onManifest(TextureNetwork.Manifest manifest) {
        class_310 mc = class_310.method_1551();
        generation = manifest.generation();
        INCOMING.clear();
        WANTED.clear();
        long total = 0;
        for (TextureNetwork.Listed t : manifest.textures()) {
            if (!acceptable(t)) {
                continue;
            }
            if (total + t.size() > PackTextures.MAX_TOTAL) {
                AEA.LOGGER.warn("[AEA] The server lists more than {} MB of data pack textures; the rest are ignored.", PackTextures.MAX_TOTAL / (1024 * 1024));
                break;
            }
            total += t.size();
            WANTED.put(t.id(), t.hash());
        }
        // Drop what the server no longer lists, or lists with different contents.
        for (class_2960 id : new ArrayList<>(REGISTERED.keySet())) {
            if (!REGISTERED.get(id).equals(WANTED.get(id))) {
                release(id);
            }
        }
        List<class_2960> missing = new ArrayList<>();
        for (TextureNetwork.Listed t : manifest.textures()) {
            if (!WANTED.containsKey(t.id()) || REGISTERED.containsKey(t.id())) {
                continue;
            }
            if (mc.method_1478().method_14486(t.id()).isPresent() || takenByGame(t.id())) {
                continue;
            }
            byte[] cached = readCache(t);
            if (cached != null) {
                decode(t.id(), t.hash(), cached);
            } else {
                INCOMING.put(t.id(), new Incoming(t));
                missing.add(t.id());
            }
        }
        for (int from = 0; from < missing.size(); from += TextureNetwork.REQUEST_BATCH) {
            class_2540 buf = PacketByteBufs.create();
            new TextureNetwork.Request(generation, from == 0, missing.subList(from, Math.min(missing.size(), from + TextureNetwork.REQUEST_BATCH))).write(buf);
            ClientPlayNetworking.send(TextureNetwork.REQUEST, buf);
        }
    }

    private static byte[] readCache(TextureNetwork.Listed t) {
        Path file = cacheFile(t.hash());
        try {
            if (!Files.isRegularFile(file) || Files.size(file) != t.size()) {
                return null;
            }
            byte[] data = Files.readAllBytes(file);
            return PackTextures.sha1(data).equals(t.hash()) ? data : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void onChunk(TextureNetwork.Chunk chunk) {
        if (chunk.generation() != generation) {
            return;
        }
        Incoming incoming = INCOMING.get(chunk.id());
        if (incoming == null) {
            return;
        }
        byte[] slice = chunk.data();
        if (incoming.data == null) {
            incoming.data = new byte[incoming.listed.size()];
        }
        // Chunks arrive in order; anything else means the two sides disagree, so the file is abandoned.
        if (chunk.offset() != incoming.received || slice.length > incoming.data.length - incoming.received) {
            INCOMING.remove(chunk.id());
            return;
        }
        System.arraycopy(slice, 0, incoming.data, incoming.received, slice.length);
        incoming.received += slice.length;
        if (incoming.received < incoming.data.length) {
            return;
        }
        INCOMING.remove(chunk.id());
        String hash = incoming.listed.hash();
        if (!PackTextures.sha1(incoming.data).equals(hash)) {
            AEA.LOGGER.warn("[AEA] Data pack texture {} arrived damaged and was dropped.", chunk.id());
            return;
        }
        byte[] data = incoming.data;
        class_156.method_27958().execute(() -> {
            try {
                Path file = cacheFile(hash);
                Files.createDirectories(file.getParent());
                Files.write(file, data);
            } catch (IOException e) {
                AEA.LOGGER.warn("[AEA] Could not cache data pack texture {}: {}", chunk.id(), e.toString());
            }
        });
        decode(chunk.id(), hash, data);
    }

    /** Width and height from the PNG header, or null when the file is not a PNG. */
    private static int[] pngSize(byte[] data) {
        if (data.length < 24 || (data[0] & 0xFF) != 0x89 || data[1] != 'P' || data[2] != 'N' || data[3] != 'G') {
            return null;
        }
        int width = ((data[16] & 0xFF) << 24) | ((data[17] & 0xFF) << 16) | ((data[18] & 0xFF) << 8) | (data[19] & 0xFF);
        int height = ((data[20] & 0xFF) << 24) | ((data[21] & 0xFF) << 16) | ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
        return new int[]{width, height};
    }

    /** Decodes off the main thread, then registers the texture on it. */
    private static void decode(class_2960 id, String hash, byte[] data) {
        int[] size = pngSize(data);
        if (size == null || size[0] < 1 || size[1] < 1 || size[0] > MAX_DIMENSION || size[1] > MAX_DIMENSION) {
            AEA.LOGGER.warn("[AEA] Data pack texture {} is not a PNG of at most {}x{} and was skipped.", id, MAX_DIMENSION, MAX_DIMENSION);
            return;
        }
        long area = (long) size[0] * size[1];
        class_156.method_18349().execute(() -> {
            class_1011 image;
            try {
                image = class_1011.method_4309(new ByteArrayInputStream(data));
            } catch (IOException | RuntimeException e) {
                AEA.LOGGER.warn("[AEA] Could not decode data pack texture {}: {}", id, e.toString());
                return;
            }
            class_310.method_1551().execute(() -> {
                // The server may have reloaded, or the player left, while this was decoding.
                if (!hash.equals(WANTED.get(id)) || takenByGame(id)) {
                    image.close();
                    return;
                }
                if (pixels + area > MAX_PIXELS) {
                    image.close();
                    AEA.LOGGER.warn("[AEA] Data pack textures use too much video memory; {} was skipped.", id);
                    return;
                }
                if (REGISTERED.containsKey(id)) {
                    release(id);
                }
                class_310.method_1551().method_1531().method_4616(id, new class_1043(image));
                REGISTERED.put(id, hash);
                PIXELS_OF.put(id, area);
                pixels += area;
            });
        });
    }

    /**
     * Whether the game already has a texture of its own under this id (a block atlas, a font page, ...). Those are
     * never replaced. A "missing texture" placeholder left by an earlier failed lookup does not count.
     */
    private static boolean takenByGame(class_2960 id) {
        if (REGISTERED.containsKey(id)) {
            return false;
        }
        class_1044 existing = class_310.method_1551().method_1531().method_34590(id, null);
        return existing != null && existing != class_1047.method_4540();
    }

    private static void release(class_2960 id) {
        Long area = PIXELS_OF.remove(id);
        if (area != null) {
            pixels -= area;
        }
        REGISTERED.remove(id);
        class_310.method_1551().method_1531().method_4615(id);
    }

    private static void clear() {
        generation = -1;
        WANTED.clear();
        INCOMING.clear();
        for (class_2960 id : new ArrayList<>(REGISTERED.keySet())) {
            release(id);
        }
    }
}
