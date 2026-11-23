package dev.alphasucks.aea.port.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class DispatchedTypeCodec {
   private DispatchedTypeCodec() {
   }

   public static <W> Codec<W> create(
      String groupName,
      DispatchedTypeCodec.CodecLookup lookup,
      BiFunction<class_2960, Object, W> ctor,
      Function<W, class_2960> typeIdGetter,
      Function<W, Object> configGetter
   ) {
      return class_2960.field_25139.partialDispatch("type", w -> DataResult.success((class_2960)typeIdGetter.apply(w)), id -> {
         DispatchedTypeCodec.CodecLookup.Resolution resolution = lookup.lookup(id);
         if (resolution == null) {
            return DataResult.error(() -> "Unknown " + groupName + " type: " + id);
         }

         class_2960 canonical = resolution.canonical();
         MapCodec<Object> objCodec = (MapCodec<Object>)resolution.codec();
         Codec<W> wrapped = objCodec.xmap(cfg -> ctor.apply(canonical, cfg), w -> configGetter.apply(w)).codec();
         return DataResult.success(wrapped);
      });
   }

   public static <W> Codec<W> createInvertible(
      String groupName,
      DispatchedTypeCodec.CodecLookup lookup,
      DispatchedTypeCodec.InvertibleCtor<W> ctor,
      Function<W, class_2960> typeIdGetter,
      Function<W, Object> configGetter,
      Function<W, Boolean> invertedGetter
   ) {
      return class_2960.field_25139.partialDispatch("type", w -> DataResult.success((class_2960)typeIdGetter.apply(w)), id -> {
         DispatchedTypeCodec.CodecLookup.Resolution resolution = lookup.lookup(id);
         if (resolution == null) {
            return DataResult.error(() -> "Unknown " + groupName + " type: " + id);
         }

         class_2960 canonical = resolution.canonical();
         MapCodec<Object> objCodec = (MapCodec<Object>)resolution.codec();
         MapCodec<W> wrappedMap = new DispatchedTypeCodec.InvertibleMapCodec<>(canonical, objCodec, ctor, configGetter, invertedGetter);
         return DataResult.success(wrappedMap.codec());
      });
   }

   @FunctionalInterface
   public interface CodecLookup {
      @Nullable
      DispatchedTypeCodec.CodecLookup.Resolution lookup(class_2960 var1);

      record Resolution(class_2960 canonical, MapCodec<?> codec) {
      }
   }

   @FunctionalInterface
   public interface InvertibleCtor<W> {
      W apply(class_2960 var1, Object var2, boolean var3);
   }

   private static final class InvertibleMapCodec<W> extends MapCodec<W> {
      private static final MapCodec<Boolean> INVERTED_FIELD = Codec.BOOL.optionalFieldOf("inverted", false);
      private final class_2960 canonical;
      private final MapCodec<Object> configCodec;
      private final DispatchedTypeCodec.InvertibleCtor<W> ctor;
      private final Function<W, Object> configGetter;
      private final Function<W, Boolean> invertedGetter;

      InvertibleMapCodec(
         class_2960 canonical,
         MapCodec<Object> configCodec,
         DispatchedTypeCodec.InvertibleCtor<W> ctor,
         Function<W, Object> configGetter,
         Function<W, Boolean> invertedGetter
      ) {
         this.canonical = canonical;
         this.configCodec = configCodec;
         this.ctor = ctor;
         this.configGetter = configGetter;
         this.invertedGetter = invertedGetter;
      }

      public <T> Stream<T> keys(DynamicOps<T> ops) {
         return Stream.concat(INVERTED_FIELD.keys(ops), this.configCodec.keys(ops));
      }

      public <T> DataResult<W> decode(DynamicOps<T> ops, MapLike<T> input) {
         DataResult<Boolean> invertedResult = INVERTED_FIELD.decode(ops, input);
         DataResult<Object> configResult = this.configCodec.decode(ops, input);
         return invertedResult.flatMap(inverted -> configResult.map(cfg -> this.ctor.apply(this.canonical, cfg, inverted)));
      }

      public <T> RecordBuilder<T> encode(W input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
         prefix = INVERTED_FIELD.encode((Boolean)this.invertedGetter.apply(input), ops, prefix);
         return this.configCodec.encode(this.configGetter.apply(input), ops, prefix);
      }
   }
}
