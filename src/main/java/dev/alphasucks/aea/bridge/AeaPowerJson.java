package dev.alphasucks.aea.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw power JSON captured while Apoli 2.9.2 loads power files, so Overgrown-only top level
 * fields (tags, skill) work on every power, including Apoli's own types.
 */
public final class AeaPowerJson {
    private static final Map<class_2960, JsonObject> JSON = new ConcurrentHashMap<>();
    private static final Set<class_2960> SUB_POWERS = ConcurrentHashMap.newKeySet();

    private AeaPowerJson() {
    }

    public static void clear() {
        JSON.clear();
        SUB_POWERS.clear();
    }

    public static void record(class_2960 id, JsonElement json, boolean subPower) {
        if (json != null && json.isJsonObject()) {
            JSON.put(id, json.getAsJsonObject());
        }
        if (subPower) {
            SUB_POWERS.add(id);
        }
    }

    @Nullable
    public static JsonObject json(class_2960 id) {
        return JSON.get(id);
    }

    public static boolean isSubPower(class_2960 id) {
        return SUB_POWERS.contains(id);
    }

    public static List<String> tags(class_2960 id) {
        JsonObject json = JSON.get(id);
        if (json == null || !json.has("tags")) {
            return List.of();
        }
        JsonElement tags = json.get("tags");
        List<String> out = new ArrayList<>();
        if (tags.isJsonArray()) {
            tags.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive()) {
                    out.add(e.getAsString());
                }
            });
        } else if (tags.isJsonPrimitive()) {
            out.add(tags.getAsString());
        }
        return List.copyOf(out);
    }
}
