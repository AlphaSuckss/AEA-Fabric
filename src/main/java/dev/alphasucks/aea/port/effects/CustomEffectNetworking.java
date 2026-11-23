package dev.alphasucks.aea.port.effects;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4081;
import net.minecraft.class_7923;

/**
 * AEA: Overgrown's FabricPacket records become plain buffers on {@code aea:} channels (AEA-internal protocol).
 * The sync payload also carries each effect's server raw id so the client can line its registry up with it.
 */
public final class CustomEffectNetworking {
   public static final class_2960 SYNC_CHANNEL = Apoli.internal("sync_custom_effects");
   public static final class_2960 RESPONSE_CHANNEL = Apoli.internal("custom_effect_response");

   private CustomEffectNetworking() {
   }

   public static void sync(class_3222 player, boolean isSinglePlayer) {
      if (send(player, isSinglePlayer)) {
         CustomEffectRegistry.awaitAck(player);
      }
   }

   /**
    * AEA: Overgrown only sent at join when the client had announced the channel, and otherwise waited for its own
    * protocol handshake. In 1.20.1 the client announces its channels only after the join packets, so that wait
    * delivered the effects after Apoli 2.9.2's power list (which resolves status effect ids and breaks on unknown
    * ones) and after the player's effect packets. Like Fabric's registry sync, send unconditionally at join;
    * clients without the channel ignore it.
    */
   public static void syncOnJoin(class_3222 player, boolean isSinglePlayer) {
      send(player, isSinglePlayer);
   }

   private static boolean send(class_3222 player, boolean isSinglePlayer) {
      // singleplayer host shares the server's registry, so it never needs the payload
      if (!isSinglePlayer && EffectConfig.get().enabled()) {
         class_2540 buf = PacketByteBufs.create();
         CustomEffectRegistry.payload().write(buf);
         ServerPlayNetworking.send(player, SYNC_CHANNEL, buf);
         return true;
      } else {
         return false;
      }
   }

   public static class_2540 response(boolean success) {
      class_2540 buf = PacketByteBufs.create();
      buf.writeBoolean(success);
      return buf;
   }

   public record ClientEffectData(class_2960 id, class_4081 category, int color, Optional<String> name, Optional<class_2960> icon, int rawId) {
      public ClientEffectData(class_2540 buf) {
         this(
            buf.method_10810(),
            buf.method_10818(class_4081.class),
            buf.readInt(),
            buf.method_37436(class_2540::method_19772),
            buf.method_37436(class_2540::method_10810),
            buf.method_10816()
         );
      }

      public void write(class_2540 buf) {
         buf.method_10812(this.id);
         buf.method_10817(this.category);
         buf.writeInt(this.color);
         buf.method_37435(this.name, class_2540::method_10814);
         buf.method_37435(this.icon, class_2540::method_10812);
         buf.method_10804(this.rawId);
      }

      static ClientEffectData of(CustomMobEffect effect) {
         return new ClientEffectData(
            effect.id, effect.method_18792(), effect.method_5556(), effect.name, effect.icon, class_7923.field_41174.method_10206(effect)
         );
      }

      public CustomMobEffect toEffect() {
         return new CustomMobEffect(this.id, this.category, this.color, List.of(), this.name, this.icon);
      }
   }

   public record SyncCustomEffectsPacket(List<ClientEffectData> effects) {
      public SyncCustomEffectsPacket(class_2540 buf) {
         this(buf.method_34066(ClientEffectData::new));
      }

      static SyncCustomEffectsPacket of(List<CustomMobEffect> effects) {
         List<ClientEffectData> data = new ArrayList(effects.size());

         for (CustomMobEffect effect : effects) {
            data.add(ClientEffectData.of(effect));
         }

         // AEA: in server raw-id order, which is the order the client must register them in
         data.sort(Comparator.comparingInt(ClientEffectData::rawId));
         return new SyncCustomEffectsPacket(List.copyOf(data));
      }

      public void write(class_2540 buf) {
         buf.method_34062(this.effects, (b, e) -> e.write(b));
      }
   }
}
