package dev.alphasucks.aea.originsport.origin;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

/** Which of a player's origins an action/condition looks at: main, active (swapped-in or main), pool, or all. */
public enum OriginSelection implements class_3542 {
    MAIN("main"),
    ACTIVE("active"),
    POOL("pool"),
    ALL("all");

    public static final Codec<OriginSelection> CODEC = class_3542.method_28140(OriginSelection::values);
    private final String name;

    OriginSelection(String name) {
        this.name = name;
    }

    @Override
    public String method_15434() {
        return name;
    }
}
