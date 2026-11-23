package dev.alphasucks.aea.originsport.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import io.github.apace100.origins.origin.OriginLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overgrown's swappable layers aren't chosen like normal layers. apace knows nothing of them, so it
 * would ask players to choose one on join; reporting them as disabled keeps them out of apace's
 * choose/auto-choose/"has all origins" logic. The flag apace sends to clients is cleared too, so a
 * client's choose screen skips them even before AEA's own topology packet arrives.
 */
@Mixin(value = OriginLayer.class, remap = false)
public abstract class OriginLayerMixin {
    @ModifyReturnValue(method = "isEnabled", at = @At("RETURN"), remap = false)
    private boolean aea$swapLayersAreNotChosen(boolean enabled) {
        return enabled && !SwapLayers.isSwapLayerAnySide(((OriginLayer) (Object) this).getIdentifier());
    }

    @ModifyExpressionValue(method = "write", at = @At(value = "FIELD", target = "Lio/github/apace100/origins/origin/OriginLayer;enabled:Z"), remap = false)
    private boolean aea$sendSwapLayersDisabled(boolean enabled) {
        return enabled && !SwapLayers.isSwapLayerAnySide(((OriginLayer) (Object) this).getIdentifier());
    }
}
