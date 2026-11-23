package dev.alphasucks.aea.compat.spell_engine;

import net.minecraft.class_2960;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Spell Engine 0.15 passes {@link Spell} objects around without their ids. This finds a spell's id, rebuilding its
 * table from {@link SpellRegistry#all()} only when a spell isn't in it (after a reload or a sync from the server).
 */
public final class SpellIds {
    private SpellIds() {}

    private static Map<Spell, class_2960> ids = new IdentityHashMap<>();

    @Nullable
    public static class_2960 of(@Nullable Spell spell) {
        if (spell == null) return null;
        class_2960 id = ids.get(spell);
        if (id != null) return id;
        Map<Spell, class_2960> rebuilt = new IdentityHashMap<>();
        for (Map.Entry<class_2960, SpellRegistry.SpellEntry> e : SpellRegistry.all().entrySet()) {
            if (e.getValue() != null && e.getValue().spell != null) rebuilt.put(e.getValue().spell, e.getKey());
        }
        ids = rebuilt;
        return rebuilt.get(spell);
    }
}
