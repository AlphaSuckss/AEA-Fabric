package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;

public record Vector(float x, float y, float z) {
   public static final Vector ZERO = new Vector(0.0F, 0.0F, 0.0F);
   private static final Codec<Vector> LIST_CODEC = Codec.FLOAT
      .listOf()
      .comapFlatMap(
         list -> list.size() == 3
            ? DataResult.success(new Vector((Float)list.get(0), (Float)list.get(1), (Float)list.get(2)))
            : DataResult.error(() -> "Vector list must have exactly 3 elements, got " + list.size()),
         v -> List.of(v.x, v.y, v.z)
      );
   private static final Codec<Vector> OBJECT_CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.FLOAT.optionalFieldOf("x", 0.0F).forGetter(Vector::x),
            Codec.FLOAT.optionalFieldOf("y", 0.0F).forGetter(Vector::y),
            Codec.FLOAT.optionalFieldOf("z", 0.0F).forGetter(Vector::z)
         )
         .apply(i, Vector::new)
   );
   public static final Codec<Vector> CODEC = Codec.either(OBJECT_CODEC, LIST_CODEC)
      .xmap(either -> (Vector)either.map(Function.identity(), Function.identity()), v -> Either.left(v));
   public static final Codec<Vector> SCALAR_OR_VECTOR = Codec.either(Codec.FLOAT, CODEC)
      .xmap(either -> (Vector)either.map(n -> new Vector(n, n, n), Function.identity()), v -> v.x == v.y && v.y == v.z ? Either.left(v.x) : Either.right(v));

   public static Vector uniform(float value) {
      return new Vector(value, value, value);
   }

   public Vector add(Vector o) {
      return new Vector(this.x + o.x, this.y + o.y, this.z + o.z);
   }

   public Vector scale(float k) {
      return new Vector(this.x * k, this.y * k, this.z * k);
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public boolean isZero() {
      return this.x == 0.0F && this.y == 0.0F && this.z == 0.0F;
   }
}
