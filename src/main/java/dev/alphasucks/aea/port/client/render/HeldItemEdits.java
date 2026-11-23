package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_811;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public final class HeldItemEdits {
   public static final int NONE = 0;
   public static final int EDITED = 1;
   public static final int HIDDEN = 2;
   private static final float[] POSE = new float[9];
   private static final Quaternionf ROTATION = new Quaternionf();

   private HeldItemEdits() {
   }

   public static int resolve(@Nullable class_1309 entity, class_811 context, boolean leftHand) {
      if (entity != null && isHand(context)) {
         List<Slot> slots = ModelPartAnimator.update(entity);
         if (slots.isEmpty()) {
            return 0;
         }

         int hand = leftHand ? 524288 : 262144;
         boolean firstPerson = context.method_29998() || FirstPersonHeldItem.drawing();
         boolean edited = false;
         boolean hidden = false;
         float[] pose = POSE;

         for (int i = 0; i < slots.size(); i++) {
            Slot slot = (Slot)slots.get(i);
            float weight = slot.weight();
            if (!(weight <= 0.0F) && slot.rendersIn(firstPerson)) {
               ModelPartTransformation transformation = slot.transformation();
               if ((transformation.bodyPart().sided(entity).attachments() & hand) != 0) {
                  if (!edited) {
                     reset(pose);
                     edited = true;
                  }

                  float value = slot.value();
                  boolean override = transformation.overrideAnimation();
                  switch (transformation.type()) {
                     case PITCH:
                        pose[3] = rotate(pose[3], value, weight, override);
                        break;
                     case YAW:
                        pose[4] = rotate(pose[4], value, weight, override);
                        break;
                     case ROLL:
                        pose[5] = rotate(pose[5], value, weight, override);
                        break;
                     case X_SCALE:
                        pose[6] = 1.0F + value * weight;
                        break;
                     case Y_SCALE:
                        pose[7] = 1.0F + value * weight;
                        break;
                     case Z_SCALE:
                        pose[8] = 1.0F + value * weight;
                        break;
                     case PIVOT_X:
                        pose[0] += value * weight;
                        break;
                     case PIVOT_Y:
                        pose[1] += value * weight;
                        break;
                     case PIVOT_Z:
                        pose[2] += value * weight;
                        break;
                     case VISIBLE:
                        if (weight >= 0.5F) {
                           hidden = value == 0.0F;
                        }
                        break;
                     case HIDDEN:
                        if (weight >= 0.5F) {
                           hidden = value != 0.0F;
                        }
                  }
               }
            }
         }

         if (hidden) {
            return 2;
         } else {
            return edited ? 1 : 0;
         }
      } else {
         return 0;
      }
   }

   public static void apply(class_4587 poseStack) {
      float[] pose = POSE;
      if (pose[0] != 0.0F || pose[1] != 0.0F || pose[2] != 0.0F) {
         poseStack.method_46416(pose[0] / 16.0F, pose[1] / 16.0F, pose[2] / 16.0F);
      }

      if (pose[3] != 0.0F || pose[4] != 0.0F || pose[5] != 0.0F) {
         poseStack.method_22907(ROTATION.rotationZYX(pose[5], pose[4], pose[3]));
      }

      if (pose[6] != 1.0F || pose[7] != 1.0F || pose[8] != 1.0F) {
         poseStack.method_22905(pose[6], pose[7], pose[8]);
      }
   }

   private static boolean isHand(class_811 context) {
      return context == class_811.field_4320 || context == class_811.field_4323 || context == class_811.field_4322 || context == class_811.field_4321;
   }

   private static float rotate(float current, float value, float weight, boolean override) {
      return override ? current + (value - current) * weight : current + value * weight;
   }

   private static void reset(float[] pose) {
      pose[0] = 0.0F;
      pose[1] = 0.0F;
      pose[2] = 0.0F;
      pose[3] = 0.0F;
      pose[4] = 0.0F;
      pose[5] = 0.0F;
      pose[6] = 1.0F;
      pose[7] = 1.0F;
      pose[8] = 1.0F;
   }
}
