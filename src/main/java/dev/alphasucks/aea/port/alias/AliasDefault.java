package dev.alphasucks.aea.port.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public record AliasDefault<V>(String field, Codec<V> codec, V value) {
   public static <V> AliasDefault<V> of(String field, Codec<V> codec, V value) {
      return new AliasDefault<>(field, codec, value);
   }

   public <T> DataResult<T> encode(DynamicOps<T> ops) {
      return this.codec.encodeStart(ops, this.value);
   }
}
