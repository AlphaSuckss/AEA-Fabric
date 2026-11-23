package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.RenderMode;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;

public final class OverlayRenderTypes {
   private OverlayRenderTypes() {
   }

   public static class_1921 forMode(RenderMode mode, class_2960 texture) {
      return forMode(mode, texture, 0.0F, 0.0F);
   }

   public static class_1921 forMode(RenderMode mode, class_2960 texture, float ageInTicks, float scrollSpeed) {
      return switch (mode) {
         case TRANSLUCENT -> class_1921.method_23580(texture);
         case TRANSLUCENT_CULL -> class_1921.method_23689(texture);
         case CUTOUT -> class_1921.method_23576(texture);
         case CUTOUT_NO_CULL -> class_1921.method_23578(texture);
         case SOLID -> class_1921.method_23572(texture);
         case EMISSIVE -> class_1921.method_42600(texture);
         case EYES -> class_1921.method_23026(texture);
         case ENERGY_SWIRL -> energySwirl(texture, ageInTicks, scrollSpeed);
      };
   }

   public static class_1921 forGeometry(RenderMode mode, class_2960 texture, float ageInTicks, float scrollSpeed) {
      return switch (mode) {
         case EMISSIVE -> DepthWritingRenderTypes.emissive(texture);
         case EYES -> DepthWritingRenderTypes.eyes(texture);
         default -> forMode(mode, texture, ageInTicks, scrollSpeed);
      };
   }

   private static class_1921 energySwirl(class_2960 texture, float ageInTicks, float scrollSpeed) {
      if (scrollSpeed == 0.0F) {
         return class_1921.method_23018(texture, 0.0F, 0.0F);
      }

      float u = class_3532.method_15362(ageInTicks * 0.02F) * 3.0F % 1.0F;
      float v = ageInTicks * scrollSpeed % 1.0F;
      return class_1921.method_23018(texture, u, v);
   }
}
