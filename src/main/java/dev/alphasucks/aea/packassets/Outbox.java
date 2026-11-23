package dev.alphasucks.aea.packassets;

import dev.alphasucks.aea.AEA;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7648;

/**
 * Files on their way to players, in 30 KB chunks. A player is only sent more once the chunks already handed to the
 * connection have been written, so a slow connection is never flooded and other packets are not held up. What one
 * player may ask for per list is capped, so asking again and again cannot be used to make the server send forever.
 */
final class Outbox<K> {
    interface ChunkWriter<K> {
        void write(class_2540 buf, int generation, K key, int offset, byte[] slice);
    }

    static final int CHUNK = 30 * 1024;
    /** Chunks that may be waiting in one player's connection at a time. */
    private static final int WINDOW = 8;

    private static final class Transfer<K> {
        final K key;
        final byte[] data;
        int offset;

        Transfer(K key, byte[] data) {
            this.key = key;
            this.data = data;
        }
    }

    private static final class State<K> {
        final ArrayDeque<Transfer<K>> queue = new ArrayDeque<>();
        final AtomicInteger inFlight = new AtomicInteger();
        int generation = Integer.MIN_VALUE;
        long served;
        boolean warned;
    }

    private final String what;
    private final class_2960 channel;
    private final ChunkWriter<K> writer;
    private final Map<UUID, State<K>> states = new HashMap<>();

    Outbox(String what, class_2960 channel, ChunkWriter<K> writer) {
        this.what = what;
        this.channel = channel;
        this.writer = writer;
    }

    /** A new list is being sent to this player: whatever was queued for them is stale. */
    void reset(UUID player) {
        State<K> state = states.get(player);
        if (state != null) {
            state.queue.clear();
        }
    }

    void forget(UUID player) {
        states.remove(player);
    }

    void clear() {
        states.clear();
    }

    /**
     * Queues files for a player. {@code budget} is the most bytes one player may be sent for one generation of the
     * list (twice the size of everything, so a failed download can be retried).
     */
    void request(class_3222 player, int generation, boolean first, List<K> keys, Map<K, byte[]> files, long budget, int maxQueued) {
        State<K> state = states.computeIfAbsent(player.method_5667(), id -> new State<>());
        if (state.generation != generation) {
            state.generation = generation;
            state.served = 0;
            state.warned = false;
            state.queue.clear();
        }
        if (first) {
            state.queue.clear();
        }
        for (K key : keys) {
            byte[] data = files.get(key);
            if (data == null) {
                continue;
            }
            if (state.queue.size() >= maxQueued || state.served + data.length > budget) {
                if (!state.warned) {
                    state.warned = true;
                    AEA.LOGGER.warn("[AEA] {} asked for more {} than the server has; the extra requests are ignored.",
                        player.method_5477().getString(), what);
                }
                return;
            }
            state.served += data.length;
            state.queue.add(new Transfer<>(key, data));
        }
    }

    void tick(List<class_3222> players, int generation) {
        if (states.isEmpty()) {
            return;
        }
        for (int i = 0; i < players.size(); i++) {
            class_3222 player = players.get(i);
            State<K> state = states.get(player.method_5667());
            if (state == null || state.queue.isEmpty() || player.field_13987 == null) {
                continue;
            }
            if (state.generation != generation) {
                state.queue.clear();
                continue;
            }
            AtomicInteger inFlight = state.inFlight;
            while (inFlight.get() < WINDOW && !state.queue.isEmpty()) {
                Transfer<K> transfer = state.queue.peek();
                int length = Math.min(CHUNK, transfer.data.length - transfer.offset);
                byte[] slice = Arrays.copyOfRange(transfer.data, transfer.offset, transfer.offset + length);
                class_2540 buf = PacketByteBufs.create();
                writer.write(buf, generation, transfer.key, transfer.offset, slice);
                inFlight.incrementAndGet();
                // Runs on the network thread once the chunk has been written (or the connection has failed).
                ServerPlayNetworking.getSender(player).sendPacket(channel, buf, class_7648.method_45084(inFlight::decrementAndGet));
                transfer.offset += length;
                if (transfer.offset >= transfer.data.length) {
                    state.queue.poll();
                }
            }
        }
    }
}
