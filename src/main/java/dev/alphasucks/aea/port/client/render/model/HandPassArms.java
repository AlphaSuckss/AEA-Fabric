package dev.alphasucks.aea.port.client.render.model;

import net.minecraft.class_591;
import net.minecraft.class_630;

public final class HandPassArms {
   private static final float PIVOT_X = 5.0F;
   private static final float PIVOT_Y = 2.0F;

   private HandPassArms() {
   }

   public static void reset(class_591<?> model) {
      reset(model.field_3401, -5.0F);
      reset(model.field_3486, -5.0F);
      reset(model.field_27433, 5.0F);
      reset(model.field_3484, 5.0F);
   }

   private static void reset(class_630 part, float x) {
      part.field_3657 = x;
      part.field_3656 = 2.0F;
      part.field_3655 = 0.0F;
      part.field_3654 = 0.0F;
      part.field_3675 = 0.0F;
      part.field_3674 = 0.0F;
      part.field_37938 = 1.0F;
      part.field_37939 = 1.0F;
      part.field_37940 = 1.0F;
   }
}
