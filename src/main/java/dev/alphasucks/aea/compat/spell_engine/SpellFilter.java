package dev.alphasucks.aea.compat.spell_engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.codec.SingleOrList;
import net.minecraft.class_2960;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Which Spell Engine spells something applies to: {@code "spell"} (a spell id or a list of them) and {@code "school"}
 * (a Spell Power school id such as {@code spell_power:fire}, or a list). Both empty means every spell; with both, a
 * spell has to match both.
 *
 * <p>Spell Engine 0.15 (Minecraft 1.20.1) has no spell tags, so {@code #}tags are read but match nothing here.
 */
public final class SpellFilter {
    public static final SpellFilter ANY = new SpellFilter(List.of(), List.of());

    private static final Codec<String> ENTRY = Codec.STRING.comapFlatMap(s -> {
        String raw = s.startsWith("#") ? s.substring(1) : s;
        return class_2960.method_12829(raw) == null ? DataResult.error(() -> "Not a spell id or #tag: " + s) : DataResult.success(s);
    }, s -> s);

    public static final MapCodec<SpellFilter> FIELDS = RecordCodecBuilder.mapCodec(i -> i.group(
        SingleOrList.of(ENTRY).optionalFieldOf("spell", List.of()).forGetter(SpellFilter::raw),
        SingleOrList.of(class_2960.field_25139).optionalFieldOf("school", List.of()).forGetter(SpellFilter::schools)
    ).apply(i, SpellFilter::new));

    private static boolean warnedTags;

    private final List<String> raw;
    private final List<class_2960> ids = new ArrayList<>();
    private final boolean tagged;
    private final List<class_2960> schools;

    public SpellFilter(List<String> raw, List<class_2960> schools) {
        this.raw = raw;
        this.schools = schools;
        boolean t = false;
        for (String s : raw) {
            if (s.startsWith("#")) t = true;
            else ids.add(new class_2960(s));
        }
        tagged = t;
        if (t && !warnedTags) {
            warnedTags = true;
            AEA.LOGGER.warn("[AEA] Spell tags (#...) need Spell Engine for 1.21.1; on 1.20.1 they match no spell. Seen in {}", raw);
        }
    }

    public List<String> raw() {
        return raw;
    }

    public List<class_2960> schools() {
        return schools;
    }

    /** Plain spell ids listed (tags left out). */
    public List<class_2960> ids() {
        return ids;
    }

    public boolean any() {
        return raw.isEmpty() && schools.isEmpty();
    }

    public boolean test(@Nullable class_2960 id) {
        return id != null && test(id, SpellRegistry.getSpell(id));
    }

    public boolean test(@Nullable Spell spell) {
        return spell != null && test(SpellIds.of(spell), spell);
    }

    public boolean test(@Nullable class_2960 id, @Nullable Spell spell) {
        if (spell == null) return false;
        if (!raw.isEmpty() && (id == null || !ids.contains(id))) return false;
        if (schools.isEmpty()) return true;
        return spell.school != null && schools.contains(spell.school.id);
    }

    boolean tagged() {
        return tagged;
    }
}
