package dev.overgrown.sync.factory.action.bientity.suppress_power.utils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2960;

public final class SuppressedPowerManager {
   private static final ConcurrentHashMap<UUID, Set<class_2960>> SUPPRESSED = new ConcurrentHashMap();
   // client-side copy, filled from SuppressedPowerSync packets; used for powers evaluated in client worlds
   private static final ConcurrentHashMap<UUID, Set<class_2960>> CLIENT_SUPPRESSED = new ConcurrentHashMap();

   private SuppressedPowerManager() {
   }

   public static void suppress(UUID entityId, class_2960 powerId) {
      if (((Set)SUPPRESSED.computeIfAbsent(entityId, k -> Collections.newSetFromMap(new ConcurrentHashMap()))).add(powerId)) {
         SuppressedPowerSync.markDirty(entityId);
      }
   }

   public static void liberate(UUID entityId, class_2960 powerId) {
      Set<class_2960> set = (Set<class_2960>)SUPPRESSED.get(entityId);
      if (set != null) {
         boolean removed = set.remove(powerId);
         if (set.isEmpty()) {
            SUPPRESSED.remove(entityId);
         }

         if (removed) {
            SuppressedPowerSync.markDirty(entityId);
         }
      }
   }

   public static void removeAll(UUID entityId) {
      if (SUPPRESSED.remove(entityId) != null) {
         SuppressedPowerSync.markDirty(entityId);
      }
   }

   public static boolean isSuppressed(UUID entityId, class_2960 powerId) {
      Set<class_2960> set = (Set<class_2960>)SUPPRESSED.get(entityId);
      return set != null && set.contains(powerId);
   }

   /** Uses the client-side copy when the entity lives in a client world. */
   public static boolean isSuppressed(class_1297 entity, class_2960 powerId) {
      class_1937 world = entity.method_37908();
      if (world != null && world.method_8608()) {
         Set<class_2960> set = (Set<class_2960>)CLIENT_SUPPRESSED.get(entity.method_5667());
         return set != null && set.contains(powerId);
      } else {
         return isSuppressed(entity.method_5667(), powerId);
      }
   }

   public static Set<class_2960> getSuppressedPowers(UUID entityId) {
      Set<class_2960> set = (Set<class_2960>)SUPPRESSED.get(entityId);
      return set == null ? Set.of() : Collections.unmodifiableSet(set);
   }

   public static void setClientSuppressed(UUID entityId, Set<class_2960> powers) {
      if (powers.isEmpty()) {
         CLIENT_SUPPRESSED.remove(entityId);
      } else {
         Set<class_2960> set = Collections.newSetFromMap(new ConcurrentHashMap());
         set.addAll(powers);
         CLIENT_SUPPRESSED.put(entityId, set);
      }
   }

   public static void clearClient(UUID entityId) {
      CLIENT_SUPPRESSED.remove(entityId);
   }

   public static void clearClient() {
      CLIENT_SUPPRESSED.clear();
   }
}
