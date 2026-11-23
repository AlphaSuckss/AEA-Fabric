package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.client.render.ArmEdits;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.FirstPersonFrame;
import dev.alphasucks.aea.port.data.HumanoidPose;
import dev.alphasucks.aea.port.data.ModelPartAnchor.Frame;
import dev.alphasucks.aea.port.mixin.firstperson.GameRendererFovInvoker;
import dev.alphasucks.aea.port.mixin.firstperson.ItemInHandRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class FirstPersonHandAnchor {
   private static final float PIXEL = 0.0625F;
   private static final float PI = (float) Math.PI;

   private FirstPersonHandAnchor() {
   }

   @Nullable
   public static Frame frame(class_310 mc, class_1297 entity, BodyPart part, class_243 base, float partialTick) {
      class_746 player = mc.field_1724;
      if (player == null || entity != player || mc.method_1560() != player) {
         return null;
      }

      if (mc.field_1690.method_31044().method_31034() && !player.method_6113() && !player.method_31550() && !player.method_7325()) {
         BodyPart sided = part.sided(player);
         if (!sided.isGroup() && sided.hasPoint()) {
            class_1306 arm;
            int limb;
            if (sided.limbs() == 4) {
               arm = class_1306.field_6183;
               limb = 2;
            } else {
               if (sided.limbs() != 8) {
                  return null;
               }

               arm = class_1306.field_6182;
               limb = 3;
            }

            boolean main = arm == player.method_6068();
            class_1268 hand = main ? class_1268.field_5808 : class_1268.field_5810;
            ItemInHandRendererAccessor renderer = (ItemInHandRendererAccessor)mc.method_1561().method_43336();
            class_1799 stack = main ? renderer.apoli$mainHandItem() : renderer.apoli$offHandItem();
            float height = main
               ? class_3532.method_16439(partialTick, renderer.apoli$oMainHandHeight(), renderer.apoli$mainHandHeight())
               : class_3532.method_16439(partialTick, renderer.apoli$oOffHandHeight(), renderer.apoli$offHandHeight());
            int edits = ArmEdits.of(player, true);
            boolean held = ArmEdits.held(edits, arm);
            float equip = held ? 0.0F : 1.0F - height;
            class_1268 swinging = player.field_6266 == null ? class_1268.field_5808 : player.field_6266;
            float swing = !held && swinging == hand ? player.method_6055(partialTick) : 0.0F;
            float side = arm == class_1306.field_6183 ? 1.0F : -1.0F;
            class_4587 pose = new class_4587();
            float xBob = class_3532.method_16439(partialTick, player.field_3914, player.field_3916);
            float yBob = class_3532.method_16439(partialTick, player.field_3931, player.field_3932);
            pose.method_22907(class_7833.field_40714.rotationDegrees((player.method_5695(partialTick) - xBob) * 0.1F));
            pose.method_22907(class_7833.field_40716.rotationDegrees((player.method_5705(partialTick) - yBob) * 0.1F));
            float xRot = 0.0F;
            float yRot = 0.0F;
            float zRot = 0.0F;
            Vector4f at;
            Matrix3f linear;
            if (!stack.method_7960() && (!ArmEdits.edited(edits, arm) || stack.method_31574(class_1802.field_8204))) {
               itemTransform(pose, side, equip, swing);
               linear = new Matrix3f(pose.method_23760().method_23761()).scale(-1.0F, -1.0F, 1.0F);
               at = pose.method_23760().method_23761().transform(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F));
            } else {
               FirstPersonFrame.arm(pose.method_23760().method_23761(), side, equip, swing);
               HumanoidPose arms = HumanoidPose.firstPersonArms(player, ModelPartAnimator.timeline(), ModelPartAnimator.now(player));
               float[] point = new float[3];
               sided.pointInto(arms.poses(), new double[3], point);
               linear = new Matrix3f(pose.method_23760().method_23761());
               at = pose.method_23760().method_23761().transform(new Vector4f(point[0] * 0.0625F, point[1] * 0.0625F, point[2] * 0.0625F, 1.0F));
               xRot = arms.xRot(limb);
               yRot = arms.yRot(limb);
               zRot = arms.zRot(limb);
            }

            class_4184 camera = mc.field_1773.method_19418();
            GameRendererFovInvoker fov = (GameRendererFovInvoker)mc.field_1773;
            double ratio = Math.tan(Math.toRadians(fov.apoli$getFov(camera, partialTick, true)) * 0.5)
               / Math.tan(Math.toRadians(fov.apoli$getFov(camera, partialTick, false)) * 0.5);
            Vector3f left = camera.method_35689();
            Vector3f up = camera.method_19336();
            Vector3f look = camera.method_19335();
            Matrix3f viewToWorld = new Matrix3f(-left.x, -left.y, -left.z, up.x, up.y, up.z, -look.x, -look.y, -look.z);
            Vector3f offset = viewToWorld.transform((float)(at.x * ratio), (float)(at.y * ratio), at.z, new Vector3f());
            class_243 position = camera.method_19326().method_1031(offset.x, offset.y, offset.z);
            return Frame.world(position.method_1020(base), xRot, yRot, zRot, viewToWorld.mul(linear, new Matrix3f()));
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static void itemTransform(class_4587 pose, float side, float equip, float swing) {
      float root = class_3532.method_15355(swing);
      pose.method_46416(
         side * -0.4F * class_3532.method_15374(root * (float) Math.PI),
         0.2F * class_3532.method_15374(root * (float) Math.PI * 2.0F),
         -0.2F * class_3532.method_15374(swing * (float) Math.PI)
      );
      pose.method_46416(side * 0.56F, -0.52F + equip * -0.6F, -0.72F);
      float twist = class_3532.method_15374(swing * swing * (float) Math.PI);
      pose.method_22907(class_7833.field_40716.rotationDegrees(side * (45.0F + twist * -20.0F)));
      float arc = class_3532.method_15374(root * (float) Math.PI);
      pose.method_22907(class_7833.field_40718.rotationDegrees(side * arc * -20.0F));
      pose.method_22907(class_7833.field_40714.rotationDegrees(arc * -80.0F));
      pose.method_22907(class_7833.field_40716.rotationDegrees(side * -45.0F));
   }
}
