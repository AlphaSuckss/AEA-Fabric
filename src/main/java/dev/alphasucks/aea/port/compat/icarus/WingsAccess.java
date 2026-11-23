package dev.alphasucks.aea.port.compat.icarus;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.PowerLookup;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class WingsAccess {
   private static final class_2960 WINGS_ID = Apoli.id("wings");
   private static final long FRESH_TICKS = 2L;
   private static final Map<class_1309, WingsAccess.Entry> SERVER_CACHE = new WeakHashMap();

   private WingsAccess() {
   }

   static void refresh(class_1309 entity, WingsPower.Config cfg) {
      SERVER_CACHE.put(entity, new WingsAccess.Entry(cfg, entity.method_37908().method_8510()));
   }

   static void invalidate(class_1309 entity) {
      SERVER_CACHE.remove(entity);
   }

   public static void clear() {
      SERVER_CACHE.clear();
   }

   @Nullable
   public static WingsPower.Config get(class_1309 entity) {
      if (entity.method_37908().method_8608()) {
         return resolveClient(entity);
      }

      WingsAccess.Entry entry = (WingsAccess.Entry)SERVER_CACHE.get(entity);
      if (entry == null) {
         return null;
      }

      if (entity.method_37908().method_8510() - entry.tick() <= 2L) {
         return entry.cfg();
      }

      SERVER_CACHE.remove(entity);
      return null;
   }

   public static boolean hasWings(class_1309 entity) {
      return get(entity) != null;
   }

   @Nullable
   private static WingsPower.Config resolveClient(class_1309 entity) {
      List<WingsPower.Config> active = PowerLookup.active(entity, WINGS_ID, WingsPower.Config.class);
      return active.isEmpty() ? null : (WingsPower.Config)active.get(0);
   }

   private record Entry(WingsPower.Config cfg, long tick) {
   }
}
