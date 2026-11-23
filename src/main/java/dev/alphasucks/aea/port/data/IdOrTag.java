package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.codec.IdCodecs;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import org.jetbrains.annotations.Nullable;

public final class IdOrTag<T> {
   private final class_2960 id;
   @Nullable
   private final class_6862<T> tag;

   private IdOrTag(class_2960 id, @Nullable class_6862<T> tag) {
      this.id = id;
      this.tag = tag;
   }

   public static <T> IdOrTag<T> id(class_2960 id) {
      return new IdOrTag<>(id, null);
   }

   public static <T> IdOrTag<T> tag(class_5321<? extends class_2378<T>> registry, class_2960 id) {
      return new IdOrTag<>(id, class_6862.method_40092(registry, id));
   }

   public static <T> Codec<IdOrTag<T>> codec(class_5321<? extends class_2378<T>> registry) {
      return Codec.STRING.comapFlatMap(raw -> read(raw, registry), IdOrTag::asString).stable();
   }

   private static <T> DataResult<IdOrTag<T>> read(String raw, class_5321<? extends class_2378<T>> registry) {
      boolean tagged = raw.startsWith("#");
      DataResult<class_2960> parsed = tagged ? IdCodecs.readTag(raw) : IdCodecs.readId(raw);
      return parsed.map(id -> new IdOrTag<>(id, tagged ? class_6862.method_40092(registry, id) : null));
   }

   public boolean matches(class_6880<T> holder) {
      return this.tag != null ? holder.method_40220(this.tag) : holder.method_40226(this.id);
   }

   public boolean isTag() {
      return this.tag != null;
   }

   public class_2960 id() {
      return this.id;
   }

   @Nullable
   public class_6862<T> tag() {
      return this.tag;
   }

   public String asString() {
      return this.tag != null ? "#" + this.id : this.id.toString();
   }

   public String toString() {
      return this.asString();
   }

   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof IdOrTag<?> o) ? false : this.id.equals(o.id) && this.tag == null == (o.tag == null);
      }
   }

   public int hashCode() {
      return this.id.hashCode() * 31 + (this.tag == null ? 0 : 1);
   }
}
