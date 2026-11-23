package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import java.util.Optional;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record DisguiseUpdateS2C(int entityId, Optional<DisguiseData> data) {
   public static final class_2960 CHANNEL = Apoli.internal("disguise_update");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_37435(this.data, (b, d) -> d.write(b));
   }

   public static DisguiseUpdateS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      Optional<DisguiseData> data = buf.method_37436(DisguiseData::read);
      return new DisguiseUpdateS2C(entityId, data);
   }
}
