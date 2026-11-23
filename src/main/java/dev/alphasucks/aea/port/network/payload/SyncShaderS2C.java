package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record SyncShaderS2C(@Nullable class_2960 shader, boolean toggleable) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_shader");

   public void write(class_2540 buf) {
      buf.writeBoolean(this.shader != null);
      if (this.shader != null) {
         buf.method_10812(this.shader);
      }

      buf.writeBoolean(this.toggleable);
   }

   public static SyncShaderS2C read(class_2540 buf) {
      class_2960 shader = buf.readBoolean() ? buf.method_10810() : null;
      return new SyncShaderS2C(shader, buf.readBoolean());
   }
}
