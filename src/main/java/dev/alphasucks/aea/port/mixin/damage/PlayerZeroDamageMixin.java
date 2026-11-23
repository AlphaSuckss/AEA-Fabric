package dev.alphasucks.aea.port.mixin.damage;

import dev.alphasucks.aea.port.power.builtin.ModifyDamageHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDamageHandler;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_1657.class)
public abstract class PlayerZeroDamageMixin {
   @ModifyVariable(method = "method_5643(Lnet/minecraft/class_1282;F)Z", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float apoli$keepZeroDamageHitForModifiers(float value, class_1282 source, float amount) {
      if (value != 0.0F) {
         return value;
      }

      class_1657 self = (class_1657)(Object)this;
      if (self.method_37908().method_8608()) {
         return value;
      }

      class_1309 attacker = source.method_5529() instanceof class_1309 le ? le : null;
      float preview = ModifyProjectileDamageHandler.previewAmount(attacker, self, source, 0.0F, self.method_37908());
      preview = ModifyDamageHandler.previewAmount(attacker, self, source, preview, self.method_37908());
      return preview > 0.0F ? Float.MIN_VALUE : value;
   }
}
