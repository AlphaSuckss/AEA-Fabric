package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record ScaleSyncS2C(int entityId, byte[] data) {
   public static final class_2960 CHANNEL = Apoli.internal("scale_sync");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10813(this.data);
   }

   public static ScaleSyncS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      return new ScaleSyncS2C(entityId, buf.method_10803(8192));
   }
}
