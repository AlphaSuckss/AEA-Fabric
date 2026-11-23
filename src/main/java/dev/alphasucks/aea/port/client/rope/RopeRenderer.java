package dev.alphasucks.aea.port.client.rope;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_761;

public final class RopeRenderer {
   private RopeRenderer() {
   }

   public static void render(WorldRenderContext context) {
      for (VerletRopeState rope : RopeClientManager.getAll()) {
         if (rope.visible && rope.points.size() >= 2) {
            drawRope(context, rope);
         }
      }
   }

   private static void drawRope(WorldRenderContext context, VerletRopeState rope) {
      class_4587 poseStack = context.matrixStack();
      class_4597 consumers = context.consumers();
      class_4184 camera = context.camera();
      if (poseStack != null && consumers != null && camera != null) {
         class_243 cameraPos = camera.method_19326();
         poseStack.method_22903();
         poseStack.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
         class_4588 vc = consumers.getBuffer(class_1921.method_23028(rope.texture));
         float tickDelta = class_310.method_1551().method_1488();

         for (int i = 0; i < rope.points.size() - 1; i++) {
            RopePoint p0 = (RopePoint)rope.points.get(i);
            RopePoint p1 = (RopePoint)rope.points.get(i + 1);
            class_243 a = p0.prevPos.method_35590(p0.pos, tickDelta);
            class_243 b = p1.prevPos.method_35590(p1.pos, tickDelta);
            int light = class_761.method_23794(context.world(), class_2338.method_49638(a.method_1019(b).method_1021(0.5)));
            class_243 dir = b.method_1020(a);
            if (!(dir.method_1027() < 1.0E-6)) {
               dir = dir.method_1029();
               class_243 view = cameraPos.method_1020(a).method_1029();
               class_243 right = dir.method_1036(view);
               if (!(right.method_1027() < 1.0E-6)) {
                  right = right.method_1029().method_1021(0.1F);
                  class_243 a1 = a.method_1019(right);
                  class_243 a2 = a.method_1020(right);
                  class_243 b1 = b.method_1019(right);
                  class_243 b2 = b.method_1020(right);
                  vertex(poseStack, vc, a1, 0.0F, 0.0F, light);
                  vertex(poseStack, vc, b1, 0.0F, 1.0F, light);
                  vertex(poseStack, vc, b2, 1.0F, 1.0F, light);
                  vertex(poseStack, vc, a2, 1.0F, 0.0F, light);
               }
            }
         }

         poseStack.method_22909();
      }
   }

   private static void vertex(class_4587 poseStack, class_4588 vc, class_243 pos, float u, float v, int light) {
      vc.method_22918(poseStack.method_23760().method_23761(), (float)pos.field_1352, (float)pos.field_1351, (float)pos.field_1350)
         .method_1336(255, 255, 255, 255)
         .method_22913(u, v)
         .method_22916(light)
         .method_1344();
   }
}
