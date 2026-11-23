package dev.overgrown.sync.factory.action.entity.teleportation.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1314;
import net.minecraft.class_18;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class EntityLocationsState extends class_18 {
   private static final String STATE_NAME = "sync_entity_locations";
   private final Map<UUID, EntityLocationsState.EntityLocationData> entityLocations = new ConcurrentHashMap();
   private final Map<UUID, Long> lastAccessTime = new ConcurrentHashMap();
   private static final long CLEANUP_INTERVAL = 12000L;
   private static final long MAX_INACTIVE_TIME = 36000L;
   private long lastCleanupTime = 0L;
   private boolean mergedPerWorldStates = false;

   public static EntityLocationsState get(class_3218 world) {
      // saved locations carry their own dimension, so keep a single state on the overworld instead of one per world
      class_3218 overworld = world.method_8503().method_30002();
      class_3218 storageWorld = overworld != null ? overworld : world;
      EntityLocationsState state = storageWorld.method_17983().method_17924(EntityLocationsState::fromNbt, EntityLocationsState::new, "sync_entity_locations");
      if (!state.mergedPerWorldStates) {
         state.mergedPerWorldStates = true;
         state.mergePerWorldStates(world.method_8503(), storageWorld);
      }

      return state;
   }

   // older saves kept one state per dimension: fold those into the overworld's once per server session
   private void mergePerWorldStates(MinecraftServer server, class_3218 storageWorld) {
      for (class_3218 other : server.method_3738()) {
         if (other != storageWorld) {
            EntityLocationsState old = other.method_17983().method_20786(EntityLocationsState::fromNbt, "sync_entity_locations");
            if (old != null && !old.entityLocations.isEmpty()) {
               old.entityLocations.forEach((entityId, data) -> this.entityLocations.merge(entityId, data, (current, incoming) -> {
                  incoming.locations.forEach(current.locations::putIfAbsent);
                  current.isPersistent |= incoming.isPersistent;
                  return current;
               }));
               old.entityLocations.clear();
               old.lastAccessTime.clear();
               old.method_80();
               this.method_80();
            }
         }
      }
   }

   public void saveLocation(
      UUID entityId, String id, class_243 position, class_5321<class_1937> dimension, float yaw, float pitch, boolean overwrite, boolean isPersistent
   ) {
      EntityLocationsState.EntityLocationData entityData = (EntityLocationsState.EntityLocationData)this.entityLocations
         .computeIfAbsent(entityId, k -> new EntityLocationsState.EntityLocationData(isPersistent));
      if (overwrite || !entityData.locations.containsKey(id)) {
         entityData.locations.put(id, new EntityLocationsState.SavedLocation(position, dimension, yaw, pitch));
         this.lastAccessTime.put(entityId, System.currentTimeMillis());
         this.method_80();
      }
   }

   @Nullable
   public EntityLocationsState.SavedLocation getLocation(UUID entityId, String id) {
      EntityLocationsState.EntityLocationData entityData = (EntityLocationsState.EntityLocationData)this.entityLocations.get(entityId);
      if (entityData == null) {
         return null;
      }

      this.lastAccessTime.put(entityId, System.currentTimeMillis());
      return (EntityLocationsState.SavedLocation)entityData.locations.get(id);
   }

   public boolean removeLocation(UUID entityId, String id) {
      EntityLocationsState.EntityLocationData entityData = (EntityLocationsState.EntityLocationData)this.entityLocations.get(entityId);
      if (entityData == null) {
         return false;
      }

      boolean removed = entityData.locations.remove(id) != null;
      if (removed) {
         this.method_80();
      }

      return removed;
   }

   public void removeAllLocationsForEntity(UUID entityId) {
      if (this.entityLocations.remove(entityId) != null) {
         this.lastAccessTime.remove(entityId);
         this.method_80();
      }
   }

   public void cleanupLocations(MinecraftServer server) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastCleanupTime >= 600000L) {
         this.lastCleanupTime = currentTime;
         boolean changed = false;
         Iterator<Entry<UUID, EntityLocationsState.EntityLocationData>> iterator = this.entityLocations.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<UUID, EntityLocationsState.EntityLocationData> entry = (Entry<UUID, EntityLocationsState.EntityLocationData>)iterator.next();
            UUID entityId = (UUID)entry.getKey();
            EntityLocationsState.EntityLocationData data = (EntityLocationsState.EntityLocationData)entry.getValue();
            if (!this.isPlayer(entityId, server) && !data.isPersistent) {
               boolean entityExists = false;

               for (class_3218 world : server.method_3738()) {
                  class_1297 entity = world.method_14190(entityId);
                  if (entity != null && entity.method_5805()) {
                     entityExists = true;
                     if (entity.method_16914() || entity instanceof class_1314) {
                        data.isPersistent = true;
                     }
                     break;
                  }
               }

               if (!entityExists) {
                  Long lastAccess = (Long)this.lastAccessTime.get(entityId);
                  if (lastAccess == null || currentTime - lastAccess > 1800000L) {
                     iterator.remove();
                     this.lastAccessTime.remove(entityId);
                     changed = true;
                  }
               }
            }
         }

         if (changed) {
            this.method_80();
         }
      }
   }

   private boolean isPlayer(UUID uuid, MinecraftServer server) {
      return server.method_3760().method_14602(uuid) != null;
   }

   public static EntityLocationsState fromNbt(class_2487 nbt) {
      EntityLocationsState state = new EntityLocationsState();

      for (class_2520 entityElement : nbt.method_10554("entities", 10)) {
         class_2487 entityCompound = (class_2487)entityElement;
         UUID entityId = entityCompound.method_25926("entityId");
         boolean isPersistent = entityCompound.method_10577("isPersistent");
         class_2499 locationsList = entityCompound.method_10554("locations", 10);
         Map<String, EntityLocationsState.SavedLocation> locations = new HashMap();

         for (class_2520 locationElement : locationsList) {
            class_2487 locationCompound = (class_2487)locationElement;
            String id = locationCompound.method_10558("id");
            class_243 position = new class_243(locationCompound.method_10574("x"), locationCompound.method_10574("y"), locationCompound.method_10574("z"));
            class_5321<class_1937> dimension = class_1937.field_25179;
            if (locationCompound.method_10545("dimension")) {
               dimension = class_5321.method_29179(class_7924.field_41223, new class_2960(locationCompound.method_10558("dimension")));
            }

            float yaw = locationCompound.method_10583("yaw");
            float pitch = locationCompound.method_10583("pitch");
            locations.put(id, new EntityLocationsState.SavedLocation(position, dimension, yaw, pitch));
         }

         state.entityLocations.put(entityId, new EntityLocationsState.EntityLocationData(locations, isPersistent));
      }

      return state;
   }

   @Override
   public class_2487 method_75(class_2487 nbt) {
      class_2499 entitiesList = new class_2499();

      for (Entry<UUID, EntityLocationsState.EntityLocationData> entityEntry : this.entityLocations.entrySet()) {
         class_2487 entityCompound = new class_2487();
         entityCompound.method_25927("entityId", (UUID)entityEntry.getKey());
         entityCompound.method_10556("isPersistent", ((EntityLocationsState.EntityLocationData)entityEntry.getValue()).isPersistent);
         class_2499 locationsList = new class_2499();

         for (Entry<String, EntityLocationsState.SavedLocation> locationEntry : ((EntityLocationsState.EntityLocationData)entityEntry.getValue())
            .locations
            .entrySet()) {
            class_2487 locationCompound = new class_2487();
            locationCompound.method_10582("id", (String)locationEntry.getKey());
            locationCompound.method_10549("x", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).position().field_1352);
            locationCompound.method_10549("y", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).position().field_1351);
            locationCompound.method_10549("z", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).position().field_1350);
            locationCompound.method_10582("dimension", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).dimension().method_29177().toString());
            locationCompound.method_10548("yaw", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).yaw());
            locationCompound.method_10548("pitch", ((EntityLocationsState.SavedLocation)locationEntry.getValue()).pitch());
            locationsList.add(locationCompound);
         }

         entityCompound.method_10566("locations", locationsList);
         entitiesList.add(entityCompound);
      }

      nbt.method_10566("entities", entitiesList);
      return nbt;
   }

   private static class EntityLocationData {
      private final Map<String, EntityLocationsState.SavedLocation> locations;
      private boolean isPersistent;

      public EntityLocationData(boolean isPersistent) {
         this.locations = new HashMap();
         this.isPersistent = isPersistent;
      }

      public EntityLocationData(Map<String, EntityLocationsState.SavedLocation> locations, boolean isPersistent) {
         this.locations = locations;
         this.isPersistent = isPersistent;
      }
   }

   public record SavedLocation(class_243 position, class_5321<class_1937> dimension, float yaw, float pitch) {
   }
}
