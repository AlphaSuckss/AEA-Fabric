package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RopeVerletLengthS2C(int id, double length) {
   public static final class_2960 CHANNEL = Apoli.internal("rope_verlet_length");

   public void write(class_2540 buf) {
      buf.method_10804(this.id);
      buf.writeDouble(this.length);
   }

   public static RopeVerletLengthS2C read(class_2540 buf) {
      return new RopeVerletLengthS2C(buf.method_10816(), buf.readDouble());
   }
}
