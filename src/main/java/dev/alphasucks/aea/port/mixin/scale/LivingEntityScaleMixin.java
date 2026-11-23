package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.minecraft.class_1309;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_1309.class)
public abstract class LivingEntityScaleMixin {
   @WrapMethod(method = "method_18378(Lnet/minecraft/class_4050;Lnet/minecraft/class_4048;)F")
   private float apoli$scaleEyeHeight(class_4050 pose, class_4048 dimensions, Operation<Float> original) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return (Float)original.call(new Object[]{pose, dimensions});
      } else {
         float width = Scales.applied(self, ScaleTypes.HITBOX_WIDTH);
         float height = Scales.applied(self, ScaleTypes.HITBOX_HEIGHT);
         float eye = Scales.applied(self, ScaleTypes.EYE_HEIGHT);
         if (width == 1.0F && height == 1.0F && eye == 1.0F) {
            return (Float)original.call(new Object[]{pose, dimensions});
         } else if (dimensions.field_18069) {
            float raw = (Float)original.call(new Object[]{pose, dimensions});
            return eye == height ? raw : raw * eye;
         } else {
            class_4048 unscaled = width > 0.0F && height > 0.0F ? dimensions.method_19539(1.0F / width, 1.0F / height) : dimensions;
            return (Float)original.call(new Object[]{pose, unscaled}) * eye;
         }
      }
   }

   @ModifyReturnValue(method = "method_6029()F", at = @At("RETURN"))
   private float apoli$scaleSpeed(float original) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return original;
      }

      float motion = Scales.applied(self, ScaleTypes.MOTION);
      return motion != 1.0F ? original * motion : original;
   }

   @ModifyReturnValue(method = "method_6106()F", at = @At("RETURN"))
   private float apoli$scaleJump(float original) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return original;
      }

      float jump = Scales.applied(self, ScaleTypes.JUMP_HEIGHT);
      return jump != 1.0F ? original * jump : original;
   }

   @ModifyVariable(method = "method_5747(FFLnet/minecraft/class_1282;)Z", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float apoli$scaleFallDistance(float fallDistance) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return fallDistance;
      }

      float falling = Scales.applied(self, ScaleTypes.FALLING);
      return falling != 1.0F && !(falling <= 0.0F) ? fallDistance / falling : fallDistance;
   }

   @ModifyVariable(method = "method_6005(DDD)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private double apoli$scaleKnockback(double strength) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return strength;
      }

      float knockback = Scales.applied(self, ScaleTypes.KNOCKBACK);
      return knockback != 1.0F && !(knockback <= 0.0F) ? strength / knockback : strength;
   }

   @ModifyReturnValue(method = "method_49476()F", at = @At("RETURN"))
   private float apoli$scaleStepHeight(float original) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return original;
      }

      float step = Scales.applied(self, ScaleTypes.STEP_HEIGHT);
      return step != 1.0F ? original * step : original;
   }

   @ModifyReturnValue(method = "method_18390", at = @At("RETURN"))
   private double apoli$scaleVisibility(double original) {
      class_1309 self = (class_1309)(Object)this;
      if (Scales.untouched(self)) {
         return original;
      }

      float visibility = Scales.applied(self, ScaleTypes.VISIBILITY);
      return visibility != 1.0F ? original * visibility : original;
   }
}
