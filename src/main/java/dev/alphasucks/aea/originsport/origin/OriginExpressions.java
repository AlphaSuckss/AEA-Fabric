package dev.alphasucks.aea.originsport.origin;

import dev.alphasucks.aea.port.data.expr.ExprParser;
import dev.alphasucks.aea.port.data.expr.ExprPeer;
import dev.alphasucks.aea.port.data.expr.ExprVars;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/**
 * Overgrown Origins' expression variables/functions for the ported expression engine:
 * {@code origin_count}, {@code is_swapped}, {@code has_origin(id)}, {@code target_has_origin(id)},
 * {@code in_origin_pool(id)}, {@code has_origin_on(layer)}, {@code origin_impact(layer)}.
 */
public final class OriginExpressions {
    private OriginExpressions() {
    }

    public static void register() {
        ExprVars.register("origin_count", (e, c, l, v) -> {
            class_1657 player = OriginView.playerOf(e);
            return player == null ? 0.0 : OriginView.layerCount(player);
        });
        ExprVars.register("is_swapped", (e, c, l, v) -> {
            class_1657 player = OriginView.playerOf(e);
            return player != null && !OriginView.swaps(player).isEmpty() ? 1.0 : 0.0;
        });
        ExprParser.registerIdFunction("has_origin", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> holds(e, id), false));
        ExprParser.registerIdFunction("target_has_origin", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
            class_1297 peer = ExprPeer.target();
            return holds(peer != null ? peer : e, id);
        }, false, true));
        ExprParser.registerIdFunction("in_origin_pool", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
            class_1657 player = OriginView.playerOf(e);
            return player != null && OriginView.inPool(player, id) ? 1.0 : 0.0;
        }, false));
        ExprParser.registerIdFunction("has_origin_on", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
            class_1657 player = OriginView.playerOf(e);
            return player != null && OriginView.activeOn(player, id) != null ? 1.0 : 0.0;
        }, false));
        ExprParser.registerIdFunction("origin_impact", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
            class_1657 player = OriginView.playerOf(e);
            return player == null ? 0.0 : OriginView.impactOn(player, id);
        }, false));
    }

    private static double holds(@Nullable class_1297 entity, class_2960 originId) {
        class_1657 player = OriginView.playerOf(entity);
        return player != null && OriginView.holds(player, originId) ? 1.0 : 0.0;
    }
}
