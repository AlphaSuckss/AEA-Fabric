package dev.alphasucks.aea.port.mixin.custom_model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.client.render.AnimationPlayer;
import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.client.render.CustomModelRenderLayer;
import dev.alphasucks.aea.port.client.render.DynamicTextures;
import dev.alphasucks.aea.port.client.render.GeometryRenderer;
import dev.alphasucks.aea.port.client.render.GhostArmState;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.client.render.PlayerRestPose;
import dev.alphasucks.aea.port.client.render.TextureOverlays;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.Config;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_630;
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
public abstract class PlayerRendererCustomModelMixin extends class_922<class_742, class_591<class_742>> {
   @Unique
   private boolean apoli$slim;

   protected PlayerRendererCustomModelMixin(class_5618 ctx, class_591<class_742> model, float shadow) {
      super(ctx, model, shadow);
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   private void apoli$addCustomModelLayer(class_5618 ctx, boolean slim, CallbackInfo ci) {
      this.apoli$slim = slim;
      this.method_4046(new CustomModelRenderLayer(this, slim));
   }

   @ModifyReturnValue(method = "method_4216(Lnet/minecraft/class_742;)Lnet/minecraft/class_2960;", at = @At("RETURN"))
   private class_2960 apoli$replaceSkin(class_2960 original, class_742 player) {
      Config cfg = CustomModelRenderPower.firstReplace(player);
      return cfg == null ? original : DynamicTextures.resolve(this.apoli$slim ? cfg.slim() : cfg.wide(), player);
   }

   @ModifyExpressionValue(method = "method_23205", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_3117()Lnet/minecraft/class_2960;"))
   private class_2960 apoli$replaceHandSkin(
      class_2960 original, class_4587 pose, class_4597 buffers, int light, class_742 player, class_630 arm, class_630 sleeve
   ) {
      Config cfg = CustomModelRenderPower.firstReplace(player);
      return cfg != null && cfg.showFirstPerson() ? DynamicTextures.resolve(this.apoli$slim ? cfg.slim() : cfg.wide(), player) : original;
   }

   @Inject(method = "method_4220", at = @At("TAIL"))
   private void apoli$rightArmOverlay(class_4587 pose, class_4597 buffers, int light, class_742 player, CallbackInfo ci) {
      this.apoli$renderArmOverlay(pose, buffers, light, player, this.method_4038().field_3401, this.method_4038().field_3486);
      this.apoli$renderArmGeometry(pose, buffers, light, player, "rightarm", this.method_4038().field_3401);
   }

   @Inject(method = "method_4221", at = @At("TAIL"))
   private void apoli$leftArmOverlay(class_4587 pose, class_4597 buffers, int light, class_742 player, CallbackInfo ci) {
      this.apoli$renderArmOverlay(pose, buffers, light, player, this.method_4038().field_27433, this.method_4038().field_3484);
      this.apoli$renderArmGeometry(pose, buffers, light, player, "leftarm", this.method_4038().field_27433);
   }

   @Unique
   private void apoli$renderArmOverlay(class_4587 pose, class_4597 buffers, int light, class_742 player, class_630 arm, class_630 sleeve) {
      List<ResolvedLayer> layers = CustomModelRenderPower.collectTextureOverlays(player);
      if (!layers.isEmpty()) {
         float alphaScale = GhostArmState.isActive() ? GhostArmState.alpha() : 1.0F;
         float ageInTicks = ModelPartAnimator.ageInTicks(player);
         class_591<class_742> model = this.method_4038();

         for (int i = 0; i < layers.size(); i++) {
            ResolvedLayer layer = (ResolvedLayer)layers.get(i);
            if (layer.showFirstPerson() && TextureOverlays.affectsArm(model, layer, player, arm, sleeve)) {
               class_2960 texture = DynamicTextures.resolve(layer.texture(this.apoli$slim), player);
               TextureOverlays.renderArm(layer, texture, alphaScale, ageInTicks, arm, sleeve, pose, buffers, light);
            }
         }
      }
   }

   @Unique
   private void apoli$renderArmGeometry(class_4587 pose, class_4597 buffers, int light, class_742 player, String slot, class_630 arm) {
      List<GeometryRender> geometry = CustomModelRenderPower.collectGeometry(player);
      if (!geometry.isEmpty()) {
         float alphaScale = GhostArmState.isActive() ? GhostArmState.alpha() : 1.0F;
         class_591<class_742> model = this.method_4038();
         class_591<class_742> rest = PlayerRestPose.get();

         for (int i = 0; i < geometry.size(); i++) {
            GeometryRender render = (GeometryRender)geometry.get(i);
            if (render.showFirstPerson()) {
               CustomModel custom = CustomModelManager.get(render.model(), render.bindBodyParts());
               if (custom != null) {
                  if (render.bindBodyParts()) {
                     GeometryRenderer.syncPlayer(custom, model, rest);
                  } else {
                     GeometryRenderer.resetAll(custom);
                  }

                  AnimationPlayer.apply(player, render, custom, class_310.method_1551().method_1488());
                  GeometryRenderer.applyVisibility(custom, render.bodyParts());
                  GeometryRenderer.drawSlot(render, custom, slot, alphaScale, pose, buffers, light, player, render.bindBodyParts() ? null : arm);
               }
            }
         }
      }
   }
}
