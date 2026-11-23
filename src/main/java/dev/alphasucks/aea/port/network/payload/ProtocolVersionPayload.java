package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record ProtocolVersionPayload(int version) {
   public static final class_2960 CHANNEL = Apoli.internal("protocol_version");

   public void write(class_2540 buf) {
      buf.method_10804(this.version);
   }

   public static ProtocolVersionPayload read(class_2540 buf) {
      return new ProtocolVersionPayload(buf.method_10816());
   }
}
