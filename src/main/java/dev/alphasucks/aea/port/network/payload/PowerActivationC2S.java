package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record PowerActivationC2S(class_2960 power) {
   public static final class_2960 CHANNEL = Apoli.internal("power_activation");

   public void write(class_2540 buf) {
      buf.method_10812(this.power);
   }

   public static PowerActivationC2S read(class_2540 buf) {
      return new PowerActivationC2S(buf.method_10810());
   }
}
