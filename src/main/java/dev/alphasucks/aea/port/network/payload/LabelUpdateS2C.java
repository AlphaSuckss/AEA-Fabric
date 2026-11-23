package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record LabelUpdateS2C(int entityId, Map<class_2960, class_2561> texts) {
   public static final class_2960 CHANNEL = Apoli.internal("label_update");

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10804(this.texts.size());

      for (Entry<class_2960, class_2561> entry : this.texts.entrySet()) {
         buf.method_10812((class_2960)entry.getKey());
         buf.method_10805((class_2561)entry.getValue());
      }
   }

   public static LabelUpdateS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      int count = buf.method_10816();
      Map<class_2960, class_2561> texts = new LinkedHashMap(Math.max(1, count));

      for (int i = 0; i < count; i++) {
         class_2960 powerId = buf.method_10810();
         texts.put(powerId, buf.method_10808());
      }

      return new LabelUpdateS2C(entityId, texts);
   }
}
