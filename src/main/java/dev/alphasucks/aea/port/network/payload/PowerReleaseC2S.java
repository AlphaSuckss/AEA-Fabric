package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record PowerReleaseC2S(class_2960 power) {
   public static final class_2960 CHANNEL = Apoli.internal("power_release");

   public void write(class_2540 buf) {
      buf.method_10812(this.power);
   }

   public static PowerReleaseC2S read(class_2540 buf) {
      return new PowerReleaseC2S(buf.method_10810());
   }
}
