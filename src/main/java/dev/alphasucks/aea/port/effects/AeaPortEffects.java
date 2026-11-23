package dev.alphasucks.aea.port.effects;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.item.ItemPowerHandler;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

/**
 * AEA: the parts of Overgrown's ApoliServer/ApoliClient that wire data-driven custom mob effects and item powers.
 * {@link #init()} must run during mod initialisation, {@link #initClient()} from the client initialiser.
 */
public final class AeaPortEffects {
   private static final class_2960 AFTER_REGISTRY_SYNC = Apoli.internal("after_registry_sync");
   private static boolean initialised;
   private static boolean clientInitialised;

   private AeaPortEffects() {
   }

   public static void init() {
      if (initialised) {
         return;
      }
      initialised = true;
      ItemPowerHandler.register();

      ServerLifecycleEvents.SERVER_STARTED.register(server -> {
         if (EffectConfig.get().enabled()) {
            CustomEffectRegistry.finishReload(true);
         }
      });
      ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
         if (EffectConfig.get().enabled()) {
            CustomEffectRegistry.reloading = true;
            CustomEffectRegistry.purge(server);
         }
      });
      ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
         if (EffectConfig.get().enabled()) {
            CustomEffectRegistry.finishReload(success);
         }

         for (class_3222 player : server.method_3760().method_14571()) {
            if (ServerPlayNetworking.canSend(player, CustomEffectNetworking.SYNC_CHANNEL)) {
               CustomEffectNetworking.sync(player, server.method_19466(player.method_7334()));
            }
         }

         CustomEffectRegistry.reloading = false;
      });
      // AEA: the registry is the JVM's, not the world's; drop this server's effects when it stops
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> CustomEffectRegistry.clear());
      ServerTickEvents.END_SERVER_TICK.register(CustomEffectRegistry::tick);

      ServerPlayNetworking.registerGlobalReceiver(CustomEffectNetworking.RESPONSE_CHANNEL, (server, player, handler, buf, responseSender) -> {
         boolean success = buf.readBoolean();
         server.execute(() -> CustomEffectRegistry.acknowledge(player, success));
      });
      // AEA: after the default phase, i.e. after Fabric's registry sync remapped the client's raw ids
      ServerPlayConnectionEvents.JOIN.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_REGISTRY_SYNC);
      ServerPlayConnectionEvents.JOIN.register(
         AFTER_REGISTRY_SYNC,
         (handler, sender, server) -> CustomEffectNetworking.syncOnJoin(handler.field_14140, server.method_19466(handler.field_14140.method_7334()))
      );
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CustomEffectRegistry.waiting.remove(handler.field_14140.method_5667()));

      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> CustomEffectRegistry.dropOrphanedGrants(newPlayer));
      // AEA: powers of an effect that was removed from the data packs are saved with the entity; drop them on load
      ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
         if (entity instanceof class_1309 living) {
            CustomEffectRegistry.dropOrphanedGrants(living);
         }
      });
   }

   public static void initClient() {
      if (clientInitialised) {
         return;
      }
      clientInitialised = true;
      CustomEffectClient.init();
   }
}
