package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public record IconData(class_1799 stack, Optional<class_2960> texture, int width, int height) {
   public static final IconData EMPTY = new IconData(class_1799.field_8037, Optional.empty(), 0, 0);
   private static final Codec<IconData> TEXTURE_CODEC = RecordCodecBuilder.create(
      i -> i.group(
            IdCodecs.ID.fieldOf("texture").forGetter(icon -> (class_2960)icon.texture.orElseThrow()),
            Codec.INT.optionalFieldOf("width", 0).forGetter(IconData::width),
            Codec.INT.optionalFieldOf("height", 0).forGetter(IconData::height)
         )
         .apply(i, IconData::ofTexture)
   );
   private static final Codec<IconData> ITEM_ID_CODEC = IdCodecs.ID
      .xmap(rl -> ofItem(new class_1799(class_7923.field_41178.method_10223(rl))), icon -> class_7923.field_41178.method_10221(icon.stack.method_7909()));
   private static final Codec<IconData> ITEM_STACK_CODEC = ItemStackData.CODEC.xmap(data -> ofItem(data.stack()), icon -> new ItemStackData(icon.stack));
   public static final Codec<IconData> CODEC = Codec.either(TEXTURE_CODEC, Codec.either(ITEM_ID_CODEC, ITEM_STACK_CODEC))
      .xmap(
         either -> (IconData)either.map(Function.identity(), inner -> (IconData)inner.map(Function.identity(), Function.identity())),
         icon -> icon.isTexture() ? Either.left(icon) : Either.right(Either.right(icon))
      );

   public IconData {
      stack = stack.method_7972();
   }

   public static IconData ofItem(class_1799 stack) {
      return new IconData(stack, Optional.empty(), 0, 0);
   }

   public static IconData ofItem(class_1792 item) {
      return new IconData(new class_1799(item), Optional.empty(), 0, 0);
   }

   public static IconData ofTexture(class_2960 texture, int width, int height) {
      return new IconData(class_1799.field_8037, Optional.of(texture), width, height);
   }

   public boolean isTexture() {
      return this.texture.isPresent();
   }

   public boolean isEmpty() {
      return this.texture.isEmpty() && this.stack.method_7960();
   }

   public static IconData grassBlock() {
      return ofItem(class_1802.field_8270);
   }

   public void write(class_2540 buf) {
      buf.method_10793(this.stack);
      buf.method_37435(this.texture, (b, id) -> b.method_10812(id));
      buf.method_10804(this.width);
      buf.method_10804(this.height);
   }

   public static IconData read(class_2540 buf) {
      class_1799 stack = buf.method_10819();
      Optional<class_2960> texture = buf.method_37436(b -> b.method_10810());
      int width = buf.method_10816();
      int height = buf.method_10816();
      return new IconData(stack, texture, width, height);
   }
}
