package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record ScrollWheelC2S(boolean up, int notches) {
   public static final int MAX_NOTCHES = 16;
   public static final class_2960 CHANNEL = Apoli.internal("scroll_wheel");

   public void write(class_2540 buf) {
      buf.writeBoolean(this.up);
      buf.method_10804(this.notches);
   }

   public static ScrollWheelC2S read(class_2540 buf) {
      boolean up = buf.readBoolean();
      int notches = Math.max(1, Math.min(16, buf.method_10816()));
      return new ScrollWheelC2S(up, notches);
   }
}
