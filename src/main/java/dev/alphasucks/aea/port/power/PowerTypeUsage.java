package dev.alphasucks.aea.port.power;

import net.minecraft.class_2960;

import java.util.Collection;

/**
 * AEA: Overgrown's "is any entity holding this type" fast path. Here a type counts as held
 * when any loaded power definition uses it, which is cheap and keeps the mixin early-outs.
 */
public final class PowerTypeUsage {
    private PowerTypeUsage() {
    }

    public static Handle handle(class_2960 canonicalType) {
        return new Handle(canonicalType);
    }

    public static boolean isHeld(class_2960 canonicalType) {
        return ApoliPowers.anyOfType(canonicalType);
    }

    public static long[] bitsOf(Collection<class_2960> canonicalTypes) {
        return new long[0];
    }

    public static void markHeld(class_2960 canonicalType) {
    }

    public static void open(long[] bits) {
    }

    public static void reset() {
    }

    public static final class Handle {
        private final class_2960 type;

        private Handle(class_2960 type) {
            this.type = type;
        }

        public boolean isHeld() {
            return ApoliPowers.anyOfType(type);
        }
    }
}
