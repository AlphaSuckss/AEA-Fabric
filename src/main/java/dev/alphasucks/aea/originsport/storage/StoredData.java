package dev.alphasucks.aea.originsport.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Overgrown's per-player origin storage: named stored origins (layer + origin) and named string values. */
public final class StoredData {
    public static final Codec<StoredData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(Codec.STRING, StoredOrigin.CODEC).optionalFieldOf("origins", Map.of()).forGetter(StoredData::originsView),
        Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("values", Map.of()).forGetter(StoredData::valuesView)
    ).apply(i, StoredData::new));

    private final Map<String, StoredOrigin> origins;
    private final Map<String, String> values;

    public StoredData() {
        this(Map.of(), Map.of());
    }

    public StoredData(Map<String, StoredOrigin> origins, Map<String, String> values) {
        this.origins = new HashMap<>(origins);
        this.values = new HashMap<>(values);
    }

    @Nullable
    public synchronized StoredOrigin getOrigin(String key) {
        return origins.get(key);
    }

    public synchronized void setOrigin(String key, StoredOrigin value) {
        origins.put(key, value);
    }

    public synchronized boolean removeOrigin(String key) {
        return origins.remove(key) != null;
    }

    @Nullable
    public synchronized String getValue(String key) {
        return values.get(key);
    }

    public synchronized void setValue(String key, String value) {
        values.put(key, value);
    }

    public synchronized boolean removeValue(String key) {
        return values.remove(key) != null;
    }

    public synchronized boolean has(String key) {
        return origins.containsKey(key) || values.containsKey(key);
    }

    public synchronized boolean isEmpty() {
        return origins.isEmpty() && values.isEmpty();
    }

    public synchronized void clear() {
        origins.clear();
        values.clear();
    }

    public synchronized Set<String> keys() {
        Set<String> out = new HashSet<>(origins.keySet());
        out.addAll(values.keySet());
        return out;
    }

    public synchronized Map<String, StoredOrigin> originsView() {
        return Map.copyOf(origins);
    }

    public synchronized Map<String, String> valuesView() {
        return Map.copyOf(values);
    }

    public void write(class_2540 buf) {
        Map<String, StoredOrigin> o = originsView();
        Map<String, String> v = valuesView();
        buf.method_10804(o.size());
        o.forEach((key, stored) -> {
            buf.method_10814(key);
            buf.method_10812(stored.layer());
            buf.method_10812(stored.origin());
        });
        buf.method_10804(v.size());
        v.forEach((key, value) -> {
            buf.method_10814(key);
            buf.method_10814(value);
        });
    }

    public static StoredData read(class_2540 buf) {
        int n = buf.method_10816();
        Map<String, StoredOrigin> o = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            String key = buf.method_19772();
            o.put(key, new StoredOrigin(buf.method_10810(), buf.method_10810()));
        }
        int m = buf.method_10816();
        Map<String, String> v = new HashMap<>(m);
        for (int i = 0; i < m; i++) {
            v.put(buf.method_19772(), buf.method_19772());
        }
        return new StoredData(o, v);
    }

    public record StoredOrigin(class_2960 layer, class_2960 origin) {
        public static final Codec<StoredOrigin> CODEC = RecordCodecBuilder.create(i -> i.group(
            class_2960.field_25139.fieldOf("layer").forGetter(StoredOrigin::layer),
            class_2960.field_25139.fieldOf("origin").forGetter(StoredOrigin::origin)
        ).apply(i, StoredOrigin::new));
    }
}
