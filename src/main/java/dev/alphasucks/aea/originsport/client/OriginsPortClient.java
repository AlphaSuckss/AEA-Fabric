package dev.alphasucks.aea.originsport.client;

import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.origin.OriginTags;
import dev.alphasucks.aea.originsport.storage.StoredClientState;
import dev.alphasucks.aea.originsport.storage.StoredData;
import dev.alphasucks.aea.originsport.swap.SwapClientState;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.port.client.ApoliKeyMappings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Client half: state mirrors, the swap screen and the "Swap Origin" key (V, as in Overgrown). */
@Environment(EnvType.CLIENT)
public final class OriginsPortClient {
    public static class_304 swapOriginKeybind;

    private OriginsPortClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.SYNC_TOPOLOGY, (client, handler, buf, sender) -> {
            SwapLayers.Topology topology = SwapLayers.Topology.read(buf);
            client.execute(() -> SwapLayers.acceptClient(topology));
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.SYNC_SWAPS, (client, handler, buf, sender) -> {
            UUID subject = buf.method_10790();
            Map<class_2960, class_2960> swaps = OriginsPortNetwork.readSwapMap(buf);
            Map<class_2960, List<class_2960>> pools = OriginsPortNetwork.readIdListMap(buf);
            client.execute(() -> {
                SwapClientState.set(subject, swaps, pools);
                refreshScreen(client, subject);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.SYNC_AVAILABLE, (client, handler, buf, sender) -> {
            Map<class_2960, List<class_2960>> available = OriginsPortNetwork.readIdListMap(buf);
            client.execute(() -> {
                if (client.field_1724 != null) {
                    SwapClientState.setAvailable(client.field_1724.method_5667(), available);
                    refreshScreen(client, client.field_1724.method_5667());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.OPEN_SWAP_SCREEN, (client, handler, buf, sender) -> {
            class_2960 layerId = buf.method_10810();
            client.execute(() -> client.method_1507(new SwapOriginScreen(layerId)));
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.SYNC_TAGS, (client, handler, buf, sender) -> {
            Map<class_2960, List<String>> tags = OriginsPortNetwork.readStringListMap(buf);
            client.execute(() -> OriginTags.acceptClient(tags));
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginsPortNetwork.SYNC_STORED, (client, handler, buf, sender) -> {
            UUID subject = buf.method_10790();
            StoredData data = StoredData.read(buf);
            client.execute(() -> StoredClientState.set(subject, data));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SwapLayers.clearClient();
            SwapClientState.clear();
            StoredClientState.clear();
            OriginTags.clearClient();
        });

        // AEA: unbound by default - V is Simple Voice Chat's menu key (Overgrown defaulted to V)
        swapOriginKeybind = KeyBindingHelper.registerKeyBinding(new class_304("key.origins.swap", class_3675.class_307.field_1668, -1, "category.origins"));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.field_1724 != null && ApoliKeyMappings.consumePress(swapOriginKeybind, false)
                && ClientPlayNetworking.canSend(OriginsPortNetwork.SWAP_CYCLE)) {
                class_2540 buf = PacketByteBufs.create();
                buf.writeBoolean(class_437.method_25442());
                ClientPlayNetworking.send(OriginsPortNetwork.SWAP_CYCLE, buf);
            }
        });
    }

    private static void refreshScreen(class_310 client, UUID subject) {
        if (client.field_1724 != null && client.field_1724.method_5667().equals(subject) && client.field_1755 instanceof SwapOriginScreen screen) {
            screen.refresh();
        }
    }
}
