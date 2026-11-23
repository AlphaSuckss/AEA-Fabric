package dev.alphasucks.aea.port.network;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.network.payload.SyncPowersChunkS2C;
import dev.alphasucks.aea.port.network.payload.SyncPowersS2C;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

public final class PowerSyncCache {
   private static final int SLICE_BYTES = 524288;
   private static final int LEGACY_MAX_CHARS = 32767;
   private static final Set<UUID> DEFERRED = ConcurrentHashMap.newKeySet();
   private static int builtGeneration = -1;
   private static SyncPowersS2C legacyPayload;
   private static List<SyncPowersChunkS2C> chunks = List.of();

   private PowerSyncCache() {
   }

   public static void broadcast(MinecraftServer server) {
      ensureCurrent();

      for (class_3222 p : server.method_3760().method_14571()) {
         sendTo(p);
      }
   }

   public static void sendTo(class_3222 player) {
      ensureCurrent();
      if (ServerPlayNetworking.canSend(player, SyncPowersChunkS2C.CHANNEL)) {
         DEFERRED.remove(player.method_5667());
         sendChunks(player);
      } else if (legacyPayload != null) {
         DEFERRED.remove(player.method_5667());
         sendLegacy(player);
      } else {
         DEFERRED.add(player.method_5667());
      }
   }

   public static void onProtocolEcho(class_3222 player) {
      if (DEFERRED.remove(player.method_5667())) {
         ensureCurrent();
         if (ServerPlayNetworking.canSend(player, SyncPowersChunkS2C.CHANNEL)) {
            sendChunks(player);
         } else if (legacyPayload != null) {
            sendLegacy(player);
         } else {
            player.field_13987
               .method_14367(class_2561.method_43470("This server's Apoli power set is too large for your Apoli build. Update Apoli on your client."));
         }
      }
   }

   public static void forget(UUID player) {
      DEFERRED.remove(player);
   }

   private static void sendChunks(class_3222 player) {
      for (SyncPowersChunkS2C c : chunks) {
         class_2540 buf = new class_2540(Unpooled.buffer());
         c.write(buf);
         ServerPlayNetworking.send(player, SyncPowersChunkS2C.CHANNEL, buf);
      }
   }

   private static void sendLegacy(class_3222 player) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      legacyPayload.write(buf);
      ServerPlayNetworking.send(player, SyncPowersS2C.CHANNEL, buf);
   }

   private static void ensureCurrent() {
      int gen = ApoliPowers.generation();
      if (gen != builtGeneration) {
         Map<class_2960, String> raw = new HashMap();
         boolean legacyEncodable = true;

         for (Entry<class_2960, Power> e : ApoliPowers.view().entrySet()) {
            DataResult<JsonElement> encoded = Power.CODEC.encodeStart(JsonOps.INSTANCE, (Power)e.getValue());
            Optional<JsonElement> json = encoded.result();
            if (json.isEmpty()) {
               Apoli.LOGGER
                  .error(
                     "[Apoli] Power {} could not be re-encoded for client sync and will be missing on every client (it still works on the server): {}",
                     e.getKey(),
                     encoded.error().map(err -> err.message()).orElse("unknown error")
                  );
            } else {
               String s = ((JsonElement)json.get()).toString();
               raw.put((class_2960)e.getKey(), s);
               if (s.length() > 32767) {
                  legacyEncodable = false;
               }
            }
         }

         byte[] gz = SyncPowersChunkS2C.encodeBlob(raw);
         int total = (gz.length + 524288 - 1) / 524288;
         List<SyncPowersChunkS2C> built = new ArrayList(total);

         for (int i = 0; i < total; i++) {
            int from = i * 524288;
            built.add(new SyncPowersChunkS2C(i, total, Arrays.copyOfRange(gz, from, Math.min(gz.length, from + 524288))));
         }

         chunks = List.copyOf(built);
         legacyPayload = legacyEncodable ? new SyncPowersS2C(raw) : null;
         builtGeneration = gen;
         if (!legacyEncodable) {
            Apoli.LOGGER
               .info(
                  "[Apoli] Power sync exceeds the legacy packet format ({} power(s), {} gz slice(s)); clients on pre-chunk Apoli builds will be asked to update.",
                  raw.size(),
                  total
               );
         }
      }
   }
}
