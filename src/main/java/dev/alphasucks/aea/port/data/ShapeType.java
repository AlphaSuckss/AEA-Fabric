package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;
import net.minecraft.class_3959.class_3960;

public enum ShapeType implements class_3542 {
   COLLIDER("collider"),
   OUTLINE("outline"),
   VISUAL("visual");

   public static final Codec<ShapeType> CODEC = class_3542.method_28140(ShapeType::values);
   private final String name;

   ShapeType(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public class_3960 vanilla() {
      return switch (this) {
         case COLLIDER -> class_3960.field_17558;
         case OUTLINE -> class_3960.field_17559;
         case VISUAL -> class_3960.field_23142;
      };
   }
}
