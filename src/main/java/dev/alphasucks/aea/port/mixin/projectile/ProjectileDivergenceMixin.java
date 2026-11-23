package dev.alphasucks.aea.port.mixin.projectile;

import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDivergencePower;
import net.minecraft.class_1676;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_1676.class)
public abstract class ProjectileDivergenceMixin {
   @ModifyVariable(method = "method_7485(DDDFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
   private float apoli$modifyDivergence(float divergence) {
      return ModifyProjectileDivergencePower.modify((class_1676)(Object)this, divergence);
   }
}
