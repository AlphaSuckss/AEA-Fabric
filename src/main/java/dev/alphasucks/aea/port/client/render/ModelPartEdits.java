package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import dev.alphasucks.aea.port.data.ModelPartTransformation.Type;
import dev.alphasucks.aea.port.mixin.modelparts.ModelPartCubesAccessor;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_630;
import net.minecraft.class_630.class_628;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ModelPartEdits {
   private ModelPartEdits() {
   }

   public static float[] snapshot(class_630 part) {
      return new float[]{
         part.field_3657,
         part.field_3656,
         part.field_3655,
         part.field_3654,
         part.field_3675,
         part.field_3674,
         part.field_37938,
         part.field_37939,
         part.field_37940,
         part.field_3665 ? 1.0F : 0.0F,
         part.field_38456 ? 1.0F : 0.0F
      };
   }

   public static void restore(class_630 part, float[] o) {
      restorePose(part, o);
      part.field_3665 = o[9] != 0.0F;
      part.field_38456 = o[10] != 0.0F;
   }

   public static void restorePose(class_630 part, float[] o) {
      part.field_3657 = o[0];
      part.field_3656 = o[1];
      part.field_3655 = o[2];
      part.field_3654 = o[3];
      part.field_3675 = o[4];
      part.field_3674 = o[5];
      part.field_37938 = o[6];
      part.field_37939 = o[7];
      part.field_37940 = o[8];
   }

   public static boolean endPoint(class_630 part, float[] out) {
      List<class_628> cubes = ((ModelPartCubesAccessor)(Object)part).apoli$modelCubes();
      int n = cubes.size();
      if (n == 0) {
         return false;
      }

      float minX = Float.POSITIVE_INFINITY;
      float minZ = Float.POSITIVE_INFINITY;
      float maxX = Float.NEGATIVE_INFINITY;
      float maxY = Float.NEGATIVE_INFINITY;
      float maxZ = Float.NEGATIVE_INFINITY;

      for (int i = 0; i < n; i++) {
         class_628 cube = (class_628)cubes.get(i);
         minX = Math.min(minX, cube.field_3645);
         minZ = Math.min(minZ, cube.field_3643);
         maxX = Math.max(maxX, cube.field_3648);
         maxY = Math.max(maxY, cube.field_3647);
         maxZ = Math.max(maxZ, cube.field_3646);
      }

      out[0] = (minX + maxX) * 0.5F;
      out[1] = maxY;
      out[2] = (minZ + maxZ) * 0.5F;
      return true;
   }

   public static boolean changesVisibility(Type type) {
      return type == Type.VISIBLE || type == Type.HIDDEN;
   }

   public static boolean hides(Slot slot) {
      if (slot.weight() < 0.5F) {
         return false;
      }

      Type type = slot.transformation().type();
      return type == Type.VISIBLE ? slot.value() == 0.0F : type == Type.HIDDEN && slot.value() != 0.0F;
   }

   public static void apply(class_630 part, Slot slot, @Nullable float[] o) {
      ModelPartTransformation t = slot.transformation();
      float value = slot.value();
      float weight = slot.weight();
      boolean override = t.overrideAnimation();
      switch (t.type()) {
         case PITCH:
            part.field_3654 = override ? part.field_3654 + (value - part.field_3654) * weight : part.field_3654 + value * weight;
            break;
         case YAW:
            part.field_3675 = override ? part.field_3675 + (value - part.field_3675) * weight : part.field_3675 + value * weight;
            break;
         case ROLL:
            part.field_3674 = override ? part.field_3674 + (value - part.field_3674) * weight : part.field_3674 + value * weight;
            break;
         case VISIBLE:
            if (weight >= 0.5F) {
               part.field_3665 = value != 0.0F;
            }
            break;
         case HIDDEN:
            if (weight >= 0.5F) {
               part.field_38456 = value != 0.0F;
            }
            break;
         case X_SCALE:
            part.field_37938 = (o != null ? o[6] : 1.0F) + value * weight;
            break;
         case Y_SCALE:
            part.field_37939 = (o != null ? o[7] : 1.0F) + value * weight;
            break;
         case Z_SCALE:
            part.field_37940 = (o != null ? o[8] : 1.0F) + value * weight;
            break;
         case PIVOT_X:
            part.field_3657 += value * weight;
            break;
         case PIVOT_Y:
            part.field_3656 += value * weight;
            break;
         case PIVOT_Z:
            part.field_3655 += value * weight;
      }
   }
}
