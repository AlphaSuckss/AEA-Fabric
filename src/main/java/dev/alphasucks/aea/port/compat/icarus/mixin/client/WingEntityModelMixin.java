package dev.alphasucks.aea.port.compat.icarus.mixin.client;

import dev.cammiescorner.icarus.client.models.WingEntityModel;
import dev.alphasucks.aea.port.compat.icarus.IcarusWingParts;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingEntityModel.class)
public abstract class WingEntityModelMixin {
   private static final String SETUP_ANIM = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V";
   @Unique
   private final IcarusWingParts apoli$wingParts = new IcarusWingParts();

   @Inject(method = "setupAnim(Lnet/minecraft/class_1309;FFFFF)V", at = @At("HEAD"), require = 0)
   private void apoli$restoreWings(class_1309 entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
      WingEntityModel<?> self = (WingEntityModel<?>)(Object)this;
      this.apoli$wingParts.restore(self.rightWing, self.leftWing);
   }

   @Inject(method = "setupAnim(Lnet/minecraft/class_1309;FFFFF)V", at = @At("TAIL"), require = 0)
   private void apoli$editWings(class_1309 entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
      WingEntityModel<?> self = (WingEntityModel<?>)(Object)this;
      this.apoli$wingParts.edit(entity, self.rightWing, self.leftWing);
   }
}
