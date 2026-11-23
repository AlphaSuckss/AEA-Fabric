package dev.alphasucks.aea.port.mixin.particle;

import dev.alphasucks.aea.port.client.particle.ApoliParticleRenderTypes;
import java.util.Map;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1060;
import net.minecraft.class_3999;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_702;
import net.minecraft.class_703;
import net.minecraft.class_765;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_702.class)
@Environment(EnvType.CLIENT)
public abstract class ParticleEngineCustomTypesMixin {
   @Shadow
   @Final
   private Map<class_3999, Queue<class_703>> field_3830;
   @Shadow
   @Final
   private class_1060 field_3831;

   @Inject(
      method = "method_3049(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597$class_4598;Lnet/minecraft/class_765;Lnet/minecraft/class_4184;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22909()V")
   )
   private void apoli$renderCustomParticles(
      class_4587 poseStack, class_4598 bufferSource, class_765 lightTexture, class_4184 camera, float partialTick, CallbackInfo ci
   ) {
      ApoliParticleRenderTypes.render(this.field_3830, this.field_3831, camera, partialTick);
   }
}
