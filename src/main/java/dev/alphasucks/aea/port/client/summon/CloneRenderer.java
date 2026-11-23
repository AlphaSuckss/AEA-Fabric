package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.client.render.DynamicTextures;
import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.Config;
import net.minecraft.class_1068;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_640;
import net.minecraft.class_742;
import net.minecraft.class_909;
import net.minecraft.class_989;
import net.minecraft.class_5617.class_5618;

public class CloneRenderer extends class_909<CloneEntity, CloneModel> {
   private final CloneModel wideModel = (CloneModel)this.method_4038();
   private final CloneModel slimModel;

   public CloneRenderer(class_5618 context) {
      super(context, new CloneModel(context.method_32167(SummonModelLayers.CLONE), false), 0.5F);
      this.slimModel = new CloneModel(context.method_32167(SummonModelLayers.CLONE_SLIM), true);
      this.method_4046(new class_989(this, context.method_43338()));
      this.method_4046(new CloneTextureOverlayLayer(this));
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_4072(CloneEntity clone, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light) {
      this.render(clone, yaw, partialTick, pose, buffers, light);
   }

   public void render(CloneEntity clone, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light) {
      this.field_4737 = resolveSlim(clone) ? this.slimModel : this.wideModel;
      if (clone.isSitting()) {
         pose.method_22903();
         pose.method_22904(0.0, -0.5, 0.0);
         super.method_4072(clone, yaw, partialTick, pose, buffers, light);
         pose.method_22909();
      } else {
         super.method_4072(clone, yaw, partialTick, pose, buffers, light);
      }
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public class_2960 method_3931(CloneEntity clone) {
      return this.getTextureLocation(clone);
   }

   public class_2960 getTextureLocation(CloneEntity clone) {
      class_640 disguise = ClientDisguiseManager.playerInfo(clone.method_5628());
      if (disguise != null) {
         return disguise.method_2968();
      }

      boolean slim = resolveSlim(clone);
      class_2960 custom = clone.getCustomTexture(slim);
      if (custom != null) {
         return custom;
      }

      class_1309 owner = clone.method_35057();
      if (owner != null) {
         Config overlay = CustomModelRenderPower.firstReplace(owner);
         if (overlay != null) {
            return DynamicTextures.resolve(slim ? overlay.slim() : overlay.wide(), owner);
         }
      }

      return owner instanceof class_742 player ? player.method_3117() : class_1068.method_4649();
   }

   // original carried a javac bridge from the intermediary name
   @Override
   protected void method_4042(CloneEntity clone, class_4587 pose, float partialTick) {
      this.scale(clone, pose, partialTick);
   }

   protected void scale(CloneEntity clone, class_4587 pose, float partialTick) {
      pose.method_22905(0.9375F, 0.9375F, 0.9375F);
   }

   public static boolean resolveSlim(CloneEntity clone) {
      class_640 disguise = ClientDisguiseManager.playerInfo(clone.method_5628());
      if (disguise != null) {
         return "slim".equals(disguise.method_2977());
      } else if (clone.getCustomTexture(false) != null) {
         return clone.isSlim();
      } else {
         return clone.method_35057() instanceof class_742 player ? "slim".equals(player.method_3121()) : clone.isSlim();
      }
   }
}
