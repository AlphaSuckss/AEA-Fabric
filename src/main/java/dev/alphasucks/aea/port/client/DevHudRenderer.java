package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class DevHudRenderer {
   private static final int HEADER_COLOUR = -11141121;
   private static final int LINE_COLOUR = -1;

   private DevHudRenderer() {
   }

   public static void render(class_332 graphics, float partialTick) {
      if (ClientDevMode.enabled()) {
         class_310 mc = class_310.method_1551();
         class_746 player = mc.field_1724;
         if (player != null && !mc.field_1690.field_1842) {
            PowerContainer container = PowerContainer.of(player);
            if (container != null) {
               List<String> lines = new ArrayList();

               for (class_2960 powerId : ClientPowerState.localPowers()) {
                  if (PowerResources.isResource(powerId)) {
                     int size = Math.max(1, PowerResources.size(container, powerId));

                     for (int slot = 0; slot < size; slot++) {
                        OptionalInt value = size > 1 ? PowerResources.readAt(container, powerId, slot) : PowerResources.read(container, powerId);
                        if (!value.isEmpty()) {
                           OptionalInt max = PowerResources.bound(container, powerId, true);
                           StringBuilder line = new StringBuilder();
                           line.append(value.getAsInt());
                           if (max.isPresent()) {
                              line.append('/').append(max.getAsInt());
                           }

                           line.append(" - ").append(powerId);
                           if (size > 1) {
                              line.append('[').append(slot).append(']');
                           }

                           lines.add(line.toString());
                        }
                     }
                  }
               }

               lines.sort(String::compareTo);
               int x = 4;
               int y = 4;
               graphics.method_51439(mc.field_1772, class_2561.method_43470("Apoli dev mode — cooldowns off"), x, y, -11141121, true);
               y += 12;

               for (int i = 0; i < lines.size(); i++) {
                  graphics.method_51439(mc.field_1772, class_2561.method_43470((String)lines.get(i)), x, y, -1, true);
                  y += 10;
               }
            }
         }
      }
   }
}
