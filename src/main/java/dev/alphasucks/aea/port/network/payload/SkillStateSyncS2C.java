package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SkillStateSyncS2C(
   Map<class_2960, Integer> points,
   Set<class_2960> purchased,
   Set<class_2960> hidden,
   Set<class_2960> locked,
   Set<class_2960> nonRefundable,
   boolean legacyFormat
) {
   public static final class_2960 CHANNEL = Apoli.internal("skill_state_sync");

   public SkillStateSyncS2C(
      Map<class_2960, Integer> points, Set<class_2960> purchased, Set<class_2960> hidden, Set<class_2960> locked, Set<class_2960> nonRefundable
   ) {
      this(points, purchased, hidden, locked, nonRefundable, false);
   }

   public void write(class_2540 buf) {
      buf.method_10804(this.points.size());

      for (Entry<class_2960, Integer> e : this.points.entrySet()) {
         buf.method_10812((class_2960)e.getKey());
         buf.method_10804((Integer)e.getValue());
      }

      buf.method_34062(this.purchased, class_2540::method_10812);
      if (!this.legacyFormat) {
         buf.method_34062(this.hidden, class_2540::method_10812);
         buf.method_34062(this.locked, class_2540::method_10812);
         buf.method_34062(this.nonRefundable, class_2540::method_10812);
      }
   }

   public static SkillStateSyncS2C read(class_2540 buf) {
      int n = buf.method_10816();
      Map<class_2960, Integer> points = new HashMap(n);

      for (int i = 0; i < n; i++) {
         class_2960 key = buf.method_10810();
         points.put(key, buf.method_10816());
      }

      Set<class_2960> purchased = new HashSet(buf.method_34066(class_2540::method_10810));
      Set<class_2960> hidden = buf.isReadable() ? new HashSet(buf.method_34066(class_2540::method_10810)) : new HashSet();
      Set<class_2960> locked = buf.isReadable() ? new HashSet(buf.method_34066(class_2540::method_10810)) : new HashSet();
      Set<class_2960> nonRefundable = buf.isReadable() ? new HashSet(buf.method_34066(class_2540::method_10810)) : new HashSet();
      return new SkillStateSyncS2C(points, purchased, hidden, locked, nonRefundable);
   }
}
