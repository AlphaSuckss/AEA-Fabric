package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record ForceKeyS2C(String key, int duration, boolean release) {
   public static final class_2960 CHANNEL = Apoli.internal("force_key");

   public void write(class_2540 buf) {
      buf.method_10814(this.key);
      buf.method_10804(this.duration);
      buf.writeBoolean(this.release);
   }

   public static ForceKeyS2C read(class_2540 buf) {
      String key = buf.method_19772();
      int duration = buf.method_10816();
      boolean release = buf.readBoolean();
      return new ForceKeyS2C(key, duration, release);
   }
}
