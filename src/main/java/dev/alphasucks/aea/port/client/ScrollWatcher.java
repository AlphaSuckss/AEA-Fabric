package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.ScrollDirection;
import dev.alphasucks.aea.port.network.payload.ScrollWheelC2S;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.ActionOnScrollWheelPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnScrollWheelPower.Config;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class ScrollWatcher {
   private ScrollWatcher() {
   }

   public static boolean onScroll(double delta) {
      if (delta == 0.0) {
         return false;
      }

      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1755 != null) {
         return false;
      }

      if (BlockedKeys.blocksScroll()) {
         return true;
      }

      ScrollDirection notch = ScrollDirection.ofDelta(delta);
      if (!ActionOnScrollWheelPower.anyAccepting(mc.field_1724, notch)) {
         return false;
      }

      boolean[] prevent = new boolean[]{false};
      PowerLookup.forEach(mc.field_1724, ApoliIds.ACTION_ON_SCROLL_WHEEL, Config.class, cfg -> {
         if (cfg.preventHotbarChange() && cfg.direction().accepts(notch)) {
            prevent[0] = true;
         }
      });
      if (ClientPlayNetworking.canSend(ScrollWheelC2S.CHANNEL)) {
         class_2540 buf = new class_2540(Unpooled.buffer());
         new ScrollWheelC2S(notch == ScrollDirection.UP, 1).write(buf);
         ClientPlayNetworking.send(ScrollWheelC2S.CHANNEL, buf);
      }

      return prevent[0];
   }
}
