package dev.alphasucks.aea.port.mixin.trade;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.alphasucks.aea.port.power.builtin.InfiniteTradesPower;
import net.minecraft.class_1914;
import net.minecraft.class_3988;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3988.class)
public abstract class AbstractVillagerInfiniteTradesMixin {
   @Inject(method = "method_8262", at = @At("HEAD"))
   private void apoli$rememberUses(class_1914 offer, CallbackInfo ci, @Share("apoli$uses") LocalIntRef uses) {
      uses.set(offer.method_8249());
   }

   @Inject(method = "method_8262", at = @At("TAIL"))
   private void apoli$keepInStock(class_1914 offer, CallbackInfo ci, @Share("apoli$uses") LocalIntRef uses) {
      class_3988 self = (class_3988)(Object)this;
      if (InfiniteTradesPower.applies(self)) {
         ((MerchantOfferAccessor)offer).apoli$setUses(uses.get());
         InfiniteTradesPower.resendOffers(self);
      }
   }
}
