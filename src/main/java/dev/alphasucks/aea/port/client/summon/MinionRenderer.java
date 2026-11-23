package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import java.util.List;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_927;
import net.minecraft.class_5617.class_5618;
import org.jetbrains.annotations.Nullable;

public class MinionRenderer extends class_927<MinionEntity, MinionModel> {
   private List<GeometryRender> geometry = List.of();

   public MinionRenderer(class_5618 context) {
      super(context, new MinionModel(context.method_32167(SummonModelLayers.MINION)), 0.5F);
      this.method_4046(new MinionCustomModelLayer(this));
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4072(MinionEntity minion, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light) {
      this.render(minion, yaw, partialTick, pose, buffers, light);
   }

   public void render(MinionEntity minion, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light) {
      this.geometry = CustomModelRenderPower.collectGeometry(minion);
      super.method_4072(minion, yaw, partialTick, pose, buffers, light);
      this.geometry = List.of();
   }

   public List<GeometryRender> activeGeometry() {
      return this.geometry;
   }

   // original carried a javac bridge from the intermediary name
   @Override
   protected class_1921 method_24302(MinionEntity minion, boolean bodyVisible, boolean translucent, boolean glowing) {
      return this.getRenderType(minion, bodyVisible, translucent, glowing);
   }

   @Nullable
   protected class_1921 getRenderType(MinionEntity minion, boolean bodyVisible, boolean translucent, boolean glowing) {
      for (int i = 0; i < this.geometry.size(); i++) {
         GeometryRender render = (GeometryRender)this.geometry.get(i);
         if (!render.renderAsOverlay() && CustomModelManager.get(render.model()) != null) {
            return null;
         }
      }

      return super.method_24302(minion, bodyVisible, translucent, glowing);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public class_2960 method_3931(MinionEntity minion) {
      return this.getTextureLocation(minion);
   }

   public class_2960 getTextureLocation(MinionEntity minion) {
      return minion.getTexture();
   }

   // original carried a javac bridge from the intermediary name
   @Override
   protected void method_4042(MinionEntity minion, class_4587 pose, float partialTick) {
      this.scale(minion, pose, partialTick);
   }

   protected void scale(MinionEntity minion, class_4587 pose, float partialTick) {
      float scale = minion.getMinionScale();
      pose.method_22905(scale, scale, scale);
   }
}
