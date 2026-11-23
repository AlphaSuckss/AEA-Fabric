package dev.alphasucks.aea.port.compat.accessory.power;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class PreventAccessoryEquipPower extends BasePreventAccessoryPower {
   public static final class_2960 CANONICAL = Apoli.id("prevent_accessory_equip");

   @Override
   public MapCodec<BasePreventAccessoryPower.Config> configCodec() {
      return baseCodec();
   }

   public static boolean isPrevented(class_1309 entity, AccessorySlotRef ref, class_1799 stack) {
      return isPrevented(entity, CANONICAL, ref, stack);
   }
}
