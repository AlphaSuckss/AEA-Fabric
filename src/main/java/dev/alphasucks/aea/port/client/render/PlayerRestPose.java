package dev.alphasucks.aea.port.client.render;

import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_591;
import net.minecraft.class_742;

public final class PlayerRestPose {
   private static class_591<class_742> rest;

   private PlayerRestPose() {
   }

   public static class_591<class_742> get() {
      if (rest == null) {
         rest = new class_591(class_5607.method_32110(class_591.method_32028(class_5605.field_27715, false), 64, 64).method_32109(), false);
      }

      return rest;
   }
}
