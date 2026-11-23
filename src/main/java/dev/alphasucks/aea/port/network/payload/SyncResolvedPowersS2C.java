package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncResolvedPowersS2C(List<class_2960> powers, int[] targets) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_resolved_powers");

   public void write(class_2540 buf) {
      buf.method_10804(this.powers.size());

      for (int i = 0; i < this.powers.size(); i++) {
         buf.method_10812((class_2960)this.powers.get(i));
         buf.method_10804(this.targets[i] + 1);
      }
   }

   public static SyncResolvedPowersS2C read(class_2540 buf) {
      int count = buf.method_10816();
      List<class_2960> powers = new ArrayList(count);
      int[] targets = new int[count];

      for (int i = 0; i < count; i++) {
         powers.add(buf.method_10810());
         targets[i] = buf.method_10816() - 1;
      }

      return new SyncResolvedPowersS2C(powers, targets);
   }
}
