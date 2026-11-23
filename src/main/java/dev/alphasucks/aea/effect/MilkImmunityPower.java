package dev.alphasucks.aea.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_4081;
import net.minecraft.class_7923;

/**
 * {@code aea:milk_immunity}: drinking milk (or anything else that clears all effects when eaten or drunk) leaves the
 * listed effects alone. With {@code inverted} it leaves every effect alone except the listed ones. With no list at all
 * milk removes nothing.
 */
public final class MilkImmunityPower extends PowerType<MilkImmunityPower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "milk_immunity");
    public static final PowerTypeGate GATE = new PowerTypeGate(ID);

    public record Cfg(Set<class_2960> effects, EnumSet<class_4081> categories, boolean inverted) {
        public boolean protects(class_1291 effect) {
            boolean listed = effects.isEmpty() && categories.isEmpty()
                || effects.contains(class_7923.field_41174.method_10221(effect))
                || categories.contains(effect.method_18792());
            return listed != inverted;
        }
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        LoggedOptionalField.of("effect", IdCodecs.ID).forGetter(c -> Optional.empty()),
        LoggedOptionalField.of("effects", IdCodecs.ID.listOf(), List.of()).forGetter(c -> new ArrayList<>(c.effects())),
        LoggedOptionalField.of("effect_type", EffectCategories.SET, EnumSet.noneOf(class_4081.class)).forGetter(Cfg::categories),
        LoggedOptionalField.of("inverted", Codec.BOOL, false).forGetter(Cfg::inverted)
    ).apply(i, (effect, effects, categories, inverted) -> {
        Set<class_2960> all = new LinkedHashSet<>(effects);
        effect.ifPresent(all::add);
        return new Cfg(Set.copyOf(all), categories.isEmpty() ? EnumSet.noneOf(class_4081.class) : EnumSet.copyOf(categories), inverted);
    }));

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    /** The effects milk must not take from this entity right now. Empty when nothing is protected. */
    public static Map<class_1291, class_1293> protectedEffects(class_1309 entity, Map<class_1291, class_1293> active) {
        if (active.isEmpty() || !GATE.inUse()) {
            return Map.of();
        }
        Map<class_1291, class_1293> kept = null;
        for (Map.Entry<class_1291, class_1293> entry : active.entrySet()) {
            if (PowerLookup.anyActive(entity, ID, Cfg.class, Cfg::protects, entry.getKey())) {
                if (kept == null) {
                    kept = new HashMap<>();
                }
                kept.put(entry.getKey(), entry.getValue());
            }
        }
        return kept == null ? Map.of() : kept;
    }
}
