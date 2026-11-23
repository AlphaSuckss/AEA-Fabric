package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record CameraPerspectiveC2S(boolean firstPerson) {
   public static final class_2960 CHANNEL = Apoli.internal("camera_perspective");

   public void write(class_2540 buf) {
      buf.writeBoolean(this.firstPerson);
   }

   public static CameraPerspectiveC2S read(class_2540 buf) {
      return new CameraPerspectiveC2S(buf.readBoolean());
   }
}
