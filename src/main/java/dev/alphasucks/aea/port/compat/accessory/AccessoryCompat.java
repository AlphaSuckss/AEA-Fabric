package dev.alphasucks.aea.port.compat.accessory;

import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.backend.AccessoriesBackend;
import dev.alphasucks.aea.port.compat.accessory.backend.TrinketsBackend;
import dev.alphasucks.aea.port.compat.accessory.power.ActionOnAccessoryChangePower;

public final class AccessoryCompat {
   private AccessoryCompat() {
   }

   public static void init() {
      if (ModCompat.TRINKETS) {
         Accessories.register(new TrinketsBackend());
      }

      if (ModCompat.ACCESSORIES) {
         AccessoriesBackend accessories = new AccessoriesBackend();
         Accessories.register(accessories);
         accessories.registerEvents();
      }

      if (Accessories.anyPresent()) {
         Accessories.setChangeListener(ActionOnAccessoryChangePower::handle);
      }
   }
}
