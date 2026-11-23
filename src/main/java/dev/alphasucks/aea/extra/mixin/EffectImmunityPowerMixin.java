package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.effect.EffectCategoryHolder;
import io.github.apace100.apoli.power.EffectImmunityPower;
import java.util.EnumSet;
import java.util.HashSet;
import net.minecraft.class_1291;
import net.minecraft.class_4081;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** {@code effect_type} on {@code apoli:effect_immunity}: the power also covers every effect of those kinds. */
@Mixin(value = EffectImmunityPower.class, remap = false)
public abstract class EffectImmunityPowerMixin implements EffectCategoryHolder {
    @Shadow
    @Final
    protected HashSet<class_1291> effects;

    @Shadow
    @Final
    private boolean inverted;

    @Unique
    private EnumSet<class_4081> aea$categories;

    @Override
    public void aea$setCategories(EnumSet<class_4081> categories) {
        aea$categories = categories;
    }

    @ModifyReturnValue(method = "doesApply(Lnet/minecraft/class_1291;)Z", at = @At("RETURN"))
    private boolean aea$byEffectType(boolean original, class_1291 effect) {
        EnumSet<class_4081> categories = aea$categories;
        if (categories == null || categories.isEmpty() || effect == null) {
            return original;
        }
        return inverted != (effects.contains(effect) || categories.contains(effect.method_18792()));
    }
}
