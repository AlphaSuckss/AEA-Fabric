package dev.alphasucks.aea.port.mixin.flag;

import dev.alphasucks.aea.port.client.particle.AnchoredParticleClient;
import dev.alphasucks.aea.port.client.render.LevelRenderPass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererPassMixin {
   @Inject(method = "method_22710", at = @At("HEAD"))
   private void apoli$beginLevelPass(CallbackInfo ci) {
      AnchoredParticleClient.beginFrame();
      LevelRenderPass.begin();
   }

   @Inject(method = "method_22710", at = @At("RETURN"))
   private void apoli$endLevelPass(CallbackInfo ci) {
      LevelRenderPass.end();
   }
}
