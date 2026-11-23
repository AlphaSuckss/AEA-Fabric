package dev.alphasucks.aea.port.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Map;
import java.util.stream.Stream;

public final class DefaultInjectingMapCodec<A> extends MapCodec<A> {
   private final MapCodec<A> delegate;
   private final Map<String, String> defaults;

   private DefaultInjectingMapCodec(MapCodec<A> delegate, Map<String, String> defaults) {
      this.delegate = delegate;
      this.defaults = Map.copyOf(defaults);
   }

   public static <A> MapCodec<A> wrap(MapCodec<A> delegate, Map<String, String> defaults) {
      return defaults != null && !defaults.isEmpty() ? new DefaultInjectingMapCodec<>(delegate, defaults) : delegate;
   }

   public <T> Stream<T> keys(DynamicOps<T> ops) {
      return this.delegate.keys(ops);
   }

   public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
      return this.delegate.decode(ops, new DefaultInjectingMapCodec.DefaultingMapLike(ops, input, this.defaults));
   }

   public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      return this.delegate.encode(input, ops, prefix);
   }

   private record DefaultingMapLike<T>(DynamicOps<T> ops, MapLike<T> wrapped, Map<String, String> defaults) implements MapLike<T> {
      public T get(T key) {
         T direct = (T)this.wrapped.get(key);
         return (T)(direct != null ? direct : this.ops.getStringValue(key).result().map(this::byName).orElse(null));
      }

      public T get(String key) {
         T direct = (T)this.wrapped.get(key);
         return direct != null ? direct : this.byName(key);
      }

      private T byName(String key) {
         String def = (String)this.defaults.get(key);
         return (T)(def != null ? this.ops.createString(def) : null);
      }

      public Stream<Pair<T, T>> entries() {
         return this.wrapped.entries();
      }
   }
}
