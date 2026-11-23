package dev.alphasucks.aea.compat.spell_engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.spell_engine.api.spell.SpellContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code aea:grant_spell}: the holder knows Spell Engine spells as if equipment gave them. They show on Spell Engine's
 * spell hotbar and cast with its own keys, cast time, cost, cooldown and visuals.
 *
 * <p>Spell Engine 0.15 builds the hotbar from the spell container of the item in the main hand
 * ({@code SpellContainerHelper.getEquipped}); {@code SpellContainerHelperMixin} adds the granted spells to it. With
 * {@code always} true (the default) they are there whatever the player holds, even an empty hand. With it false, they
 * follow Spell Engine's own rules, like a spell book: a held wand, staff or other spell item that takes spell books has
 * to accept them. Resolved on the server, so the client's hotbar matches.
 */
public final class GrantSpellPower extends PowerType<GrantSpellPower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "grant_spell");
    public static final PowerTypeGate GATE = new PowerTypeGate(ID);

    public record Cfg(List<class_2960> spells, boolean always) {}

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        SingleOrList.of(class_2960.field_25139).fieldOf("spell").forGetter(Cfg::spells),
        Codec.BOOL.optionalFieldOf("always", true).forGetter(Cfg::always)
    ).apply(i, Cfg::new));

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    @Override
    public boolean resolvesForClient() {
        return true;
    }

    /**
     * The container Spell Engine uses for the held item, with the player's granted spells added. Runs every client tick
     * for the hotbar, so it returns straight away when the player has no grants.
     */
    @Nullable
    public static SpellContainer extend(@Nullable SpellContainer equipped, class_1657 player) {
        if (player == null || !SpellPowers.holds(player, ID)) return equipped;
        List<String> always = new ArrayList<>(), withItem = new ArrayList<>();
        SpellPowers.forEach(player, ID, GATE, Cfg.class, cfg -> {
            List<String> ids = cfg.always() ? always : withItem;
            for (class_2960 id : cfg.spells()) {
                String s = id.toString();
                if (!ids.contains(s)) ids.add(s);
            }
        });
        boolean usable = equipped != null && equipped.isValid();
        // Without always, spells only go where a spell book could: into a held spell item that takes books.
        if (!usable || !equipped.is_proxy) withItem.clear();
        if (always.isEmpty() && withItem.isEmpty()) return equipped;

        List<String> merged = new ArrayList<>(usable && equipped.spell_ids != null ? equipped.spell_ids : List.of());
        add(merged, withItem);
        add(merged, always);
        if (usable && merged.size() == equipped.spell_ids.size()) return equipped;
        SpellContainer.ContentType content = usable ? equipped.content : SpellContainer.ContentType.MAGIC;
        String pool = usable && equipped.pool != null ? equipped.pool : "";
        int max = Math.max(usable ? equipped.max_spell_count : 0, merged.size());
        return new SpellContainer(content, usable && equipped.is_proxy, pool, max, merged);
    }

    private static void add(List<String> to, List<String> ids) {
        for (int i = 0; i < ids.size(); i++) {
            if (!to.contains(ids.get(i))) to.add(ids.get(i));
        }
    }

    /** Spell ids this player is granted, whatever they hold; for {@code aea:knows_spell}. */
    static List<class_2960> granted(class_1657 player) {
        List<class_2960> out = new ArrayList<>();
        SpellPowers.forEach(player, ID, GATE, Cfg.class, cfg -> {
            for (class_2960 id : cfg.spells()) if (!out.contains(id)) out.add(id);
        });
        return out;
    }
}
