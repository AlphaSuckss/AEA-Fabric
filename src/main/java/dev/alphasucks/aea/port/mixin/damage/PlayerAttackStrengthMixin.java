package dev.alphasucks.aea.port.mixin.damage;

import dev.alphasucks.aea.port.data.AttackStrengthContext;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1657.class)
public abstract class PlayerAttackStrengthMixin {
   @Unique
   private long apoli$previousAttackStrength = AttackStrengthContext.NONE;

   @Inject(method = "method_7324(Lnet/minecraft/class_1297;)V", at = @At("HEAD"))
   private void apoli$captureAttackStrength(class_1297 target, CallbackInfo ci) {
      class_1657 self = (class_1657)(Object)this;
      this.apoli$previousAttackStrength = AttackStrengthContext.set(self.method_5628(), self.method_7261(0.5F));
   }

   @Inject(method = "method_7324(Lnet/minecraft/class_1297;)V", at = @At("RETURN"))
   private void apoli$releaseAttackStrength(class_1297 target, CallbackInfo ci) {
      AttackStrengthContext.restore(this.apoli$previousAttackStrength);
      this.apoli$previousAttackStrength = AttackStrengthContext.NONE;
   }
}
