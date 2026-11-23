package dev.overgrown.sync.factory.action.bientity.suppress_power.utils;

import dev.overgrown.sync.Sync;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1297;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

/**
 * Sends the server's suppressed power sets to clients, so powers evaluated client-side are suppressed there too.
 * {@link SuppressedPowerManager} only marks entities dirty (safe from any thread, with or without a server); the
 * packets go out at the end of the next server tick.
 */
public final class SuppressedPowerSync {
   public static final class_2960 SUPPRESSED_POWERS_UPDATE = Sync.identifier("suppressed_powers_update");
   private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();
   private static volatile boolean serverRunning = false;

   private SuppressedPowerSync() {
   }

   public static void markDirty(UUID entityId) {
      // without a running server (e.g. a call on a multiplayer client) there is nothing to send and nothing would drain it
      if (entityId != null && serverRunning) {
         DIRTY.add(entityId);
      }
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(SuppressedPowerSync::flush);
      EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
         Set<class_2960> powers = SuppressedPowerManager.getSuppressedPowers(trackedEntity.method_5667());
         if (!powers.isEmpty()) {
            ServerPlayNetworking.send(player, SUPPRESSED_POWERS_UPDATE, buildPacket(trackedEntity.method_5667(), powers));
         }
      });
      // the client drops its copy when the entity unloads (also for itself on dimension change / respawn)
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> markIfSuppressed(handler.field_14140));
      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> markIfSuppressed(player));
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> markIfSuppressed(newPlayer));
      ServerLifecycleEvents.SERVER_STARTING.register(server -> serverRunning = true);
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
         serverRunning = false;
         DIRTY.clear();
      });
   }

   private static void markIfSuppressed(class_3222 player) {
      if (!SuppressedPowerManager.getSuppressedPowers(player.method_5667()).isEmpty()) {
         markDirty(player.method_5667());
      }
   }

   private static void flush(MinecraftServer server) {
      if (!DIRTY.isEmpty()) {
         Iterator<UUID> iterator = DIRTY.iterator();

         while (iterator.hasNext()) {
            UUID id = iterator.next();
            iterator.remove();
            class_1297 entity = findEntity(server, id);
            if (entity != null) {
               send(entity);
            }
         }
      }
   }

   private static class_1297 findEntity(MinecraftServer server, UUID id) {
      class_3222 player = server.method_3760().method_14602(id);
      if (player != null) {
         return player;
      } else {
         for (class_3218 world : server.method_3738()) {
            class_1297 entity = world.method_14190(id);
            if (entity != null) {
               return entity;
            }
         }

         return null;
      }
   }

   private static void send(class_1297 entity) {
      class_2540 buf = buildPacket(entity.method_5667(), SuppressedPowerManager.getSuppressedPowers(entity.method_5667()));

      for (class_3222 player : PlayerLookup.tracking(entity)) {
         ServerPlayNetworking.send(player, SUPPRESSED_POWERS_UPDATE, new class_2540(buf.copy()));
      }

      if (entity instanceof class_3222 self) {
         ServerPlayNetworking.send(self, SUPPRESSED_POWERS_UPDATE, new class_2540(buf.copy()));
      }
   }

   private static class_2540 buildPacket(UUID entityId, Set<class_2960> powers) {
      Set<class_2960> copy = new HashSet<>(powers);
      class_2540 buf = new class_2540(Unpooled.buffer());
      buf.method_10797(entityId);
      buf.method_10804(copy.size());

      for (class_2960 id : copy) {
         buf.method_10812(id);
      }

      return buf;
   }

   /** Client side: decodes a packet into the entity id and its suppressed set. */
   public static void readPacket(class_2540 buf, java.util.function.BiConsumer<UUID, Set<class_2960>> consumer) {
      UUID entityId = buf.method_10790();
      int count = buf.method_10816();
      Set<class_2960> powers = new HashSet<>();

      for (int i = 0; i < count; i++) {
         powers.add(buf.method_10810());
      }

      consumer.accept(entityId, powers);
   }
}
