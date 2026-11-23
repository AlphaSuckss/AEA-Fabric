package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncEntityPowersS2C(int entityId, Map<class_2960, Set<class_2960>> powersBySource, Map<class_2960, Integer> auxInt, Set<class_2960> suppressed) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_entity_powers");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10804(this.powersBySource.size());

      for (Entry<class_2960, Set<class_2960>> e : this.powersBySource.entrySet()) {
         buf.method_10812((class_2960)e.getKey());
         buf.method_34062((Collection)e.getValue(), class_2540::method_10812);
      }

      buf.method_10804(this.auxInt.size());

      for (Entry<class_2960, Integer> e : this.auxInt.entrySet()) {
         buf.method_10812((class_2960)e.getKey());
         buf.method_10804((Integer)e.getValue());
      }

      buf.method_34062(this.suppressed, class_2540::method_10812);
   }

   public static SyncEntityPowersS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      int n = buf.method_10816();
      Map<class_2960, Set<class_2960>> map = new HashMap(n);

      for (int i = 0; i < n; i++) {
         class_2960 key = buf.method_10810();
         Set<class_2960> srcs = new HashSet(buf.method_34066(class_2540::method_10810));
         map.put(key, srcs);
      }

      int m = buf.method_10816();
      Map<class_2960, Integer> aux = new HashMap(m);

      for (int i = 0; i < m; i++) {
         class_2960 key = buf.method_10810();
         int value = buf.method_10816();
         aux.put(key, value);
      }

      Set<class_2960> suppressed = new HashSet(buf.method_34066(class_2540::method_10810));
      return new SyncEntityPowersS2C(entityId, map, aux, suppressed);
   }
}
