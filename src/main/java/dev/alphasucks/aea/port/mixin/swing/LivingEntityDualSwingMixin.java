package dev.alphasucks.aea.port.mixin.swing;

import dev.alphasucks.aea.port.access.DualSwingHolder;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1309.class)
public abstract class LivingEntityDualSwingMixin implements DualSwingHolder {
   private static final String SWING = "swing(Lnet/minecraft/world/InteractionHand;Z)V";
   @Shadow
   public boolean field_6252;
   @Shadow
   public class_1268 field_6266;
   @Shadow
   public int field_6279;
   @Unique
   private boolean apoli$bothArms;
   @Unique
   private boolean apoli$wasSwinging;
   @Unique
   private int apoli$previousSwingTime;
   @Unique
   private class_1268 apoli$previousArm;

   @Override
   public boolean apoli$isSwingingBothArms() {
      return this.apoli$bothArms;
   }

   @Override
   public void apoli$setSwingingBothArms(boolean value) {
      this.apoli$bothArms = value;
   }

   @Inject(method = "method_23667(Lnet/minecraft/class_1268;Z)V", at = @At("HEAD"))
   private void apoli$captureSwing(class_1268 hand, boolean broadcastToSelf, CallbackInfo ci) {
      this.apoli$wasSwinging = this.field_6252;
      this.apoli$previousSwingTime = this.field_6279;
      this.apoli$previousArm = this.field_6266;
   }

   @Inject(method = "method_23667(Lnet/minecraft/class_1268;Z)V", at = @At("RETURN"))
   private void apoli$pairSwing(class_1268 hand, boolean broadcastToSelf, CallbackInfo ci) {
      class_1268 previous = this.apoli$previousArm;
      this.apoli$previousArm = null;
      if (this.field_6279 == -1 && this.field_6266 == hand) {
         if (!this.apoli$wasSwinging || this.apoli$previousSwingTime >= 0 || previous == null) {
            this.apoli$bothArms = false;
         } else if (previous != hand) {
            this.apoli$bothArms = true;
         }
      }
   }

   @Inject(method = "method_6119", at = @At("TAIL"))
   private void apoli$clearBothArms(CallbackInfo ci) {
      if (!this.field_6252) {
         this.apoli$bothArms = false;
      }
   }
}
