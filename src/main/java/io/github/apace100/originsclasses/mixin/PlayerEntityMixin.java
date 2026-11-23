package io.github.apace100.originsclasses.mixin;

import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.originsclasses.effect.StealthEffect;
import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1723;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1657.class)
public abstract class PlayerEntityMixin extends class_1309 {
   @Shadow
   @Final
   public class_1723 field_7498;

   protected PlayerEntityMixin(class_1299<? extends class_1309> entityType, class_1937 world) {
      super(entityType, world);
   }

   // AEA: @WrapOperation instead of @Redirect so other mods hooking the same call still apply
   @WrapOperation(
      method = "method_7282(DDD)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_7322(F)V", ordinal = 3)
   )
   private void removeSprintingExhaustion(class_1657 playerEntity, float exhaustion, Operation<Void> original) {
      if (!ClassPowerTypes.NO_SPRINT_EXHAUSTION.isActive(playerEntity)) {
         original.call(playerEntity, exhaustion);
      }
   }

   @ModifyVariable(
      method = "method_7324(Lnet/minecraft/class_1297;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_7261(F)F"),
      ordinal = 0
   )
   private float modifyBaseAttackDamageInStealth(float originalAttackDamage, class_1297 target) {
      float modifiedDamage = originalAttackDamage;
      boolean isInStealth = this.method_6059(StealthEffect.INSTANCE);
      if (target != null && isInStealth) {
         float yawTarget = target.method_5705(1.0F);

         while (yawTarget < 0.0F) {
            yawTarget += 360.0F;
         }

         yawTarget %= 360.0F;
         float yawSelf = this.method_5705(1.0F);

         while (yawSelf < 0.0F) {
            yawSelf += 360.0F;
         }

         yawSelf %= 360.0F;
         float yawDiff = Math.abs(yawTarget - yawSelf);
         if (yawDiff < 80.0F) {
            modifiedDamage *= 2.0F;
         }
      }

      if (ClassPowerTypes.STEALTH.isActive(this)) {
         VariableIntPower stealthCounter = (VariableIntPower)ClassPowerTypes.STEALTH.get(this);
         stealthCounter.setValue(stealthCounter.getMin());
      }

      if (isInStealth) {
         this.method_6016(StealthEffect.INSTANCE);
      }

      return modifiedDamage;
   }

   @Inject(method = "method_5773()V", at = @At("TAIL"))
   private void tickStealthCounter(CallbackInfo ci) {
      if (ClassPowerTypes.STEALTH.isActive(this)) {
         VariableIntPower stealthCounter = (VariableIntPower)ClassPowerTypes.STEALTH.get(this);
         if (this.method_5715()) {
            if (stealthCounter.increment() == stealthCounter.getMax() && !this.method_6059(StealthEffect.INSTANCE)) {
               this.method_6092(new class_1293(StealthEffect.INSTANCE, 33000, 0, false, false, true));
            }
         } else {
            stealthCounter.setValue(stealthCounter.getMin());
            if (this.method_6059(StealthEffect.INSTANCE)) {
               this.method_6016(StealthEffect.INSTANCE);
            }
         }
      }
   }

   @Inject(method = "method_5783(Lnet/minecraft/class_3414;FF)V", at = @At("HEAD"), cancellable = true)
   private void muffleSoundsInStealth(class_3414 sound, float volume, float pitch, CallbackInfo ci) {
      if (this.method_6059(StealthEffect.INSTANCE)) {
         ci.cancel();
      }
   }

   // AEA: @WrapOperation instead of @Redirect so other mods hooking the same call still apply
   @WrapOperation(
      method = "method_18866(Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;)Lnet/minecraft/class_1799;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1937;method_43128(Lnet/minecraft/class_1657;DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V"
      )
   )
   private void muffleEatingFinishSound(
      class_1937 world, class_1657 player, double x, double y, double z, class_3414 sound, class_3419 category, float volume, float pitch, Operation<Void> original
   ) {
      if (!this.method_6059(StealthEffect.INSTANCE)) {
         original.call(world, player, x, y, z, sound, category, volume, pitch);
      }
   }
}
