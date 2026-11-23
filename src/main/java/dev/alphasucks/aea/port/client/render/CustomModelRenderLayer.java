package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.List;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_742;

public class CustomModelRenderLayer extends class_3887<class_742, class_591<class_742>> {
   private final boolean slim;

   public CustomModelRenderLayer(class_3883<class_742, class_591<class_742>> parent, boolean slim) {
      super(parent);
      this.slim = slim;
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4199(class_4587 pose, class_4597 buffers, int light, class_742 player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.render(pose, buffers, light, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
   }

   public void render(
      class_4587 pose,
      class_4597 buffers,
      int light,
      class_742 player,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      class_591<class_742> model = this.method_17165();
      List<ResolvedLayer> layers = CustomModelRenderPower.collectTextureOverlays(player);

      for (int i = 0; i < layers.size(); i++) {
         ResolvedLayer layer = (ResolvedLayer)layers.get(i);
         class_2960 texture = DynamicTextures.resolve(layer.texture(this.slim), player);
         TextureOverlays.render(model, layer, texture, 1.0F, ageInTicks, player, pose, buffers, light);
      }

      List<GeometryRender> geometry = CustomModelRenderPower.collectGeometry(player);
      if (!geometry.isEmpty()) {
         class_591<class_742> rest = PlayerRestPose.get();

         for (GeometryRender render : geometry) {
            CustomModel custom = CustomModelManager.get(render.model(), render.bindBodyParts());
            if (custom != null) {
               if (render.bindBodyParts()) {
                  GeometryRenderer.syncPlayer(custom, model, rest);
               } else {
                  GeometryRenderer.resetAll(custom);
               }

               AnimationPlayer.apply(player, render, custom, partialTick);
               GeometryRenderer.applyVisibility(custom, render.bodyParts());
               GeometryRenderer.draw(render, custom, pose, buffers, light, player);
            }
         }
      }
   }
}
