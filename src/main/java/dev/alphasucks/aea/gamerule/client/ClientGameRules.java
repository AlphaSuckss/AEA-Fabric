package dev.alphasucks.aea.gamerule.client;

import dev.alphasucks.aea.gamerule.DataGameRules;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** The game rule values the server sent, for conditions and expressions evaluated on the client. */
public final class ClientGameRules {
    private ClientGameRules() {
    }

    private static final int MAX_RULES = 4096;
    private static volatile Map<String, Integer> rules = Map.of();

    public static void init() {
        DataGameRules.setClientLookup(name -> {
            Integer value = rules.get(name);
            return value == null ? OptionalInt.empty() : OptionalInt.of(value);
        });
        ClientPlayNetworking.registerGlobalReceiver(DataGameRules.CHANNEL, (client, handler, buf, sender) -> {
            int count = buf.method_10816();
            if (count < 0 || count > MAX_RULES) {
                return;
            }
            Map<String, Integer> received = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                String name = buf.method_10800(256);
                received.put(name, buf.readInt());
            }
            rules = Map.copyOf(received);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> rules = Map.of());
    }
}
