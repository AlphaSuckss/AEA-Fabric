package dev.alphasucks.aea.port.condition;

import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.condition.context.StaticCtx;

/** AEA: Overgrown's condition registries (collection only, see TypedConditionRegistry). */
public final class ConditionTypes {
    public static final TypedConditionRegistry<EntityCtx> ENTITY = new TypedConditionRegistry<>("entity");
    public static final TypedConditionRegistry<BiEntityCtx> BI_ENTITY = new TypedConditionRegistry<>("bi_entity");
    public static final TypedConditionRegistry<BlockCtx> BLOCK = new TypedConditionRegistry<>("block");
    public static final TypedConditionRegistry<ItemCtx> ITEM = new TypedConditionRegistry<>("item");
    public static final TypedConditionRegistry<DamageCtx> DAMAGE = new TypedConditionRegistry<>("damage");
    public static final TypedConditionRegistry<FluidCtx> FLUID = new TypedConditionRegistry<>("fluid");
    public static final TypedConditionRegistry<BiomeCtx> BIOME = new TypedConditionRegistry<>("biome");
    public static final TypedConditionRegistry<StaticCtx> STATIC = new TypedConditionRegistry<>("static");

    private ConditionTypes() {
    }
}
