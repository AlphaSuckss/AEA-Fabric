package dev.alphasucks.aea.port.scale;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum ScaleEasing implements class_3542 {
   LINEAR("linear") {
      @Override
      public float apply(float t) {
         return t;
      }
   },
   EASE_IN_SINE("ease_in_sine") {
      @Override
      public float apply(float t) {
         return 1.0F - (float)Math.cos(t * Math.PI / 2.0);
      }
   },
   EASE_OUT_SINE("ease_out_sine") {
      @Override
      public float apply(float t) {
         return (float)Math.sin(t * Math.PI / 2.0);
      }
   },
   EASE_IN_OUT_SINE("ease_in_out_sine") {
      @Override
      public float apply(float t) {
         return (float)(-(Math.cos(Math.PI * t) - 1.0) / 2.0);
      }
   },
   EASE_IN_QUAD("ease_in_quad") {
      @Override
      public float apply(float t) {
         return t * t;
      }
   },
   EASE_OUT_QUAD("ease_out_quad") {
      @Override
      public float apply(float t) {
         return 1.0F - (1.0F - t) * (1.0F - t);
      }
   },
   EASE_IN_OUT_QUAD("ease_in_out_quad") {
      @Override
      public float apply(float t) {
         return t < 0.5F ? 2.0F * t * t : 1.0F - (float)Math.pow(-2.0 * t + 2.0, 2.0) / 2.0F;
      }
   },
   EASE_IN_CUBIC("ease_in_cubic") {
      @Override
      public float apply(float t) {
         return t * t * t;
      }
   },
   EASE_OUT_CUBIC("ease_out_cubic") {
      @Override
      public float apply(float t) {
         return 1.0F - (float)Math.pow(1.0 - t, 3.0);
      }
   },
   EASE_IN_OUT_CUBIC("ease_in_out_cubic") {
      @Override
      public float apply(float t) {
         return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0 * t + 2.0, 3.0) / 2.0F;
      }
   },
   EASE_IN_EXPO("ease_in_expo") {
      @Override
      public float apply(float t) {
         return t <= 0.0F ? 0.0F : (float)Math.pow(2.0, 10.0 * t - 10.0);
      }
   },
   EASE_OUT_EXPO("ease_out_expo") {
      @Override
      public float apply(float t) {
         return t >= 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0 * t);
      }
   },
   EASE_OUT_BACK("ease_out_back") {
      @Override
      public float apply(float t) {
         float c1 = 1.70158F;
         float c3 = c1 + 1.0F;
         float u = t - 1.0F;
         return 1.0F + c3 * u * u * u + c1 * u * u;
      }
   },
   EASE_OUT_BOUNCE("ease_out_bounce") {
      @Override
      public float apply(float t) {
         float n1 = 7.5625F;
         float d1 = 2.75F;
         if (t < 1.0F / d1) {
            return n1 * t * t;
         } else if (t < 2.0F / d1) {
            float u = t - 1.5F / d1;
            return n1 * u * u + 0.75F;
         } else if (t < 2.5F / d1) {
            float u = t - 2.25F / d1;
            return n1 * u * u + 0.9375F;
         } else {
            float u = t - 2.625F / d1;
            return n1 * u * u + 0.984375F;
         }
      }
   };

   public static final Codec<ScaleEasing> CODEC = class_3542.method_28140(ScaleEasing::values);
   private final String name;

   ScaleEasing(String name) {
      this.name = name;
   }

   public abstract float apply(float var1);

   @Override
   public String method_15434() {
      return this.name;
   }

   public static ScaleEasing byOrdinal(int ordinal) {
      ScaleEasing[] values = values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LINEAR;
   }
}
