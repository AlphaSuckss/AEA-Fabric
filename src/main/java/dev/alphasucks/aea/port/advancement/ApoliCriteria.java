package dev.alphasucks.aea.port.advancement;

import net.minecraft.class_1297;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class ApoliCriteria {
   public static ResourceTrigger RESOURCE;
   public static PowerTrigger POWER;

   private ApoliCriteria() {
   }

   public static void register() {
      RESOURCE = CriterionRegistry.register(new ResourceTrigger());
      POWER = CriterionRegistry.register(new PowerTrigger());
   }

   public static void resourceChanged(@Nullable class_1297 owner, class_2960 resource, int value) {
      if (RESOURCE != null && owner instanceof class_3222 player) {
         RESOURCE.trigger(player, resource, value);
      }
   }

   public static void powerGranted(@Nullable class_1297 owner, class_2960 power, class_2960 source) {
      if (POWER != null && owner instanceof class_3222 player) {
         POWER.trigger(player, power, source);
      }
   }
}
