package dev.alphasucks.aea.port.mixin.player_model;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.client.render.ApoliPlayerModels;
import dev.alphasucks.aea.port.client.render.PlayerModelExtraTextureLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_742;
import net.minecraft.class_922;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererModelSwapMixin extends class_922<class_742, class_591<class_742>> {
   @Unique
   private class_591<class_742> apoli$cachedModel;
   @Unique
   private boolean apoli$slimVariant;

   protected PlayerRendererModelSwapMixin(class_5618 ctx, class_591<class_742> model, float shadow) {
      super(ctx, model, shadow);
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   private void apoli$bakeModels(class_5618 ctx, boolean slim, CallbackInfo ci) {
      this.apoli$slimVariant = slim;
      ApoliPlayerModels.bake(ctx, slim);
      this.method_4046(new PlayerModelExtraTextureLayer(this));
   }

   @Inject(method = "method_4215(Lnet/minecraft/class_742;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V", at = @At("HEAD"))
   private void apoli$swapModelPre(class_742 player, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci) {
      this.apoli$cachedModel = this.field_4737;
      this.field_4737 = ApoliPlayerModels.override(player, this.field_4737, this.apoli$slimVariant);
   }

   @Inject(method = "method_4215(Lnet/minecraft/class_742;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V", at = @At("RETURN"))
   private void apoli$swapModelPost(class_742 player, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci) {
      this.field_4737 = this.apoli$cachedModel;
   }

   @WrapMethod(method = "method_4220")
   private void apoli$swapModelRightHand(class_4587 pose, class_4597 buffers, int light, class_742 player, Operation<Void> original) {
      this.apoli$withOverriddenModel(player, () -> original.call(new Object[]{pose, buffers, light, player}));
   }

   @WrapMethod(method = "method_4221")
   private void apoli$swapModelLeftHand(class_4587 pose, class_4597 buffers, int light, class_742 player, Operation<Void> original) {
      this.apoli$withOverriddenModel(player, () -> original.call(new Object[]{pose, buffers, light, player}));
   }

   @Unique
   private void apoli$withOverriddenModel(class_742 player, Runnable body) {
      class_591<class_742> cached = this.field_4737;
      this.field_4737 = ApoliPlayerModels.override(player, this.field_4737, this.apoli$slimVariant);

      try {
         body.run();
      } finally {
         this.field_4737 = cached;
      }
   }
}
