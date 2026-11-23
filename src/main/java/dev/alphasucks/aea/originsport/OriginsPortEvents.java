package dev.alphasucks.aea.originsport;

import dev.alphasucks.aea.originsport.command.StorageCommands;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

/** Server-side lifecycle for the swap / storage / tag systems (Overgrown's OriginsServerEvents swap parts). */
public final class OriginsPortEvents {
    private OriginsPortEvents() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> StorageCommands.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            class_3222 player = handler.field_14140;
            try {
                OriginsPortNetwork.sendTopology(player);
                OriginsPortNetwork.sendTags(player);
                SwapManager.forgetAvailable(player);
                // suppression is in memory only (dropped on disconnect): re-apply swaps
                SwapManager.reconcileAll(player);
                SwapManager.revalidate(player);
                OriginsPortNetwork.sendSwapsTo(player, player);
                OriginsPortNetwork.sendStoredTo(player, player);
                for (class_3222 other : server.method_3760().method_14571()) {
                    if (other != player) {
                        OriginsPortNetwork.sendSwapsTo(player, other);
                        OriginsPortNetwork.sendSwapsTo(other, player);
                        OriginsPortNetwork.sendStoredTo(player, other);
                        OriginsPortNetwork.sendStoredTo(other, player);
                    }
                }
            } catch (RuntimeException e) {
                OriginsPortIds.LOGGER.error("Origin swap/storage sync for {} failed", player.method_5477().getString(), e);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SwapManager.forgetAvailable(handler.field_14140));

        // Sync drops every suppression on death; the swap's hidden main-origin powers must come back
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            try {
                SwapManager.reconcileAll(newPlayer);
            } catch (RuntimeException e) {
                OriginsPortIds.LOGGER.error("Re-applying origin swaps for {} after respawn failed", newPlayer.method_5477().getString(), e);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(SwapManager::tickPools);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> SwapLayers.rebuildServer());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            SwapLayers.clearServer();
            SwapManager.clearServer();
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                afterReload(server);
            }
        });
    }

    private static void afterReload(MinecraftServer server) {
        SwapLayers.rebuildServer();
        OriginsPortNetwork.broadcastTopology(server);
        OriginsPortNetwork.broadcastTags(server);
        for (class_3222 player : server.method_3760().method_14571()) {
            try {
                SwapManager.forgetAvailable(player);
                SwapManager.revalidate(player);
                // origins may have gained/lost powers
                SwapManager.reconcileAll(player);
                OriginsPortNetwork.broadcastSwaps(server, player);
            } catch (RuntimeException e) {
                OriginsPortIds.LOGGER.error("Re-applying origin swaps for {} after reload failed", player.method_5477().getString(), e);
            }
        }
    }
}
