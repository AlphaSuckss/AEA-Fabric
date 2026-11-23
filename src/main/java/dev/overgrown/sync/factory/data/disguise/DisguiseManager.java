package dev.overgrown.sync.factory.data.disguise;

import dev.overgrown.sync.networking.ModPackets;
import io.netty.buffer.Unpooled;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public class DisguiseManager {
   private static final Map<UUID, DisguiseData> DISGUISES = new ConcurrentHashMap();

   public static void applyDisguise(class_1309 actor, class_1297 target, boolean overwrite) {
      if (!actor.method_37908().method_8608()) {
         if (overwrite || !DISGUISES.containsKey(actor.method_5667())) {
            UUID playerUuid = target instanceof class_1657 ? target.method_5667() : null;
            class_2487 nbt = buildClientNbt(target);

            DisguiseData data = new DisguiseData(class_7923.field_41177.method_10221(target.method_5864()), target.method_5628(), playerUuid, nbt);
            DISGUISES.put(actor.method_5667(), data);
            syncSet(actor, data);
         }
      }
   }

   // keys of an entity's NBT the client-side disguise dummy never needs; the big ones (inventories, AI memories,
   // trades, attached components) could otherwise push the packet past the size limit
   private static final String[] CLIENT_NBT_STRIPPED_KEYS = new String[]{
      "UUID", "Pos", "Motion", "Passengers", "Leash", "Brain", "Attributes", "ActiveEffects", "Items", "Inventory", "EnderItems",
      "Offers", "Gossips", "cardinal_components", "fabric:attachments"
   };

   /** What the disguise renderer needs from the target: the name for a player, the visual NBT for anything else. */
   private static class_2487 buildClientNbt(class_1297 target) {
      class_2487 nbt = new class_2487();
      if (target instanceof class_1657) {
         nbt.method_10582("sync$player_name", target.method_5477().getString());
         return nbt;
      } else {
         target.method_5647(nbt);

         for (String key : CLIENT_NBT_STRIPPED_KEYS) {
            nbt.method_10551(key);
         }

         return nbt;
      }
   }

   public static void forceApplyDisguise(class_1309 actor, DisguiseData data) {
      if (!actor.method_37908().method_8608()) {
         DISGUISES.put(actor.method_5667(), data);
         syncSet(actor, data);
      }
   }

   public static void removeDisguise(class_1309 actor) {
      if (!actor.method_37908().method_8608()) {
         if (DISGUISES.containsKey(actor.method_5667())) {
            DISGUISES.remove(actor.method_5667());
            syncRemove(actor);
         }
      }
   }

   public static boolean isDisguised(UUID uuid) {
      return DISGUISES.containsKey(uuid);
   }

   @Nullable
   public static DisguiseData getDisguise(UUID uuid) {
      return (DisguiseData)DISGUISES.get(uuid);
   }

   public static boolean isDisguisedAs(UUID actorUuid, class_1297 target) {
      DisguiseData data = (DisguiseData)DISGUISES.get(actorUuid);
      if (data == null) {
         return false;
      } else {
         return target instanceof class_1657 && data.getTargetPlayerUuid() != null
            ? data.getTargetPlayerUuid().equals(target.method_5667())
            : data.getTargetEntityTypeId().equals(class_7923.field_41177.method_10221(target.method_5864()));
      }
   }

   public static void removePlayer(UUID uuid) {
      DISGUISES.remove(uuid);
   }

   public static Map<UUID, DisguiseData> getAllDisguises() {
      return Collections.unmodifiableMap(DISGUISES);
   }

   private static void syncSet(class_1309 actor, DisguiseData data) {
      class_2540 buf = buildSetPacket(actor.method_5628(), data);
      broadcast(actor, buf);
   }

   private static void syncRemove(class_1309 actor) {
      class_2540 buf = buildRemovePacket(actor.method_5628());
      broadcast(actor, buf);
   }

   public static class_2540 buildSetPacket(int entityNetId, DisguiseData data) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      buf.writeInt(entityNetId);
      buf.writeBoolean(true);
      data.write(buf);
      return buf;
   }

   public static class_2540 buildRemovePacket(int entityNetId) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      buf.writeInt(entityNetId);
      buf.writeBoolean(false);
      return buf;
   }

   private static void broadcast(class_1309 actor, class_2540 buf) {
      if (actor.method_37908() instanceof class_3218) {
         for (class_3222 player : PlayerLookup.tracking(actor)) {
            ServerPlayNetworking.send(player, ModPackets.DISGUISE_UPDATE, new class_2540(buf.copy()));
         }

         if (actor instanceof class_3222 self) {
            ServerPlayNetworking.send(self, ModPackets.DISGUISE_UPDATE, new class_2540(buf.copy()));
         }
      }
   }
}
