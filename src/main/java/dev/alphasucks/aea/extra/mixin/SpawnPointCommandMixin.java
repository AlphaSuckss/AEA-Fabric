package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.spawnpoint.PreventSpawnPointPower;
import java.util.Collection;
import net.minecraft.class_2168;
import net.minecraft.class_2338;
import net.minecraft.class_3127;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;

/** Marks spawn points set by {@code /spawnpoint}, so {@code aea:prevent_spawn_point} can tell them apart. */
@Mixin(class_3127.class)
public abstract class SpawnPointCommandMixin {
    @WrapMethod(method = "method_13645")
    private static int aea$markCommand(class_2168 source, Collection<class_3222> targets, class_2338 pos, float angle, Operation<Integer> original) {
        boolean outer = PreventSpawnPointPower.inCommand;
        PreventSpawnPointPower.inCommand = true;
        try {
            return original.call(source, targets, pos, angle);
        } finally {
            PreventSpawnPointPower.inCommand = outer;
        }
    }
}
