package dev.alphasucks.aea.port.mixin.effects;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.effects.CustomMobEffect;
import net.minecraft.class_1058;
import net.minecraft.class_1291;
import net.minecraft.class_2960;
import net.minecraft.class_4074;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_4074.class)
public class MobEffectTextureManagerCustomEffectSpriteMixin {
   @WrapMethod(method = "method_18663")
   private class_1058 apoli$getCustomSprite(class_1291 mobEffect, Operation<class_1058> original) {
      return mobEffect instanceof CustomMobEffect effect && effect.icon.isPresent()
         ? ((TextureAtlasHolderInvoker)(Object)this).apoli$invokeGetSprite((class_2960)effect.icon.get())
         : (class_1058)original.call(new Object[]{mobEffect});
   }
}
