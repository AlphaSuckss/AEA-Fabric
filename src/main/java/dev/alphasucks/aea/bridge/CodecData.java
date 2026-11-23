package dev.alphasucks.aea.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

import java.util.function.Supplier;

/**
 * A {@link SerializableData} that parses its whole JSON object with a DFU {@link MapCodec}
 * (the format Overgrown's Apoli uses) instead of Calio's per-field reading.
 * <p>
 * The decoded config is stored under {@link #CFG}; the source JSON under {@link #JSON} so the
 * object can be re-sent to clients (Apoli 2.9.2 syncs power and action data over the network
 * by re-serialising the data instance).
 */
public class CodecData<C> extends SerializableData {
    public static final String CFG = "aea$cfg";
    public static final String JSON = "aea$json";
    /** namespace/path of the power file this was read in (what `*` stands for); null outside power files */
    public static final String NS = "aea$ns";
    public static final String PATH = "aea$path";
    /** generous limit: power JSON can be large (multiple sub powers, long action trees) */
    private static final int MAX_JSON = 1 << 21;

    private final class_2960 typeId;
    private final Supplier<MapCodec<C>> codec;
    private boolean readsCondition;
    private java.util.function.UnaryOperator<JsonObject> preprocessor = j -> j;

    public CodecData(class_2960 typeId, Supplier<MapCodec<C>> codec) {
        this.typeId = typeId;
        this.codec = codec;
    }

    @Override
    public <T> SerializableData add(String name, io.github.apace100.calio.data.SerializableDataType<T> type, T defaultValue) {
        if ("condition".equals(name)) {
            readsCondition = true;
        }
        return super.add(name, type, defaultValue);
    }

    @Override
    public Instance read(JsonObject json) {
        String ns = SerializableData.CURRENT_NAMESPACE;
        String path = SerializableData.CURRENT_PATH;
        Instance instance = new Instance();
        instance.set(JSON, json);
        instance.set(NS, ns);
        instance.set(PATH, path);
        instance.set("inverted", json.has("inverted") && json.get("inverted").isJsonPrimitive() && json.get("inverted").getAsBoolean());
        if (readsCondition) {
            // AEA: always set it - Calio's isPresent() on an unset field with a null default throws
            // "Tried to get field "condition" from data, which did not exist." (powers without a condition failed)
            instance.set("condition", json.has("condition") ? ApoliDataTypes.ENTITY_CONDITION.read(json.get("condition")) : null);
        }
        JsonObject source = preprocessor.apply(json);
        if (ns != null) {
            // AEA: `*` / `*:*` stand for the power file's namespace / id (Overgrown resolves them before decoding;
            // AEA's codecs used to reject them outright)
            JsonElement expanded = dev.alphasucks.aea.port.loader.IdWildcards.apply(source, new class_2960(ns, path != null ? path : ""));
            if (expanded != null && expanded.isJsonObject()) {
                source = expanded.getAsJsonObject();
            }
        }
        instance.set(CFG, AeaCodecs.decodeMap(codec.get(), source, typeId));
        return instance;
    }

    /**
     * Client side of the power/action sync. The JSON is re-parsed here, so the power file's namespace/path
     * travel with it: without them a `*` in an id (e.g. {@code "resource": "*:*_charges"}) only resolves on
     * the server, and the client threw "Identifier may not contain a `*` in the namespace when read here."
     * <p>
     * This never throws: Apoli reads the rest of the power (name, description, ...) after this data, and an
     * exception here left those bytes unread, so every following power in the packet was garbage and the
     * player was disconnected. A config that can't be read becomes an inert one (CFG = null) instead.
     */
    @Override
    public Instance read(class_2540 buffer) {
        String ns = buffer.readBoolean() ? buffer.method_19772() : null;
        String path = buffer.readBoolean() ? buffer.method_19772() : null;
        String raw = buffer.method_10800(MAX_JSON);
        String prevNs = SerializableData.CURRENT_NAMESPACE;
        String prevPath = SerializableData.CURRENT_PATH;
        SerializableData.CURRENT_NAMESPACE = ns;
        SerializableData.CURRENT_PATH = path;
        try {
            return read(JsonParser.parseString(raw).getAsJsonObject());
        } catch (RuntimeException e) {
            dev.alphasucks.aea.AEA.LOGGER.error("[AEA] Couldn't read synced {} data{}: {}", typeId,
                ns != null ? " in " + ns + ":" + (path != null ? path : "") : "", e.getMessage());
            Instance broken = new Instance();
            JsonObject json;
            try {
                json = JsonParser.parseString(raw).getAsJsonObject();
            } catch (RuntimeException ignored) {
                json = new JsonObject();
            }
            broken.set(JSON, json);
            broken.set(NS, ns);
            broken.set(PATH, path);
            broken.set("inverted", false);
            if (readsCondition) {
                broken.set("condition", null);
            }
            broken.set(CFG, null);
            return broken;
        } finally {
            SerializableData.CURRENT_NAMESPACE = prevNs;
            SerializableData.CURRENT_PATH = prevPath;
        }
    }

    @Override
    public void write(class_2540 buffer, Instance instance) {
        String ns = opt(instance, NS);
        String path = opt(instance, PATH);
        buffer.writeBoolean(ns != null);
        if (ns != null) {
            buffer.method_10814(ns);
        }
        buffer.writeBoolean(path != null);
        if (path != null) {
            buffer.method_10814(path);
        }
        JsonElement json = opt(instance, JSON);
        buffer.method_10788(json == null ? "{}" : json.toString(), MAX_JSON);
    }

    /** Calio's get() throws for keys that were never set (instances built outside read()). */
    @org.jetbrains.annotations.Nullable
    private static <T> T opt(Instance instance, String key) {
        try {
            return instance.get(key);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public SerializableData copy() {
        CodecData<C> copy = new CodecData<>(typeId, codec);
        copy.preprocessor = preprocessor;
        if (readsCondition) {
            copy.add("condition", ApoliDataTypes.ENTITY_CONDITION, null);
        }
        return copy;
    }

    public void setPreprocessor(java.util.function.UnaryOperator<JsonObject> preprocessor) {
        this.preprocessor = preprocessor;
    }

    public class_2960 typeId() {
        return typeId;
    }
}
