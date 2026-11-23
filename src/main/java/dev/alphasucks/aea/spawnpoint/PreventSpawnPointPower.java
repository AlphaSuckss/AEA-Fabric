package dev.alphasucks.aea.spawnpoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3481;
import net.minecraft.class_4969;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;

/**
 * {@code aea:prevent_spawn_point}, as in AEA for NeoForge: the holder's spawn point can't be set or moved. Every way
 * the game sets one goes through {@code ServerPlayerEntity.setSpawnPoint}; this refuses it there. Clearing the spawn
 * point (a bed that was broken, an anchor that ran out) is left alone, and so is respawning, which copies the old
 * spawn point onto the new player.
 * <ul>
 * <li>{@code source}: {@code bed}, {@code respawn_anchor}, {@code command} ({@code /spawnpoint}) or {@code other},
 * one or a list. Default: all of them.</li>
 * <li>{@code message}: text shown above the hotbar when a spawn point is refused.</li>
 * </ul>
 */
public final class PreventSpawnPointPower extends PowerType<PreventSpawnPointPower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "prevent_spawn_point");
    public static final PowerTypeGate GATE = new PowerTypeGate(ID);

    public enum Source {
        BED, RESPAWN_ANCHOR, COMMAND, OTHER;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static final Codec<Source> CODEC = Codec.STRING.comapFlatMap(raw -> {
            for (Source source : values()) {
                if (source.id().equals(raw)) {
                    return DataResult.success(source);
                }
            }
            return DataResult.error(() -> "Unknown spawn point source \"" + raw + "\" (use bed, respawn_anchor, command or other)");
        }, Source::id);
    }

    /** One source or a list; a bad entry is reported as itself, not as "not a list". */
    private static final Codec<List<Source>> SOURCE_LIST = new Codec<>() {
        @Override
        public <T> DataResult<com.mojang.datafixers.util.Pair<List<Source>, T>> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
            if (ops.getStream(input).result().isPresent()) {
                return Source.CODEC.listOf().decode(ops, input);
            }
            return Source.CODEC.decode(ops, input).map(pair -> pair.mapFirst(List::of));
        }

        @Override
        public <T> DataResult<T> encode(List<Source> input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return Source.CODEC.listOf().encode(input, ops, prefix);
        }
    };

    private static final Codec<Set<Source>> SOURCES = SOURCE_LIST.xmap(
        list -> list.isEmpty() ? EnumSet.allOf(Source.class) : EnumSet.copyOf(list), ArrayList::new);

    public record Cfg(Set<Source> source, Optional<class_2561> message) {
        boolean blocks(Source from) {
            return source.contains(from);
        }
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        // strict, as on NeoForge: a misspelled source fails the power instead of blocking every source
        LoggedOptionalField.strict("source", SOURCES).forGetter(c -> Optional.of(c.source())),
        LoggedOptionalField.strict("message", TextComponent.CODEC).forGetter(Cfg::message)
    ).apply(i, (source, message) -> new Cfg(source.orElseGet(() -> EnumSet.allOf(Source.class)), message)));

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    /** Set while {@code /spawnpoint} runs. Server thread only. */
    public static boolean inCommand;
    /** Set while a player respawns: the game copies the old spawn point onto the new player. Server thread only. */
    public static boolean inRespawn;

    /** Whether this spawn point must be refused; shows the power's message when it is. */
    public static boolean refuse(class_3222 player, class_5321<class_1937> dimension, @Nullable class_2338 pos) {
        if (pos == null || inRespawn || !GATE.inUse()) {
            return false;
        }
        Source from = source(player, dimension, pos);
        Cfg[] hit = {null};
        PowerLookup.forEach(player, ID, Cfg.class, cfg -> {
            if (hit[0] == null && cfg.blocks(from)) {
                hit[0] = cfg;
            }
        });
        if (hit[0] == null) {
            return false;
        }
        hit[0].message().ifPresent(m -> player.method_7353(m, true));
        return true;
    }

    /** Told apart by the block at the new spawn point, where that block is already loaded. */
    private static Source source(class_3222 player, class_5321<class_1937> dimension, class_2338 pos) {
        if (inCommand) {
            return Source.COMMAND;
        }
        class_3218 world = player.field_13995.method_3847(dimension);
        if (world == null || !world.method_22340(pos)) {
            return Source.OTHER;
        }
        class_2680 state = world.method_8320(pos);
        if (state.method_26164(class_3481.field_16443)) {
            return Source.BED;
        }
        return state.method_26204() instanceof class_4969 ? Source.RESPAWN_ANCHOR : Source.OTHER;
    }
}
