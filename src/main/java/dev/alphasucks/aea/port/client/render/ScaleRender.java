package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_4050;
import net.minecraft.class_4587;

public final class ScaleRender {
   public static final float MATTRESS_DROP = 0.125F;
   private static final float NAME_TAG_GAP = 0.5F;
   private static boolean doll;

   private ScaleRender() {
   }

   public static void beginDoll() {
      doll = true;
   }

   public static void endDoll() {
      doll = false;
   }

   public static boolean inDoll() {
      return doll;
   }

   public static float modelWidth(class_1297 entity, float partialTick) {
      return Scales.appliedIfScaled(entity, doll ? ScaleTypes.DOLL : ScaleTypes.MODEL_WIDTH, partialTick);
   }

   public static float modelHeight(class_1297 entity, float partialTick) {
      return Scales.appliedIfScaled(entity, doll ? ScaleTypes.DOLL : ScaleTypes.MODEL_HEIGHT, partialTick);
   }

   public static boolean lying(class_1309 entity) {
      return entity.method_41328(class_4050.field_18078) && !ModelPartAnimator.overridesPose(entity);
   }

   public static void undoOuterScale(class_1297 entity, class_4587 poseStack, float partialTick) {
      float width = modelWidth(entity, partialTick);
      float height = modelHeight(entity, partialTick);
      if (width != 1.0F || height != 1.0F) {
         poseStack.method_22905(1.0F / width, 1.0F / height, 1.0F / width);
      }
   }

   public static float nameTagOffset(class_1297 entity, float offset, float partialTick) {
      if (!doll && !Scales.untouched(entity)) {
         float up = Scales.applied(entity, ScaleTypes.MODEL_HEIGHT, partialTick) / Scales.applied(entity, ScaleTypes.HITBOX_HEIGHT);
         return up == 1.0F ? offset : (offset - 0.5F) * up + 0.5F;
      } else {
         return offset;
      }
   }

   public static class_243 crouchOffset(class_1297 entity, class_243 offset, float partialTick) {
      if (offset.field_1351 == 0.0) {
         return offset;
      }

      float height = modelHeight(entity, partialTick);
      return height == 1.0F ? offset : new class_243(offset.field_1352, offset.field_1351 * height, offset.field_1350);
   }
}
