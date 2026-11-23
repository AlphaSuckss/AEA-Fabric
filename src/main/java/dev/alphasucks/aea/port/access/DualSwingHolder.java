package dev.alphasucks.aea.port.access;

import net.minecraft.class_1309;

public interface DualSwingHolder {
   boolean apoli$isSwingingBothArms();

   void apoli$setSwingingBothArms(boolean var1);

   static boolean of(class_1309 entity) {
      return entity instanceof DualSwingHolder holder && holder.apoli$isSwingingBothArms();
   }
}
