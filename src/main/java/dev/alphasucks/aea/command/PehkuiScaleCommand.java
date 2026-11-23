package dev.alphasucks.aea.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.alphasucks.aea.port.scale.ScaleEasing;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypeCodec;
import dev.alphasucks.aea.port.scale.Scales;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_1324;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_5134;
import net.minecraft.class_7923;

/**
 * Pehkui's {@code /scale} command, backed by AEA's own scale system. Only registered when Pehkui itself is not
 * installed, so packs that call {@code scale set pehkui:height 0.5 @s} keep working without it.
 */
public final class PehkuiScaleCommand {
    private PehkuiScaleCommand() {
    }

    private static final int DEFAULT_DELAY = 20;
    private static final Map<UUID, Map<class_2960, Integer>> DELAYS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<class_2960, ScaleEasing>> EASINGS = new ConcurrentHashMap<>();
    private static final class_2960 BASE = new class_2960("pehkui", "base");
    private static final List<String> ATTRIBUTE_TYPES = List.of("reach", "block_reach", "entity_reach", "health", "attack_speed");
    /** Pehkui types with nothing behind them here. They are accepted and do nothing, so packs using them do not error. */
    private static final List<String> UNSUPPORTED_TYPES = List.of("view_bobbing", "flight", "drops", "projectiles", "explosions");
    private static final DynamicCommandExceptionType UNKNOWN_TYPE =
        new DynamicCommandExceptionType(id -> class_2561.method_43470("Unknown scale type: " + id));

    private static boolean pehkuiNamespace(class_2960 id) {
        return id.method_12836().equals("pehkui") || id.method_12836().equals("minecraft");
    }

    private static boolean known(class_2960 id) {
        return ScaleTypeCodec.resolve(id) != null
            || pehkuiNamespace(id) && (ATTRIBUTE_TYPES.contains(id.method_12832()) || UNSUPPORTED_TYPES.contains(id.method_12832()));
    }

    /** The scale type argument, refused when it is not a type at all (a typo would otherwise do nothing, silently). */
    private static class_2960 typeArg(CommandContext<class_2168> c) throws CommandSyntaxException {
        class_2960 id = class_2232.method_9443(c, "scale_type");
        if (!known(id)) {
            throw UNKNOWN_TYPE.create(id);
        }
        return id;
    }

    /** Pehkui types AEA's scale system has no equivalent for, mapped onto attributes (reach needs Reach Entity Attributes). */
    private static List<class_1320> attributesFor(String path) {
        return switch (path) {
            case "reach" -> registered("reach-entity-attributes:reach", "reach-entity-attributes:attack_range");
            case "block_reach" -> registered("reach-entity-attributes:reach");
            case "entity_reach" -> registered("reach-entity-attributes:attack_range");
            case "health" -> List.of(class_5134.field_23716);
            case "attack_speed" -> List.of(class_5134.field_23723);
            default -> List.of();
        };
    }

    private static List<class_1320> registered(String... ids) {
        List<class_1320> out = new ArrayList<>(ids.length);
        for (String id : ids) {
            class_7923.field_41190.method_17966(new class_2960(id)).ifPresent(out::add);
        }
        return out;
    }

    private static UUID modifierId(String path) {
        return UUID.nameUUIDFromBytes(("aea:pehkui_" + path).getBytes(StandardCharsets.UTF_8));
    }

    public static void register(CommandDispatcher<class_2168> d) {
        d.register(class_2170.method_9247("scale").requires(s -> s.method_9259(2))
            .then(op("set", Op.SET))
            .then(op("add", Op.ADD))
            .then(op("subtract", Op.SUBTRACT))
            .then(op("multiply", Op.MULTIPLY))
            .then(op("divide", Op.DIVIDE))
            .then(op("power", Op.POWER))
            .then(class_2170.method_9247("reset")
                .executes(c -> reset(null, List.of(c.getSource().method_9229())))
                // The type comes first: "reset width" must mean the type, not a player called width. Selectors and
                // names with capitals are not valid type ids and fall through to "targets".
                .then(class_2170.method_9244("scale_type", class_2232.method_9441())
                    .executes(PehkuiScaleCommand::resetTypeOrPlayer)
                    .then(class_2170.method_9244("targets", class_2186.method_9306())
                        .executes(c -> reset(typeArg(c), class_2186.method_9317(c, "targets")))))
                .then(class_2170.method_9244("targets", class_2186.method_9306())
                    .executes(c -> reset(null, class_2186.method_9317(c, "targets")))))
            .then(class_2170.method_9247("get")
                .executes(c -> get(c, BASE, c.getSource().method_9229(), 1))
                .then(class_2170.method_9244("scale_type", class_2232.method_9441())
                    .executes(c -> get(c, typeArg(c), c.getSource().method_9229(), 1))
                    .then(class_2170.method_9244("entity", class_2186.method_9309())
                        .executes(c -> get(c, typeArg(c), class_2186.method_9313(c, "entity"), 1))
                        .then(class_2170.method_9244("scalingFactor", DoubleArgumentType.doubleArg())
                            .executes(c -> get(c, typeArg(c), class_2186.method_9313(c, "entity"),
                                DoubleArgumentType.getDouble(c, "scalingFactor")))))))
            .then(class_2170.method_9247("delay")
                .then(class_2170.method_9247("set")
                    .then(class_2170.method_9244("scale_type", class_2232.method_9441())
                        .then(class_2170.method_9244("ticks", IntegerArgumentType.integer(0))
                            .executes(c -> delay(c, List.of(c.getSource().method_9229())))
                            .then(class_2170.method_9244("targets", class_2186.method_9306())
                                .executes(c -> delay(c, class_2186.method_9317(c, "targets")))))))
                .then(class_2170.method_9247("reset")
                    .executes(c -> {
                        DELAYS.remove(c.getSource().method_9229().method_5667());
                        return 1;
                    })
                    .then(class_2170.method_9244("targets", class_2186.method_9306())
                        .executes(c -> {
                            for (class_1297 e : class_2186.method_9317(c, "targets")) {
                                DELAYS.remove(e.method_5667());
                            }
                            return 1;
                        })))
                .then(class_2170.method_9247("get").then(class_2170.method_9244("scale_type", class_2232.method_9441())
                    .executes(c -> delayFor(c.getSource().method_9229(), typeArg(c))))))
            .then(class_2170.method_9247("easing")
                .then(class_2170.method_9247("set").then(class_2170.method_9244("scale_type", class_2232.method_9441())
                    .then(class_2170.method_9244("easing", class_2232.method_9441())
                        .executes(c -> easing(c, List.of(c.getSource().method_9229())))
                        .then(class_2170.method_9244("targets", class_2186.method_9306())
                            .executes(c -> easing(c, class_2186.method_9317(c, "targets")))))))
                .then(class_2170.method_9247("reset").executes(c -> {
                    EASINGS.remove(c.getSource().method_9229().method_5667());
                    return 1;
                })))
            // AEA's scale state already persists; accept every Pehkui persist form as a no-op.
            .then(class_2170.method_9247("persist").executes(c -> 1)
                .then(class_2170.method_9244("args", StringArgumentType.greedyString()).executes(c -> 1))));
    }

    enum Op { SET, ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER }

    private static ArgumentBuilder<class_2168, ?> op(String name, Op op) {
        return class_2170.method_9247(name)
            .then(class_2170.method_9244("value", FloatArgumentType.floatArg())
                .executes(c -> apply(c, op, BASE, List.of(c.getSource().method_9229())))
                .then(class_2170.method_9244("targets", class_2186.method_9306())
                    .executes(c -> apply(c, op, BASE, class_2186.method_9317(c, "targets")))))
            .then(class_2170.method_9244("scale_type", class_2232.method_9441())
                .then(class_2170.method_9244("value", FloatArgumentType.floatArg())
                    .executes(c -> apply(c, op, typeArg(c), List.of(c.getSource().method_9229())))
                    .then(class_2170.method_9244("targets", class_2186.method_9306())
                        .executes(c -> apply(c, op, typeArg(c), class_2186.method_9317(c, "targets"))))));
    }

    private static float combine(Op op, float current, float v) {
        float out = switch (op) {
            case SET -> v;
            case ADD -> current + v;
            case SUBTRACT -> current - v;
            case MULTIPLY -> current * v;
            case DIVIDE -> v == 0 ? current : current / v;
            case POWER -> (float) Math.pow(current, v);
        };
        return Float.isFinite(out) ? out : current;
    }

    private static float clampScale(float v) {
        return Math.max(ScaleState.MIN, Math.min(ScaleState.MAX, v));
    }

    private static int apply(CommandContext<class_2168> c, Op op, class_2960 id, Collection<? extends class_1297> targets) {
        float v = FloatArgumentType.getFloat(c, "value");
        ScaleType type = ScaleTypeCodec.resolve(id);
        int n = 0;
        for (class_1297 e : targets) {
            if (type != null) {
                Scales.set(e, type, clampScale(combine(op, Scales.base(e, type), v)), delayFor(e, id), easingFor(e, id));
                n++;
            } else if (e instanceof class_1309 living) {
                UUID mid = modifierId(id.method_12832());
                for (class_1320 attribute : attributesFor(id.method_12832())) {
                    class_1324 inst = living.method_5996(attribute);
                    if (inst == null) {
                        continue;
                    }
                    class_1322 old = inst.method_6199(mid);
                    float current = old == null ? 1f : (float) (1 + old.method_6186());
                    float next = Math.max(0.0001f, combine(op, current, v));
                    inst.method_6200(mid);
                    if (next != 1f) {
                        inst.method_26837(new class_1322(mid, "aea:pehkui_" + id.method_12832(), next - 1, class_1322.class_1323.field_6331));
                    }
                    if (attribute == class_5134.field_23716 && living.method_6032() > living.method_6063()) {
                        living.method_6033(living.method_6063());
                    }
                }
                n++;
            }
        }
        return n;
    }

    private static void removeAttributeScale(class_1309 living, String path) {
        for (class_1320 attribute : attributesFor(path)) {
            class_1324 inst = living.method_5996(attribute);
            if (inst != null) {
                inst.method_6200(modifierId(path));
            }
        }
    }

    /** {@code /scale reset <word>}: a scale type for the caller, or else the name of an online player. */
    private static int resetTypeOrPlayer(CommandContext<class_2168> c) throws CommandSyntaxException {
        class_2960 id = class_2232.method_9443(c, "scale_type");
        if (known(id)) {
            return reset(id, List.of(c.getSource().method_9229()));
        }
        class_3222 player = id.method_12836().equals("minecraft")
            ? c.getSource().method_9211().method_3760().method_14566(id.method_12832()) : null;
        if (player == null) {
            throw UNKNOWN_TYPE.create(id);
        }
        return reset(null, List.of(player));
    }

    private static int reset(class_2960 id, Collection<? extends class_1297> targets) {
        for (class_1297 e : targets) {
            if (id == null) {
                Scales.reset(e);
                if (e instanceof class_1309 living) {
                    for (String path : ATTRIBUTE_TYPES) {
                        removeAttributeScale(living, path);
                    }
                }
                continue;
            }
            ScaleType type = ScaleTypeCodec.resolve(id);
            if (type != null) {
                Scales.set(e, type, 1f, delayFor(e, id), easingFor(e, id));
            } else if (e instanceof class_1309 living) {
                removeAttributeScale(living, id.method_12832());
            }
        }
        return targets.size();
    }

    private static int get(CommandContext<class_2168> c, class_2960 id, class_1297 e, double factor) {
        ScaleType type = ScaleTypeCodec.resolve(id);
        float value = 1f;
        if (type != null) {
            value = Scales.value(e, type);
        } else if (e instanceof class_1309 living) {
            for (class_1320 attribute : attributesFor(id.method_12832())) {
                class_1324 inst = living.method_5996(attribute);
                class_1322 m = inst == null ? null : inst.method_6199(modifierId(id.method_12832()));
                if (m != null) {
                    value = (float) (1 + m.method_6186());
                    break;
                }
            }
        }
        float shown = value;
        c.getSource().method_9226(() -> class_2561.method_43470(id + " scale of " + e.method_5477().getString() + " is " + shown), false);
        return (int) (value * factor);
    }

    private static int delay(CommandContext<class_2168> c, Collection<? extends class_1297> targets) throws CommandSyntaxException {
        class_2960 id = typeArg(c);
        int ticks = IntegerArgumentType.getInteger(c, "ticks");
        for (class_1297 e : targets) {
            DELAYS.computeIfAbsent(e.method_5667(), k -> new ConcurrentHashMap<>()).put(id, ticks);
        }
        return targets.size();
    }

    private static int easing(CommandContext<class_2168> c, Collection<? extends class_1297> targets) throws CommandSyntaxException {
        class_2960 id = typeArg(c);
        ScaleEasing easing = easingByName(class_2232.method_9443(c, "easing").method_12832());
        for (class_1297 e : targets) {
            EASINGS.computeIfAbsent(e.method_5667(), k -> new ConcurrentHashMap<>()).put(id, easing);
        }
        return targets.size();
    }

    static ScaleEasing easingByName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        for (ScaleEasing e : ScaleEasing.values()) {
            if (e.method_15434().equals(n)) {
                return e;
            }
        }
        // Pehkui names: quad_in, quad_out, quad_in_out, sine_in, ... → ease_in_quad, ...
        int split = n.indexOf('_');
        if (split > 0 && split < n.length() - 1) {
            String wanted = "ease_" + n.substring(split + 1) + "_" + n.substring(0, split);
            for (ScaleEasing e : ScaleEasing.values()) {
                if (e.method_15434().equals(wanted)) {
                    return e;
                }
            }
        }
        return ScaleEasing.LINEAR;
    }

    private static int delayFor(class_1297 e, class_2960 id) {
        Map<class_2960, Integer> m = DELAYS.get(e.method_5667());
        if (m == null) {
            return DEFAULT_DELAY;
        }
        Integer v = m.get(id);
        if (v == null) {
            v = m.get(BASE);
        }
        return v == null ? DEFAULT_DELAY : v;
    }

    private static ScaleEasing easingFor(class_1297 e, class_2960 id) {
        Map<class_2960, ScaleEasing> m = EASINGS.get(e.method_5667());
        if (m == null) {
            return ScaleEasing.LINEAR;
        }
        ScaleEasing v = m.get(id);
        return v == null ? ScaleEasing.LINEAR : v;
    }
}
