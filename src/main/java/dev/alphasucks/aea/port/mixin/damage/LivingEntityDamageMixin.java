package dev.alphasucks.aea.port.mixin.damage;

import dev.alphasucks.aea.port.power.builtin.HitActionHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyDamageHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDamageHandler;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public abstract class LivingEntityDamageMixin {
   @Unique
   private float apoli$modifiedDamage;

   @Inject(method = "method_5643(Lnet/minecraft/class_1282;F)Z", at = @At("HEAD"), cancellable = true)
   private void apoli$modifyDamage(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
      class_1309 self = (class_1309)(Object)this;
      if (self.method_37908().method_8608()) {
         this.apoli$modifiedDamage = amount;
      } else {
         class_1309 attacker = source.method_5529() instanceof class_1309 le ? le : null;
         float afterProjectile = ModifyProjectileDamageHandler.modifyAmount(attacker, self, source, amount, self.method_37908());
         this.apoli$modifiedDamage = ModifyDamageHandler.modifyAmount(attacker, self, source, afterProjectile, self.method_37908());
         if (amount > 0.0F && this.apoli$modifiedDamage <= 0.0F) {
            cir.setReturnValue(false);
         }
      }
   }

   @ModifyVariable(method = "method_5643(Lnet/minecraft/class_1282;F)Z", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float apoli$applyModifiedDamage(float original) {
      return this.apoli$modifiedDamage;
   }

   @Inject(method = "method_5643(Lnet/minecraft/class_1282;F)Z", at = @At("RETURN"))
   private void apoli$invokeHitActions(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
      class_1309 self = (class_1309)(Object)this;
      if (!self.method_37908().method_8608()) {
         class_1309 attacker = source.method_5529() instanceof class_1309 le ? le : null;
         HitActionHandler.fire(attacker, self, source, amount, self.method_37908(), Boolean.TRUE.equals(cir.getReturnValue()));
      }
   }
}
