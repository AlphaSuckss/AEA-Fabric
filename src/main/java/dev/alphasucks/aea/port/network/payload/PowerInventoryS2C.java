package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record PowerInventoryS2C(class_2960 powerId, class_2487 contents) {
   public static final class_2960 CHANNEL = Apoli.internal("power_inventory");

   public void write(class_2540 buf) {
      buf.method_10812(this.powerId);
      buf.method_10794(this.contents);
   }

   public static PowerInventoryS2C read(class_2540 buf) {
      return new PowerInventoryS2C(buf.method_10810(), buf.method_10798());
   }
}
