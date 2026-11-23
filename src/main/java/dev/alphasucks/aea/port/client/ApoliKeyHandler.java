package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.network.payload.PowerActivationC2S;
import dev.alphasucks.aea.port.network.payload.PowerToggleC2S;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import dev.alphasucks.aea.port.power.builtin.TogglePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower.Config;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;

public final class ApoliKeyHandler {
   private ApoliKeyHandler() {
   }

   public static void onClientTick() {
      ClientPowerState.tickCooldowns();
      ApoliKeyMappings.beginTick();
      BlockedKeys.tick();

      for (class_2960 powerId : ClientPowerState.localPowers()) {
         Power power = ApoliPowers.get(powerId);
         if (power != null) {
            PowerType<?> type = PowerTypeRegistry.get(power.typeId());
            if (type instanceof ActionOnKeyPressPower) {
               if (power.config() instanceof Config cfg) {
                  class_304 km = ApoliKeyMappings.resolve(cfg.key().key());
                  if (km != null && ApoliKeyMappings.consumePress(km, cfg.key().continuous()) && ClientPowerState.getCooldown(powerId) <= 0) {
                     class_2540 buf = new class_2540(Unpooled.buffer());
                     new PowerActivationC2S(powerId).write(buf);
                     ClientPlayNetworking.send(PowerActivationC2S.CHANNEL, buf);
                     if (cfg.releases()) {
                        KeyReleases.pressed(powerId, km);
                     }

                     ScreenCloseKeys.expect(km);
                  }
               }
            } else if (type instanceof FireProjectilePower) {
               if (power.config() instanceof dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config cfg) {
                  Key fpKey = (Key)cfg.params().key().orElse(Key.DEFAULT_PRIMARY);
                  class_304 km = ApoliKeyMappings.resolve(fpKey.key());
                  if (km != null && ApoliKeyMappings.consumePress(km, fpKey.continuous()) && ClientPowerState.getCooldown(powerId) <= 0) {
                     class_2540 buf = new class_2540(Unpooled.buffer());
                     new PowerActivationC2S(powerId).write(buf);
                     ClientPlayNetworking.send(PowerActivationC2S.CHANNEL, buf);
                  }
               }
            } else if (type instanceof InventoryPower) {
               if (power.config() instanceof dev.alphasucks.aea.port.power.builtin.InventoryPower.Config cfg) {
                  class_304 km = ApoliKeyMappings.resolve(cfg.key().key());
                  if (km != null && ApoliKeyMappings.consumePress(km, cfg.key().continuous())) {
                     class_2540 buf = new class_2540(Unpooled.buffer());
                     new PowerActivationC2S(powerId).write(buf);
                     ClientPlayNetworking.send(PowerActivationC2S.CHANNEL, buf);
                     ScreenCloseKeys.expect(km);
                  }
               }
            } else if (type instanceof TogglePower && power.config() instanceof dev.alphasucks.aea.port.power.builtin.TogglePower.Config cfg) {
               class_304 km = ApoliKeyMappings.resolve(cfg.key().key());
               if (km != null && ApoliKeyMappings.consumePress(km, false)) {
                  class_2540 buf = new class_2540(Unpooled.buffer());
                  new PowerToggleC2S(powerId).write(buf);
                  ClientPlayNetworking.send(PowerToggleC2S.CHANNEL, buf);
               }
            }
         }
      }

      KeyReleases.tick();
      KeyPressWatcher.tick();
      ScreenCloseKeys.tick(class_310.method_1551());
   }
}
