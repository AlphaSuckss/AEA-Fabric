package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_243;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RopeSwingC2S(int ropeId, class_243 inputDir) {
   public static final class_2960 CHANNEL = Apoli.internal("rope_swing");

   public void write(class_2540 buf) {
      buf.method_10804(this.ropeId);
      buf.writeDouble(this.inputDir.field_1352);
      buf.writeDouble(this.inputDir.field_1351);
      buf.writeDouble(this.inputDir.field_1350);
   }

   public static RopeSwingC2S read(class_2540 buf) {
      return new RopeSwingC2S(buf.method_10816(), new class_243(buf.readDouble(), buf.readDouble(), buf.readDouble()));
   }
}
