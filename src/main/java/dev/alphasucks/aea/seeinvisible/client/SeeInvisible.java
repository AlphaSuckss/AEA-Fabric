package dev.alphasucks.aea.seeinvisible.client;

import dev.alphasucks.aea.seeinvisible.SeeInvisiblePower;
import net.minecraft.class_1297;
import net.minecraft.class_310;

/** Client side of {@code aea:see_invisible}: how see-through the local player sees an entity. */
public final class SeeInvisible {
    private SeeInvisible() {
    }

    /** Opacity for the local player (0 solid to 1 not drawn), or NaN when no power of theirs reveals the entity. */
    public static float opacity(class_1297 target) {
        return SeeInvisiblePower.revealOpacity(class_310.method_1551().field_1724, target);
    }
}
