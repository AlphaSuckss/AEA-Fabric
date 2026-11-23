package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.entity.CameraPerspectives;
import dev.alphasucks.aea.port.network.payload.CameraPerspectiveC2S;
import dev.alphasucks.aea.port.network.payload.CameraTypeC2S;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_310;
import net.minecraft.class_5498;

public final class CameraPerspectiveReporter {
   private static int lastSent = -1;

   private CameraPerspectiveReporter() {
   }

   public static void tick(class_310 mc) {
      if (mc.field_1724 == null) {
         lastSent = -1;
      } else {
         class_5498 type = mc.field_1690.method_31044();
         int current = type.method_31034() ? 0 : (type.method_31035() ? 2 : 1);
         if (lastSent != current) {
            class_2540 buf = new class_2540(Unpooled.buffer());
            if (ClientPlayNetworking.canSend(CameraTypeC2S.CHANNEL)) {
               new CameraTypeC2S(current).write(buf);
               ClientPlayNetworking.send(CameraTypeC2S.CHANNEL, buf);
            } else {
               if (!ClientPlayNetworking.canSend(CameraPerspectiveC2S.CHANNEL)) {
                  return;
               }

               new CameraPerspectiveC2S(current == 0).write(buf);
               ClientPlayNetworking.send(CameraPerspectiveC2S.CHANNEL, buf);
            }

            CameraPerspectives.setType(mc.field_1724.method_5667(), current);
            lastSent = current;
         }
      }
   }

   public static void reset() {
      lastSent = -1;
   }
}
