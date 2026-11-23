package dev.alphasucks.aea.port.client;

import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_304;

@Environment(EnvType.CLIENT)
public final class KeyPressWatcher {
   private static volatile Set<String> watched = Set.of();
   private static volatile Set<String> heldSnapshot = Set.of();
   private static final Set<String> UNCONFIRMED = new HashSet();
   private static final Map<String, Long> RELEASED = new HashMap();
   private static Set<String> lastSent = Set.of();
   private static long tickCount;
   private static Consumer<List<String>> sender = keys -> {};

   private KeyPressWatcher() {
   }

   public static void setSender(Consumer<List<String>> next) {
      sender = next;
   }

   public static void reset() {
      watched = Set.of();
      heldSnapshot = Set.of();
      UNCONFIRMED.clear();
      RELEASED.clear();
      lastSent = Set.of();
      tickCount = 0L;
   }

   public static boolean isLocalHeld(String key, int grace) {
      if (heldSnapshot.contains(key)) {
         return true;
      }

      if (grace <= 0) {
         return false;
      }

      Long at = (Long)RELEASED.get(key);
      return at != null && tickCount - at < grace;
   }

   public static void rebuild(Map<class_2960, String> rawPowers) {
      Set<String> next = new HashSet();

      for (String json : rawPowers.values()) {
         try {
            collect(new Dynamic(JsonOps.INSTANCE, JsonParser.parseString(json)), next);
         } catch (RuntimeException var5) {
         }
      }

      watched = Set.copyOf(next);
   }

   public static void tick() {
      tickCount++;
      Set<String> keys = watched;
      if (keys.isEmpty()) {
         if (!heldSnapshot.isEmpty()) {
            heldSnapshot = Set.of();
         }

         UNCONFIRMED.clear();
         if (!lastSent.isEmpty()) {
            lastSent = Set.of();
            sender.accept(List.of());
         }
      } else {
         Set<String> previous = heldSnapshot;
         Set<String> held = new HashSet();

         for (String key : keys) {
            class_304 mapping = ApoliKeyMappings.resolve(key);
            if (mapping != null && ApoliKeyMappings.isDown(mapping)) {
               UNCONFIRMED.remove(key);
               held.add(key);
            } else if (previous.contains(key) && UNCONFIRMED.add(key)) {
               held.add(key);
            }
         }

         UNCONFIRMED.retainAll(held);

         for (String key : previous) {
            if (!held.contains(key)) {
               RELEASED.put(key, tickCount);
            }
         }

         pruneReleased();
         heldSnapshot = held;
         if (!held.equals(lastSent)) {
            lastSent = held;
            sender.accept(new ArrayList(held));
         }
      }
   }

   private static void pruneReleased() {
      if (!RELEASED.isEmpty()) {
         Iterator<Entry<String, Long>> it = RELEASED.entrySet().iterator();

         while (it.hasNext()) {
            if (tickCount - (Long)((Entry)it.next()).getValue() > 40L) {
               it.remove();
            }
         }
      }
   }

   private static <T> void collect(Dynamic<T> data, Set<String> out) {
      Map<Dynamic<T>, Dynamic<T>> fields = (Map<Dynamic<T>, Dynamic<T>>)data.getMapValues().result().orElse(null);
      if (fields == null) {
         data.asStreamOpt().result().ifPresent(stream -> stream.forEach(child -> collect(child, out)));
      } else {
         String type = (String)data.get("type").asString().result().orElse(null);
         if (type != null) {
            if (isKeyPressedType(type)) {
               out.add(extractKey(data));
            } else if (isKeySequenceType(type)) {
               collectSequenceKeys(data, out);
            }
         }

         for (Dynamic<T> value : fields.values()) {
            collect(value, out);
         }
      }
   }

   private static boolean isKeyPressedType(String type) {
      class_2960 id = class_2960.method_12829(type);
      String path = id != null ? id.method_12832() : type;
      return path.equals("key_pressed") || path.equals("key_held") || path.equals("held_key");
   }

   private static boolean isKeySequenceType(String type) {
      class_2960 id = class_2960.method_12829(type);
      String path = id != null ? id.method_12832() : type;
      return path.equals("action_on_key_sequence");
   }

   private static <T> void collectSequenceKeys(Dynamic<T> power, Set<String> out) {
      collectKeyList(power.get("keys"), out);
      collectKeyList(power.get("key_sequence"), out);
   }

   private static <T> void collectKeyList(OptionalDynamic<T> field, Set<String> out) {
      field.result().ifPresent(list -> list.asStreamOpt().result().ifPresent(stream -> stream.forEach(entry -> {
         String direct = (String)entry.asString().result().orElse(null);
         if (direct != null) {
            out.add(direct);
         } else {
            Dynamic<T> key = (Dynamic<T>)entry.get("key").result().orElse(null);
            if (key != null) {
               String plain = (String)key.asString().result().orElse(null);
               if (plain != null) {
                  out.add(plain);
               } else {
                  key.get("key").asString().result().ifPresent(out::add);
               }
            }
         }
      })));
   }

   private static <T> String extractKey(Dynamic<T> condition) {
      Dynamic<T> key = (Dynamic<T>)condition.get("key").result().orElse(null);
      if (key == null) {
         return "key.apoli.primary_active";
      }

      String direct = (String)key.asString().result().orElse(null);
      return direct != null ? direct : (String)key.get("key").asString().result().orElse("key.apoli.primary_active");
   }
}
