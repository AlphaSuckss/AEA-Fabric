package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;
import net.minecraft.class_3610;
import net.minecraft.class_3959.class_242;

public enum FluidHandling implements class_3542 {
   ANY("any") {
      @Override
      public boolean test(class_3610 state) {
         return !state.method_15769();
      }
   },
   NONE("none") {
      @Override
      public boolean test(class_3610 state) {
         return state.method_15769();
      }
   },
   SOURCE_ONLY("source_only") {
      @Override
      public boolean test(class_3610 state) {
         return state.method_15771();
      }
   };

   public static final Codec<FluidHandling> CODEC = class_3542.method_28140(FluidHandling::values);
   private final String name;

   FluidHandling(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public abstract boolean test(class_3610 var1);

   public class_242 vanilla() {
      return switch (this) {
         case ANY -> class_242.field_1347;
         case NONE -> class_242.field_1348;
         case SOURCE_ONLY -> class_242.field_1345;
      };
   }
}
