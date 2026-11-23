package dev.alphasucks.aea.port.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public final class FirstPersonHeldItem {
   private static final Quaternionf GRIP_PITCH = class_7833.field_40714.rotationDegrees(-90.0F);
   private static final Quaternionf GRIP_YAW = class_7833.field_40716.rotationDegrees(180.0F);
   @Nullable
   private static class_759 renderer;
   private static class_1799 stack = class_1799.field_8037;
   @Nullable
   private static class_1306 arm;
   private static boolean drawn;
   private static boolean drawing;

   private FirstPersonHeldItem() {
   }

   public static boolean attaches(class_742 player, class_1268 hand, class_1799 stack) {
      return !stack.method_7960() && !player.method_6123() ? !player.method_6115() || player.method_6014() <= 0 || player.method_6058() != hand : false;
   }

   public static void begin(class_759 renderer, class_1799 stack, class_1306 arm) {
      FirstPersonHeldItem.renderer = renderer;
      FirstPersonHeldItem.stack = stack;
      FirstPersonHeldItem.arm = arm;
      drawn = false;
   }

   public static boolean end() {
      boolean result = drawn;
      renderer = null;
      stack = class_1799.field_8037;
      arm = null;
      drawn = false;
      return result;
   }

   public static boolean drawing() {
      return drawing;
   }

   public static void render(class_591<?> model, class_630 rendered, class_742 player, class_4587 pose, class_4597 buffers, int light) {
      class_759 itemRenderer = renderer;
      if (itemRenderer != null && !drawn) {
         class_1306 side = rendered == model.field_3401 ? class_1306.field_6183 : (rendered == model.field_27433 ? class_1306.field_6182 : null);
         if (side != null && side == arm) {
            boolean left = side == class_1306.field_6182;
            drawn = true;
            drawing = true;
            pose.method_22903();

            try {
               model.method_2803(side, pose);
               pose.method_22907(GRIP_PITCH);
               pose.method_22907(GRIP_YAW);
               pose.method_46416((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
               itemRenderer.method_3233(player, stack, left ? class_811.field_4323 : class_811.field_4320, left, pose, buffers, light);
            } finally {
               pose.method_22909();
               drawing = false;
            }
         }
      }
   }
}
