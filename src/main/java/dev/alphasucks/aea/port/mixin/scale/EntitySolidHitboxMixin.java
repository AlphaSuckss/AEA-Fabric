package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.SolidHitboxPower;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1297.class)
public abstract class EntitySolidHitboxMixin {
   @ModifyReturnValue(method = "method_30948", at = @At("RETURN"))
   private boolean apoli$solidHitbox(boolean original) {
      return original || SolidHitboxPower.isSolid((class_1297)(Object)this);
   }

   @ModifyReturnValue(method = "method_5810", at = @At("RETURN"))
   private boolean apoli$solidNotPushable(boolean original) {
      return original && SolidHitboxPower.isPushable((class_1297)(Object)this);
   }

   @ModifyReturnValue(method = "method_30949", at = @At("RETURN"))
   private boolean apoli$solidOnlyForSome(boolean original, class_1297 other) {
      return original && SolidHitboxPower.isSolid(other) ? SolidHitboxPower.isSolidFor(other, (class_1297)(Object)this) : original;
   }
}
