package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record KeyHeldC2S(List<String> keys) {
   public static final int MAX_KEYS = 64;
   public static final class_2960 CHANNEL = Apoli.internal("key_held");

   public void write(class_2540 buf) {
      int n = Math.min(this.keys.size(), 64);
      buf.method_10804(n);

      for (int i = 0; i < n; i++) {
         buf.method_10814((String)this.keys.get(i));
      }
   }

   public static KeyHeldC2S read(class_2540 buf) {
      int n = buf.method_10816();
      List<String> out = new ArrayList(Math.min(Math.max(n, 0), 64));

      for (int i = 0; i < n; i++) {
         String key = buf.method_19772();
         if (out.size() < 64) {
            out.add(key);
         }
      }

      return new KeyHeldC2S(out);
   }
}
