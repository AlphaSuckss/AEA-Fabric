package dev.alphasucks.aea.port.network.payload;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.Power;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncPowersS2C(Map<class_2960, String> rawPowers) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_powers");

   public void write(class_2540 buf) {
      buf.method_10804(this.rawPowers.size());

      for (Entry<class_2960, String> e : this.rawPowers.entrySet()) {
         buf.method_10812((class_2960)e.getKey());
         buf.method_10814((String)e.getValue());
      }
   }

   public static SyncPowersS2C read(class_2540 buf) {
      int n = buf.method_10816();
      Map<class_2960, String> out = new HashMap(n);

      for (int i = 0; i < n; i++) {
         class_2960 id = buf.method_10810();
         String json = buf.method_19772();
         out.put(id, json);
      }

      return new SyncPowersS2C(out);
   }

   public Map<class_2960, Power> decodeAll() {
      Map<class_2960, Power> result = new HashMap();

      for (Entry<class_2960, String> e : this.rawPowers.entrySet()) {
         JsonElement el = JsonParser.parseString((String)e.getValue());
         Power.CODEC.parse(JsonOps.INSTANCE, el).result().ifPresent(p -> result.put((class_2960)e.getKey(), p));
      }

      return result;
   }
}
