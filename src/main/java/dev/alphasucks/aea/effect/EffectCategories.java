package dev.alphasucks.aea.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.codec.SingleOrList;
import io.github.apace100.calio.data.SerializableDataType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import net.minecraft.class_1291;
import net.minecraft.class_4081;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code effect_type} field: {@code "beneficial"}, {@code "harmful"} or {@code "neutral"}, or a list of them.
 * Matches every status effect of those kinds, modded ones included.
 */
public final class EffectCategories {
    private EffectCategories() {
    }

    @Nullable
    public static class_4081 parse(String raw) {
        String name = raw.trim();
        for (class_4081 category : class_4081.values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }

    public static String name(class_4081 category) {
        return category.name().toLowerCase(Locale.ROOT);
    }

    public static boolean matches(@Nullable EnumSet<class_4081> categories, @Nullable class_1291 effect) {
        return categories != null && effect != null && categories.contains(effect.method_18792());
    }

    public static final Codec<class_4081> CATEGORY = Codec.STRING.comapFlatMap(raw -> {
        class_4081 category = parse(raw);
        return category != null ? DataResult.success(category)
            : DataResult.error(() -> "Unknown effect type \"" + raw + "\" (use beneficial, harmful or neutral)");
    }, EffectCategories::name);

    public static final Codec<EnumSet<class_4081>> SET = SingleOrList.of(CATEGORY).xmap(
        list -> list.isEmpty() ? EnumSet.noneOf(class_4081.class) : EnumSet.copyOf(list), ArrayList::new);

    /** The same field for Apoli's own types (Calio data). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final SerializableDataType<EnumSet<class_4081>> CALIO = new SerializableDataType<>((Class) EnumSet.class,
        (buf, set) -> {
            int bits = 0;
            for (class_4081 category : set) {
                bits |= 1 << category.ordinal();
            }
            buf.writeByte(bits);
        },
        buf -> {
            int bits = buf.readByte();
            EnumSet<class_4081> set = EnumSet.noneOf(class_4081.class);
            for (class_4081 category : class_4081.values()) {
                if ((bits & 1 << category.ordinal()) != 0) {
                    set.add(category);
                }
            }
            return set;
        },
        json -> {
            EnumSet<class_4081> set = EnumSet.noneOf(class_4081.class);
            if (json instanceof JsonArray array) {
                for (JsonElement element : array) {
                    set.add(read(element));
                }
            } else {
                set.add(read(json));
            }
            return set;
        });

    private static class_4081 read(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonSyntaxException("Expected an effect type (beneficial, harmful or neutral)");
        }
        class_4081 category = parse(element.getAsString());
        if (category == null) {
            throw new JsonSyntaxException("Unknown effect type \"" + element.getAsString() + "\" (use beneficial, harmful or neutral)");
        }
        return category;
    }
}
