package dev.alphasucks.aea.port.client;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.class_2960;
import net.minecraft.class_304;

public final class KeyReleases {
   private static final Map<class_2960, class_304> PRESSED = new LinkedHashMap();
   private static Consumer<class_2960> sender = power -> {};

   private KeyReleases() {
   }

   public static void setSender(Consumer<class_2960> next) {
      sender = next;
   }

   public static void pressed(class_2960 powerId, class_304 key) {
      PRESSED.putIfAbsent(powerId, key);
   }

   public static void tick() {
      if (!PRESSED.isEmpty()) {
         Iterator<Entry<class_2960, class_304>> it = PRESSED.entrySet().iterator();

         while (it.hasNext()) {
            Entry<class_2960, class_304> entry = (Entry<class_2960, class_304>)it.next();
            class_304 key = (class_304)entry.getValue();
            if (!ApoliKeyMappings.isDown(key) && !ForcedKeys.isForced(key)) {
               it.remove();
               sender.accept((class_2960)entry.getKey());
            }
         }
      }
   }

   public static void clear() {
      PRESSED.clear();
   }
}
