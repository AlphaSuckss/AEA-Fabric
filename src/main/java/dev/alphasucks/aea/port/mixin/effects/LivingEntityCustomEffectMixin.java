package dev.alphasucks.aea.port.mixin.effects;

import dev.alphasucks.aea.port.effects.CustomEffectRegistry;
import dev.alphasucks.aea.port.effects.CustomMobEffect;
import dev.alphasucks.aea.port.power.PowerContainer;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public class LivingEntityCustomEffectMixin {
   @Inject(method = "method_6129", at = @At("HEAD"))
   private void apoli$onRemoveEffect(class_1293 mobEffectInstance, CallbackInfo ci) {
      if (mobEffectInstance.method_5579() instanceof CustomMobEffect effect) {
         PowerContainer holder = PowerContainer.of((class_1309)(Object)this);
         if (holder != null) {
            effect.revokePowers(holder);
         }
      }
   }

   @Inject(method = "method_6020", at = @At("HEAD"))
   private void apoli$onAddedEffect(class_1293 mobEffectInstance, @Nullable class_1297 entity, CallbackInfo ci) {
      if (mobEffectInstance.method_5579() instanceof CustomMobEffect effect) {
         effect.grantPowers((class_1309)(Object)this);
      }
   }

   @Inject(method = "method_6049", at = @At("HEAD"), cancellable = true)
   private void apoli$preventEffectsDuringReload(class_1293 mobEffectInstance, CallbackInfoReturnable<Boolean> cir) {
      // AEA: side check first so the client thread never reads the server's sync state
      if (!((class_1309)(Object)this).method_37908().method_8608()
         && mobEffectInstance.method_5579() instanceof CustomMobEffect
         && CustomEffectRegistry.blocksCustomEffects()) {
         cir.setReturnValue(false);
      }
   }
}
