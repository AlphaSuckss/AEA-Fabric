package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.network.payload.MouseMovementC2S;
import dev.alphasucks.aea.port.power.builtin.ActionOnMouseMovementPower;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_1297;
import net.minecraft.class_2540;
import net.minecraft.class_310;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public final class MouseMovementWatcher {
   private static final float TURN_TO_DEGREES = 0.15F;
   private static final float EPSILON = 1.0E-4F;
   private static float yaw;
   private static float pitch;

   private MouseMovementWatcher() {
   }

   public static void onTurn(class_1297 entity, double rawYaw, double rawPitch) {
      class_310 mc = class_310.method_1551();
      if (entity == mc.field_1724) {
         yaw += (float)rawYaw * 0.15F;
         pitch += (float)rawPitch * 0.15F;
      }
   }

   public static void clientTick(class_310 mc) {
      float sentYaw = yaw;
      float sentPitch = pitch;
      yaw = 0.0F;
      pitch = 0.0F;
      if (mc.field_1724 != null && mc.field_1755 == null) {
         if (!(Math.abs(sentYaw) < 1.0E-4F) || !(Math.abs(sentPitch) < 1.0E-4F)) {
            if (ActionOnMouseMovementPower.anyWatching(mc.field_1724)) {
               if (ClientPlayNetworking.canSend(MouseMovementC2S.CHANNEL)) {
                  class_2540 buf = new class_2540(Unpooled.buffer());
                  new MouseMovementC2S(class_3532.method_15363(sentYaw, -720.0F, 720.0F), class_3532.method_15363(sentPitch, -720.0F, 720.0F)).write(buf);
                  ClientPlayNetworking.send(MouseMovementC2S.CHANNEL, buf);
               }
            }
         }
      }
   }

   public static void reset() {
      yaw = 0.0F;
      pitch = 0.0F;
   }
}
