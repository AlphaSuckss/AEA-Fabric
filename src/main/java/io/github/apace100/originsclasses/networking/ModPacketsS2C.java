package io.github.apace100.originsclasses.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Init;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.class_2540;
import net.minecraft.class_310;
import net.minecraft.class_634;

public class ModPacketsS2C {
   public static boolean isWanderingTrader;
   public static boolean isMultiMining;

   public static void register() {
      ClientPlayConnectionEvents.INIT.register((Init)(clientPlayNetworkHandler, minecraftClient) -> {
         // AEA: don't carry the last server's trader / multi-mine flags into the next connection
         isWanderingTrader = false;
         isMultiMining = false;
         ClientPlayNetworking.registerReceiver(ModPackets.TRADER_TYPE, ModPacketsS2C::receiveTraderType);
         ClientPlayNetworking.registerReceiver(ModPackets.MULTI_MINING, ModPacketsS2C::receiveMultiMine);
      });
   }

   @Environment(EnvType.CLIENT)
   private static void receiveTraderType(class_310 minecraftClient, class_634 clientPlayNetworkHandler, class_2540 packetByteBuf, PacketSender packetSender) {
      boolean isWandering = packetByteBuf.readBoolean();
      minecraftClient.execute(() -> isWanderingTrader = isWandering);
   }

   @Environment(EnvType.CLIENT)
   private static void receiveMultiMine(class_310 minecraftClient, class_634 clientPlayNetworkHandler, class_2540 packetByteBuf, PacketSender packetSender) {
      boolean isMulti = packetByteBuf.readBoolean();
      minecraftClient.execute(() -> isMultiMining = isMulti);
   }
}
