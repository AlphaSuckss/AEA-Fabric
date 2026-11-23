package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RopeChangeLengthC2S(int ropeId, double delta) {
   public static final class_2960 CHANNEL = Apoli.internal("rope_change_length");

   public void write(class_2540 buf) {
      buf.method_10804(this.ropeId);
      buf.writeDouble(this.delta);
   }

   public static RopeChangeLengthC2S read(class_2540 buf) {
      return new RopeChangeLengthC2S(buf.method_10816(), buf.readDouble());
   }
}
