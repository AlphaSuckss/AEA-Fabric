package dev.alphasucks.aea.port.keybind;

import dev.alphasucks.aea.port.dev.DevMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.server.MinecraftServer;

public final class HeldKeys {
   public static final int MAX_GRACE = 40;
   private static final Map<UUID, Set<String>> SERVER = new ConcurrentHashMap();
   private static final Map<UUID, Map<String, Integer>> FORCED = new ConcurrentHashMap();
   private static final Map<UUID, Map<String, Long>> RELEASED = new ConcurrentHashMap();
   private static volatile HeldKeys.ClientLookup clientLookup;

   private HeldKeys() {
   }

   public static void setServerHeld(class_1297 owner, Collection<String> keys) {
      if (DevMode.isEnabled(owner)) {
         reportChange(owner, keys);
      }

      MinecraftServer server = owner.method_37908().method_8503();
      recordReleases(owner.method_5667(), keys, server == null ? Long.MIN_VALUE : server.method_3780());
      setServerHeld(owner.method_5667(), keys);
   }

   private static void reportChange(class_1297 owner, Collection<String> keys) {
      Set<String> before = serverHeldRaw(owner.method_5667());
      StringBuilder delta = null;

      for (String key : before) {
         if (!keys.contains(key)) {
            delta = appendDelta(delta, '-', key);
         }
      }

      for (String key : keys) {
         if (!before.contains(key)) {
            delta = appendDelta(delta, '+', key);
         }
      }

      if (delta != null) {
         DevMode.report(owner, "held keys " + delta);
      }
   }

   private static StringBuilder appendDelta(StringBuilder delta, char sign, String key) {
      return delta == null ? new StringBuilder().append(sign).append(key) : delta.append(' ').append(sign).append(key);
   }

   private static void recordReleases(UUID player, Collection<String> keys, long tick) {
      if (tick != Long.MIN_VALUE) {
         Set<String> before = (Set<String>)SERVER.get(player);
         if (before != null && !before.isEmpty()) {
            Map<String, Long> released = null;

            for (String key : before) {
               if (keys == null || !keys.contains(key)) {
                  if (released == null) {
                     released = (Map<String, Long>)RELEASED.computeIfAbsent(player, u -> new ConcurrentHashMap());
                  }

                  released.put(key, tick);
               }
            }

            if (released != null) {
               Iterator<Entry<String, Long>> it = released.entrySet().iterator();

               while (it.hasNext()) {
                  if (tick - (Long)((Entry)it.next()).getValue() > 40L) {
                     it.remove();
                  }
               }
            }
         }
      }
   }

   public static void setServerHeld(UUID player, Collection<String> keys) {
      if (keys != null && !keys.isEmpty()) {
         SERVER.put(player, Set.copyOf(keys));
      } else {
         SERVER.remove(player);
      }
   }

   public static boolean serverHeld(UUID entity, String key) {
      Set<String> held = (Set<String>)SERVER.get(entity);
      return held != null && held.contains(key) ? true : forcedHeld(entity, key);
   }

   public static boolean serverHeld(UUID entity, String key, int grace, long now) {
      if (serverHeld(entity, key)) {
         return true;
      }

      if (grace > 0 && now != Long.MIN_VALUE) {
         Map<String, Long> released = (Map<String, Long>)RELEASED.get(entity);
         if (released == null) {
            return false;
         }

         Long at = (Long)released.get(key);
         return at != null && now - at < grace;
      } else {
         return false;
      }
   }

   public static Set<String> serverHeldRaw(UUID entity) {
      Set<String> held = (Set<String>)SERVER.get(entity);
      return held == null ? Set.of() : held;
   }

   public static Set<String> serverHeldSet(UUID entity) {
      Set<String> held = (Set<String>)SERVER.get(entity);
      Map<String, Integer> forced = (Map<String, Integer>)FORCED.get(entity);
      if (forced == null || forced.isEmpty()) {
         return held == null ? Set.of() : held;
      } else if (held != null && !held.isEmpty()) {
         Set<String> merged = new HashSet(held);
         merged.addAll(forced.keySet());
         return merged;
      } else {
         return Set.copyOf(forced.keySet());
      }
   }

   public static void clearServer(UUID entity) {
      SERVER.remove(entity);
      FORCED.remove(entity);
      RELEASED.remove(entity);
   }

   public static void force(UUID entity, String key, int ticks) {
      if (ticks <= 0) {
         release(entity, key);
      } else {
         ((Map)FORCED.computeIfAbsent(entity, u -> new ConcurrentHashMap())).put(key, ticks);
      }
   }

   public static void release(UUID entity, String key) {
      Map<String, Integer> keys = (Map<String, Integer>)FORCED.get(entity);
      if (keys != null) {
         keys.remove(key);
         if (keys.isEmpty()) {
            FORCED.remove(entity);
         }
      }
   }

   public static Set<String> forcedKeys(UUID entity) {
      if (FORCED.isEmpty()) {
         return Set.of();
      }

      Map<String, Integer> keys = (Map<String, Integer>)FORCED.get(entity);
      return keys != null && !keys.isEmpty() ? keys.keySet() : Set.of();
   }

   public static boolean anyForced() {
      return !FORCED.isEmpty();
   }

   public static boolean forcedHeld(UUID entity, String key) {
      Map<String, Integer> keys = (Map<String, Integer>)FORCED.get(entity);
      return keys != null && keys.containsKey(key);
   }

   public static void tickForced(MinecraftServer server) {
      if (!FORCED.isEmpty()) {
         List<UUID> expiredIds = null;
         List<String> expiredKeys = null;
         Iterator<Entry<UUID, Map<String, Integer>>> outer = FORCED.entrySet().iterator();

         while (outer.hasNext()) {
            Entry<UUID, Map<String, Integer>> owner = (Entry<UUID, Map<String, Integer>>)outer.next();
            Map<String, Integer> keys = (Map<String, Integer>)owner.getValue();
            Iterator<Entry<String, Integer>> inner = keys.entrySet().iterator();

            while (inner.hasNext()) {
               Entry<String, Integer> entry = (Entry<String, Integer>)inner.next();
               int left = (Integer)entry.getValue() - 1;
               if (left > 0) {
                  entry.setValue(left);
               } else {
                  inner.remove();
                  if (expiredIds == null) {
                     expiredIds = new ArrayList(2);
                     expiredKeys = new ArrayList(2);
                  }

                  expiredIds.add((UUID)owner.getKey());
                  expiredKeys.add((String)entry.getKey());
               }
            }

            if (keys.isEmpty()) {
               outer.remove();
            }
         }

         if (expiredIds != null) {
            for (int i = 0; i < expiredIds.size(); i++) {
               KeyDispatch.releaseExpired(server, (UUID)expiredIds.get(i), (String)expiredKeys.get(i));
            }
         }
      }
   }

   public static void setClientLookup(HeldKeys.ClientLookup lookup) {
      clientLookup = lookup;
   }

   public static boolean clientHeld(class_1297 entity, String key, int grace) {
      HeldKeys.ClientLookup lookup = clientLookup;
      return lookup != null && lookup.isHeld(entity, key, grace);
   }

   public interface ClientLookup {
      boolean isHeld(class_1297 var1, String var2, int var3);
   }
}
