package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncEntitySetsS2C(class_2960 powerId, List<UUID> members) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_entity_sets");

   public void write(class_2540 buf) {
      buf.method_10812(this.powerId);
      buf.method_10804(this.members.size());

      for (int i = 0; i < this.members.size(); i++) {
         buf.method_10797((UUID)this.members.get(i));
      }
   }

   public static SyncEntitySetsS2C read(class_2540 buf) {
      class_2960 powerId = buf.method_10810();
      int count = buf.method_10816();
      List<UUID> members = new ArrayList(count);

      for (int i = 0; i < count; i++) {
         members.add(buf.method_10790());
      }

      return new SyncEntitySetsS2C(powerId, members);
   }
}
