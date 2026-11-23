package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1309;
import net.minecraft.class_243;

public final class HitLocationTracker {
   private static final Map<UUID, class_243> LAST_HIT = new ConcurrentHashMap();

   private HitLocationTracker() {
   }

   public static void record(class_1309 entity, double xNorm, double yNorm, double zNorm) {
      if (entity != null) {
         LAST_HIT.put(entity.method_5667(), new class_243(xNorm, yNorm, zNorm));
      }
   }

   public static class_243 getAndClear(class_1309 entity) {
      return entity == null ? null : (class_243)LAST_HIT.remove(entity.method_5667());
   }

   public static void remove(UUID uuid) {
      LAST_HIT.remove(uuid);
   }

   /** Hits are recorded right before the damage call of the same tick; anything left at the end of a tick is stale. */
   public static void clearAll() {
      LAST_HIT.clear();
   }
}
