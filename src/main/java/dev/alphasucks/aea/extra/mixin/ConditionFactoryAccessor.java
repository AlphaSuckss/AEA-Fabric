package dev.alphasucks.aea.extra.mixin;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import java.util.function.BiFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("rawtypes")
@Mixin(value = ConditionFactory.class, remap = false)
public interface ConditionFactoryAccessor {
    @Accessor("condition")
    BiFunction aea$getCondition();

    @Mutable
    @Accessor("condition")
    void aea$setCondition(BiFunction condition);
}
