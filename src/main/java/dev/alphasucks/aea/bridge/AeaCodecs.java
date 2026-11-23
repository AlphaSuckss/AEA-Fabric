package dev.alphasucks.aea.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.class_2960;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Glue between DFU codecs (Overgrown's format) and Calio data types (Apoli 2.9.2). */
public final class AeaCodecs {
    private AeaCodecs() {
    }

    /** Decodes a whole JSON object with a map codec, throwing a readable error for the power loader. */
    public static <C> C decodeMap(MapCodec<C> codec, JsonObject json, class_2960 typeId) {
        DataResult<MapLike<JsonElement>> map = JsonOps.INSTANCE.getMap(json);
        DataResult<C> result = map.flatMap(m -> codec.decode(JsonOps.INSTANCE, m));
        if (result.error().isPresent()) {
            throw new JsonSyntaxException("Invalid '" + typeId + "': " + result.error().get().message());
        }
        return result.result().orElseThrow(() -> new JsonSyntaxException("Invalid '" + typeId + "'"));
    }

    /**
     * A codec that hands the raw JSON to a Calio data type (e.g. {@code ApoliDataTypes.ENTITY_ACTION})
     * and keeps the JSON so the value can be encoded again.
     */
    public static <T, W> Codec<W> calio(SerializableDataType<T> type, BiFunction<T, JsonElement, W> wrap, Function<W, JsonElement> source) {
        return Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
            JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
            try {
                T value = type.read(json);
                if (value == null) {
                    return DataResult.error(() -> "Could not read " + json);
                }
                return DataResult.success(wrap.apply(value, json));
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                return DataResult.error(() -> msg);
            }
        }, w -> new Dynamic<>(JsonOps.INSTANCE, source.apply(w)));
    }

    /** Converts any dynamic value to JSON. */
    public static <T> JsonElement toJson(DynamicOps<T> ops, T value) {
        return ops.convertTo(JsonOps.INSTANCE, value);
    }

    public static <A> Pair<A, JsonElement> pair(A a, JsonElement b) {
        return Pair.of(a, b);
    }
}
