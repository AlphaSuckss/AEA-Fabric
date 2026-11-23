package dev.alphasucks.aea.port.scale;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum ScaleOperation implements class_3542 {
   SET("set") {
      @Override
      public float apply(float current, float argument) {
         return argument;
      }
   },
   ADD("add") {
      @Override
      public float apply(float current, float argument) {
         return current + argument;
      }
   },
   SUBTRACT("subtract") {
      @Override
      public float apply(float current, float argument) {
         return current - argument;
      }
   },
   MULTIPLY("multiply") {
      @Override
      public float apply(float current, float argument) {
         return current * argument;
      }
   },
   DIVIDE("divide") {
      @Override
      public float apply(float current, float argument) {
         return argument == 0.0F ? current : current / argument;
      }
   },
   POWER("power") {
      @Override
      public float apply(float current, float argument) {
         return (float)Math.pow(current, argument);
      }
   };

   public static final Codec<ScaleOperation> CODEC = class_3542.method_28140(ScaleOperation::values);
   private final String name;

   ScaleOperation(String name) {
      this.name = name;
   }

   public abstract float apply(float var1, float var2);

   @Override
   public String method_15434() {
      return this.name;
   }
}
