package dev.alphasucks.aea.port.compat.accessory.power;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.compat.accessory.power.BasePreventAccessoryPower.Config;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class PreventAccessoryUnequipPower extends BasePreventAccessoryPower {
   public static final class_2960 CANONICAL = Apoli.id("prevent_accessory_unequip");

   public MapCodec<Config> configCodec() {
      return baseCodec();
   }

   public static boolean isPrevented(class_1309 entity, AccessorySlotRef ref, class_1799 stack) {
      return isPrevented(entity, CANONICAL, ref, stack);
   }
}
