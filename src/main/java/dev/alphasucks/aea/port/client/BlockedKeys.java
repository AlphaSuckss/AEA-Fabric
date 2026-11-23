package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.power.builtin.PreventKeyPressPower;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_304;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class BlockedKeys {
   private static boolean any;
   private static boolean all;
   private static boolean scroll;
   private static boolean forced;
   private static Set<String> named = Set.of();

   private BlockedKeys() {
   }

   public static void tick() {
      class_1657 player = class_310.method_1551().field_1724;
      if (any || PreventKeyPressPower.any(player)) {
         boolean[] flags = new boolean[]{false, false, false, false, false};
         Set<String> keys = new HashSet();
         PreventKeyPressPower.forEachActive(player, cfg -> {
            flags[0] = true;
            if (cfg.blocksEverything()) {
               flags[1] = true;
            }

            if (cfg.blocksScroll()) {
               flags[2] = true;
            }

            if (cfg.affectForced()) {
               flags[3] = true;
            }

            if (cfg.unpress()) {
               flags[4] = true;
            }

            cfg.keys().ifPresent(list -> {
               for (Key key : list) {
                  keys.add(key.key());
               }
            });
         });
         boolean wasAny = any;
         any = flags[0];
         all = flags[1];
         scroll = flags[2];
         forced = flags[3];
         named = keys.isEmpty() ? Set.of() : Set.copyOf(keys);
         if (any != wasAny && (!any || flags[4])) {
            class_304.method_1437();
         }
      }
   }

   public static void clear() {
      boolean wasAny = any;
      any = false;
      all = false;
      scroll = false;
      forced = false;
      named = Set.of();
      if (wasAny) {
         class_304.method_1437();
      }
   }

   public static boolean active() {
      return any;
   }

   public static boolean blocksScroll() {
      return any && scroll;
   }

   public static boolean blocks(class_304 mapping) {
      if (!any) {
         return false;
      } else {
         return !forced && ForcedKeys.isForced(mapping) ? false : all || named.contains(mapping.method_1431());
      }
   }

   public static boolean blocks(String name) {
      return !any ? false : all || named.contains(name);
   }
}
