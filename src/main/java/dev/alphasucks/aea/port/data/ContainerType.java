package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_1277;
import net.minecraft.class_1707;
import net.minecraft.class_1716;
import net.minecraft.class_1722;
import net.minecraft.class_2561;
import net.minecraft.class_3542;
import net.minecraft.class_3908;
import net.minecraft.class_747;

public enum ContainerType implements class_3542 {
   CHEST("chest", 27),
   HOPPER("hopper", 5),
   DROPPER("dropper", 9),
   DISPENSER("dispenser", 9),
   DOUBLE_CHEST("double_chest", 54);

   public static final Codec<ContainerType> CODEC = class_3542.method_28140(ContainerType::values);
   private final String name;
   private final int slots;

   ContainerType(String name, int slots) {
      this.name = name;
      this.slots = slots;
   }

   public int slots() {
      return this.slots;
   }

   public class_3908 menuProvider(class_2561 title, class_1277 container) {
      return new class_747((id, inv, p) -> {
         return switch (this) {
            case CHEST -> class_1707.method_19245(id, inv, container);
            case HOPPER -> new class_1722(id, inv, container);
            case DROPPER, DISPENSER -> new class_1716(id, inv, container);
            case DOUBLE_CHEST -> class_1707.method_19247(id, inv, container);
         };
      }, title);
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
