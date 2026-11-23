package dev.alphasucks.aea.port.condition.builtin.entity;

import dev.alphasucks.aea.port.compat.ModCompat;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;

public final class EntityNbtSnapshot {
   private static final ThreadLocal<Boolean> SKIP_RECIPE_BOOK = ThreadLocal.withInitial(() -> Boolean.FALSE);
   private static final ThreadLocal<EntityNbtSnapshot.TickCache> CACHE = ThreadLocal.withInitial(EntityNbtSnapshot.TickCache::new);

   private EntityNbtSnapshot() {
   }

   public static boolean isSkippingRecipeBook() {
      return !ModCompat.NERB && (Boolean)SKIP_RECIPE_BOOK.get();
   }

   public static class_2487 of(class_1297 entity, class_2487 expected) {
      Set<String> keys = expected.method_10541();
      return canFastPath(keys) ? fastSnapshot(entity, keys) : cachedFullSnapshot(entity, expected);
   }

   private static boolean canFastPath(Set<String> keys) {
      for (String key : keys) {
         if (!"Tags".equals(key)) {
            return false;
         }
      }

      return true;
   }

   private static class_2487 fastSnapshot(class_1297 entity, Set<String> keys) {
      class_2487 actual = new class_2487();
      if (keys.contains("Tags")) {
         Set<String> tags = entity.method_5752();
         if (!tags.isEmpty()) {
            class_2499 list = new class_2499();

            for (String tag : tags) {
               list.add(class_2519.method_23256(tag));
            }

            actual.method_10566("Tags", list);
         }
      }

      return actual;
   }

   public static class_2487 cached(class_1297 entity) {
      long now = entity.method_37908().method_8510();
      EntityNbtSnapshot.TickCache cache = (EntityNbtSnapshot.TickCache)CACHE.get();
      if (cache.tick != now) {
         cache.snapshots.clear();
         cache.tick = now;
      }

      class_2487 snapshot = (class_2487)cache.snapshots.get(entity);
      if (snapshot == null) {
         snapshot = serialize(entity, true);
         cache.snapshots.put(entity, snapshot);
      }

      return snapshot;
   }

   private static class_2487 cachedFullSnapshot(class_1297 entity, class_2487 expected) {
      return !ModCompat.NERB && expected.method_10545("recipeBook") ? serialize(entity, false) : cached(entity);
   }

   private static class_2487 serialize(class_1297 entity, boolean skipRecipeBook) {
      class_2487 actual = new class_2487();
      boolean mark = skipRecipeBook && !ModCompat.NERB;
      if (mark) {
         SKIP_RECIPE_BOOK.set(Boolean.TRUE);
      }

      try {
         entity.method_5647(actual);
      } finally {
         if (mark) {
            SKIP_RECIPE_BOOK.set(Boolean.FALSE);
         }
      }

      return actual;
   }

   private static final class TickCache {
      long tick = Long.MIN_VALUE;
      final Map<class_1297, class_2487> snapshots = new IdentityHashMap();
   }
}
