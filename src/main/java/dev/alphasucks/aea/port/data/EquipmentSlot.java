package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_1304;
import net.minecraft.class_3542;

public enum EquipmentSlot implements class_3542 {
   MAINHAND("mainhand", class_1304.field_6173),
   OFFHAND("offhand", class_1304.field_6171),
   HEAD("head", class_1304.field_6169),
   CHEST("chest", class_1304.field_6174),
   LEGS("legs", class_1304.field_6172),
   FEET("feet", class_1304.field_6166);

   public static final Codec<EquipmentSlot> CODEC = class_3542.method_28140(EquipmentSlot::values);
   private final String name;
   private final class_1304 vanilla;

   EquipmentSlot(String name, class_1304 vanilla) {
      this.name = name;
      this.vanilla = vanilla;
   }

   public class_1304 vanilla() {
      return this.vanilla;
   }

   public static EquipmentSlot fromVanilla(class_1304 slot) {
      for (EquipmentSlot s : values()) {
         if (s.vanilla == slot) {
            return s;
         }
      }

      throw new IllegalArgumentException("No Apoli EquipmentSlot for vanilla " + slot);
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
