package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.client.render.DynamicTextures;
import dev.alphasucks.aea.port.client.render.TextureOverlays;
import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;

public class CloneTextureOverlayLayer extends class_3887<CloneEntity, CloneModel> {
   public CloneTextureOverlayLayer(class_3883<CloneEntity, CloneModel> parent) {
      super(parent);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4199(class_4587 pose, class_4597 buffers, int light, CloneEntity clone, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.render(pose, buffers, light, clone, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
   }

   public void render(
      class_4587 pose,
      class_4597 buffers,
      int light,
      CloneEntity clone,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      class_1309 owner = clone.method_35057();
      if (owner != null) {
         List<ResolvedLayer> layers = CustomModelRenderPower.collectTextureOverlays(owner);
         if (!layers.isEmpty()) {
            boolean slim = CloneRenderer.resolveSlim(clone);
            CloneModel model = this.method_17165();

            for (int i = 0; i < layers.size(); i++) {
               ResolvedLayer layer = (ResolvedLayer)layers.get(i);
               class_2960 texture = DynamicTextures.resolve(layer.texture(slim), owner);
               TextureOverlays.render(model, layer, texture, 1.0F, ageInTicks, owner, pose, buffers, light);
            }
         }
      }
   }
}
