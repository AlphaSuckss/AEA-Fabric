package dev.alphasucks.aea.port.mixin.typetag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ModifyTypeTagPower;
import net.minecraft.class_1299;
import net.minecraft.class_6862;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1299.class)
public abstract class EntityTypeTagMixin {
   @ModifyReturnValue(method = "method_20210(Lnet/minecraft/class_6862;)Z", at = @At("RETURN"))
   private boolean apoli$modifyTypeTag(boolean original, class_6862<class_1299<?>> tag) {
      return !ModifyTypeTagPower.active() ? original : ModifyTypeTagPower.resolve(original, (class_1299<?>)(Object)this, tag);
   }
}
