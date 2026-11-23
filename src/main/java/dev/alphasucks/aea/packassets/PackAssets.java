package dev.alphasucks.aea.packassets;

import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.AeaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
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
import net.minecraft.class_3288;
import net.minecraft.class_3300;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;

/**
 * Data pack assets: the {@code assets/} folder of every data pack in the world's {@code datapacks} folder is sent to
 * players and loaded on their game as a resource pack. Unlike data pack textures this reloads the player's resources,
 * so it also covers models, sounds, fonts, language files and animated textures.
 */
public final class PackAssets {
    private PackAssets() {
    }

    public static final int MAX_FILE = 16 * 1024 * 1024;
    public static final int MAX_TOTAL = 64 * 1024 * 1024;
    public static final int MAX_COUNT = 4096;
    public static final int CHUNK = Outbox.CHUNK;

    private record Stored(AssetNetwork.Listed listed, byte[] data) {
    }

    private static volatile Map<String, Stored> stored = Map.of();
    private static volatile String packHash = "";
    private static volatile int generation;
    /** Set when data packs reload. The files are read again the next time a list is sent. */
    private static volatile boolean stale = true;
    /** Total bytes of {@link #stored}. */
    private static volatile long storedBytes;
    private static final Outbox<String> OUTBOX = new Outbox<>("data pack assets", AssetNetwork.CHUNK,
        (buf, gen, path, offset, slice) -> new AssetNetwork.Chunk(gen, path, offset, slice).write(buf));

    public static void init() {
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public class_2960 getFabricId() {
                return new class_2960("aea", "data_pack_assets");
            }

            @Override
            public void method_14491(class_3300 manager) {
                stale = true;
            }
        });
        // Fires when a player joins and after /reload. An empty list tells the client to drop what it had.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> sendManifest(player));
        ServerTickEvents.END_SERVER_TICK.register(server -> OUTBOX.tick(server.method_3760().method_14571(), generation));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.method_32311().method_5667();
            server.execute(() -> OUTBOX.forget(id));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            OUTBOX.clear();
            stored = Map.of();
            stale = true;
        });
        ServerPlayNetworking.registerGlobalReceiver(AssetNetwork.REQUEST, (server, player, handler, buf, sender) -> {
            AssetNetwork.Request request = AssetNetwork.Request.read(buf);
            server.execute(() -> onRequest(player, request));
        });
    }

    /** Reads the {@code assets/} folder of every enabled data pack in the world's {@code datapacks} folder. */
    private static void load(MinecraftServer server) {
        Map<String, Stored> found = new TreeMap<>();
        if (AeaConfig.packAssets()) {
            Path packs = server.method_27050(class_5218.field_24186);
            // Later packs win, as they do for data.
            for (class_3288 pack : server.method_3836().method_14444()) {
                String name = pack.method_14463();
                if (!name.startsWith("file/")) {
                    continue;
                }
                try {
                    Path root = packs.resolve(name.substring("file/".length()));
                    if (Files.isDirectory(root)) {
                        collect(root.resolve("assets"), found);
                    } else if (Files.isRegularFile(root)) {
                        try (FileSystem zip = FileSystems.newFileSystem(root)) {
                            collect(zip.getPath("assets"), found);
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    AEA.LOGGER.warn("[AEA] Could not read the assets of data pack {}: {}", name, e.toString());
                }
            }
        }
        long total = 0;
        StringBuilder all = new StringBuilder();
        for (Stored s : found.values()) {
            total += s.data().length;
            all.append(s.listed().path()).append('\0').append(s.listed().hash()).append('\n');
        }
        stored = found;
        storedBytes = total;
        packHash = found.isEmpty() ? "" : PackTextures.sha1(all.toString().getBytes(StandardCharsets.UTF_8));
        generation++;
        if (!found.isEmpty()) {
            AEA.LOGGER.info("[AEA] Loaded {} data pack asset file(s), {} KB.", found.size(), total / 1024);
        }
    }

    private static void collect(Path assets, Map<String, Stored> out) throws IOException {
        if (!Files.isDirectory(assets)) {
            return;
        }
        List<Path> files;
        try (Stream<Path> walk = Files.walk(assets)) {
            files = walk.filter(Files::isRegularFile).sorted().toList();
        }
        long total = 0;
        for (Stored s : out.values()) {
            total += s.data().length;
        }
        for (Path file : files) {
            String path = assets.relativize(file).toString().replace('\\', '/');
            if (!valid(path)) {
                AEA.LOGGER.warn("[AEA] Data pack asset {} has a name the game cannot use and is not sent.", path);
                continue;
            }
            long size = Files.size(file);
            if (size == 0) {
                continue;
            }
            if (size > MAX_FILE) {
                AEA.LOGGER.warn("[AEA] Data pack asset {} is larger than {} MB and is not sent.", path, MAX_FILE / (1024 * 1024));
                continue;
            }
            Stored previous = out.get(path);
            long without = total - (previous == null ? 0 : previous.data().length);
            if (previous == null && out.size() >= MAX_COUNT) {
                AEA.LOGGER.warn("[AEA] More than {} data pack asset files; {} and the rest are not sent.", MAX_COUNT, path);
                return;
            }
            if (without + size > MAX_TOTAL) {
                AEA.LOGGER.warn("[AEA] Data pack assets add up to more than {} MB; {} and the rest are not sent.", MAX_TOTAL / (1024 * 1024), path);
                return;
            }
            byte[] data = Files.readAllBytes(file);
            total = without + data.length;
            out.put(path, new Stored(new AssetNetwork.Listed(path, PackTextures.sha1(data), data.length), data));
        }
    }

    /** {@code path} is {@code <namespace>/<path inside the namespace>}, both made of characters an ID allows. */
    public static boolean valid(String path) {
        int slash = path.indexOf('/');
        return slash > 0 && slash < path.length() - 1 && path.length() <= AssetNetwork.MAX_PATH
            && class_2960.method_43902(path.substring(0, slash), path.substring(slash + 1)) != null;
    }

    private static void sendManifest(class_3222 player) {
        MinecraftServer server = player.method_5682();
        if (server == null || player.field_13987 == null) {
            return;
        }
        if (stale) {
            stale = false;
            load(server);
        }
        List<AssetNetwork.Listed> list = new ArrayList<>(stored.size());
        for (Stored s : stored.values()) {
            list.add(s.listed());
        }
        OUTBOX.reset(player.method_5667());
        int from = 0;
        do {
            int to = Math.min(list.size(), from + AssetNetwork.MANIFEST_BATCH);
            class_2540 buf = PacketByteBufs.create();
            new AssetNetwork.Manifest(generation, packHash, from == 0, to == list.size(), list.subList(from, to)).write(buf);
            ServerPlayNetworking.send(player, AssetNetwork.MANIFEST, buf);
            from = to;
        } while (from < list.size());
    }

    private static void onRequest(class_3222 player, AssetNetwork.Request request) {
        if (request.generation() != generation) {
            return;
        }
        Map<String, byte[]> files = new HashMap<>();
        Map<String, Stored> current = stored;
        for (String path : request.paths()) {
            Stored s = current.get(path);
            if (s != null) {
                files.put(path, s.data());
            }
        }
        OUTBOX.request(player, generation, request.first(), request.paths(), files, 2 * storedBytes + CHUNK, MAX_COUNT);
    }
}
