package dev.alphasucks.aea.port.mixin.firstperson;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.access.PartVisibilityUndo;
import dev.alphasucks.aea.port.client.render.FirstPersonHeldItem;
import dev.alphasucks.aea.port.client.render.HandRenderPass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererHandPassMixin {
   @Inject(method = "method_23205", at = @At("HEAD"))
   private void apoli$beginHandPass(class_4587 poseStack, class_4597 buffers, int light, class_742 player, class_630 arm, class_630 sleeve, CallbackInfo ci) {
      HandRenderPass.begin();
   }

   @WrapOperation(method = "method_23205", at = @At(value = "FIELD", target = "Lnet/minecraft/class_630;field_3654:F", opcode = 181))
   private void apoli$keepEditedPitch(class_630 part, float value, Operation<Void> original) {
      if (!HandRenderPass.edited()) {
         original.call(new Object[]{part, value});
      }
   }

   @Inject(method = "method_23205", at = @At("RETURN"))
   private void apoli$endHandPass(class_4587 poseStack, class_4597 buffers, int light, class_742 player, class_630 arm, class_630 sleeve, CallbackInfo ci) {
      class_591<class_742> model = (class_591<class_742>)((class_1007)(Object)this).method_4038();
      if (model instanceof PartVisibilityUndo undo) {
         undo.apoli$undoPartVisibility();
      }

      HandRenderPass.end();
      FirstPersonHeldItem.render(model, arm, player, poseStack, buffers, light);
   }
}
