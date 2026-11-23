package dev.alphasucks.aea.port.network;

import dev.alphasucks.aea.port.network.payload.ProtocolVersionPayload;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_3222;

public final class ProtocolCompat {
   public static final int VERSION = 15;
   private static final Set<UUID> SENT_LEGACY = ConcurrentHashMap.newKeySet();

   private ProtocolCompat() {
   }

   public static boolean useLegacyFormats(class_3222 player) {
      return !ServerPlayNetworking.canSend(player, ProtocolVersionPayload.CHANNEL);
   }

   public static void markSentLegacy(class_3222 player) {
      SENT_LEGACY.add(player.method_5667());
   }

   public static boolean consumeSentLegacy(class_3222 player) {
      return SENT_LEGACY.remove(player.method_5667());
   }

   public static void forget(UUID player) {
      SENT_LEGACY.remove(player);
   }
}
