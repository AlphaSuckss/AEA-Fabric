package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class ClientResolvedPowers {
   private static Map<class_2960, Integer> targets = Map.of();
   private static final Map<class_2960, Long> SINCE = new HashMap();
   private static long ticks;

   private ClientResolvedPowers() {
   }

   public static void apply(SyncResolvedPowersS2C payload) {
      List<class_2960> powers = payload.powers();
      int[] payloadTargets = payload.targets();
      Map<class_2960, Integer> next = new HashMap(Math.max(4, powers.size() * 2));

      for (int i = 0; i < powers.size(); i++) {
         next.put((class_2960)powers.get(i), payloadTargets[i]);
      }

      SINCE.keySet().retainAll(next.keySet());

      for (class_2960 id : next.keySet()) {
         SINCE.putIfAbsent(id, ticks);
      }

      targets = next;
   }

   public static boolean isActive(class_2960 powerId) {
      return targets.containsKey(powerId);
   }

   public static boolean isEmpty() {
      return targets.isEmpty();
   }

   public static int target(class_2960 powerId) {
      Integer target = (Integer)targets.get(powerId);
      return target == null ? -1 : target;
   }

   public static float age(class_2960 powerId, float partialTick) {
      Long since = (Long)SINCE.get(powerId);
      return since == null ? 0.0F : (float)(ticks - since) + partialTick;
   }

   public static void tick(class_310 mc) {
      if (mc.field_1724 != null && !mc.method_1493()) {
         ticks++;
      }
   }

   public static void clear() {
      targets = Map.of();
      SINCE.clear();
   }
}
