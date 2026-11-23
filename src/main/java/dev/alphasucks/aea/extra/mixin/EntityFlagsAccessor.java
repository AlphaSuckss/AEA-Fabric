package dev.alphasucks.aea.extra.mixin;

import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_1297.class)
public interface EntityFlagsAccessor {
    /** The shared entity flags; they are synced to every player tracking the entity. */
    @Invoker("method_5795")
    boolean aea$sharedFlag(int flag);
}
