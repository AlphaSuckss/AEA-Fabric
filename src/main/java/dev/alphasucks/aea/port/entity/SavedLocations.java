package dev.alphasucks.aea.port.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_18;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_4844;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class SavedLocations extends class_18 {
   public static final String FILE_ID = "apoli_locations";
   private static final int MAX_ENTITIES = 4096;
   private static final int MAX_PER_ENTITY = 32;
   private static final Codec<SavedLocations.Location> LOCATION = RecordCodecBuilder.create(
      i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(SavedLocations.Location::x),
            Codec.DOUBLE.fieldOf("y").forGetter(SavedLocations.Location::y),
            Codec.DOUBLE.fieldOf("z").forGetter(SavedLocations.Location::z),
            class_5321.method_39154(class_7924.field_41223).fieldOf("dimension").forGetter(SavedLocations.Location::dimension),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(SavedLocations.Location::yaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(SavedLocations.Location::pitch)
         )
         .apply(i, SavedLocations.Location::new)
   );
   private static final Codec<Map<UUID, Map<String, SavedLocations.Location>>> CODEC = Codec.unboundedMap(
      class_4844.field_41525, Codec.unboundedMap(Codec.STRING, LOCATION)
   );
   private final Map<UUID, Map<String, SavedLocations.Location>> byEntity = new LinkedHashMap<UUID, Map<String, SavedLocations.Location>>(16, 0.75F, true) {
      protected boolean removeEldestEntry(Entry<UUID, Map<String, SavedLocations.Location>> eldest) {
         return this.size() > 4096;
      }
   };

   public static SavedLocations get(MinecraftServer server) {
      return server.method_30002().method_17983().method_17924(SavedLocations::load, SavedLocations::new, "apoli_locations");
   }

   @Nullable
   public static SavedLocations of(class_1297 entity) {
      MinecraftServer server = entity.method_5682();
      return server == null ? null : get(server);
   }

   public static SavedLocations load(class_2487 tag) {
      SavedLocations state = new SavedLocations();
      class_2520 stored = tag.method_10580("locations");
      if (stored == null) {
         return state;
      }

      CODEC.parse(class_2509.field_11560, stored)
         .resultOrPartial(error -> Apoli.LOGGER.error("[Apoli] Could not read saved locations: {}", error))
         .ifPresent(map -> map.forEach((id, locations) -> {
            if (!locations.isEmpty()) {
               state.byEntity.put(id, new LinkedHashMap(locations));
            }
         }));
      return state;
   }

   @Override
   public class_2487 method_75(class_2487 tag) {
      CODEC.encodeStart(class_2509.field_11560, this.byEntity)
         .resultOrPartial(error -> Apoli.LOGGER.error("[Apoli] Could not write saved locations: {}", error))
         .ifPresent(encoded -> tag.method_10566("locations", encoded));
      return tag;
   }

   public void put(UUID entity, String id, SavedLocations.Location location, boolean overwrite) {
      Map<String, SavedLocations.Location> locations = (Map<String, SavedLocations.Location>)this.byEntity.computeIfAbsent(entity, key -> new LinkedHashMap(4));
      if (overwrite || !locations.containsKey(id)) {
         locations.put(id, location);

         while (locations.size() > 32) {
            locations.remove(locations.keySet().iterator().next());
         }

         this.method_80();
      }
   }

   @Nullable
   public SavedLocations.Location get(UUID entity, String id) {
      Map<String, SavedLocations.Location> locations = (Map<String, SavedLocations.Location>)this.byEntity.get(entity);
      return locations == null ? null : (SavedLocations.Location)locations.get(id);
   }

   public boolean has(UUID entity, String id) {
      return this.get(entity, id) != null;
   }

   public boolean remove(UUID entity, String id) {
      Map<String, SavedLocations.Location> locations = (Map<String, SavedLocations.Location>)this.byEntity.get(entity);
      if (locations != null && locations.remove(id) != null) {
         if (locations.isEmpty()) {
            this.byEntity.remove(entity);
         }

         this.method_80();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeAll(UUID entity) {
      if (this.byEntity.remove(entity) == null) {
         return false;
      }

      this.method_80();
      return true;
   }

   public Set<String> ids(UUID entity) {
      Map<String, SavedLocations.Location> locations = (Map<String, SavedLocations.Location>)this.byEntity.get(entity);
      return locations == null ? Set.of() : Set.copyOf(locations.keySet());
   }

   public record Location(double x, double y, double z, class_5321<class_1937> dimension, float yaw, float pitch) {
   }
}
