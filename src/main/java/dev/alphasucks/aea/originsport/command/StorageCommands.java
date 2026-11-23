package dev.alphasucks.aea.originsport.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.storage.OriginStorage;
import dev.alphasucks.aea.originsport.storage.StoredData;
import dev.alphasucks.aea.originsport.storage.StoredDataAttachment;
import dev.alphasucks.aea.port.command.ApoliPermissions;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_5250;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;

/**
 * Overgrown's {@code /origin storage ...} subcommands, merged into apace's {@code /origin} command
 * (brigadier merges same-named literals; the root keeps apace's permission level 2).
 */
public final class StorageCommands {
    private static final SuggestionProvider<class_2168> LAYERS = (ctx, b) ->
        class_2172.method_9257(ApaceOrigins.layersOrdered().stream().map(OriginLayer::getIdentifier), b);
    private static final SuggestionProvider<class_2168> KEYS = (ctx, b) -> {
        try {
            StoredData data = StoredDataAttachment.get(class_2186.method_9315(ctx, "target"));
            return class_2172.method_9265(data == null ? List.of() : data.keys(), b);
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            return class_2172.method_9265(List.<String>of(), b);
        }
    };

    private StorageCommands() {
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        // AEA: the root literal carries the same requirement as apace's /origin, so whichever mod
        // registers first, merging can't open apace's subcommands to everyone
        dispatcher.register(class_2170.method_9247("origin")
            .requires(source -> source.method_9259(2))
            .then(build()));
    }

    public static LiteralArgumentBuilder<class_2168> build() {
        return class_2170.method_9247("storage")
            .requires(ApoliPermissions.require("origins.command.origin.storage", 2))
            .then(class_2170.method_9247("list")
                .then(class_2170.method_9244("target", class_2186.method_9305()).executes(StorageCommands::list)))
            .then(class_2170.method_9247("get")
                .then(class_2170.method_9244("target", class_2186.method_9305())
                    .then(class_2170.method_9244("key", StringArgumentType.word()).suggests(KEYS).executes(StorageCommands::get))))
            .then(class_2170.method_9247("store")
                .then(class_2170.method_9247("origin")
                    .then(class_2170.method_9244("target", class_2186.method_9305())
                        .then(class_2170.method_9244("key", StringArgumentType.word())
                            .then(class_2170.method_9244("source", class_2186.method_9305())
                                .executes(ctx -> storeOrigin(ctx, OriginStorage.DEFAULT_LAYER))
                                .then(class_2170.method_9244("layer", class_2232.method_9441()).suggests(LAYERS)
                                    .executes(ctx -> storeOrigin(ctx, class_2232.method_9443(ctx, "layer"))))))))
                .then(class_2170.method_9247("value")
                    .then(class_2170.method_9244("target", class_2186.method_9305())
                        .then(class_2170.method_9244("key", StringArgumentType.word())
                            .then(class_2170.method_9244("value", StringArgumentType.greedyString()).executes(StorageCommands::storeValue))))))
            .then(class_2170.method_9247("apply")
                .then(class_2170.method_9244("target", class_2186.method_9305())
                    .then(class_2170.method_9244("key", StringArgumentType.word()).suggests(KEYS)
                        .executes(ctx -> apply(ctx, null))
                        .then(class_2170.method_9244("layer", class_2232.method_9441()).suggests(LAYERS)
                            .executes(ctx -> apply(ctx, class_2232.method_9443(ctx, "layer")))))))
            .then(class_2170.method_9247("clear")
                .then(class_2170.method_9244("target", class_2186.method_9305())
                    .executes(ctx -> clear(ctx, null))
                    .then(class_2170.method_9244("key", StringArgumentType.word()).suggests(KEYS)
                        .executes(ctx -> clear(ctx, StringArgumentType.getString(ctx, "key"))))))
            .then(class_2170.method_9247("run")
                .then(class_2170.method_9244("target", class_2186.method_9305())
                    .then(class_2170.method_9244("command", StringArgumentType.greedyString()).executes(StorageCommands::run))));
    }

    private static String nameOf(class_3222 player) {
        return player.method_7334().getName();
    }

    private static int list(CommandContext<class_2168> ctx) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        StoredData data = StoredDataAttachment.get(target);
        if (data == null || data.isEmpty()) {
            ctx.getSource().method_9226(() -> class_2561.method_43470(nameOf(target) + " has no stored data."), false);
            return 0;
        }
        class_5250 out = class_2561.method_43470("Stored data for " + nameOf(target) + ":").method_27692(class_124.field_1067);
        for (Map.Entry<String, StoredData.StoredOrigin> entry : data.originsView().entrySet()) {
            Origin origin = ApaceOrigins.origin(entry.getValue().origin());
            out.method_10852(class_2561.method_43470("\n  " + entry.getKey() + " = ").method_27692(class_124.field_1068))
                .method_10852(origin != null ? origin.getName() : class_2561.method_43470(entry.getValue().origin().toString()))
                .method_10852(class_2561.method_43470(" (" + entry.getValue().layer() + ")").method_27692(class_124.field_1080));
        }
        for (Map.Entry<String, String> entry : data.valuesView().entrySet()) {
            out.method_10852(class_2561.method_43470("\n  " + entry.getKey() + " = " + entry.getValue()).method_27692(class_124.field_1068));
        }
        ctx.getSource().method_9226(() -> out, false);
        return data.keys().size();
    }

    private static int get(CommandContext<class_2168> ctx) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        String key = StringArgumentType.getString(ctx, "key");
        String value = OriginStorage.lookup(target, key);
        if (value == null) {
            ctx.getSource().method_9213(class_2561.method_43470("No stored value under '" + key + "'"));
            return 0;
        }
        ctx.getSource().method_9226(() -> class_2561.method_43470(key + " = " + value), false);
        return 1;
    }

    private static int storeOrigin(CommandContext<class_2168> ctx, class_2960 layerId) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        class_3222 source = class_2186.method_9315(ctx, "source");
        String key = StringArgumentType.getString(ctx, "key");
        if (!OriginStorage.storeOrigin(target, key, source, layerId)) {
            ctx.getSource().method_9213(class_2561.method_43470(nameOf(source) + " has no origin on " + layerId));
            return 0;
        }
        ctx.getSource().method_9226(() -> class_2561.method_43470(
            "Stored " + nameOf(source) + "'s " + layerId + " origin as '" + key + "' on " + nameOf(target)), true);
        return 1;
    }

    private static int storeValue(CommandContext<class_2168> ctx) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        String key = StringArgumentType.getString(ctx, "key");
        String raw = StringArgumentType.getString(ctx, "value");
        String value = OriginStorage.resolve(target, raw);
        if (value == null) {
            ctx.getSource().method_9213(class_2561.method_43470("Unresolved placeholder in: " + raw));
            return 0;
        }
        OriginStorage.storeValue(target, key, value);
        ctx.getSource().method_9226(() -> class_2561.method_43470("Stored '" + key + "' = " + value + " on " + nameOf(target)), true);
        return 1;
    }

    private static int apply(CommandContext<class_2168> ctx, class_2960 layerId) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        String key = StringArgumentType.getString(ctx, "key");
        if (!OriginStorage.applyStoredOrigin(target, key, layerId, false)) {
            ctx.getSource().method_9213(class_2561.method_43470("No usable origin stored under '" + key + "'"));
            return 0;
        }
        ctx.getSource().method_9226(() -> class_2561.method_43470("Applied stored origin '" + key + "' to " + nameOf(target)), true);
        return 1;
    }

    private static int clear(CommandContext<class_2168> ctx, String key) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        if (!OriginStorage.clear(target, key)) {
            ctx.getSource().method_9213(class_2561.method_43470("Nothing to clear."));
            return 0;
        }
        ctx.getSource().method_9226(() -> class_2561.method_43470(
            "Cleared " + (key == null ? "all stored data" : "'" + key + "'") + " on " + nameOf(target)), true);
        return 1;
    }

    private static int run(CommandContext<class_2168> ctx) throws CommandSyntaxException {
        class_3222 target = class_2186.method_9315(ctx, "target");
        String template = StringArgumentType.getString(ctx, "command");
        String command = OriginStorage.resolve(target, template);
        if (command == null) {
            ctx.getSource().method_9213(class_2561.method_43470("Unresolved placeholder in: " + template));
            return 0;
        }
        MinecraftServer server = target.method_5682();
        if (server == null) {
            return 0;
        }
        // AEA: Overgrown ran the command from the server's own source (permission level 4) — an
        // escalation for a level-2 command; it now runs as the target with the caller's permissions
        class_2168 source = ctx.getSource().method_9232(target).method_9208(target.method_19538()).method_9216(target.method_5802());
        return server.method_3734().method_44252(source, command);
    }
}
