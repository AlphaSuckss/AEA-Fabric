package dev.overgrown.sync.factory.data.rope.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_243;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.server.MinecraftServer;

public class RopeManager {
   private static final Map<UUID, RopeState> ropes = new HashMap();

   public static void attach(class_3222 player, class_243 anchor, float maxLength, class_2960 texture) {
      UUID uuid = player.method_5667();
      double length = player.method_5829().method_1005().method_1022(anchor);
      length = Math.min(length, maxLength);
      RopeState state = new RopeState(anchor, uuid, length, maxLength, texture);
      ropes.put(uuid, state);
      if (player.method_6128()) {
         if (player.method_5715()) {
            player.method_23670();
         } else {
            state.length = class_3532.method_15350(state.length - 5.0, 1.0, maxLength);
         }
      }

      class_2540 buf = buildCreatePacket(state);

      for (class_3222 p : player.method_5682().method_3760().method_14571()) {
         ServerPlayNetworking.send(p, RopePackets.ROPE_CREATE, buf);
      }
   }

   private static class_2540 buildCreatePacket(RopeState state) {
      class_2540 buf = PacketByteBufs.create();
      buf.method_10797(state.owner);
      buf.writeDouble(state.anchor.field_1352);
      buf.writeDouble(state.anchor.field_1351);
      buf.writeDouble(state.anchor.field_1350);
      buf.writeDouble(state.length);
      buf.writeFloat(state.maxLength);
      buf.method_10812(state.texture);
      return buf;
   }

   /** Sends every existing rope to a player that joined after they were created. */
   public static void syncAllTo(class_3222 player) {
      for (RopeState state : ropes.values()) {
         ServerPlayNetworking.send(player, RopePackets.ROPE_CREATE, buildCreatePacket(state));
      }
   }

   public static void detach(class_3222 player) {
      UUID uuid = player.method_5667();
      if (ropes.remove(uuid) != null) {
         class_2540 buf = PacketByteBufs.create();
         buf.method_10797(uuid);

         for (class_3222 p : player.method_5682().method_3760().method_14571()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_DELETE, buf);
         }
      }
   }

   public static RopeState get(UUID uuid) {
      return (RopeState)ropes.get(uuid);
   }

   public static boolean has(UUID uuid) {
      return ropes.containsKey(uuid);
   }

   private static void applyLeashConstraint(class_3222 player, RopeState rope) {
      class_243 anchor = rope.anchor;
      class_243 pos = player.method_5829().method_1005();
      class_243 delta = pos.method_1020(anchor);
      double dist = delta.method_1033();
      if (!(dist <= rope.length)) {
         if (rope.anchor.field_1351 > player.method_23318()) {
            player.field_6017 = Math.max(0.0F, player.field_6017 - 1.0F);
         }

         class_243 dir = delta.method_1029();
         double excess = dist - rope.length;
         class_243 vel = player.method_18798();
         double radial = vel.method_1026(dir);
         class_243 radialVel = dir.method_1021(radial);
         class_243 tangentialVel = vel.method_1020(radialVel);
         if (radial > 0.0) {
            radialVel = radialVel.method_1021(0.85F);
         }

         if (player.method_5624() && tangentialVel.method_1033() < 0.7F) {
            tangentialVel = tangentialVel.method_1029().method_1021(tangentialVel.method_1033() * 1.08F);
         }

         player.method_18799(tangentialVel.method_1019(radialVel));
         double springScale = radial < 0.0 ? 0.65F : 1.0;
         class_243 correction = dir.method_1021(-excess * 0.1F * springScale);
         player.method_5762(correction.field_1352, correction.field_1351, correction.field_1350);
         player.field_6037 = true;
      }
   }

   public static void handleChangeLength(class_3222 player, double delta) {
      RopeState state = (RopeState)ropes.get(player.method_5667());
      if (state != null) {
         double dist = player.method_5829().method_1005().method_1020(state.anchor).method_1033();
         if (delta < 0.0 && dist < 0.95 * state.length) {
            delta *= 5.0;
         }

         state.length = class_3532.method_15350(state.length + delta, 1.0, state.maxLength);
         sendVerletLength(player);
      }
   }

   public static void handleSwing(class_3222 player, class_243 inputDir) {
      RopeState state = (RopeState)ropes.get(player.method_5667());
      if (state != null) {
         class_243 forward = class_243.method_1030(player.method_36455(), player.method_36454());
         class_243 right = forward.method_1036(new class_243(0.0, 1.0, 0.0));
         if (right.method_1027() < 1.0E-6) {
            double yawRad = Math.toRadians(player.method_36454());
            right = new class_243(Math.cos(yawRad), 0.0, Math.sin(yawRad));
         } else {
            right = right.method_1029();
         }

         class_243 localMomentum = forward.method_1021(inputDir.field_1350).method_1019(right.method_1021(inputDir.field_1352));
         player.method_5762(localMomentum.field_1352 * 0.045F, localMomentum.field_1351 * 0.045F, localMomentum.field_1350 * 0.045F);
         player.field_6037 = true;
      }
   }

   public static void tick(MinecraftServer server) {
      for (class_3222 player : server.method_3760().method_14571()) {
         RopeState rope = (RopeState)ropes.get(player.method_5667());
         if (rope != null) {
            if (player.method_6128()) {
               rope.playerFlightTicks++;
               if (rope.playerFlightTicks >= 10) {
                  detach(player);
               }
            } else {
               rope.playerFlightTicks = 0;
            }

            applyLeashConstraint(player, rope);
         }
      }
   }

   private static void sendVerletLength(class_3222 player) {
      RopeState state = (RopeState)ropes.get(player.method_5667());
      if (state != null) {
         class_2540 buf = PacketByteBufs.create();
         buf.method_10797(player.method_5667());
         buf.writeDouble(state.length);

         for (class_3222 p : player.method_5682().method_3760().method_14571()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_VERLET_LENGTH, buf);
         }
      }
   }

   public static void removeAll() {
      ropes.clear();
   }
}
