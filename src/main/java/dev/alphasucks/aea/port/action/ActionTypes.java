package dev.alphasucks.aea.port.action;

import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;

/** AEA: Overgrown's action registries (collection only, see TypedActionRegistry). */
public final class ActionTypes {
    public static final TypedActionRegistry<EntityCtx> ENTITY = new TypedActionRegistry<>("entity");
    public static final TypedActionRegistry<BiEntityCtx> BI_ENTITY = new TypedActionRegistry<>("bi_entity");
    public static final TypedActionRegistry<BlockCtx> BLOCK = new TypedActionRegistry<>("block");
    public static final TypedActionRegistry<ItemCtx> ITEM = new TypedActionRegistry<>("item");

    private ActionTypes() {
    }
}
