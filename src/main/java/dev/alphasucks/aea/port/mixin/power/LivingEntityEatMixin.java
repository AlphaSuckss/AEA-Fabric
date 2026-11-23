package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.alphasucks.aea.port.power.builtin.EdibleItemHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyFoodHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyFoodHandler.Eaten;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public abstract class LivingEntityEatMixin {
   @WrapOperation(method = "method_6019", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7935()I"))
   private int apoli$modifyFoodEatTicks(class_1799 stack, Operation<Integer> original, class_1268 hand) {
      int ticks = (Integer)original.call(new Object[]{stack});
      if (ticks > 0 && EdibleItemHandler.armedDuration(stack) < 0) {
         double modified = ModifyFoodHandler.eatTicks((class_1309)(Object)this, stack, ticks);
         return Math.max(1, (int)Math.round(modified));
      } else {
         return ticks;
      }
   }

   @Inject(method = "method_18865", at = @At("HEAD"), cancellable = true)
   private void apoli$preventFoodEffects(class_1799 stack, class_1937 level, class_1309 eater, CallbackInfo ci) {
      if (ModifyFoodHandler.preventsEffects(eater, stack)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_18866", at = @At("HEAD"))
   private void apoli$beforeEat(class_1937 level, class_1799 stack, CallbackInfoReturnable<class_1799> cir, @Share("apoli$eaten") LocalRef<Eaten> eaten) {
      if (stack.method_19267()) {
         eaten.set(ModifyFoodHandler.beforeEat((class_1309)(Object)this, stack));
      }
   }

   @Inject(method = "method_18866", at = @At("RETURN"))
   private void apoli$afterEat(class_1937 level, class_1799 stack, CallbackInfoReturnable<class_1799> cir, @Share("apoli$eaten") LocalRef<Eaten> eaten) {
      ModifyFoodHandler.afterEat((class_1309)(Object)this, stack, (Eaten)eaten.get());
   }

   @ModifyReturnValue(method = "method_18866", at = @At("RETURN"))
   private class_1799 apoli$replaceEatenStack(class_1799 result, class_1937 level, class_1799 stack, @Share("apoli$eaten") LocalRef<Eaten> eaten) {
      class_1309 self = (class_1309)(Object)this;
      class_1799 replacement = ModifyFoodHandler.replacement((Eaten)eaten.get());
      if (replacement != null && !replacement.method_7960()) {
         if (self instanceof class_1657 player && player.method_31549().field_7477) {
            return result;
         } else {
            if (result.method_7960()) {
               return replacement;
            }

            if (level.method_8608()) {
               return result;
            }

            if (self instanceof class_1657 player) {
               if (!player.method_31548().method_7394(replacement)) {
                  player.method_7328(replacement, false);
               }
            } else {
               self.method_5775(replacement);
            }

            return result;
         }
      } else {
         return result;
      }
   }
}
