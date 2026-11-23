package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RopeDeleteS2C(int id) {
   public static final class_2960 CHANNEL = Apoli.internal("rope_delete");

   public void write(class_2540 buf) {
      buf.method_10804(this.id);
   }

   public static RopeDeleteS2C read(class_2540 buf) {
      return new RopeDeleteS2C(buf.method_10816());
   }
}
