package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.render.model.ExtraTexturedModel;
import dev.alphasucks.aea.port.power.builtin.ModifyPlayerModelPower;
import java.util.List;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_922;

public class PlayerModelExtraTextureLayer extends class_3887<class_742, class_591<class_742>> {
   public PlayerModelExtraTextureLayer(class_3883<class_742, class_591<class_742>> parent) {
      super(parent);
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
      if (this.method_17165() instanceof ExtraTexturedModel model) {
         List<class_630> parts = model.extraTexturedParts();
         if (!parts.isEmpty()) {
            class_2960 declared = ModifyPlayerModelPower.firstActiveModelTexture(player);
            class_2960 texture = declared == null ? model.defaultExtraTexture() : DynamicTextures.resolve(declared, player);
            class_4588 consumer = buffers.getBuffer(class_1921.method_23578(texture));
            int overlay = class_922.method_23622(player, 0.0F);

            for (int i = 0; i < parts.size(); i++) {
               ((class_630)parts.get(i)).method_22698(pose, consumer, light, overlay);
            }
         }
      }
   }
}
