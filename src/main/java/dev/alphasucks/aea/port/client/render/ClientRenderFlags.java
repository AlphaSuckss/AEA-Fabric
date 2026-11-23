package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class ClientRenderFlags {
   public static final int PREVENT_ENTITY_RENDER = 1;
   public static final int PREVENT_ENTITY_SELECTION = 2;
   public static final int ENTITY_GLOW = 4;
   public static final int PREVENT_BLOCK_SELECTION = 8;
   public static final int PREVENT_FEATURE_RENDER = 16;
   private static volatile int flags;

   private ClientRenderFlags() {
   }

   public static boolean has(int flag) {
      return (flags & flag) != 0;
   }

   public static void clientTick(class_310 client) {
      class_746 player = client.field_1724;
      if (player == null) {
         flags = 0;
      } else {
         PowerContainer container = PowerContainer.of(player);
         if (container != null && !container.isEmpty()) {
            int next = bit(container, ApoliIds.PREVENT_ENTITY_RENDER, 1)
               | bit(container, ApoliIds.PREVENT_ENTITY_SELECTION, 2)
               | bit(container, ApoliIds.ENTITY_GLOW, 4)
               | bit(container, ApoliIds.PREVENT_BLOCK_SELECTION, 8)
               | bit(container, ApoliIds.PREVENT_FEATURE_RENDER, 16);
            flags = next;
         } else {
            flags = 0;
         }
      }
   }

   public static void clear() {
      flags = 0;
   }

   private static int bit(PowerContainer container, class_2960 typeId, int flag) {
      return container.powersOfType(typeId).isEmpty() ? 0 : flag;
   }
}
