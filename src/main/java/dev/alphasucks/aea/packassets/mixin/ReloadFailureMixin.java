package dev.alphasucks.aea.packassets.mixin;

import dev.alphasucks.aea.packassets.client.ClientPackAssets;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a resource reload fails the game turns its resource packs off and reloads. The server's data pack assets are
 * always on, so they have to be taken out here or a broken pack would fail every reload, including at the next start.
 */
@Mixin(class_310.class)
public abstract class ReloadFailureMixin {
    @Inject(method = "method_31186", at = @At("HEAD"))
    private void aea$dropAssetsOnReloadFailure(Throwable exception, class_2561 resourceName, CallbackInfo ci) {
        ClientPackAssets.onReloadFailed();
    }
}
