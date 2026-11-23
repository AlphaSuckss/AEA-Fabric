package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.WaterBreathingPower;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1309.class)
public abstract class LivingEntityBreathingMixin {
   @ModifyReturnValue(method = "method_6094()Z", at = @At("RETURN"))
   private boolean apoli$canBreatheUnderwater(boolean original) {
      return original || WaterBreathingPower.canBreatheUnderwater((class_1309)(Object)this);
   }

   @Inject(method = "method_5670()V", at = @At("TAIL"))
   private void apoli$waterBreathingTick(CallbackInfo ci) {
      WaterBreathingPower.tick((class_1309)(Object)this);
   }
}
