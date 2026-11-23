package dev.alphasucks.aea.port.client;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;

@Environment(EnvType.CLIENT)
public final class ForcedKeys {
   private static final Map<String, ForcedKeys.Entry> BY_NAME = new HashMap();
   private static Map<class_304, ForcedKeys.Entry> resolved = Map.of();
   private static boolean any;
   private static boolean needsResolve;

   private ForcedKeys() {
   }

   public static void force(String key, int duration, boolean release) {
      if (key != null && !key.isEmpty()) {
         if (release) {
            if (BY_NAME.remove(key) == null) {
               return;
            }
         } else {
            ForcedKeys.Entry entry = (ForcedKeys.Entry)BY_NAME.get(key);
            if (entry == null) {
               entry = new ForcedKeys.Entry();
               BY_NAME.put(key, entry);
            }

            int ticks = Math.max(1, duration);
            if (ticks > entry.ticks) {
               entry.ticks = ticks;
            }

            entry.clicks++;
            entry.localClicks++;
         }

         needsResolve = true;
         rebuild();
      }
   }

   public static void tick() {
      if (any || needsResolve) {
         boolean expired = false;
         Iterator<java.util.Map.Entry<String, ForcedKeys.Entry>> it = BY_NAME.entrySet().iterator();

         while (it.hasNext()) {
            ForcedKeys.Entry entry = (ForcedKeys.Entry)((java.util.Map.Entry)it.next()).getValue();
            entry.ticks--;
            if (entry.ticks <= 0) {
               it.remove();
               expired = true;
            }
         }

         if (expired || needsResolve) {
            rebuild();
         }
      }
   }

   public static void clear() {
      BY_NAME.clear();
      resolved = Map.of();
      any = false;
      needsResolve = false;
   }

   public static boolean isForced(class_304 mapping) {
      return any && resolved.containsKey(mapping);
   }

   public static boolean consumeForcedClick(class_304 mapping) {
      if (!any) {
         return false;
      } else {
         ForcedKeys.Entry entry = (ForcedKeys.Entry)resolved.get(mapping);
         if (entry != null && entry.clicks > 0) {
            entry.clicks--;
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean consumeLocalForcedClick(class_304 mapping) {
      if (!any) {
         return false;
      } else {
         ForcedKeys.Entry entry = (ForcedKeys.Entry)resolved.get(mapping);
         if (entry != null && entry.localClicks > 0) {
            entry.localClicks--;
            return true;
         } else {
            return false;
         }
      }
   }

   private static void rebuild() {
      if (BY_NAME.isEmpty()) {
         resolved = Map.of();
         any = false;
         needsResolve = false;
      } else {
         Map<class_304, ForcedKeys.Entry> next = new IdentityHashMap(BY_NAME.size());
         boolean unresolved = false;

         for (java.util.Map.Entry<String, ForcedKeys.Entry> entry : BY_NAME.entrySet()) {
            class_304 mapping = ApoliKeyMappings.resolve((String)entry.getKey());
            if (mapping == null) {
               unresolved = true;
            } else {
               next.put(mapping, (ForcedKeys.Entry)entry.getValue());
            }
         }

         resolved = next;
         any = !next.isEmpty();
         needsResolve = unresolved;
      }
   }

   private static final class Entry {
      int ticks;
      int clicks;
      int localClicks;
   }
}
