package dev.alphasucks.aea.port.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.function.Supplier;

public final class LazyCodec<A> implements Codec<A> {
   private final Supplier<Codec<A>> supplier;
   private volatile Codec<A> delegate;

   public LazyCodec(Supplier<Codec<A>> supplier) {
      this.supplier = supplier;
   }

   private Codec<A> get() {
      Codec<A> c = this.delegate;
      if (c == null) {
         c = (Codec<A>)this.supplier.get();
         this.delegate = c;
      }

      return c;
   }

   public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
      return this.get().decode(ops, input);
   }

   public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
      return this.get().encode(input, ops, prefix);
   }
}
