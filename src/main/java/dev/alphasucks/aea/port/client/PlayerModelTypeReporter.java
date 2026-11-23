package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.PlayerModelType;
import dev.alphasucks.aea.port.entity.PlayerModelTypes;
import dev.alphasucks.aea.port.network.payload.PlayerModelTypeC2S;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

public final class PlayerModelTypeReporter {
   @Nullable
   private static PlayerModelType lastSent;

   private PlayerModelTypeReporter() {
   }

   public static void reset() {
      lastSent = null;
   }

   public static void tick(class_310 mc) {
      if (mc.field_1724 == null) {
         lastSent = null;
      } else {
         PlayerModelType current = "slim".equals(mc.field_1724.method_3121()) ? PlayerModelType.SLIM : PlayerModelType.WIDE;
         if (current != lastSent) {
            if (ClientPlayNetworking.canSend(PlayerModelTypeC2S.CHANNEL)) {
               class_2540 buf = new class_2540(Unpooled.buffer());
               new PlayerModelTypeC2S(current).write(buf);
               ClientPlayNetworking.send(PlayerModelTypeC2S.CHANNEL, buf);
               PlayerModelTypes.set(mc.field_1724.method_5667(), current);
               lastSent = current;
            }
         }
      }
   }
}
