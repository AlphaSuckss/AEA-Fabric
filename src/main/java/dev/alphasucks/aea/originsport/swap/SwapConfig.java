package dev.alphasucks.aea.originsport.swap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * The {@code "swappable"} field of an Overgrown origin layer: {@code true}/{@code false} or
 * {@code {"enabled", "target_layer", "shift_returns_to_main", "wrap_to_main"}}.
 */
public record SwapConfig(boolean enabled, Optional<class_2960> targetLayer, boolean shiftReturnsToMain, boolean wrapToMain) {
    public static final SwapConfig DISABLED = new SwapConfig(false, Optional.empty(), true, true);
    public static final SwapConfig ENABLED = new SwapConfig(true, Optional.empty(), true, true);

    private static final Codec<SwapConfig> OBJECT_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(SwapConfig::enabled),
        class_2960.field_25139.optionalFieldOf("target_layer").forGetter(SwapConfig::targetLayer),
        Codec.BOOL.optionalFieldOf("shift_returns_to_main", true).forGetter(SwapConfig::shiftReturnsToMain),
        Codec.BOOL.optionalFieldOf("wrap_to_main", true).forGetter(SwapConfig::wrapToMain)
    ).apply(i, SwapConfig::new));

    public static final Codec<SwapConfig> CODEC = Codec.either(Codec.BOOL, OBJECT_CODEC).xmap(
        either -> either.map(flag -> flag ? ENABLED : DISABLED, Function.identity()),
        config -> config.equals(ENABLED) ? Either.left(true) : config.equals(DISABLED) ? Either.left(false) : Either.right(config));

    /** Parses a layer file's {@code swappable} value; null when it doesn't parse (the field is then ignored). */
    @Nullable
    public static SwapConfig parse(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null);
    }

    public static boolean has(JsonObject json) {
        return json.has("swappable");
    }

    public void write(class_2540 buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(targetLayer.isPresent());
        targetLayer.ifPresent(buf::method_10812);
        buf.writeBoolean(shiftReturnsToMain);
        buf.writeBoolean(wrapToMain);
    }

    public static SwapConfig read(class_2540 buf) {
        boolean enabled = buf.readBoolean();
        Optional<class_2960> target = buf.readBoolean() ? Optional.of(buf.method_10810()) : Optional.empty();
        boolean shift = buf.readBoolean();
        boolean wrap = buf.readBoolean();
        return new SwapConfig(enabled, target, shift, wrap);
    }
}
