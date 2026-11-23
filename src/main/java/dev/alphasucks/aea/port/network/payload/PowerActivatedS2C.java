package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record PowerActivatedS2C(class_2960 power, int cooldown) {
   public static final class_2960 CHANNEL = Apoli.internal("power_activated");

   public void write(class_2540 buf) {
      buf.method_10812(this.power);
      buf.method_10804(this.cooldown);
   }

   public static PowerActivatedS2C read(class_2540 buf) {
      return new PowerActivatedS2C(buf.method_10810(), buf.method_10816());
   }
}
