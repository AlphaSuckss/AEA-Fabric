package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.class_2487;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_2522;

public record Nbt(class_2487 tag) {
   private static final Codec<Nbt> OBJECT_CODEC = Codec.PASSTHROUGH
      .flatXmap(
         dyn -> {
            class_2520 t = (class_2520)dyn.convert(class_2509.field_11560).getValue();
            return t instanceof class_2487 c
               ? DataResult.success(new Nbt(c))
               : DataResult.error(() -> "Expected NBT compound, got " + t.method_23258().method_23259());
         },
         (Nbt nbt) -> DataResult.success(new Dynamic<>(class_2509.field_11560, nbt.tag()))
      );
   private static final Codec<Nbt> STRING_CODEC = Codec.STRING.comapFlatMap(s -> {
      try {
         return DataResult.success(new Nbt(class_2522.method_10718(s)));
      } catch (Exception e) {
         return DataResult.error(() -> "Invalid SNBT: " + e.getMessage());
      }
   }, nbt -> nbt.tag().toString());
   public static final Codec<Nbt> CODEC = new Codec<Nbt>() {
      public <T> DataResult<Pair<Nbt, T>> decode(DynamicOps<T> ops, T input) {
         return ops.getStringValue(input).result().isPresent() ? Nbt.STRING_CODEC.decode(ops, input) : Nbt.OBJECT_CODEC.decode(ops, input);
      }

      public <T> DataResult<T> encode(Nbt input, DynamicOps<T> ops, T prefix) {
         return Nbt.OBJECT_CODEC.encode(input, ops, prefix);
      }
   };
}
