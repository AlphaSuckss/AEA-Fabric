package dev.alphasucks.aea.extra.mixin;

import dev.alphasucks.aea.spawnpoint.PreventSpawnPointPower;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3222;
import net.minecraft.class_5321;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** {@code aea:prevent_spawn_point}: refuses a new spawn point before anything is changed or sent. */
@Mixin(class_3222.class)
public abstract class ServerPlayerSpawnPointMixin {
    @Inject(method = "method_26284", at = @At("HEAD"), cancellable = true)
    private void aea$preventSpawnPoint(class_5321<class_1937> dimension, class_2338 pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (PreventSpawnPointPower.refuse((class_3222) (Object) this, dimension, pos)) {
            ci.cancel();
        }
    }
}
