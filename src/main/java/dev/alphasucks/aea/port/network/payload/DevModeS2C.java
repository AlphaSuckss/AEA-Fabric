package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record DevModeS2C(boolean enabled) {
   public static final class_2960 CHANNEL = Apoli.internal("dev_mode");

   public void write(class_2540 buf) {
      buf.writeBoolean(this.enabled);
   }

   public static DevModeS2C read(class_2540 buf) {
      return new DevModeS2C(buf.readBoolean());
   }
}
