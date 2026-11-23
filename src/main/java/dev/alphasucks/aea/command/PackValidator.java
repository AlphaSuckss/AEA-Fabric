package dev.alphasucks.aea.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.TypedActionRegistry;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.TypedConditionRegistry;
import dev.alphasucks.aea.port.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.origins.origin.OriginRegistry;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.class_2168;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.server.MinecraftServer;

/**
 * {@code /aea validate}: goes through every power, origin and origin layer file of the loaded data packs and reports
 * what is broken: files that did not load, power / action / condition types nothing provides, origins that list
 * powers that do not exist, and commands that do not parse. {@code /aea validate fields} also lists fields no type
 * reads (usually typos), which the game ignores without a word.
 */
final class PackValidator {
    private PackValidator() {
    }

    private static final Set<String> POWER_COMMON = Set.of("type", "name", "description", "hidden", "condition", "loading_priority",
        "badges", "tags", "skill", "fabric:load_conditions");
    private static final int CHAT_LINES = 12;

    private static final List<class_2378<? extends Factory>> APOLI_ACTIONS = List.of(ApoliRegistries.ENTITY_ACTION,
        ApoliRegistries.BIENTITY_ACTION, ApoliRegistries.BLOCK_ACTION, ApoliRegistries.ITEM_ACTION);
    private static final List<class_2378<? extends Factory>> APOLI_CONDITIONS = List.of(ApoliRegistries.ENTITY_CONDITION,
        ApoliRegistries.BIENTITY_CONDITION, ApoliRegistries.BLOCK_CONDITION, ApoliRegistries.ITEM_CONDITION,
        ApoliRegistries.DAMAGE_CONDITION, ApoliRegistries.FLUID_CONDITION, ApoliRegistries.BIOME_CONDITION);
    private static final List<TypedActionRegistry<?>> AEA_ACTIONS = List.of(ActionTypes.ENTITY, ActionTypes.BI_ENTITY, ActionTypes.BLOCK, ActionTypes.ITEM);
    private static final List<TypedConditionRegistry<?>> AEA_CONDITIONS = List.of(ConditionTypes.ENTITY, ConditionTypes.BI_ENTITY,
        ConditionTypes.BLOCK, ConditionTypes.ITEM, ConditionTypes.DAMAGE, ConditionTypes.FLUID, ConditionTypes.BIOME);

    private enum Kind { POWER, ACTION, CONDITION }

    /** What one run found. Keys are the problem lines; values count how often each came up. */
    private static final class Report {
        final Map<String, Integer> notLoaded = new TreeMap<>();
        final Map<String, Integer> unknownTypes = new TreeMap<>();
        /** Where each unknown type was first seen. */
        final Map<String, String> unknownTypeSeen = new java.util.HashMap<>();
        final Map<String, Integer> badCommands = new LinkedHashMap<>();
        final Map<String, Integer> badReferences = new TreeMap<>();
        final Map<String, Integer> ignoredFields = new TreeMap<>();
        final Set<String> seenCommands = new HashSet<>();
        final Map<String, Set<String>> fieldCache = new java.util.HashMap<>();
        int powers;
        int origins;
        int layers;
        int skipped;
    }

    static int run(class_2168 source, boolean fields) {
        MinecraftServer server = source.method_9211();
        class_3300 resources = server.method_34864();
        CommandDispatcher<class_2168> dispatcher = server.method_3734().method_9235();
        int level = 2;
        try {
            level = io.github.apace100.apoli.Apoli.config.executeCommand.permissionLevel;
        } catch (Throwable ignored) {
        }
        // Commands are checked as whoever runs /aea validate, at the permission level Apoli runs them with.
        class_2168 commandSource = source.method_9230(level);
        Report report = new Report();

        for (Map.Entry<class_2960, JsonObject> file : read(resources, "powers", report).entrySet()) {
            class_2960 id = file.getKey();
            report.powers++;
            if (!PowerTypeRegistry.contains(id) && !PowerTypeRegistry.isDisabled(id)) {
                if (conditional(file.getValue())) {
                    // most likely left out on purpose (a mod it asks for is not installed)
                    report.skipped++;
                    continue;
                }
                report.notLoaded.merge("power " + id, 1, Integer::sum);
            }
            walk(file.getValue(), "", Kind.POWER, "power " + id, report, fields, dispatcher, commandSource);
        }
        for (Map.Entry<class_2960, JsonObject> file : read(resources, "origins", report).entrySet()) {
            class_2960 id = file.getKey();
            report.origins++;
            if (!OriginRegistry.contains(id)) {
                if (conditional(file.getValue())) {
                    report.skipped++;
                    continue;
                }
                report.notLoaded.merge("origin " + id, 1, Integer::sum);
            }
            if (file.getValue().get("powers") instanceof JsonArray powers) {
                for (JsonElement entry : powers) {
                    if (!entry.isJsonPrimitive()) {
                        continue;
                    }
                    String raw = entry.getAsString();
                    class_2960 power = class_2960.method_12829(raw);
                    if (power == null || power.method_12832().isEmpty()) {
                        report.badReferences.merge("origin " + id + " lists \"" + raw + "\", which is not a valid power id", 1, Integer::sum);
                    } else if (!PowerTypeRegistry.contains(power) && !PowerTypeRegistry.isDisabled(power)) {
                        report.badReferences.merge("origin " + id + " lists power " + power + ", which does not exist or did not load", 1, Integer::sum);
                    }
                }
            }
        }
        for (Map.Entry<class_2960, JsonObject> file : read(resources, "origin_layers", report).entrySet()) {
            class_2960 id = file.getKey();
            report.layers++;
            if (file.getValue().get("origins") instanceof JsonArray origins) {
                for (JsonElement entry : origins) {
                    if (entry.isJsonPrimitive()) {
                        checkLayerOrigin(id, entry.getAsString(), report);
                    } else if (entry instanceof JsonObject conditioned) {
                        if (conditioned.get("origins") instanceof JsonArray inner) {
                            for (JsonElement e : inner) {
                                if (e.isJsonPrimitive()) {
                                    checkLayerOrigin(id, e.getAsString(), report);
                                }
                            }
                        }
                        walk(conditioned, "", null, "origin layer " + id, report, fields, dispatcher, commandSource);
                    }
                }
            }
        }

        section("did not load (the reason is in the log from when data packs loaded)", report.notLoaded);
        report.unknownTypes.forEach((type, count) -> AEA.LOGGER.warn("[AEA] validate: unknown type (a typo, or a mod that is not installed): {}{}, first in {}",
            type, count > 1 ? " (x" + count + ")" : "", report.unknownTypeSeen.get(type)));
        section("missing reference", report.badReferences);
        section("command does not parse", report.badCommands);
        if (fields) {
            section("field is not read by its type (ignored by the game)", report.ignoredFields);
        }

        int problems = report.notLoaded.size() + report.unknownTypes.size() + report.badReferences.size() + report.badCommands.size();
        StringBuilder summary = new StringBuilder("[AEA] Checked ").append(report.powers).append(" power file(s), ")
            .append(report.origins).append(" origin(s) and ").append(report.layers).append(" layer(s)");
        if (report.skipped > 0) {
            summary.append(" (").append(report.skipped).append(" skipped by load conditions)");
        }
        summary.append(": ");
        if (problems == 0) {
            summary.append("no problems found.");
        } else {
            summary.append(report.notLoaded.size()).append(" did not load, ")
                .append(report.unknownTypes.size()).append(" unknown type(s), ")
                .append(report.badReferences.size()).append(" missing reference(s), ")
                .append(report.badCommands.size()).append(" command(s) that do not parse.");
        }
        if (fields) {
            summary.append(' ').append(report.ignoredFields.size()).append(" ignored field(s).");
        }
        String text = summary.toString();
        source.method_9226(() -> class_2561.method_43470(text), false);
        AEA.LOGGER.info(text);

        List<String> lines = new ArrayList<>();
        report.notLoaded.keySet().forEach(k -> lines.add("did not load: " + k));
        report.badReferences.keySet().forEach(lines::add);
        report.unknownTypes.forEach((k, n) -> lines.add("unknown type: " + k + (n > 1 ? " (x" + n + ")" : "")
            + ", first in " + report.unknownTypeSeen.get(k)));
        report.badCommands.keySet().forEach(k -> lines.add("command: " + k));
        if (fields) {
            report.ignoredFields.forEach((k, n) -> lines.add("ignored field: " + k + (n > 1 ? " (x" + n + ")" : "")));
        }
        for (int i = 0; i < lines.size() && i < CHAT_LINES; i++) {
            String line = lines.get(i);
            source.method_9226(() -> class_2561.method_43470(" - " + line), false);
        }
        if (lines.size() > CHAT_LINES) {
            int more = lines.size() - CHAT_LINES;
            source.method_9226(() -> class_2561.method_43470(" ... and " + more + " more, see the log."), false);
        }
        return problems + (fields ? report.ignoredFields.size() : 0);
    }

    private static void section(String title, Map<String, Integer> lines) {
        lines.forEach((line, count) -> AEA.LOGGER.warn("[AEA] validate: {}: {}{}", title, line, count > 1 ? " (x" + count + ")" : ""));
    }

    private static void checkLayerOrigin(class_2960 layer, String raw, Report report) {
        class_2960 origin = class_2960.method_12829(raw);
        if (origin == null) {
            report.badReferences.merge("origin layer " + layer + " lists \"" + raw + "\", which is not a valid origin id", 1, Integer::sum);
        } else if (!OriginRegistry.contains(origin)) {
            report.badReferences.merge("origin layer " + layer + " lists origin " + origin + ", which does not exist or did not load", 1, Integer::sum);
        }
    }

    /** Whether the file has load conditions. Whether they pass is only known while data packs load, so it is not asked here. */
    private static boolean conditional(JsonObject file) {
        return file.has(ResourceConditions.CONDITIONS_KEY);
    }

    /** Every JSON file under {@code data/<namespace>/<folder>/}, by id. */
    private static Map<class_2960, JsonObject> read(class_3300 resources, String folder, Report report) {
        Map<class_2960, JsonObject> out = new TreeMap<>();
        Map<class_2960, class_3298> found = resources.method_14488(folder, id -> id.method_12832().endsWith(".json"));
        for (Map.Entry<class_2960, class_3298> entry : found.entrySet()) {
            String path = entry.getKey().method_12832();
            class_2960 id = new class_2960(entry.getKey().method_12836(), path.substring(folder.length() + 1, path.length() - ".json".length()));
            JsonElement json;
            try (Reader reader = new InputStreamReader(entry.getValue().method_14482(), StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader);
            } catch (Exception e) {
                report.notLoaded.merge(folder + " file " + id + " is not valid JSON: " + e.getMessage(), 1, Integer::sum);
                continue;
            }
            if (!(json instanceof JsonObject object)) {
                continue;
            }
            out.put(id, object);
        }
        return out;
    }

    /** Fields are told apart by name: "entity_action", "action_on_hit", "bientity_condition", "conditions", ... */
    private static Kind kindOf(String key) {
        Kind kind = null;
        for (String word : key.split("_")) {
            if (word.equals("action") || word.equals("actions")) {
                kind = Kind.ACTION;
            } else if (word.equals("condition") || word.equals("conditions")) {
                kind = Kind.CONDITION;
            }
        }
        return kind;
    }

    private static void walk(JsonElement element, String key, Kind kind, String where, Report report, boolean fields,
                             CommandDispatcher<class_2168> dispatcher, class_2168 commandSource) {
        if (element instanceof JsonArray array) {
            for (JsonElement child : array) {
                walk(child, key, kind, where, report, fields, dispatcher, commandSource);
            }
            return;
        }
        if (!(element instanceof JsonObject object)) {
            return;
        }
        String typeText = object.get("type") != null && object.get("type").isJsonPrimitive() ? object.get("type").getAsString() : null;
        class_2960 type = typeText == null ? null : class_2960.method_12829(typeText);
        boolean multiple = false;
        if (kind != null && typeText != null) {
            Set<String> known = type == null ? null : fieldsOf(type, kind, report);
            if (known == null) {
                String unknown = kind.name().toLowerCase(java.util.Locale.ROOT) + " type " + typeText;
                report.unknownTypes.merge(unknown, 1, Integer::sum);
                report.unknownTypeSeen.putIfAbsent(unknown, where);
            } else {
                multiple = kind == Kind.POWER && type.method_12832().equals("multiple");
                if (fields && !multiple && !known.isEmpty()) {
                    for (String field : object.keySet()) {
                        if (!known.contains(field) && !field.equals("type")) {
                            report.ignoredFields.merge(typeText + " ." + field, 1, Integer::sum);
                        }
                    }
                }
            }
        }
        if (typeText != null) {
            // "command", "command_at_hit", "command_along_ray", ...
            for (Map.Entry<String, JsonElement> field : object.entrySet()) {
                String name = field.getKey();
                if ((name.equals("command") || name.startsWith("command_")) && field.getValue().isJsonPrimitive()
                    && field.getValue().getAsJsonPrimitive().isString()) {
                    checkCommand(field.getValue().getAsString(), where, report, dispatcher, commandSource);
                }
            }
        }
        boolean gui = type != null && type.method_12832().equals("gui") && kind == Kind.POWER;
        for (Map.Entry<String, JsonElement> child : object.entrySet()) {
            String childKey = child.getKey();
            if (childKey.equals("badges")) {
                continue;
            }
            Kind childKind;
            if (multiple) {
                // every other object of an apoli:multiple is a sub-power
                childKind = childKey.equals("condition") ? Kind.CONDITION
                    : POWER_COMMON.contains(childKey) || !child.getValue().isJsonObject() ? null : Kind.POWER;
            } else {
                childKind = gui && childKey.equals("elements") ? null : kindOf(childKey);
                // An object without a type is only a wrapper ({"element": {...}, "weight": 1}): what it holds is of
                // the same kind as the list it sits in.
                if (childKind == null && typeText == null && (kind == Kind.ACTION || kind == Kind.CONDITION)
                    && !childKey.equals("elements")) {
                    childKind = kind;
                }
            }
            walk(child.getValue(), childKey, childKind, where, report, fields, dispatcher, commandSource);
        }
    }

    /**
     * The fields a type reads, or null when no such type is registered. An empty set means the type exists but its
     * fields are not known (so they are not checked).
     */
    private static Set<String> fieldsOf(class_2960 id, Kind kind, Report report) {
        String cacheKey = kind + " " + id;
        if (report.fieldCache.containsKey(cacheKey)) {
            return report.fieldCache.get(cacheKey);
        }
        Set<String> result = computeFields(id, kind);
        report.fieldCache.put(cacheKey, result);
        return result;
    }

    private static Set<String> computeFields(class_2960 id, Kind kind) {
        Set<String> out = new HashSet<>();
        boolean found = false;
        // a codec whose fields could not be listed, or a bridged Apoli entry without its AEA type
        boolean open = false;
        boolean ported = false;
        boolean bridged = false;
        class_2960 aliased = NamespaceAlias.hasAlias(id) ? NamespaceAlias.resolveAlias(id) : id;
        if (kind == Kind.POWER) {
            for (class_2960 candidate : List.of(id, aliased)) {
                PowerType<?> type = dev.alphasucks.aea.port.power.PowerTypeRegistry.get(candidate);
                if (type != null) {
                    ported = true;
                    open |= !addKeys(type.configCodec(), out);
                    out.addAll(dev.alphasucks.aea.port.power.PowerTypeRegistry.options(
                        dev.alphasucks.aea.port.power.PowerTypeRegistry.resolveId(candidate)).fieldAliases().keySet());
                    break;
                }
            }
            Factory factory = lookup(ApoliRegistries.POWER_FACTORY, id, aliased);
            if (factory != null) {
                found = true;
                bridged |= !addFields(factory, out);
            }
            out.addAll(POWER_COMMON);
        } else if (kind == Kind.ACTION) {
            for (TypedActionRegistry<?> registry : AEA_ACTIONS) {
                for (class_2960 candidate : List.of(id, aliased)) {
                    ActionType<?, ?> type = registry.get(candidate);
                    if (type != null) {
                        ported = true;
                        open |= !addKeys(type.codec(), out);
                        TypedActionRegistry.Entry<?, ?> entry = registry.view().get(registry.resolveId(candidate));
                        if (entry != null) {
                            out.addAll(entry.aliases().fieldAliases().keySet());
                        }
                        break;
                    }
                }
            }
            for (class_2378<? extends Factory> registry : APOLI_ACTIONS) {
                Factory factory = lookup(registry, id, aliased);
                if (factory != null) {
                    found = true;
                    bridged |= !addFields(factory, out);
                }
            }
        } else {
            for (TypedConditionRegistry<?> registry : AEA_CONDITIONS) {
                for (class_2960 candidate : List.of(id, aliased)) {
                    ConditionType<?, ?> type = registry.get(candidate);
                    if (type != null) {
                        ported = true;
                        open |= !addKeys(type.codec(), out);
                        TypedConditionRegistry.Entry<?, ?> entry = registry.view().get(registry.resolveId(candidate));
                        if (entry != null) {
                            out.addAll(entry.aliases().fieldAliases().keySet());
                        }
                        break;
                    }
                }
            }
            for (class_2378<? extends Factory> registry : APOLI_CONDITIONS) {
                Factory factory = lookup(registry, id, aliased);
                if (factory != null) {
                    found = true;
                    bridged |= !addFields(factory, out);
                }
            }
            out.add("inverted");
        }
        if (!found && !ported) {
            return null;
        }
        return open || bridged && !ported ? Set.of() : out;
    }

    private static Factory lookup(class_2378<? extends Factory> registry, class_2960 id, class_2960 aliased) {
        Factory factory = registry.method_10223(id);
        return factory != null || aliased.equals(id) ? factory : registry.method_10223(aliased);
    }

    /** Adds the field names of an Apoli type. False for AEA's bridged types, which keep their fields in a codec instead. */
    private static boolean addFields(Factory factory, Set<String> out) {
        io.github.apace100.calio.data.SerializableData data = factory.getSerializableData();
        if (data == null || data.getClass() != io.github.apace100.calio.data.SerializableData.class) {
            return false;
        }
        for (String name : data.getFieldNames()) {
            out.add(name);
        }
        return true;
    }

    private static boolean addKeys(MapCodec<?> codec, Set<String> out) {
        try {
            int before = out.size();
            codec.keys(JsonOps.INSTANCE).forEach(j -> out.add(j.isJsonPrimitive() ? j.getAsString() : j.toString()));
            return out.size() > before;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void checkCommand(String raw, String where, Report report, CommandDispatcher<class_2168> dispatcher, class_2168 source) {
        String command = raw.startsWith("/") ? raw.substring(1) : raw;
        // macro arguments and placeholders are filled in at run time
        if (command.isBlank() || command.contains("$(") || command.contains("%") || !report.seenCommands.add(command)) {
            return;
        }
        String probe = command.replace("{player}", "Steve").replaceAll("\\{value}|\\{slider:[^}]*}", "1");
        String why;
        try {
            ParseResults<class_2168> parsed = dispatcher.parse(probe, source);
            CommandContextBuilder<class_2168> last = parsed.getContext();
            while (last.getChild() != null) {
                last = last.getChild();
            }
            if (!parsed.getExceptions().isEmpty()) {
                why = parsed.getExceptions().values().iterator().next().getMessage();
            } else if (parsed.getReader().canRead()) {
                why = "not understood from character " + parsed.getReader().getCursor();
            } else if (last.getCommand() == null) {
                why = "incomplete command";
            } else {
                return;
            }
        } catch (RuntimeException e) {
            why = e.toString();
        }
        report.badCommands.merge(command + "  <-- " + why + "  (in " + where + ")", 1, Integer::sum);
    }
}
