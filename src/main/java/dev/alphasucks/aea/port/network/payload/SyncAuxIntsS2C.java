package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncAuxIntsS2C(int entityId, Map<class_2960, Integer> auxInt) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_aux_ints");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10804(this.auxInt.size());

      for (Entry<class_2960, Integer> entry : this.auxInt.entrySet()) {
         buf.method_10812((class_2960)entry.getKey());
         buf.method_10804((Integer)entry.getValue());
      }
   }

   public static SyncAuxIntsS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      int count = buf.method_10816();
      Map<class_2960, Integer> values = new HashMap(count);

      for (int i = 0; i < count; i++) {
         values.put(buf.method_10810(), buf.method_10816());
      }

      return new SyncAuxIntsS2C(entityId, values);
   }
}
