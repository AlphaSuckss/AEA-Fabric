package dev.alphasucks.aea.port.mixin.typetag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ModifyTypeTagPower;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1297.class)
public abstract class EntityTypeLinkMixin {
   @ModifyReturnValue(method = "method_5864()Lnet/minecraft/class_1299;", at = @At("RETURN"))
   private class_1299<?> apoli$linkTypeToEntity(class_1299<?> original) {
      if (ModifyTypeTagPower.active()) {
         ModifyTypeTagPower.link((class_1297)(Object)this);
      }

      return original;
   }
}
