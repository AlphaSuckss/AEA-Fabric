package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2411;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2248.class)
public class BlockMixin {
   @Inject(method = "method_9556(Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_2586;Lnet/minecraft/class_1799;)V", at = @At("TAIL"))
   private void dropAdditionalCrops(
      class_1937 world, class_1657 player, class_2338 pos, class_2680 state, class_2586 blockEntity, class_1799 stack, CallbackInfo ci
   ) {
      if ((state.method_26204() instanceof class_2302 || state.method_26204() instanceof class_2411)
         && player != null
         && ClassPowerTypes.MORE_CROP_DROPS.isActive(player)
         && new Random().nextInt(10) < 3) {
         class_2248.method_9511(state, world, pos, blockEntity, player, stack);
      }
   }

   @ModifyExpressionValue(method = "method_9556(Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_2586;Lnet/minecraft/class_1799;)V", at = @At(value = "CONSTANT", args = "floatValue=0.005F"))
   private float preventBlockMiningExhaustion(float exhaustion, @Local(argsOnly = true) class_1657 playerEntity) {
      return ClassPowerTypes.NO_MINING_EXHAUSTION.isActive(playerEntity) ? 0.0F : exhaustion;
   }
}
