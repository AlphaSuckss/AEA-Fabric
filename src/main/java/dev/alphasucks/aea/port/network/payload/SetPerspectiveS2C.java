package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SetPerspectiveS2C(int perspective) {
   public static final class_2960 CHANNEL = Apoli.internal("set_perspective");

   public void write(class_2540 buf) {
      buf.method_10804(this.perspective);
   }

   public static SetPerspectiveS2C read(class_2540 buf) {
      return new SetPerspectiveS2C(buf.method_10816());
   }
}
