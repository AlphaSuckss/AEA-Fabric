package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.client.render.AnimationPlayer;
import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.client.render.GeometryRenderer;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;

public class MinionCustomModelLayer extends class_3887<MinionEntity, MinionModel> {
   private final MinionRenderer renderer;
   private MinionModel restPose;

   public MinionCustomModelLayer(MinionRenderer renderer) {
      super(renderer);
      this.renderer = renderer;
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4199(class_4587 pose, class_4597 buffers, int light, MinionEntity minion, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      this.render(pose, buffers, light, minion, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
   }

   public void render(
      class_4587 pose,
      class_4597 buffers,
      int light,
      MinionEntity minion,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      List<GeometryRender> geometry = this.renderer.activeGeometry();
      if (!geometry.isEmpty()) {
         MinionModel model = this.method_17165();
         MinionModel rest = this.restPose();

         for (int i = 0; i < geometry.size(); i++) {
            GeometryRender render = (GeometryRender)geometry.get(i);
            CustomModel custom = CustomModelManager.get(render.model(), false);
            if (custom != null) {
               GeometryRenderer.resetAll(custom);
               if (render.bindBodyParts()) {
                  for (String name : MinionModel.BONES) {
                     GeometryRenderer.syncBone(custom, name, model.bone(name), rest.bone(name));
                  }
               }

               AnimationPlayer.apply(minion, render, custom, partialTick);
               GeometryRenderer.applyVisibility(custom, render.bodyParts());
               GeometryRenderer.draw(render, custom, pose, buffers, light, minion);
            }
         }
      }
   }

   private MinionModel restPose() {
      if (this.restPose == null) {
         this.restPose = new MinionModel(class_310.method_1551().method_31974().method_32072(SummonModelLayers.MINION));
      }

      return this.restPose;
   }
}
