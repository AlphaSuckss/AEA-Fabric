package dev.alphasucks.aea.port.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import dev.alphasucks.aea.port.Apoli;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_2960;

public final class LoggedOptionalField<A> extends MapCodec<Optional<A>> {
   private static final ThreadLocal<String> CONTEXT = new ThreadLocal();
   private final String name;
   private final Codec<A> elementCodec;
   private final boolean strict;

   private LoggedOptionalField(String name, Codec<A> elementCodec, boolean strict) {
      this.name = name;
      this.elementCodec = elementCodec;
      this.strict = strict;
   }

   public static <A> MapCodec<Optional<A>> of(String name, Codec<A> elementCodec) {
      return new LoggedOptionalField<>(name, elementCodec, false);
   }

   public static <A> MapCodec<A> of(String name, Codec<A> elementCodec, A fallback) {
      return new LoggedOptionalField<>(name, elementCodec, false).xmap(o -> o.orElse(fallback), Optional::ofNullable);
   }

   public static <A> MapCodec<Optional<A>> strict(String name, Codec<A> elementCodec) {
      return new LoggedOptionalField<>(name, elementCodec, true);
   }

   public static void setContext(class_2960 id) {
      CONTEXT.set(id == null ? null : id.toString());
   }

   public static void clearContext() {
      CONTEXT.remove();
   }

   public static String context() {
      return (String)CONTEXT.get();
   }

   public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of(ops.createString(this.name));
   }

   public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
      T value = (T)input.get(this.name);
      if (value == null) {
         return DataResult.success(Optional.empty());
      } else {
         DataResult<A> parsed = this.elementCodec.parse(ops, value);
         Optional<A> result = parsed.result();
         if (result.isPresent()) {
            return DataResult.success(result);
         } else {
            String where = (String)CONTEXT.get();
            if (this.strict) {
               String reason = (String)parsed.error().map(e -> e.message()).orElse("unknown error");
               return DataResult.error(
                  () -> "The '" + this.name + "' field" + (where == null ? "" : " of " + where) + " is present but failed to parse: " + reason
               );
            } else {
               Apoli.LOGGER
                  .warn(
                     "[Apoli] Ignoring the '{}' field{} — it is present but failed to parse, so it was dropped and everything else loaded. {}",
                     new Object[]{this.name, where == null ? "" : " of " + where, parsed.error().map(e -> e.message()).orElse("unknown error")}
                  );
               return DataResult.success(Optional.empty());
            }
         }
      }
   }

   public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      return input.isPresent() ? prefix.add(this.name, this.elementCodec.encodeStart(ops, input.get())) : prefix;
   }
}
