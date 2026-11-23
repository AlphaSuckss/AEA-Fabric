package dev.alphasucks.aea.port.effects;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.effects.CustomEffectNetworking.SyncCustomEffectsPacket;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client half of the custom effect sync (Overgrown's {@code client.SyncCustomEffectRegistry} plus AEA cleanup). */
@Environment(EnvType.CLIENT)
public final class CustomEffectClient {
   private CustomEffectClient() {
   }

   public static void init() {
      ClientPlayNetworking.registerGlobalReceiver(CustomEffectNetworking.SYNC_CHANNEL, (client, handler, buf, sender) -> {
         SyncCustomEffectsPacket packet = new SyncCustomEffectsPacket(buf);
         boolean success;
         try {
            // AEA: like Fabric's registry sync, hold the network thread until the effects are registered, so the
            // effect packets that follow are decoded against them
            success = client.method_5385(() -> apply(packet)).get(30, TimeUnit.SECONDS);
         } catch (Exception e) {
            Apoli.LOGGER.error("[Apoli] Could not apply the server's custom effects", e);
            success = false;
         }

         sender.sendPacket(CustomEffectNetworking.RESPONSE_CHANNEL, CustomEffectNetworking.response(success));
      });
      // AEA: a remote server's effects must not outlive the connection (Fabric's registry unmap does not know them)
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
         if (client.method_1576() == null) {
            CustomEffectRegistry.clear();
         }
      }));
   }

   private static boolean apply(SyncCustomEffectsPacket packet) {
      try {
         return CustomEffectRegistry.installFromServer(packet.effects());
      } catch (RuntimeException e) {
         Apoli.LOGGER.error("[Apoli] Could not apply the server's custom effects", e);
         return false;
      }
   }
}
