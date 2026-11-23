package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.client.render.AnimationPlayer;
import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.client.render.GeometryRenderer;
import dev.alphasucks.aea.port.entity.CustomProjectileEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import net.minecraft.class_897;
import net.minecraft.class_918;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_5617.class_5618;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CustomProjectileRenderer extends class_897<CustomProjectileEntity> {
   private static final class_2960 MISSING = new class_2960("textures/misc/unknown_pack.png");
   private final class_918 itemRenderer;

   public CustomProjectileRenderer(class_5618 context) {
      super(context);
      this.itemRenderer = context.method_32168();
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_3936(CustomProjectileEntity entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light) {
      this.render(entity, yaw, partialTick, poseStack, buffers, light);
   }

   public void render(CustomProjectileEntity entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light) {
      List<GeometryRender> geometry = resolveGeometry(entity);
      if (geometry.isEmpty() || !this.renderGeometry(entity, geometry, partialTick, poseStack, buffers, light)) {
         class_1799 stack = entity.getItem();
         if (stack.method_7960()) {
            this.renderBillboard(entity, poseStack, buffers, light);
         } else {
            this.renderItem(entity, stack, poseStack, buffers, light);
         }
      }

      super.method_3936(entity, yaw, partialTick, poseStack, buffers, light);
   }

   private void renderItem(CustomProjectileEntity entity, class_1799 stack, class_4587 poseStack, class_4597 buffers, int light) {
      poseStack.method_22903();
      poseStack.method_22907(this.field_4676.method_24197());
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
      this.itemRenderer
         .method_23178(stack, class_811.field_4318, light, class_4608.field_21444, poseStack, buffers, entity.method_37908(), entity.method_5628());
      poseStack.method_22909();
   }

   private void renderBillboard(CustomProjectileEntity entity, class_4587 poseStack, class_4597 buffers, int light) {
      poseStack.method_22903();
      poseStack.method_22907(this.field_4676.method_24197());
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
      class_4588 consumer = buffers.getBuffer(class_1921.method_23578(this.getTextureLocation(entity)));
      billboardQuad(consumer, poseStack.method_23760(), light);
      poseStack.method_22909();
   }

   public static void billboardQuad(class_4588 consumer, class_4665 pose, int light) {
      Matrix4f matrix = pose.method_23761();
      Matrix3f normal = pose.method_23762();
      vertex(consumer, matrix, normal, light, 0.0F, 0, 0, 1);
      vertex(consumer, matrix, normal, light, 1.0F, 0, 1, 1);
      vertex(consumer, matrix, normal, light, 1.0F, 1, 1, 0);
      vertex(consumer, matrix, normal, light, 0.0F, 1, 0, 0);
   }

   public static List<GeometryRender> resolveGeometry(CustomProjectileEntity entity) {
      List<class_2960> stamped = entity.getModelPowers();
      if (!stamped.isEmpty()) {
         if (stamped.size() == 1) {
            GeometryRender synced = CustomModelRenderPower.geometryOf((class_2960)stamped.get(0));
            if (synced != null) {
               return List.of(synced);
            }
         } else {
            List<GeometryRender> synced = new ArrayList(stamped.size());

            for (int i = 0; i < stamped.size(); i++) {
               GeometryRender render = CustomModelRenderPower.geometryOf((class_2960)stamped.get(i));
               if (render != null) {
                  synced.add(render);
               }
            }

            if (!synced.isEmpty()) {
               return synced;
            }
         }
      }

      return CustomModelRenderPower.collectGeometry(entity);
   }

   private boolean renderGeometry(
      CustomProjectileEntity entity, List<GeometryRender> geometry, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      float bodyYaw = class_3532.method_16439(partialTick, entity.field_5982, entity.method_36454());
      float bodyPitch = class_3532.method_16439(partialTick, entity.field_6004, entity.method_36455());
      poseStack.method_22903();
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(bodyYaw + 180.0F));
      poseStack.method_22907(class_7833.field_40714.rotationDegrees(bodyPitch));
      poseStack.method_22905(-1.0F, -1.0F, 1.0F);
      poseStack.method_46416(0.0F, -1.5F, 0.0F);
      boolean drew = false;

      for (int i = 0; i < geometry.size(); i++) {
         GeometryRender render = (GeometryRender)geometry.get(i);
         CustomModel custom = CustomModelManager.get(render.model(), false);
         if (custom != null) {
            GeometryRenderer.resetAll(custom);
            AnimationPlayer.apply(entity, render, custom, partialTick);
            GeometryRenderer.applyVisibility(custom, render.bodyParts());
            GeometryRenderer.draw(render, custom, poseStack, buffers, light, entity);
            drew = true;
         }
      }

      poseStack.method_22909();
      return drew;
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public class_2960 method_3931(CustomProjectileEntity entity) {
      return this.getTextureLocation(entity);
   }

   public class_2960 getTextureLocation(CustomProjectileEntity entity) {
      return textureOf(entity);
   }

   public static class_2960 textureOf(CustomProjectileEntity entity) {
      class_2960 texture = entity.getTexture();
      return texture != null ? texture : MISSING;
   }

   private static void vertex(class_4588 consumer, Matrix4f matrix, Matrix3f normal, int light, float x, int y, int u, int v) {
      consumer.method_22918(matrix, x - 0.5F, y - 0.25F, 0.0F)
         .method_1336(255, 255, 255, 255)
         .method_22913(u, v)
         .method_22922(class_4608.field_21444)
         .method_22916(light)
         .method_23763(normal, 0.0F, 1.0F, 0.0F)
         .method_1344();
   }
}
