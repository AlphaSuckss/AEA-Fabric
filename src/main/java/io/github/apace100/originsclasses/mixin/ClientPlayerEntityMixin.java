package io.github.apace100.originsclasses.mixin;

import com.mojang.authlib.GameProfile;
import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1268;
import net.minecraft.class_1753;
import net.minecraft.class_1799;
import net.minecraft.class_1819;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(class_746.class)
public abstract class ClientPlayerEntityMixin extends class_742 {
   public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
      super(world, profile);
   }

   @Shadow
   public abstract class_1268 method_6058();

   // AEA: @ModifyExpressionValue (chains with AEA's modify_use_slowdown and other mods) instead of @ModifyConstant
   @ModifyExpressionValue(method = "method_6007()V", at = @At(value = "CONSTANT", args = "floatValue=0.2F"))
   private float modifyItemUseSlowdown(float originalSlowdown) {
      class_1799 stackInUse = this.method_5998(this.method_6058());
      if (stackInUse.method_7909() instanceof class_1819 && ClassPowerTypes.LESS_SHIELD_SLOWDOWN.isActive(this)) {
         return Math.max(originalSlowdown, 0.6F);
      } else {
         return stackInUse.method_7909() instanceof class_1753 && ClassPowerTypes.LESS_BOW_SLOWDOWN.isActive(this) ? Math.max(originalSlowdown, 0.6F) : originalSlowdown;
      }
   }
}
