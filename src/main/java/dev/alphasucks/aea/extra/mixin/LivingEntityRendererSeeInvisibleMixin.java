package dev.alphasucks.aea.extra.mixin;

import dev.alphasucks.aea.seeinvisible.client.SeeInvisible;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws an invisible entity seen through {@code aea:see_invisible} at the power's opacity instead of vanilla's. */
@Mixin(class_922.class)
public abstract class LivingEntityRendererSeeInvisibleMixin {
    /** Vanilla's alpha for an invisible entity that may be seen (an invisible team mate). */
    @Unique
    private static final float VANILLA_SEE_THROUGH = 0.15F;

    @Unique
    private float aea$seeAlpha = -1.0F;

    @Inject(method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V", at = @At("HEAD"))
    private void aea$pickSeeAlpha(class_1309 entity, float yaw, float tickDelta, class_4587 matrices, class_4597 buffers, int light, CallbackInfo ci) {
        float opacity = entity.method_5767() ? SeeInvisible.opacity(entity) : Float.NaN;
        aea$seeAlpha = Float.isNaN(opacity) ? -1.0F : 1.0F - opacity;
    }

    @ModifyArg(method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_583;method_2828(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;IIFFFF)V"),
        index = 7)
    private float aea$seeInvisibleAlpha(float alpha) {
        float wanted = aea$seeAlpha;
        aea$seeAlpha = -1.0F;
        if (wanted < 0.0F || alpha > VANILLA_SEE_THROUGH + 0.0001F) {
            return alpha;
        }
        // Scaled rather than replaced, so a model colour power's alpha still applies on top.
        return Math.min(1.0F, alpha / VANILLA_SEE_THROUGH * wanted);
    }
}
