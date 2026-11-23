package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.networking.ModPacketsS2C;
import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1645;
import net.minecraft.class_1657;
import net.minecraft.class_1914;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(class_1645.class)
public class SimpleMerchantMixin {
   @Shadow
   @Final
   private class_1657 field_7441;

   // AEA: @WrapOperation instead of @Redirect
   @WrapOperation(method = "method_8262(Lnet/minecraft/class_1914;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1914;method_8244()V"))
   private void preventUseClientSide(class_1914 tradeOffer, Operation<Void> original) {
      if (ModPacketsS2C.isWanderingTrader || !ClassPowerTypes.TRADE_AVAILABILITY.isActive(this.field_7441)) {
         original.call(tradeOffer);
      }
   }
}
