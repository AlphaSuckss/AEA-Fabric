package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.spawnpoint.PreventSpawnPointPower;
import net.minecraft.class_3222;
import net.minecraft.class_3324;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Respawning copies the old spawn point onto the new player through the same method a bed uses. On 1.21.1 that copy
 * goes another way, so AEA for NeoForge never sees it; here it is marked so {@code aea:prevent_spawn_point} lets it
 * through and the holder keeps the spawn point they already had.
 */
@Mixin(class_3324.class)
public abstract class PlayerManagerRespawnMixin {
    @WrapMethod(method = "method_14556")
    private class_3222 aea$markRespawn(class_3222 player, boolean alive, Operation<class_3222> original) {
        boolean outer = PreventSpawnPointPower.inRespawn;
        PreventSpawnPointPower.inRespawn = true;
        try {
            return original.call(player, alive);
        } finally {
            PreventSpawnPointPower.inRespawn = outer;
        }
    }
}
