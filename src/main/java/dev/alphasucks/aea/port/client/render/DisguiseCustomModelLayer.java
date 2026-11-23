package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_742;

public class DisguiseCustomModelLayer<T extends class_1309, M extends class_583<T>> extends class_3887<T, M> {
   public DisguiseCustomModelLayer(class_3883<T, M> parent) {
      super(parent);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4199(class_4587 pose, class_4597 buffers, int light, T dummy, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.render(pose, buffers, light, dummy, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
   }

   public void render(
      class_4587 pose,
      class_4597 buffers,
      int light,
      T dummy,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (ClientDisguiseManager.renderActor() instanceof class_742 player) {
         if (this.method_17165() instanceof class_572<?> humanoid) {
            boolean var20 = isSlim(player);
            List overlays = CustomModelRenderPower.collectTextureOverlays(player);

            for (int geometry = 0; geometry < overlays.size(); geometry++) {
               ResolvedLayer layer = (ResolvedLayer)overlays.get(geometry);
               class_2960 texture = DynamicTextures.resolve(layer.texture(var20), player);
               TextureOverlays.render(humanoid, layer, texture, 1.0F, ageInTicks, player, pose, buffers, light);
            }

            List<GeometryRender> geometry = CustomModelRenderPower.collectGeometry(player);

            for (int i = 0; i < geometry.size(); i++) {
               GeometryRender render = (GeometryRender)geometry.get(i);
               CustomModel custom = CustomModelManager.get(render.model(), render.bindBodyParts());
               if (custom != null) {
                  if (render.bindBodyParts()) {
                     GeometryRenderer.syncHumanoid(custom, humanoid);
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

   private static boolean isSlim(class_742 player) {
      return "slim".equals(player.method_3121());
   }
}
