package dev.alphasucks.aea.port.compat.icarus.mixin.client;

import com.r3x.client.models.FixedWingEntityModel;
import dev.cammiescorner.icarus.client.models.WingEntityModel;
import dev.alphasucks.aea.port.compat.icarus.IcarusWingParts;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FixedWingEntityModel.class)
public abstract class FixedWingEntityModelMixin {
   private static final String SETUP_ANIM = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V";
   @Unique
   private final IcarusWingParts apoli$reWingedParts = new IcarusWingParts();

   @Inject(method = "setupAnim(Lnet/minecraft/class_1309;FFFFF)V", at = @At("HEAD"), require = 0)
   private void apoli$restoreReWinged(class_1309 entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
      WingEntityModel<?> self = (WingEntityModel<?>)(Object)this;
      this.apoli$reWingedParts.restore(self.rightWing, self.leftWing);
   }

   @Inject(method = "setupAnim(Lnet/minecraft/class_1309;FFFFF)V", at = @At("TAIL"), require = 0)
   private void apoli$editReWinged(class_1309 entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
      WingEntityModel<?> self = (WingEntityModel<?>)(Object)this;
      this.apoli$reWingedParts.edit(entity, self.rightWing, self.leftWing);
   }
}
