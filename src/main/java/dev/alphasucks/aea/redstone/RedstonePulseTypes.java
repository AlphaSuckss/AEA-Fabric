package dev.alphasucks.aea.redstone;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2262;
import net.minecraft.class_2267;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

import java.util.List;
import java.util.Locale;

/** {@code aea:redstone_pulse} (block action) and {@code /aea redstone pulse}. See {@link RedstonePulses}. */
public final class RedstonePulseTypes {
    private RedstonePulseTypes() {}

    public static final class_2960 ID = new class_2960("aea", "redstone_pulse");

    public record Cfg(int strength, int duration, RedstonePulses.Mode mode, boolean strong, List<class_2350> sides) {
        int sideMask() {
            if (sides.isEmpty()) return RedstonePulses.ALL_SIDES;
            int mask = 0;
            for (class_2350 d : sides) mask |= 1 << d.method_10146();
            return mask;
        }
    }

    private static final Codec<RedstonePulses.Mode> MODE = Codec.STRING.comapFlatMap(s -> switch (s) {
        case "receive" -> DataResult.success(RedstonePulses.Mode.RECEIVE);
        case "emit" -> DataResult.success(RedstonePulses.Mode.EMIT);
        default -> DataResult.error(() -> "Unknown pulse mode \"" + s + "\"; use receive or emit");
    }, m -> m == RedstonePulses.Mode.EMIT ? "emit" : "receive");

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.intRange(1, 15).optionalFieldOf("strength", 15).forGetter(Cfg::strength),
        Codec.intRange(1, 72000).optionalFieldOf("duration", 4).forGetter(Cfg::duration),
        MODE.optionalFieldOf("mode", RedstonePulses.Mode.RECEIVE).forGetter(Cfg::mode),
        Codec.BOOL.optionalFieldOf("strong", false).forGetter(Cfg::strong),
        SingleOrList.of((Codec<class_2350>) class_2350.field_29502).optionalFieldOf("sides", List.of()).forGetter(Cfg::sides)
    ).apply(i, Cfg::new));

    public static void register() {
        RedstonePulses.register();
        ActionTypes.BLOCK.register(ID, new ActionType<BlockCtx, Cfg>() {
            @Override public MapCodec<Cfg> codec() { return CODEC; }

            @Override
            public void run(Cfg cfg, BlockCtx ctx) {
                if (ctx.level() instanceof class_3218 level) {
                    RedstonePulses.pulse(level, ctx.pos(), cfg.mode(), cfg.strength(), cfg.duration(), cfg.sideMask(), cfg.strong());
                }
            }
        });
    }

    /**
     * {@code /aea redstone pulse <pos> [receive|emit] [strength] [duration] [strong]}, defaulting to receive, 15, 4 ticks,
     * weak. For packs that keep a position elsewhere, such as at a marker entity:
     * {@code execute at @e[tag=linked] run aea redstone pulse ~ ~ ~}.
     */
    public static LiteralArgumentBuilder<class_2168> command() {
        RequiredArgumentBuilder<class_2168, class_2267> pos = class_2170.method_9244("pos", class_2262.method_9698());
        pos.executes(c -> run(c, RedstonePulses.Mode.RECEIVE));
        for (RedstonePulses.Mode mode : RedstonePulses.Mode.values()) {
            pos.then(class_2170.method_9247(mode == RedstonePulses.Mode.EMIT ? "emit" : "receive").executes(c -> run(c, mode))
                .then(class_2170.method_9244("strength", IntegerArgumentType.integer(1, 15)).executes(c -> run(c, mode))
                    .then(class_2170.method_9244("duration", IntegerArgumentType.integer(1, 72000)).executes(c -> run(c, mode))
                        .then(class_2170.method_9244("strong", BoolArgumentType.bool()).executes(c -> run(c, mode))))));
        }
        return class_2170.method_9247("redstone").then(class_2170.method_9247("pulse").then(pos));
    }

    private static int run(CommandContext<class_2168> c, RedstonePulses.Mode mode) throws CommandSyntaxException {
        class_2338 pos = class_2262.method_9696(c, "pos");
        int strength = arg(c, "strength", 15);
        int duration = arg(c, "duration", 4);
        boolean strong = hasArg(c, "strong") && BoolArgumentType.getBool(c, "strong");
        RedstonePulses.pulse(c.getSource().method_9225(), pos, mode, strength, duration, RedstonePulses.ALL_SIDES, strong);
        c.getSource().method_9226(() -> class_2561.method_43470("Redstone pulse (" + mode.name().toLowerCase(Locale.ROOT) + ", "
            + strength + ", " + duration + " ticks" + (strong ? ", strong" : "") + ") at " + pos.method_23854()), false);
        return strength;
    }

    private static int arg(CommandContext<class_2168> c, String name, int fallback) {
        return hasArg(c, name) ? IntegerArgumentType.getInteger(c, name) : fallback;
    }

    private static boolean hasArg(CommandContext<class_2168> c, String name) {
        try {
            c.getArgument(name, Object.class);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
