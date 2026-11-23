package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record CameraTypeC2S(int cameraType) {
   public static final class_2960 CHANNEL = Apoli.internal("camera_type");

   public void write(class_2540 buf) {
      buf.method_10804(this.cameraType);
   }

   public static CameraTypeC2S read(class_2540 buf) {
      return new CameraTypeC2S(buf.method_10816());
   }
}
