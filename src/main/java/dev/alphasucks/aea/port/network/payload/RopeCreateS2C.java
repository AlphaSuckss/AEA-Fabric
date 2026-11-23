package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.rope.RopeAnchor;
import java.util.UUID;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record RopeCreateS2C(
   int id, RopeAnchor from, RopeAnchor to, double length, float maxLength, class_2960 texture, @Nullable UUID owner, boolean controllable
) {
   public static final class_2960 CHANNEL = Apoli.internal("rope_create");

   public void write(class_2540 buf) {
      buf.method_10804(this.id);
      this.from.write(buf);
      this.to.write(buf);
      buf.writeDouble(this.length);
      buf.writeFloat(this.maxLength);
      buf.method_10812(this.texture);
      buf.writeBoolean(this.owner != null);
      if (this.owner != null) {
         buf.method_10797(this.owner);
      }

      buf.writeBoolean(this.controllable);
   }

   public static RopeCreateS2C read(class_2540 buf) {
      int id = buf.method_10816();
      RopeAnchor from = RopeAnchor.read(buf);
      RopeAnchor to = RopeAnchor.read(buf);
      double length = buf.readDouble();
      float maxLength = buf.readFloat();
      class_2960 texture = buf.method_10810();
      UUID owner = buf.readBoolean() ? buf.method_10790() : null;
      boolean controllable = buf.readBoolean();
      return new RopeCreateS2C(id, from, to, length, maxLength, texture, owner, controllable);
   }
}
