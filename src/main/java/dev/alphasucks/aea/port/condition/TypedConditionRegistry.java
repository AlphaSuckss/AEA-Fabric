package dev.alphasucks.aea.port.condition;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AEA: collects Overgrown's condition registrations. {@link dev.alphasucks.aea.bridge.AeaRegistrar}
 * later exposes the ones Apoli 2.9.2 and Sync don't already provide.
 */
public final class TypedConditionRegistry<CTX> {
    private final String groupName;
    private final Map<class_2960, Entry<CTX, ?>> byId = new LinkedHashMap<>();
    private final Map<class_2960, class_2960> aliases = new LinkedHashMap<>();

    public TypedConditionRegistry(String groupName) {
        this.groupName = groupName;
    }

    public String groupName() {
        return groupName;
    }

    public class_2960 resolveId(class_2960 id) {
        return aliases.getOrDefault(id, id);
    }

    public <C> ConditionType<CTX, C> register(class_2960 id, ConditionType<CTX, C> type) {
        return register(id, type, AliasingOptions.NONE);
    }

    public <C> ConditionType<CTX, C> register(class_2960 id, ConditionType<CTX, C> type, AliasingOptions opts) {
        if (byId.containsKey(id)) {
            throw new IllegalStateException("Duplicate " + groupName + " condition: " + id);
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
    public ConditionType<CTX, ?> get(class_2960 id) {
        Entry<CTX, ?> e = byId.get(resolveId(id));
        return e == null ? null : e.type();
    }

    public Map<class_2960, Entry<CTX, ?>> view() {
        return Map.copyOf(byId);
    }

    public record Entry<CTX, C>(ConditionType<CTX, C> type, AliasingOptions aliases) {
    }
}
