package dev.alphasucks.aea.port.entity.summon;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.mixin.summon.ChunkMapEntityMapAccessor;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3215;
import net.minecraft.class_3218;

public final class Summons {
   public static final class_2960 POWER_SOURCE = Apoli.id("summon");
   private static final double MOVED_SQR = 1.0E-4;

   private Summons() {
   }

   public static void resendIfMoved(class_1297 summon, class_243 spawnedAt) {
      if (!summon.method_31481() && summon.method_37908() instanceof class_3218 level) {
         if (!(summon.method_19538().method_1025(spawnedAt) < 1.0E-4)) {
            class_3215 chunks = level.method_14178();
            if (((ChunkMapEntityMapAccessor)chunks.field_17254).apoli$entityMap().containsKey(summon.method_5628())) {
               chunks.method_18753(summon);
               chunks.method_18755(summon);
            }
         }
      }
   }

   public static void grantPowers(class_1297 summon, List<class_2960> powers) {
      if (!powers.isEmpty()) {
         PowerContainer holder = PowerContainerAttachment.getOrCreate(summon);
         if (holder != null) {
            for (class_2960 power : powers) {
               holder.addPower(power, POWER_SOURCE);
            }
         }
      }
   }

   public static void expire(class_1309 summon) {
      if (!summon.method_31481()) {
         if (summon.method_37908() instanceof class_3218 && !summon.method_29504()) {
            summon.method_5643(summon.method_48923().method_51847(), Float.MAX_VALUE);
         }

         if (!summon.method_31481()) {
            summon.method_31472();
         }
      }
   }

   public static void onRemoved(class_1297 summon) {
      if (!summon.method_37908().field_9236) {
         DisguiseManager.remove(summon);
         HeldKeys.clearServer(summon.method_5667());
      }
   }
}
