package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ScalePower;
import dev.alphasucks.aea.port.scale.ScaleHolder;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2487;
import net.minecraft.class_4048;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class EntityScaleMixin implements ScaleHolder {
   @Unique
   private ScaleState apoli$scaleState;

   @Nullable
   @Override
   public ScaleState apoli$scales() {
      return this.apoli$scaleState;
   }

   @Override
   public ScaleState apoli$scalesOrCreate() {
      ScaleState state = this.apoli$scaleState;
      if (state == null) {
         state = new ScaleState((class_1297)(Object)this);
         this.apoli$scaleState = state;
      }

      return state;
   }

   @ModifyReturnValue(method = "method_18377", at = @At("RETURN"))
   private class_4048 apoli$scaleDimensions(class_4048 original) {
      return Scales.scaledDimensions((class_1297)(Object)this, original);
   }

   @ModifyReturnValue(method = "method_18378(Lnet/minecraft/class_4050;Lnet/minecraft/class_4048;)F", at = @At("RETURN"))
   private float apoli$scaleEyeHeight(float original) {
      class_1297 self = (class_1297)(Object)this;
      if (!(self instanceof class_1309) && !Scales.untouched(self)) {
         float height = Scales.applied(self, ScaleTypes.HITBOX_HEIGHT);
         float eye = Scales.applied(self, ScaleTypes.EYE_HEIGHT);
         return eye != height && !(height <= 0.0F) ? original * (eye / height) : original;
      } else {
         return original;
      }
   }

   @Inject(method = "method_5773", at = @At("HEAD"))
   private void apoli$tickScales(CallbackInfo ci) {
      class_1297 self = (class_1297)(Object)this;
      ScaleState state = this.apoli$scaleState;
      if (state != null && state.needsTick()) {
         Scales.tick(self, state);
      } else if (self.method_37908().method_8608()) {
         if (Scales.anyPowerLoaded() && ScalePower.hasAny(self)) {
            Scales.refreshDimensions(self, this.apoli$scalesOrCreate());
         }
      }
   }

   @ModifyReturnValue(method = "method_5647", at = @At("RETURN"))
   private class_2487 apoli$saveScales(class_2487 saved) {
      ScaleState state = this.apoli$scaleState;
      if (state != null && !state.isDefault()) {
         state.save(saved);
      }

      return saved;
   }

   @Inject(method = "method_5651", at = @At("RETURN"))
   private void apoli$loadScales(class_2487 tag, CallbackInfo ci) {
      if (tag.method_10573("scales", 9)) {
         this.apoli$scalesOrCreate().load(tag);
         class_1297 self = (class_1297)(Object)this;
         Scales.refreshDimensions(self, this.apoli$scaleState);
      }
   }

   @Inject(method = "method_5878", at = @At("RETURN"))
   private void apoli$restoreScales(class_1297 original, CallbackInfo ci) {
      ScaleState from = ((ScaleHolder)original).apoli$scales();
      if (from != null && !from.isDefault()) {
         this.apoli$scalesOrCreate().copyFrom(from);
         Scales.refreshDimensions((class_1297)(Object)this, this.apoli$scaleState);
      }
   }
}
