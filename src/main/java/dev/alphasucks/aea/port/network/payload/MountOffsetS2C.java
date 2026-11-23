package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.mount.MountRotation;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record MountOffsetS2C(int passengerId, double x, double y, double z, Space space, MountRotation rotation) {
   public static final class_2960 CHANNEL = Apoli.internal("mount_offset");

   public void write(class_2540 buf) {
      buf.method_10804(this.passengerId);
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.method_10817(this.space);
      buf.method_10817(this.rotation);
   }

   public static MountOffsetS2C read(class_2540 buf) {
      int passengerId = buf.method_10816();
      double x = buf.readDouble();
      double y = buf.readDouble();
      double z = buf.readDouble();
      Space space = buf.method_10818(Space.class);
      MountRotation rotation = buf.isReadable() ? buf.method_10818(MountRotation.class) : MountRotation.HEAD;
      return new MountOffsetS2C(passengerId, x, y, z, space, rotation);
   }
}
