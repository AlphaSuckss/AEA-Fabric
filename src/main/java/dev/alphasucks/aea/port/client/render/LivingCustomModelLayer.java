package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1309;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_630;

public class LivingCustomModelLayer<T extends class_1309, M extends class_583<T>> extends class_3887<T, M> {
   public LivingCustomModelLayer(class_3883<T, M> parent) {
      super(parent);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4199(class_4587 pose, class_4597 buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.render(pose, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
   }

   public void render(
      class_4587 pose,
      class_4597 buffers,
      int light,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      List<ResolvedLayer> layers = CustomModelRenderPower.collectTextureOverlays(entity);

      for (int i = 0; i < layers.size(); i++) {
         ResolvedLayer layer = (ResolvedLayer)layers.get(i);
         TextureOverlays.render(
            this.method_17165(), layer, DynamicTextures.resolve(layer.texture(false), entity), 1.0F, ageInTicks, entity, pose, buffers, light
         );
      }

      if (!(entity instanceof MinionEntity)) {
         List<GeometryRender> geometry = CustomModelRenderPower.collectGeometry(entity);
         if (!geometry.isEmpty()) {
            Map<String, class_630> parts = ModelPartNames.of(this.method_17165());

            for (int i = 0; i < geometry.size(); i++) {
               GeometryRender render = (GeometryRender)geometry.get(i);
               CustomModel custom = CustomModelManager.get(render.model(), render.bindBodyParts());
               if (custom != null) {
                  if (render.bindBodyParts()) {
                     GeometryRenderer.syncNamed(custom, parts);
                  } else {
                     GeometryRenderer.resetAll(custom);
                  }

                  AnimationPlayer.apply(entity, render, custom, partialTick);
                  GeometryRenderer.applyVisibility(custom, render.bodyParts());
                  GeometryRenderer.draw(render, custom, pose, buffers, light, entity);
               }
            }
         }
      }
   }
}
