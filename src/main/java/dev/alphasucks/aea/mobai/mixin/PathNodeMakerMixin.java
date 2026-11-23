package dev.alphasucks.aea.mobai.mixin;

import dev.alphasucks.aea.mobai.ResolvedPath;
import net.minecraft.class_1308;
import net.minecraft.class_1950;
import net.minecraft.class_8;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Resolves the mob's aea:mob_pathing config once per path search (init) and drops it afterwards (clear). */
@Mixin(class_8.class)
public abstract class PathNodeMakerMixin implements ResolvedPath.Holder {
    @Unique
    @Nullable
    private ResolvedPath aea$path;

    @Inject(method = "method_12", at = @At("TAIL"))
    private void aea$resolve(class_1950 cachedWorld, class_1308 mob, CallbackInfo ci) {
        this.aea$path = ResolvedPath.of(mob);
    }

    @Inject(method = "method_19", at = @At("HEAD"))
    private void aea$drop(CallbackInfo ci) {
        this.aea$path = null;
    }

    @Override
    @Nullable
    public ResolvedPath aea$resolvedPath() {
        return this.aea$path;
    }
}
