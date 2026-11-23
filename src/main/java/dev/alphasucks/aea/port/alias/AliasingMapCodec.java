package dev.alphasucks.aea.port.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class AliasingMapCodec<A> extends MapCodec<A> {
   private final MapCodec<A> delegate;
   private final Map<String, String> oldToNew;
   private final Map<String, String> newToOld;

   private AliasingMapCodec(MapCodec<A> delegate, Map<String, String> oldToNew) {
      this.delegate = delegate;
      this.oldToNew = Map.copyOf(oldToNew);
      Map<String, String> inverse = new HashMap(oldToNew.size());
      oldToNew.forEach((oldName, newName) -> inverse.put(newName, oldName));
      this.newToOld = Map.copyOf(inverse);
   }

   public static <A> MapCodec<A> wrap(MapCodec<A> delegate, Map<String, String> oldToNew) {
      return oldToNew != null && !oldToNew.isEmpty() ? new AliasingMapCodec<>(delegate, oldToNew) : delegate;
   }

   public <T> Stream<T> keys(DynamicOps<T> ops) {
      return this.delegate.keys(ops);
   }

   public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
      return this.delegate.decode(ops, new AliasingMapCodec.AliasedMapLike(ops, input, this.newToOld));
   }

   public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      return this.delegate.encode(input, ops, prefix);
   }

   private static final class AliasedMapLike<T> implements MapLike<T> {
      private final DynamicOps<T> ops;
      private final MapLike<T> wrapped;
      private final Map<String, String> newToOld;

      AliasedMapLike(DynamicOps<T> ops, MapLike<T> wrapped, Map<String, String> newToOld) {
         this.ops = ops;
         this.wrapped = wrapped;
         this.newToOld = newToOld;
      }

      public T get(T key) {
         T direct = (T)this.wrapped.get(key);
         return (T)(direct != null ? direct : this.ops.getStringValue(key).result().map(this::getByName).orElse(null));
      }

      public T get(String key) {
         T direct = (T)this.wrapped.get(key);
         return direct != null ? direct : this.getByName(key);
      }

      private T getByName(String newName) {
         String oldName = (String)this.newToOld.get(newName);
         return (T)(oldName != null ? this.wrapped.get(oldName) : null);
      }

      public Stream<Pair<T, T>> entries() {
         return this.wrapped.entries();
      }
   }
}
