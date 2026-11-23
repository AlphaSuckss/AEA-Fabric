package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.data.FunctionalKey;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.keybind.ApoliKeybinds;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower.Config;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class PowerKeys {
   private PowerKeys() {
   }

   public static void collect(Power power, Collection<String> out) {
      Object cfg = power.config();
      if (cfg instanceof Config c) {
         out.add(c.key().key());
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.TogglePower.Config c) {
         out.add(c.key().key());
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.InventoryPower.Config c) {
         out.add(c.key().key());
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config c) {
         c.params().key().map(Key::key).ifPresent(out::add);
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.ActionOnKeySequencePower.Config c) {
         for (FunctionalKey fk : c.keys()) {
            out.add(fk.key().key());
         }

         out.addAll(c.atomKeys());
      }
   }

   @Nullable
   public static String activeKey(Power power) {
      Object cfg = power.config();
      if (cfg instanceof Config c) {
         return c.key().key();
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.InventoryPower.Config c) {
         return c.key().key();
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config c) {
         return (String)c.params().key().map(Key::key).orElse(null);
      } else if (cfg instanceof dev.alphasucks.aea.port.power.builtin.ActionOnKeySequencePower.Config c) {
         List<String> atoms = c.atomKeys();
         return atoms.isEmpty() ? null : (String)atoms.get(0);
      } else {
         return null;
      }
   }

   public static boolean uses(Power power, String key) {
      Set<String> keys = new LinkedHashSet(2);
      collect(power, keys);
      return keys.contains(key);
   }

   public static List<class_2960> heldPowersUsingKey(PowerContainer container, String key) {
      List<class_2960> out = new ArrayList();

      for (class_2960 id : container.allPowers()) {
         Power power = ApoliPowers.get(id);
         if (power != null && uses(power, key)) {
            out.add(id);
         }
      }

      return out;
   }

   public static List<String> knownKeys() {
      Set<String> out = new LinkedHashSet();
      out.add("key.apoli.primary_active");

      for (class_2960 id : ApoliKeybinds.view().keySet()) {
         out.add("key." + id.method_12836() + "." + id.method_12832().replace('/', '.'));
      }

      for (Power power : ApoliPowers.view().values()) {
         collect(power, out);
      }

      return new ArrayList(out);
   }

   public static List<String> keysHeldBy(PowerContainer container) {
      Set<String> out = new LinkedHashSet();

      for (class_2960 id : container.allPowers()) {
         Power power = ApoliPowers.get(id);
         if (power != null) {
            collect(power, out);
         }
      }

      return new ArrayList(out);
   }
}
