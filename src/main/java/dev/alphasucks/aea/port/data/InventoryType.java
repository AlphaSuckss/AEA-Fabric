package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum InventoryType implements class_3542 {
   INVENTORY("inventory"),
   POWER("power");

   public static final Codec<InventoryType> CODEC = class_3542.method_28140(InventoryType::values);
   private final String name;

   InventoryType(String n) {
      this.name = n;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
