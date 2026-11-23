package dev.alphasucks.aea.port.mixin.typetag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ModifyTypeTagPower;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1309.class)
public abstract class LivingEntityMobTypeMixin {
   @ModifyReturnValue(method = "method_6046()Lnet/minecraft/class_1310;", at = @At("RETURN"))
   private class_1310 apoli$modifyMobType(class_1310 original) {
      return !ModifyTypeTagPower.active() ? original : ModifyTypeTagPower.resolveMobType((class_1309)(Object)this, original);
   }
}
