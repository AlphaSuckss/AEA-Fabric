package dev.alphasucks.aea.originsport.origin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Overgrown origins carry a {@code "tags"} field (string or list of strings) that the
 * {@code origins:origin_tag} condition tests. apace's Origin ignores unknown fields, so this
 * read-only side loader collects just that field from the same {@code data/<ns>/origins/*.json}
 * files apace loads (same file-wins rule: highest {@code loading_priority}, first one on ties).
 * Nothing about how apace loads or uses origins changes.
 */
public final class OriginTags implements SimpleSynchronousResourceReloadListener {
    private static final String DIRECTORY = "origins";
    private static volatile Map<class_2960, List<String>> server = Map.of();
    private static volatile Map<class_2960, List<String>> client = Map.of();

    @Override
    public class_2960 getFabricId() {
        return OriginsPortIds.aea("origin_tags");
    }

    @Override
    public Collection<class_2960> getFabricDependencies() {
        return Set.of(new class_2960("origins", "origins"));
    }

    @Override
    public void method_14491(class_3300 manager) {
        Map<class_2960, List<String>> out = new HashMap<>();
        Map<class_2960, List<class_3298>> found = manager.method_41265(DIRECTORY, id -> id.method_12832().endsWith(".json"));
        for (Map.Entry<class_2960, List<class_3298>> e : found.entrySet()) {
            class_2960 fileId = e.getKey();
            String path = fileId.method_12832();
            String originPath = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
            class_2960 originId = class_2960.method_12829(fileId.method_12836() + ":" + originPath);
            if (originId == null) {
                continue;
            }
            List<String> best = null;
            int bestPriority = Integer.MIN_VALUE;
            for (class_3298 resource : e.getValue()) {
                try (Reader reader = resource.method_43039()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (element == null || !element.isJsonObject()) {
                        continue;
                    }
                    JsonObject json = element.getAsJsonObject();
                    int priority = json.has("loading_priority") && json.get("loading_priority").isJsonPrimitive()
                        ? json.get("loading_priority").getAsInt() : 0;
                    if (best == null || priority > bestPriority) {
                        best = readTags(json.get("tags"));
                        bestPriority = priority;
                    }
                } catch (Exception ex) {
                    OriginsPortIds.LOGGER.debug("Couldn't read origin tags from {}: {}", fileId, ex.getMessage());
                }
            }
            if (best != null && !best.isEmpty()) {
                out.put(originId, List.copyOf(best));
            }
        }
        server = Map.copyOf(out);
    }

    private static List<String> readTags(JsonElement element) {
        List<String> tags = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return tags;
        }
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonPrimitive()) {
                    tags.add(entry.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            tags.add(element.getAsString());
        }
        return tags;
    }

    public static List<String> tags(class_2960 originId, boolean clientSide) {
        return (clientSide ? client : server).getOrDefault(originId, List.of());
    }

    public static boolean hasTag(class_2960 originId, String tag, boolean clientSide) {
        return tags(originId, clientSide).contains(tag);
    }

    public static Map<class_2960, List<String>> serverSnapshotForSync() {
        return server;
    }

    public static void acceptClient(Map<class_2960, List<String>> synced) {
        client = Map.copyOf(synced);
    }

    public static void clearClient() {
        client = Map.of();
    }
}
