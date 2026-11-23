package dev.overgrown.sync.factory.data.rope.common;

import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_243;

public class RopeInit {
   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(RopeManager::tick);
      ServerPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_CHANGE_LENGTH, (server, player, handler, buf, responseSender) -> {
         UUID uuid = buf.method_10790();
         double delta = buf.readDouble();
         server.execute(() -> {
            if (player.method_5667().equals(uuid) && Double.isFinite(delta)) {
               RopeManager.handleChangeLength(player, delta);
            }
         });
      });
      ServerPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_SWING, (server, player, handler, buf, responseSender) -> {
         double x = buf.readDouble();
         double y = buf.readDouble();
         double z = buf.readDouble();
         class_243 rawDir = new class_243(x, y, z);
         if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
            // client input: clamp to unit length (the client already normalizes diagonal input)
            class_243 inputDir = rawDir.method_1027() > 1.0 ? rawDir.method_1029() : rawDir;
            server.execute(() -> RopeManager.handleSwing(player, inputDir));
         }
      });
      // ropes are anchored to world coordinates and keyed by player: drop them when the player leaves the world
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RopeManager.detach(handler.field_14140));
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> RopeManager.detach(newPlayer));
      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> RopeManager.detach(player));
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> RopeManager.removeAll());
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> RopeManager.syncAllTo(handler.field_14140)));
   }
}
