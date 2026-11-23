package dev.alphasucks.aea.port.entity;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_3222;
import net.minecraft.class_3312;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class OfflinePlayers {
   private static final int CAPACITY = 32;
   private static final Map<UUID, OfflinePlayerSnapshot> SNAPSHOTS = new LinkedHashMap(16, 0.75F, true);
   private static final Set<UUID> WITHOUT_DATA = new HashSet();

   private OfflinePlayers() {
   }

   @Nullable
   public static class_3222 get(MinecraftServer server, UUID uuid) {
      class_3222 online = server.method_3760().method_14602(uuid);
      if (online != null) {
         return online;
      }

      OfflinePlayerSnapshot cached = (OfflinePlayerSnapshot)SNAPSHOTS.get(uuid);
      if (cached != null) {
         return cached;
      }

      if (WITHOUT_DATA.contains(uuid)) {
         return null;
      }

      OfflinePlayerSnapshot loaded = hasSavedData(server, uuid) ? OfflinePlayerSnapshot.load(server, profile(server, uuid)) : null;
      if (loaded == null) {
         WITHOUT_DATA.add(uuid);
         return null;
      }

      SNAPSHOTS.put(uuid, loaded);
      if (SNAPSHOTS.size() > 32) {
         Iterator<UUID> eldest = SNAPSHOTS.keySet().iterator();
         eldest.next();
         eldest.remove();
      }

      return loaded;
   }

   public static boolean isSnapshot(class_1297 entity) {
      return entity instanceof OfflinePlayerSnapshot;
   }

   public static void forget(UUID uuid) {
      SNAPSHOTS.remove(uuid);
      WITHOUT_DATA.remove(uuid);
   }

   public static void clear() {
      SNAPSHOTS.clear();
      WITHOUT_DATA.clear();
   }

   private static boolean hasSavedData(MinecraftServer server, UUID uuid) {
      return Files.isRegularFile(server.method_27050(class_5218.field_24182).resolve(uuid + ".dat"), new LinkOption[0]);
   }

   private static GameProfile profile(MinecraftServer server, UUID uuid) {
      class_3312 cache = server.method_3793();
      if (cache != null) {
         Optional<GameProfile> known = cache.method_14512(uuid);
         if (known.isPresent()) {
            return (GameProfile)known.get();
         }
      }

      return new GameProfile(uuid, uuid.toString().replace("-", "").substring(0, 16));
   }
}
