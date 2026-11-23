package dev.alphasucks.aea.port;

import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.network.PowerSyncCache;
import dev.alphasucks.aea.port.network.ProtocolCompat;
import dev.alphasucks.aea.port.network.payload.AnchoredParticlesS2C;
import dev.alphasucks.aea.port.network.payload.ApplyVelocityS2C;
import dev.alphasucks.aea.port.network.payload.DevModeS2C;
import dev.alphasucks.aea.port.network.payload.DisguiseUpdateS2C;
import dev.alphasucks.aea.port.network.payload.ForceKeyS2C;
import dev.alphasucks.aea.port.network.payload.LabelUpdateS2C;
import dev.alphasucks.aea.port.network.payload.MountOffsetS2C;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.network.payload.PowerInventoryS2C;
import dev.alphasucks.aea.port.network.payload.ProtocolVersionPayload;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C;
import dev.alphasucks.aea.port.network.payload.RopeCreateS2C;
import dev.alphasucks.aea.port.network.payload.RopeDeleteS2C;
import dev.alphasucks.aea.port.network.payload.RopeVerletLengthS2C;
import dev.alphasucks.aea.port.network.payload.ScaleSyncS2C;
import dev.alphasucks.aea.port.network.payload.SetPerspectiveS2C;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C;
import dev.alphasucks.aea.port.network.payload.SkillStateSyncS2C;
import dev.alphasucks.aea.port.network.payload.SyncAuxIntsS2C;
import dev.alphasucks.aea.port.network.payload.SyncEntityPowersS2C;
import dev.alphasucks.aea.port.network.payload.SyncEntitySetsS2C;
import dev.alphasucks.aea.port.network.payload.SyncKeybindsS2C;
import dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C;
import dev.alphasucks.aea.port.network.payload.SyncResourceTablesS2C;
import dev.alphasucks.aea.port.network.payload.SyncShaderS2C;
import dev.alphasucks.aea.port.network.payload.SyncTrackedEntitySetS2C;
import dev.alphasucks.aea.port.network.payload.TextDisplayS2C;
import dev.alphasucks.aea.port.network.payload.TickRateS2C;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C.Entry;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import dev.alphasucks.aea.port.radial.RadialMenuManager;
import dev.alphasucks.aea.port.skill.SkillData;
import dev.alphasucks.aea.port.skill.SkillDataAttachment;
import dev.alphasucks.aea.port.skill.SkillRegistry;
import dev.alphasucks.aea.port.skill.SkillTree;
import dev.alphasucks.aea.port.skill.SkillTrees;
import dev.alphasucks.aea.port.skill.SkillTrees.Visibility;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1297;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

public final class ApoliNetwork {
   private ApoliNetwork() {
   }

   public static boolean connected(class_3222 recipient) {
      return recipient != null && recipient.field_13987 != null;
   }

   public static void sendPowers(class_3222 recipient) {
      if (connected(recipient)) {
         PowerSyncCache.sendTo(recipient);
         InventoryPower.syncAll(recipient);
      }
   }

   public static void sendPowerInventory(class_3222 recipient, PowerInventoryS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, PowerInventoryS2C.CHANNEL)) {
         send(recipient, PowerInventoryS2C.CHANNEL, payload::write);
      }
   }

   public static void sendMountOffset(class_3222 recipient, MountOffsetS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, MountOffsetS2C.CHANNEL)) {
         send(recipient, MountOffsetS2C.CHANNEL, payload::write);
      }
   }

   public static void broadcastMountOffset(class_1297 passenger, MountOffsetS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(passenger)) {
         sendMountOffset(viewer, payload);
      }

      if (passenger instanceof class_3222 self) {
         sendMountOffset(self, payload);
      }
   }

   public static boolean canSendAnchoredParticles(class_3222 recipient) {
      return connected(recipient) && ServerPlayNetworking.canSend(recipient, AnchoredParticlesS2C.CHANNEL);
   }

   public static void sendAnchoredParticles(class_3222 recipient, AnchoredParticlesS2C payload) {
      send(recipient, AnchoredParticlesS2C.CHANNEL, payload::write);
   }

   public static void sendResourceTables(class_3222 recipient, SyncResourceTablesS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, SyncResourceTablesS2C.CHANNEL)) {
         send(recipient, SyncResourceTablesS2C.CHANNEL, payload::write);
      }
   }

   public static boolean canSendAuxInts(class_3222 recipient) {
      return connected(recipient) && ServerPlayNetworking.canSend(recipient, SyncAuxIntsS2C.CHANNEL);
   }

   public static void sendAuxInts(class_3222 recipient, SyncAuxIntsS2C payload) {
      send(recipient, SyncAuxIntsS2C.CHANNEL, payload::write);
   }

   public static void sendDevMode(class_3222 recipient, DevModeS2C payload) {
      send(recipient, DevModeS2C.CHANNEL, payload::write);
   }

   public static void sendEntitySets(class_3222 recipient, SyncEntitySetsS2C payload) {
      send(recipient, SyncEntitySetsS2C.CHANNEL, payload::write);
   }

   public static void sendTrackedEntitySet(class_3222 recipient, SyncTrackedEntitySetS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, SyncTrackedEntitySetS2C.CHANNEL)) {
         send(recipient, SyncTrackedEntitySetS2C.CHANNEL, payload::write);
      }
   }

   public static void sendEntitySetToTrackers(class_1297 owner, SyncTrackedEntitySetS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(owner)) {
         if (viewer != owner) {
            sendTrackedEntitySet(viewer, payload);
         }
      }
   }

   public static void sendEntityPowers(class_3222 recipient, SyncEntityPowersS2C payload) {
      send(recipient, SyncEntityPowersS2C.CHANNEL, payload::write);
   }

   public static void sendActivated(class_3222 recipient, PowerActivatedS2C payload) {
      send(recipient, PowerActivatedS2C.CHANNEL, payload::write);
   }

   public static void sendKeybinds(class_3222 recipient, SyncKeybindsS2C payload) {
      send(recipient, SyncKeybindsS2C.CHANNEL, payload::write);
   }

   public static void sendScale(class_3222 recipient, ScaleSyncS2C payload) {
      send(recipient, ScaleSyncS2C.CHANNEL, payload::write);
   }

   public static void broadcastScale(class_1297 entity, ScaleSyncS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(entity)) {
         sendScale(viewer, payload);
      }

      if (entity instanceof class_3222 self) {
         sendScale(self, payload);
      }
   }

   public static void broadcastTickRate(MinecraftServer server, TickRateS2C payload) {
      broadcast(server, TickRateS2C.CHANNEL, payload::write);
   }

   public static void sendTickRateToTrackers(class_1297 entity, TickRateS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(entity)) {
         send(viewer, TickRateS2C.CHANNEL, payload::write);
      }

      if (entity instanceof class_3222 self) {
         send(self, TickRateS2C.CHANNEL, payload::write);
      }
   }

   public static void sendApplyVelocityToTrackers(class_1297 entity, ApplyVelocityS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(entity)) {
         send(viewer, ApplyVelocityS2C.CHANNEL, payload::write);
      }

      if (entity instanceof class_3222 self) {
         send(self, ApplyVelocityS2C.CHANNEL, payload::write);
      }
   }

   public static void broadcastDisguise(class_1297 entity, DisguiseUpdateS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(entity)) {
         send(viewer, DisguiseUpdateS2C.CHANNEL, payload::write);
      }

      if (entity instanceof class_3222 self) {
         send(self, DisguiseUpdateS2C.CHANNEL, payload::write);
      }
   }

   public static void sendDisguise(class_3222 recipient, DisguiseUpdateS2C payload) {
      send(recipient, DisguiseUpdateS2C.CHANNEL, payload::write);
   }

   public static void sendTextDisplay(class_3222 recipient, TextDisplayS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, TextDisplayS2C.CHANNEL)) {
         send(recipient, TextDisplayS2C.CHANNEL, payload::write);
      }
   }

   public static void sendForceKey(class_3222 recipient, ForceKeyS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, ForceKeyS2C.CHANNEL)) {
         send(recipient, ForceKeyS2C.CHANNEL, payload::write);
      }
   }

   public static void sendResolvedPowers(class_3222 recipient, SyncResolvedPowersS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, SyncResolvedPowersS2C.CHANNEL)) {
         send(recipient, SyncResolvedPowersS2C.CHANNEL, payload::write);
      }
   }

   public static void sendPerspective(class_3222 recipient, SetPerspectiveS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, SetPerspectiveS2C.CHANNEL)) {
         send(recipient, SetPerspectiveS2C.CHANNEL, payload::write);
      }
   }

   public static void sendShader(class_3222 recipient, SyncShaderS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, SyncShaderS2C.CHANNEL)) {
         send(recipient, SyncShaderS2C.CHANNEL, payload::write);
      }
   }

   public static void broadcastLabel(class_1297 entity, LabelUpdateS2C payload) {
      for (class_3222 viewer : PlayerLookup.tracking(entity)) {
         sendLabel(viewer, payload);
      }

      if (entity instanceof class_3222 self) {
         sendLabel(self, payload);
      }
   }

   public static void sendLabel(class_3222 recipient, LabelUpdateS2C payload) {
      if (connected(recipient) && ServerPlayNetworking.canSend(recipient, LabelUpdateS2C.CHANNEL)) {
         send(recipient, LabelUpdateS2C.CHANNEL, payload::write);
      }
   }

   public static void sendSkillDefs(class_3222 recipient) {
      boolean legacy = ProtocolCompat.useLegacyFormats(recipient);
      if (legacy) {
         ProtocolCompat.markSentLegacy(recipient);
      }

      send(recipient, SkillDefsSyncS2C.CHANNEL, SkillDefsSyncS2C.fromCurrent(legacy)::write);
   }

   public static void sendSkillState(class_3222 recipient) {
      if (connected(recipient)) {
         SkillData data = SkillDataAttachment.get(recipient);
         Visibility vis = SkillTrees.computeVisibility(recipient);
         boolean legacy = ProtocolCompat.useLegacyFormats(recipient);
         if (legacy) {
            ProtocolCompat.markSentLegacy(recipient);
         }

         Set<class_2960> nonRefundable = new HashSet();

         for (SkillTree tree : SkillRegistry.trees()) {
            if (!tree.refundable()) {
               nonRefundable.add(tree.id());
            }
         }

         SkillStateSyncS2C payload = new SkillStateSyncS2C(
            new HashMap(data.pointsView()), new HashSet(data.purchasedView()), vis.hidden(), vis.locked(), nonRefundable, legacy
         );
         send(recipient, SkillStateSyncS2C.CHANNEL, payload::write);
      }
   }

   public static void sendProtocolVersion(class_3222 recipient) {
      send(recipient, ProtocolVersionPayload.CHANNEL, new ProtocolVersionPayload(15)::write);
   }

   public static void broadcastRopeCreate(class_3218 level, RopeCreateS2C payload) {
      for (class_3222 p : level.method_18456()) {
         send(p, RopeCreateS2C.CHANNEL, payload::write);
      }
   }

   public static void sendRopeCreate(class_3222 recipient, RopeCreateS2C payload) {
      send(recipient, RopeCreateS2C.CHANNEL, payload::write);
   }

   public static void broadcastRopeDelete(class_3218 level, RopeDeleteS2C payload) {
      for (class_3222 p : level.method_18456()) {
         send(p, RopeDeleteS2C.CHANNEL, payload::write);
      }
   }

   public static void broadcastRopeVerletLength(class_3218 level, RopeVerletLengthS2C payload) {
      for (class_3222 p : level.method_18456()) {
         send(p, RopeVerletLengthS2C.CHANNEL, payload::write);
      }
   }

   public static void broadcastPowers(MinecraftServer server) {
      PowerSyncCache.broadcast(server);
   }

   public static void broadcastKeybinds(MinecraftServer server, SyncKeybindsS2C payload) {
      broadcast(server, SyncKeybindsS2C.CHANNEL, payload::write);
   }

   public static void openRadialMenu(class_3222 player, Optional<class_2960> sprite, List<Entry> display, List<EntityAction> actions) {
      if (connected(player)) {
         if (ServerPlayNetworking.canSend(player, RadialMenuOpenS2C.CHANNEL)) {
            int nonce = RadialMenuManager.open(player, actions);
            RadialMenuOpenS2C payload = new RadialMenuOpenS2C(nonce, sprite, display);
            send(player, RadialMenuOpenS2C.CHANNEL, payload::write);
         }
      }
   }

   private static void send(class_3222 recipient, class_2960 channel, Consumer<class_2540> writer) {
      if (connected(recipient)) {
         class_2540 buf = new class_2540(Unpooled.buffer());
         writer.accept(buf);
         ServerPlayNetworking.send(recipient, channel, buf);
      }
   }

   private static void broadcast(MinecraftServer server, class_2960 channel, Consumer<class_2540> writer) {
      class_2540 prototype = new class_2540(Unpooled.buffer());
      writer.accept(prototype);

      for (class_3222 p : server.method_3760().method_14571()) {
         ServerPlayNetworking.send(p, channel, new class_2540(prototype.copy()));
      }
   }
}
