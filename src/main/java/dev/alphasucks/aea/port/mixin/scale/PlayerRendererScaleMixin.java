package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.render.ScaleRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_243;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererScaleMixin {
   @ModifyReturnValue(method = "method_23206(Lnet/minecraft/class_742;F)Lnet/minecraft/class_243;", at = @At("RETURN"))
   private class_243 apoli$scaleCrouchOffset(class_243 offset, class_742 player, float partialTick) {
      return ScaleRender.crouchOffset(player, offset, partialTick);
   }
}
