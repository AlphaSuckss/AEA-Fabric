package dev.alphasucks.aea.port.particle;

import com.mojang.serialization.Codec;
import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2378;
import net.minecraft.class_2396;
import net.minecraft.class_7923;
import net.minecraft.class_2394.class_2395;

public final class ApoliParticles {
   public static final class_2396<CustomParticleOptions> CUSTOM = new class_2396<CustomParticleOptions>(false, CustomParticleOptions.DESERIALIZER) {
      @Override
      public Codec<CustomParticleOptions> method_29138() {
         return CustomParticleOptions.CODEC;
      }
   };

   private ApoliParticles() {
   }

   public static void register() {
      class_2378.method_10230(class_7923.field_41180, Apoli.id("custom"), CUSTOM);
   }
}
