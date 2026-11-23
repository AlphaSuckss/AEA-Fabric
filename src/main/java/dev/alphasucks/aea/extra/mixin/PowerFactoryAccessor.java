package dev.alphasucks.aea.extra.mixin;

import io.github.apace100.apoli.power.factory.PowerFactory;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("rawtypes")
@Mixin(value = PowerFactory.class, remap = false)
public interface PowerFactoryAccessor {
    @Accessor("factoryConstructor")
    Function aea$getFactoryConstructor();

    @Accessor("factoryConstructor")
    void aea$setFactoryConstructor(Function constructor);
}
