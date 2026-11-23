package dev.alphasucks.aea.originsport.origin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.Locale;

/** Overgrown's Impact.LEVEL_CODEC: an impact as a number or as none/low/medium/high. */
public final class ImpactLevels {
    private static final String[] NAMES = {"none", "low", "medium", "high"};

    private static final Codec<Integer> NAME_CODEC = Codec.STRING.comapFlatMap(
        name -> {
            String lower = name.toLowerCase(Locale.ROOT);
            for (int i = 0; i < NAMES.length; i++) {
                if (NAMES[i].equals(lower)) {
                    return com.mojang.serialization.DataResult.success(i);
                }
            }
            return com.mojang.serialization.DataResult.error(() -> "Unknown impact: " + name);
        },
        level -> level >= 0 && level < NAMES.length ? NAMES[level] : String.valueOf(level));

    public static final Codec<Integer> LEVEL_CODEC = Codec.either(Codec.INT, NAME_CODEC).xmap(
        either -> either.map(level -> level, level -> level),
        level -> level >= 0 && level < NAMES.length ? Either.right(level) : Either.left(level));

    private ImpactLevels() {
    }
}
