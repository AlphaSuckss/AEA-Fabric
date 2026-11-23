package dev.alphasucks.aea.port.mixin.effects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.effects.CustomMobEffect;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * AEA (not in Overgrown): saved effect instances store a raw numeric id, and runtime custom effects are kept out
 * of Fabric's saved id map, so their raw ids are not stable across restarts or data pack changes. Custom effects
 * are saved with their id as well and loaded by it; an instance whose custom effect no longer exists is dropped,
 * and a raw id that only happens to point at a custom effect is not trusted.
 */
@Mixin(class_1293.class)
public abstract class MobEffectInstanceCustomEffectMixin {
   @Unique
   private static final String AEA_CUSTOM_EFFECT = "aea:custom_effect";

   @ModifyReturnValue(method = "method_5582(Lnet/minecraft/class_2487;)Lnet/minecraft/class_2487;", at = @At("RETURN"))
   private class_2487 aea$saveCustomEffectId(class_2487 tag) {
      if (tag != null && ((class_1293)(Object)this).method_5579() instanceof CustomMobEffect custom) {
         tag.method_10582(AEA_CUSTOM_EFFECT, custom.id.toString());
      }
      return tag;
   }

   @WrapOperation(
      method = "method_5583(Lnet/minecraft/class_2487;)Lnet/minecraft/class_1293;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1291;method_5569(I)Lnet/minecraft/class_1291;")
   )
   private static class_1291 aea$loadCustomEffectById(int rawId, Operation<class_1291> original, @Local(argsOnly = true) class_2487 tag) {
      if (tag.method_10573(AEA_CUSTOM_EFFECT, 8)) {
         class_2960 id = class_2960.method_12829(tag.method_10558(AEA_CUSTOM_EFFECT));
         if (id == null) {
            return null;
         }
         var ref = class_7923.field_41174.method_40264(net.minecraft.class_5321.method_29179(net.minecraft.class_7924.field_41208, id));
         return ref.isPresent() && ref.get().method_40227() && ref.get().comp_349() instanceof CustomMobEffect custom ? custom : null;
      }
      class_1291 effect = original.call(rawId);
      return effect instanceof CustomMobEffect ? null : effect;
   }
}
