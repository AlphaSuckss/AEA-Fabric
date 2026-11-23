package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.power.builtin.InvisibilityPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererOutlineMixin {
   @ModifyExpressionValue(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_310;method_27022(Lnet/minecraft/class_1297;)Z")
   )
   private boolean apoli$hideOutlineWhenInvisible(boolean original, class_1309 rendered) {
      return !original ? false : !InvisibilityPower.hidesOutline(ClientDisguiseManager.powerSource(rendered));
   }
}
