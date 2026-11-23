package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3532;

public record MouseMovementC2S(float yaw, float pitch) {
   public static final float MAX_DEGREES = 720.0F;
   public static final class_2960 CHANNEL = Apoli.internal("mouse_movement");

   public void write(class_2540 buf) {
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
   }

   public static MouseMovementC2S read(class_2540 buf) {
      return new MouseMovementC2S(clamp(buf.readFloat()), clamp(buf.readFloat()));
   }

   private static float clamp(float value) {
      return !Float.isFinite(value) ? 0.0F : class_3532.method_15363(value, -720.0F, 720.0F);
   }
}
