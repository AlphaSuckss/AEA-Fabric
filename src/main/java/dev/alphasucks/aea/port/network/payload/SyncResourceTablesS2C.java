package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncResourceTablesS2C(int entityId, Map<class_2960, int[]> tables) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_resource_tables");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10804(this.tables.size());

      for (Entry<class_2960, int[]> entry : this.tables.entrySet()) {
         buf.method_10812((class_2960)entry.getKey());
         buf.method_10806((int[])entry.getValue());
      }
   }

   public static SyncResourceTablesS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      int count = buf.method_10816();
      Map<class_2960, int[]> tables = new HashMap(count);

      for (int i = 0; i < count; i++) {
         class_2960 key = buf.method_10810();
         tables.put(key, buf.method_10787());
      }

      return new SyncResourceTablesS2C(entityId, tables);
   }
}
