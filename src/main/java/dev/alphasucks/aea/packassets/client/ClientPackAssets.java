package dev.alphasucks.aea.packassets.client;

import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.packassets.AssetNetwork;
import dev.alphasucks.aea.packassets.PackAssets;
import dev.alphasucks.aea.packassets.PackTextures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3288;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_5352;
import net.minecraft.class_7699;
import org.jetbrains.annotations.Nullable;

/**
 * Client side of data pack assets. Files are kept in a cache folder under their hash, so each one is downloaded once.
 * When every file of the server's list is there, they are offered to the game as a resource pack that is always on,
 * and resources are reloaded.
 *
 * <p>Reloading is the slow part, so it only happens when the set of files changes. The pack stays loaded after the
 * player leaves, and it is remembered on disk so the next game start loads it with everything else. Joining the same
 * server again then costs nothing.
 */
@Environment(EnvType.CLIENT)
public final class ClientPackAssets {
    private ClientPackAssets() {
    }

    public static final String PACK_NAME = "aea_data_pack_assets";
    /** Resource pack format of Minecraft 1.20.1. */
    private static final int PACK_FORMAT = 15;

    /** A download in progress. */
    private static final class Incoming {
        final AssetNetwork.Listed listed;
        /** Allocated when the first chunk arrives. */
        byte[] data;
        int received;

        Incoming(AssetNetwork.Listed listed) {
            this.listed = listed;
        }
    }

    private static int generation = -1;
    /** The list as it arrives (it can span several packets). */
    private static List<AssetNetwork.Listed> arriving = new ArrayList<>();
    /** The list being downloaded, and its hash. */
    private static List<AssetNetwork.Listed> pending = List.of();
    private static String pendingHash = "";
    private static final Map<String, Incoming> INCOMING = new HashMap<>();
    /** The files of the pack the game is using now, or null when there is none. */
    private static volatile Map<class_2960, Path> active;
    private static String activeHash = "";
    /** Whether that pack replaces files of another pack. Such a pack must not outlive the server it came from. */
    private static boolean activeOverrides;
    private static boolean restored;
    /** A pack that made a resource reload fail. It is not applied again in this game session. */
    private static String rejectedHash = "";
    /** Counts ticks after joining until the server's list arrives; -1 when not waiting. */
    private static int waitingForList = -1;
    private static final int LIST_TIMEOUT_TICKS = 200;

    public static void init() {
        restore();
        ClientPlayNetworking.registerGlobalReceiver(AssetNetwork.MANIFEST, (client, handler, buf, sender) -> {
            AssetNetwork.Manifest manifest = AssetNetwork.Manifest.read(buf);
            client.execute(() -> onManifestPart(manifest));
        });
        ClientPlayNetworking.registerGlobalReceiver(AssetNetwork.CHUNK, (client, handler, buf, sender) -> {
            AssetNetwork.Chunk chunk = AssetNetwork.Chunk.read(buf);
            client.execute(() -> onChunk(chunk));
        });
        // The pack is kept when the player leaves, so there is nothing to reload. Only a download in progress stops.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            generation = -1;
            arriving = new ArrayList<>();
            pending = List.of();
            INCOMING.clear();
            waitingForList = -1;
        }));
        // A server without AEA sends no list at all. A pack that replaces files of other packs must not follow the
        // player there, so it is dropped when no list has arrived a few seconds after joining.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> waitingForList = 0));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (waitingForList >= 0 && ++waitingForList > LIST_TIMEOUT_TICKS) {
                waitingForList = -1;
                if (active != null && activeOverrides) {
                    AEA.LOGGER.info("[AEA] This server sends no data pack assets; removing the pack from the previous server.");
                    apply(null, "", false, null);
                }
            }
        });
    }

    /**
     * Called when a resource reload fails. If the server's pack is loaded it is the likely cause, and because the pack
     * is always on the game could never get rid of it by itself: drop it, forget it and do not take it again.
     */
    public static void onReloadFailed() {
        if (active == null) {
            return;
        }
        AEA.LOGGER.error("[AEA] Resources failed to reload with the server's data pack assets loaded; the pack was removed.");
        rejectedHash = activeHash;
        active = null;
        activeHash = "";
        activeOverrides = false;
        remember(null);
    }

    /** The always-on resource pack made of the downloaded files, or null when there is none. Called on every pack scan. */
    @Nullable
    public static class_3288 profile() {
        restore();
        Map<class_2960, Path> files = active;
        if (files == null) {
            return null;
        }
        return class_3288.method_14456(PACK_NAME, class_2561.method_43470("Data pack assets"), true,
            name -> new AssetPackResources(name, files),
            new class_3288.class_7679(class_2561.method_43470("Sent by the server (AEA)"), PACK_FORMAT, class_7699.method_45397()),
            class_3264.field_14188, class_3288.class_3289.field_14280, true, class_5352.field_25350);
    }

    private static Path cacheDir() {
        return FabricLoader.getInstance().getGameDir().resolve("aea-asset-cache");
    }

    private static Path cacheFile(String hash) {
        return cacheDir().resolve(hash);
    }

    /** Brings back the pack that was loaded when the game last closed, so it loads with the rest at startup. */
    private static synchronized void restore() {
        if (restored) {
            return;
        }
        restored = true;
        try {
            Path state = cacheDir().resolve("active.txt");
            if (!Files.isRegularFile(state)) {
                return;
            }
            List<String> lines = Files.readAllLines(state);
            if (lines.size() < 3 || !PackTextures.hex40(lines.get(0))) {
                return;
            }
            Map<class_2960, Path> files = new HashMap<>();
            for (String line : lines.subList(2, lines.size())) {
                int tab = line.indexOf('\t');
                if (tab < 0) {
                    return;
                }
                String path = line.substring(0, tab);
                String hash = line.substring(tab + 1);
                // A missing file means the cache was cleared: start without the pack and let the server send it again.
                if (!PackAssets.valid(path) || !PackTextures.hex40(hash) || !Files.isRegularFile(cacheFile(hash))) {
                    return;
                }
                class_2960 id = id(path);
                if (id != null) {
                    files.put(id, cacheFile(hash));
                }
            }
            active = files;
            activeHash = lines.get(0);
            activeOverrides = lines.get(1).equals("overrides");
        } catch (IOException | RuntimeException e) {
            AEA.LOGGER.warn("[AEA] Could not restore data pack assets: {}", e.toString());
        }
    }

    @Nullable
    private static class_2960 id(String path) {
        int slash = path.indexOf('/');
        return class_2960.method_43902(path.substring(0, slash), path.substring(slash + 1));
    }

    private static void remember(@Nullable List<AssetNetwork.Listed> files) {
        try {
            Path state = cacheDir().resolve("active.txt");
            if (files == null) {
                Files.deleteIfExists(state);
                return;
            }
            List<String> lines = new ArrayList<>(files.size() + 2);
            lines.add(activeHash);
            lines.add(activeOverrides ? "overrides" : "adds");
            for (AssetNetwork.Listed f : files) {
                lines.add(f.path() + "\t" + f.hash());
            }
            Files.createDirectories(state.getParent());
            Files.write(state, lines);
        } catch (IOException e) {
            AEA.LOGGER.warn("[AEA] Could not remember data pack assets: {}", e.toString());
        }
    }

    private static void onManifestPart(AssetNetwork.Manifest part) {
        if (part.first()) {
            arriving = new ArrayList<>();
        }
        if (arriving.size() + part.files().size() > PackAssets.MAX_COUNT) {
            AEA.LOGGER.warn("[AEA] The server's data pack asset list is too long; it was ignored.");
            arriving = new ArrayList<>();
            return;
        }
        arriving.addAll(part.files());
        if (part.last()) {
            List<AssetNetwork.Listed> files = arriving;
            arriving = new ArrayList<>();
            onManifest(part.generation(), part.packHash(), files);
        }
    }

    private static void onManifest(int newGeneration, String packHash, List<AssetNetwork.Listed> files) {
        waitingForList = -1;
        generation = newGeneration;
        INCOMING.clear();
        pending = List.of();
        if (files.isEmpty()) {
            // This server sends no assets. A pack left over from another server stays loaded unless it replaces
            // files of other packs, which would change how this server looks.
            if (active != null && activeOverrides) {
                apply(null, "", false, null);
            }
            return;
        }
        long total = 0;
        for (AssetNetwork.Listed f : files) {
            total += f.size();
            if (!PackAssets.valid(f.path()) || !PackTextures.hex40(f.hash()) || f.size() <= 0 || f.size() > PackAssets.MAX_FILE || total > PackAssets.MAX_TOTAL) {
                AEA.LOGGER.warn("[AEA] The server's data pack asset list is not usable ({}); it was ignored.", f.path());
                return;
            }
        }
        if (!PackTextures.hex40(packHash) || packHash.equals(activeHash)) {
            return;
        }
        if (packHash.equals(rejectedHash)) {
            AEA.LOGGER.warn("[AEA] The server's data pack assets broke a resource reload earlier and are not applied again until the game restarts.");
            return;
        }
        pending = files;
        pendingHash = packHash;
        List<String> missing = new ArrayList<>();
        java.util.Set<String> requestedHashes = new java.util.HashSet<>();
        for (AssetNetwork.Listed f : pending) {
            Path file = cacheFile(f.hash());
            try {
                if (Files.isRegularFile(file) && Files.size(file) == f.size()) {
                    continue;
                }
            } catch (IOException e) {
                // Treated as missing.
            }
            // Two paths can share one file; it is only downloaded once.
            if (requestedHashes.add(f.hash())) {
                INCOMING.put(f.path(), new Incoming(f));
                missing.add(f.path());
            }
        }
        if (missing.isEmpty()) {
            finish();
            return;
        }
        AEA.LOGGER.info("[AEA] Downloading {} data pack asset file(s) from the server.", missing.size());
        for (int from = 0; from < missing.size(); from += AssetNetwork.REQUEST_BATCH) {
            class_2540 buf = PacketByteBufs.create();
            new AssetNetwork.Request(generation, from == 0, missing.subList(from, Math.min(missing.size(), from + AssetNetwork.REQUEST_BATCH))).write(buf);
            ClientPlayNetworking.send(AssetNetwork.REQUEST, buf);
        }
    }

    private static void onChunk(AssetNetwork.Chunk chunk) {
        if (chunk.generation() != generation) {
            return;
        }
        Incoming incoming = INCOMING.get(chunk.path());
        if (incoming == null) {
            return;
        }
        byte[] slice = chunk.data();
        if (incoming.data == null) {
            incoming.data = new byte[incoming.listed.size()];
        }
        // Chunks arrive in order; anything else means the two sides disagree, so the download is abandoned.
        if (chunk.offset() != incoming.received || slice.length > incoming.data.length - incoming.received) {
            abandon("out of order");
            return;
        }
        System.arraycopy(slice, 0, incoming.data, incoming.received, slice.length);
        incoming.received += slice.length;
        if (incoming.received < incoming.data.length) {
            return;
        }
        INCOMING.remove(chunk.path());
        String hash = incoming.listed.hash();
        if (!PackTextures.sha1(incoming.data).equals(hash)) {
            abandon("damaged file " + chunk.path());
            return;
        }
        try {
            Path file = cacheFile(hash);
            Files.createDirectories(file.getParent());
            Files.write(file, incoming.data);
        } catch (IOException e) {
            abandon(e.toString());
            return;
        }
        if (INCOMING.isEmpty()) {
            finish();
        }
    }

    private static void abandon(String why) {
        AEA.LOGGER.warn("[AEA] Data pack assets were not applied: {}.", why);
        INCOMING.clear();
        pending = List.of();
    }

    /** Every file is in the cache: turn the list into a pack and reload. */
    private static void finish() {
        Map<class_2960, Path> files = new HashMap<>();
        for (AssetNetwork.Listed f : pending) {
            class_2960 id = id(f.path());
            if (id != null) {
                files.put(id, cacheFile(f.hash()));
            }
        }
        String hash = pendingHash;
        List<AssetNetwork.Listed> list = pending;
        pending = List.of();
        apply(files, hash, overridesOthers(files), list);
    }

    /**
     * Whether the pack changes anything that exists without it: a file another pack provides, or a language file
     * (translations of every pack are merged, so any language file can change any text).
     */
    private static boolean overridesOthers(Map<class_2960, Path> files) {
        class_3300 manager = class_310.method_1551().method_1478();
        for (class_2960 id : files.keySet()) {
            if (id.method_12832().startsWith("lang/")) {
                return true;
            }
            for (class_3298 resource : manager.method_14489(id)) {
                if (!PACK_NAME.equals(resource.method_14480())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void apply(@Nullable Map<class_2960, Path> files, String hash, boolean overrides, @Nullable List<AssetNetwork.Listed> list) {
        if (hash.equals(activeHash)) {
            return;
        }
        active = files;
        activeHash = hash;
        activeOverrides = overrides;
        // Forget the old pack now; the new one is only remembered once it has loaded without errors.
        remember(null);
        AEA.LOGGER.info("[AEA] {} data pack assets; reloading resources.", files == null ? "Removing" : "Applying " + files.size());
        reloadUntilApplied(hash, list, 2);
    }

    /**
     * Reloads resources and, once that has finished without errors, remembers the pack. If another reload was already
     * running it finished without this change, so the check is repeated after one more reload.
     */
    private static void reloadUntilApplied(String hash, @Nullable List<AssetNetwork.Listed> list, int attempts) {
        class_310 client = class_310.method_1551();
        client.method_1521().thenRun(() -> client.execute(() -> {
            if (!hash.equals(activeHash)) {
                return;
            }
            boolean wanted = active != null;
            boolean loaded = client.method_1520().method_29210().contains(PACK_NAME);
            if (wanted != loaded) {
                if (attempts > 1) {
                    reloadUntilApplied(hash, list, attempts - 1);
                }
            } else if (list != null) {
                remember(list);
            }
        }));
    }
}
