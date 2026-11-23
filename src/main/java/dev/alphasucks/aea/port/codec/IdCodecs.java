package dev.alphasucks.aea.port.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_6862;

public final class IdCodecs {
   public static final Codec<class_2960> ID = Codec.STRING.comapFlatMap(IdCodecs::readId, class_2960::toString).stable();
   public static final Codec<class_2960> TAG = Codec.STRING.comapFlatMap(IdCodecs::readTag, id -> "#" + id).stable();

   private IdCodecs() {
   }

   public static <T> Codec<class_6862<T>> tagKey(class_5321<? extends class_2378<T>> registry) {
      return Codec.STRING.comapFlatMap(raw -> readTag(raw).map(id -> class_6862.method_40092(registry, id)), tag -> "#" + tag.comp_327()).stable();
   }

   public static DataResult<class_2960> readId(String raw) {
      return raw.startsWith("#")
         ? DataResult.error(() -> "'" + raw + "' is a tag, and this field takes a single id. Drop the '#', or use the matching in_tag condition.")
         : parse(raw);
   }

   public static DataResult<class_2960> readTag(String raw) {
      return parse(raw.startsWith("#") ? raw.substring(1) : raw);
   }

   private static DataResult<class_2960> parse(String raw) {
      if (raw.indexOf(42) >= 0) {
         return DataResult.error(
            () -> "'"
               + raw
               + "' still contains a '*' wildcard. '*' stands for the namespace of the file it is written in, and '*:*' for that file's full id, so it only resolves inside a power, origin, layer, global power set or skill tree file."
         );
      }

      class_2960 parsed = class_2960.method_12829(raw);
      return parsed == null
         ? DataResult.error(
            () -> "'" + raw + "' is not a valid id. Ids look like 'namespace:path', where the namespace is [a-z0-9_.-] and the path may also contain '/'."
         )
         : DataResult.success(parsed);
   }
}
