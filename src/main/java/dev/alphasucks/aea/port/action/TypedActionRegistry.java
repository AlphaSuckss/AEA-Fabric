package dev.alphasucks.aea.port.action;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AEA: collects Overgrown's action registrations. {@link dev.alphasucks.aea.bridge.AeaRegistrar}
 * later exposes the ones Apoli 2.9.2 and Sync don't already provide.
 */
public final class TypedActionRegistry<CTX> {
    private final String groupName;
    private final Map<class_2960, Entry<CTX, ?>> byId = new LinkedHashMap<>();
    private final Map<class_2960, class_2960> aliases = new LinkedHashMap<>();

    public TypedActionRegistry(String groupName) {
        this.groupName = groupName;
    }

    public String groupName() {
        return groupName;
    }

    public class_2960 resolveId(class_2960 id) {
        return aliases.getOrDefault(id, id);
    }

    public <C> ActionType<CTX, C> register(class_2960 id, ActionType<CTX, C> type) {
        return register(id, type, AliasingOptions.NONE);
    }

    public <C> ActionType<CTX, C> register(class_2960 id, ActionType<CTX, C> type, AliasingOptions opts) {
        if (byId.containsKey(id)) {
            throw new IllegalStateException("Duplicate " + groupName + " action: " + id);
        }
        byId.put(id, new Entry<>(type, opts));
        for (class_2960 old : opts.typeAliases()) {
            aliases.put(old, id);
        }
        return type;
    }

    public MapCodec<?> applyAliasDefaults(class_2960 originalId, MapCodec<?> codec) {
        return codec;
    }

    @Nullable
    public ActionType<CTX, ?> get(class_2960 id) {
        Entry<CTX, ?> e = byId.get(resolveId(id));
        return e == null ? null : e.type();
    }

    public Map<class_2960, Entry<CTX, ?>> view() {
        return Map.copyOf(byId);
    }

    public record Entry<CTX, C>(ActionType<CTX, C> type, AliasingOptions aliases) {
    }
}
