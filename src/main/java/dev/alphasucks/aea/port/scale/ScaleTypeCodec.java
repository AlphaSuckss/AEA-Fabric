package dev.alphasucks.aea.port.scale;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import java.util.List;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ScaleTypeCodec {
   public static final Codec<ScaleType> CODEC = IdCodecs.ID.comapFlatMap(id -> {
      ScaleType type = resolve(id);
      return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown scale type: " + id);
   }, ScaleType::id);
   public static final Codec<List<ScaleType>> LIST_OR_SINGLE = Codec.either(CODEC, CODEC.listOf())
      .xmap(either -> (List)either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left((ScaleType)list.get(0)) : Either.right(list));

   private ScaleTypeCodec() {
   }

   @Nullable
   public static ScaleType resolve(class_2960 id) {
      ScaleType type = ScaleTypes.get(id);
      if (type != null) {
         return type;
      }

      String namespace = id.method_12836();
      return !namespace.equals("minecraft") && !namespace.equals("pehkui") && !namespace.equals("origins") ? null : ScaleTypes.get(Apoli.id(id.method_12832()));
   }
}
