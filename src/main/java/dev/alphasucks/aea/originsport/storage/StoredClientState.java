package dev.alphasucks.aea.originsport.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AEA: client mirror of players' stored origins/values ({@code aea:origin_stored_sync}). Overgrown
 * never synced storage, so stored_origin / stored_value were always false in client worlds.
 */
public final class StoredClientState {
    private static final Map<UUID, StoredData> DATA = new ConcurrentHashMap<>();

    private StoredClientState() {
    }

    public static void set(UUID player, StoredData data) {
        if (data.isEmpty()) {
            DATA.remove(player);
        } else {
            DATA.put(player, data);
        }
    }

    @Nullable
    public static StoredData get(UUID player) {
        return DATA.get(player);
    }

    public static void clear() {
        DATA.clear();
    }
}
