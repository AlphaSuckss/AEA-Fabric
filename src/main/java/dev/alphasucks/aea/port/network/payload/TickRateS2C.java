package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record TickRateS2C(int entityId, int rate, int baseRate) {
   public static final class_2960 CHANNEL = Apoli.internal("tick_rate");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10804(this.rate + 1);
      buf.method_10804(this.baseRate);
   }

   public static TickRateS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      int rate = buf.method_10816() - 1;
      int baseRate = Math.max(1, buf.method_10816());
      return new TickRateS2C(entityId, rate, baseRate);
   }
}
