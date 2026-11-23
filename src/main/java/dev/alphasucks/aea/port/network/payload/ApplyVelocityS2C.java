package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record ApplyVelocityS2C(int entityId, double x, double y, double z, boolean set) {
   public static final class_2960 CHANNEL = Apoli.internal("apply_velocity");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeBoolean(this.set);
   }

   public static ApplyVelocityS2C read(class_2540 buf) {
      return new ApplyVelocityS2C(buf.method_10816(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
   }
}
