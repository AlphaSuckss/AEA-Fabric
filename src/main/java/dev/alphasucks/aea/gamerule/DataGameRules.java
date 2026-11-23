package dev.alphasucks.aea.gamerule;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.data.expr.ExprParseException;
import dev.alphasucks.aea.port.data.expr.ExprParser;
import dev.alphasucks.aea.port.data.expr.ExprVars;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_1928;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_18;
import net.minecraft.class_3222;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Data pack gamerules, as in AEA for NeoForge: {@code data/<namespace>/gamerules/<name>.json} defines the rule
 * {@code <namespace>:<name>}:
 * <pre>{ "type": "boolean", "default": true }
 * { "type": "integer", "default": 6, "min": 0, "max": 100 }</pre>
 * It is set with the vanilla command ({@code /gamerule example:creeper_break_blocks false}), saved with the world and
 * kept when the pack is removed. Packs read it with the {@code aea:gamerule} condition or the {@code gamerule(...)}
 * expression function; the server sends players the current values.
 */
public final class DataGameRules {
    private DataGameRules() {
    }

    public enum Kind { BOOLEAN, INTEGER }

    public record Def(String name, Kind kind, int defaultValue, int min, int max) {
        int clamp(int value) {
            return kind == Kind.BOOLEAN ? (value != 0 ? 1 : 0) : Math.max(min, Math.min(max, value));
        }

        String show(int value) {
            return kind == Kind.BOOLEAN ? Boolean.toString(value != 0) : Integer.toString(value);
        }
    }

    public static final class_2960 CHANNEL = new class_2960("aea", "gamerules");
    private static final int MAX_RULES = 1024;

    private static volatile Map<String, Def> defs = Map.of();
    @Nullable
    private static volatile MinecraftServer server;
    @Nullable
    private static Values values;
    private static Map<String, Integer> lastSent = Map.of();
    private static int ticks;
    /** Set by the client: reads the values the server sent. */
    @Nullable
    private static Function<String, OptionalInt> clientLookup;
    /** Vanilla (and other mods') rules by name, bool and int rules only. */
    @Nullable
    private static Map<String, class_1928.class_4313<?>> vanilla;

    public static void init() {
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public class_2960 getFabricId() {
                return new class_2960("aea", "gamerules");
            }

            @Override
            public void method_14491(class_3300 manager) {
                load(manager);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(started -> {
            server = started;
            values = started.method_30002().method_17983().method_17924(Values::read, Values::new, "aea_gamerules");
            addCommands(started);
            lastSent = Map.of();
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((reloaded, resources, success) -> {
            addCommands(reloaded);
            syncAll(reloaded);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> {
            server = null;
            values = null;
            lastSent = Map.of();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, joined) -> joined.execute(() -> send(handler.method_32311(), snapshot(joined))));
        // vanilla rules can be changed by anything; checking once a second is cheap and catches them all
        ServerTickEvents.END_SERVER_TICK.register(ticking -> {
            if (++ticks >= 20) {
                ticks = 0;
                syncIfChanged(ticking);
            }
        });
        // gamerule(<namespace>:<name>): the rule's value, with true and false as 1 and 0 (0 when no pack defines it)
        ExprParser.registerIdFunction("gamerule", (rule, args, name, at) -> {
            String ruleName = rule.toString();
            return new ExprVars.ResolvedVar((entity, container, level, value) -> {
                class_1937 world = level != null ? level : entity != null ? entity.method_37908() : null;
                return read(ruleName, world).orElse(0);
            }, false);
        });
    }

    public static void setClientLookup(Function<String, OptionalInt> lookup) {
        clientLookup = lookup;
    }

    public static Map<String, Def> definitions() {
        return defs;
    }

    // ------------------------------------------------------------------ reading

    /**
     * The value of a rule (custom or vanilla): booleans are 1 or 0. {@code world} decides the side; with no world the
     * server is used on its own thread and the client's copy everywhere else.
     */
    public static OptionalInt read(String name, @Nullable class_1937 world) {
        // no client lookup means a dedicated server: always the server's values
        boolean client = world != null ? world.field_9236 : clientLookup != null && !onServerThread();
        if (client) {
            Function<String, OptionalInt> lookup = clientLookup;
            OptionalInt value = lookup == null ? OptionalInt.empty() : lookup.apply(name);
            if (value.isPresent() || world == null) {
                return value;
            }
            return vanillaValue(world.method_8450(), name);
        }
        MinecraftServer s = server;
        if (s == null) {
            return OptionalInt.empty();
        }
        Def def = defs.get(name);
        if (def != null) {
            return OptionalInt.of(valueOf(def));
        }
        return vanillaValue(s.method_3767(), name);
    }

    private static boolean onServerThread() {
        MinecraftServer s = server;
        return s != null && s.method_18854();
    }

    private static int valueOf(Def def) {
        Values v = values;
        Integer stored = v == null ? null : v.values.get(def.name());
        return def.clamp(stored == null ? def.defaultValue() : stored);
    }

    private static OptionalInt vanillaValue(class_1928 rules, String name) {
        class_1928.class_4313<?> key = vanillaKeys().get(name);
        if (key == null) {
            return OptionalInt.empty();
        }
        Object rule = rules.method_20746(key);
        if (rule instanceof class_1928.class_4310 bool) {
            return OptionalInt.of(bool.method_20753() ? 1 : 0);
        }
        if (rule instanceof class_1928.class_4312 integer) {
            return OptionalInt.of(integer.method_20763());
        }
        return OptionalInt.empty();
    }

    private static Map<String, class_1928.class_4313<?>> vanillaKeys() {
        Map<String, class_1928.class_4313<?>> keys = vanilla;
        if (keys == null) {
            Map<String, class_1928.class_4313<?>> found = new HashMap<>();
            class_1928.method_20744(new class_1928.class_4311() {
                @Override
                public void method_27329(class_1928.class_4313<class_1928.class_4310> key, class_1928.class_4314<class_1928.class_4310> type) {
                    found.put(key.method_20771(), key);
                }

                @Override
                public void method_27330(class_1928.class_4313<class_1928.class_4312> key, class_1928.class_4314<class_1928.class_4312> type) {
                    found.put(key.method_20771(), key);
                }
            });
            vanilla = keys = Map.copyOf(found);
        }
        return keys;
    }

    // ------------------------------------------------------------------ loading

    private static void load(class_3300 manager) {
        Map<String, Def> found = new LinkedHashMap<>();
        Map<class_2960, class_3298> files = manager.method_14488("gamerules", id -> id.method_12832().endsWith(".json"));
        for (Map.Entry<class_2960, class_3298> file : files.entrySet()) {
            class_2960 fileId = file.getKey();
            String path = fileId.method_12832().substring("gamerules/".length(), fileId.method_12832().length() - ".json".length());
            String full = fileId.method_12836() + ":" + path;
            try (Reader reader = new InputStreamReader(file.getValue().method_14482(), StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!(root instanceof JsonObject json)) {
                    throw new IllegalArgumentException("not a JSON object");
                }
                Def def = parse(full, json);
                if (found.size() >= MAX_RULES) {
                    AEA.LOGGER.warn("[AEA] More than {} data pack game rules; {} and the rest are ignored.", MAX_RULES, def.name());
                    break;
                }
                found.put(def.name(), def);
            } catch (Exception e) {
                AEA.LOGGER.warn("[AEA] Could not load gamerule {}: {}", full, e.getMessage());
            }
        }
        defs = Map.copyOf(found);
        if (!found.isEmpty()) {
            AEA.LOGGER.info("[AEA] Loaded {} data pack game rule(s).", found.size());
        }
    }

    /** Same rules as AEA for NeoForge: {@code type} is required, booleans take true/false and numbers a number. */
    static Def parse(String full, JsonObject json) {
        if (!json.has("type") || !json.get("type").isJsonPrimitive()) {
            throw new IllegalArgumentException("No key type in MapLike");
        }
        String type = json.get("type").getAsString();
        Kind kind = switch (type) {
            case "boolean" -> Kind.BOOLEAN;
            case "integer" -> Kind.INTEGER;
            default -> throw new IllegalArgumentException("Unknown element name:" + type + " (use boolean or integer)");
        };
        JsonElement def = json.get("default");
        boolean boolDefault = def != null && def.isJsonPrimitive() && def.getAsJsonPrimitive().isBoolean();
        int min = json.has("min") ? json.get("min").getAsInt() : Integer.MIN_VALUE;
        int max = json.has("max") ? json.get("max").getAsInt() : Integer.MAX_VALUE;
        if (min > max) {
            throw new IllegalArgumentException("min is larger than max");
        }
        int value = def == null ? 0 : boolDefault ? (def.getAsBoolean() ? 1 : 0) : def.getAsInt();
        if (kind == Kind.BOOLEAN) {
            return new Def(full, kind, value != 0 ? 1 : 0, 0, 1);
        }
        if (boolDefault) {
            throw new IllegalArgumentException("an integer rule needs a number as its default");
        }
        return new Def(full, kind, Math.max(min, Math.min(max, value)), min, max);
    }

    // ------------------------------------------------------------------ command

    /**
     * Adds the rules to the existing {@code /gamerule} command. This runs after data packs have loaded (the command
     * tree is built before that), then sends players the updated tree.
     */
    private static void addCommands(MinecraftServer s) {
        Map<String, Def> current = defs;
        if (current.isEmpty()) {
            return;
        }
        CommandDispatcher<class_2168> dispatcher = s.method_3734().method_9235();
        LiteralArgumentBuilder<class_2168> root = class_2170.method_9247("gamerule").requires(source -> source.method_9259(2));
        for (Def def : current.values()) {
            ArgumentType<?> argument = def.kind() == Kind.BOOLEAN ? BoolArgumentType.bool() : IntegerArgumentType.integer(def.min(), def.max());
            root.then(class_2170.method_9247(def.name())
                .executes(c -> query(c, def))
                .then(class_2170.method_9244("value", argument).executes(c -> set(c, def))));
        }
        dispatcher.register(root);
        for (class_3222 player : s.method_3760().method_14571()) {
            s.method_3734().method_9241(player);
        }
    }

    private static int query(CommandContext<class_2168> c, Def def) {
        if (defs.get(def.name()) == null) {
            c.getSource().method_9213(class_2561.method_43470("The gamerule " + def.name() + " is no longer defined by any data pack."));
            return 0;
        }
        int value = valueOf(def);
        c.getSource().method_9226(() -> class_2561.method_43469("commands.gamerule.query", def.name(), def.show(value)), false);
        return value;
    }

    private static int set(CommandContext<class_2168> c, Def def) {
        int value = def.kind() == Kind.BOOLEAN ? (BoolArgumentType.getBool(c, "value") ? 1 : 0) : IntegerArgumentType.getInteger(c, "value");
        Values v = values;
        if (v == null || defs.get(def.name()) == null) {
            c.getSource().method_9213(class_2561.method_43470("The gamerule " + def.name() + " is no longer defined by any data pack."));
            return 0;
        }
        v.values.put(def.name(), def.clamp(value));
        v.method_80();
        int clamped = def.clamp(value);
        c.getSource().method_9226(() -> class_2561.method_43469("commands.gamerule.set", def.name(), def.show(clamped)), true);
        syncAll(c.getSource().method_9211());
        return clamped;
    }

    // ------------------------------------------------------------------ sync

    private static Map<String, Integer> snapshot(MinecraftServer s) {
        Map<String, Integer> out = new HashMap<>();
        class_1928 rules = s.method_3767();
        for (String name : vanillaKeys().keySet()) {
            vanillaValue(rules, name).ifPresent(v -> out.put(name, v));
        }
        for (Def def : defs.values()) {
            out.put(def.name(), valueOf(def));
        }
        return out;
    }

    private static void syncIfChanged(MinecraftServer s) {
        Map<String, Integer> now = snapshot(s);
        if (!now.equals(lastSent)) {
            lastSent = now;
            for (class_3222 player : s.method_3760().method_14571()) {
                send(player, now);
            }
        }
    }

    private static void syncAll(MinecraftServer s) {
        lastSent = Map.of();
        syncIfChanged(s);
    }

    private static void send(class_3222 player, Map<String, Integer> rules) {
        if (player.field_13987 == null) {
            return;
        }
        class_2540 buf = PacketByteBufs.create();
        buf.method_10804(rules.size());
        rules.forEach((name, value) -> {
            buf.method_10788(name, 256);
            buf.writeInt(value);
        });
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }

    /** Values set with {@code /gamerule}, saved with the overworld. */
    static final class Values extends class_18 {
        final Map<String, Integer> values = new HashMap<>();

        static Values read(class_2487 nbt) {
            Values v = new Values();
            class_2487 rules = nbt.method_10562("rules");
            for (String key : rules.method_10541()) {
                v.values.put(key, rules.method_10550(key));
            }
            return v;
        }

        @Override
        public class_2487 method_75(class_2487 nbt) {
            class_2487 rules = new class_2487();
            values.forEach(rules::method_10569);
            nbt.method_10566("rules", rules);
            return nbt;
        }
    }
}
