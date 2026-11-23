package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_7923;

public record ItemStackData(class_1799 stack) {
   private static final int HIDE_UNBREAKABLE = 4;
   public static final MapCodec<ItemStackData> MAP_CODEC = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         i -> i.group(
               class_7923.field_41178.method_39673().fieldOf("item").forGetter(s -> s.stack.method_7909()),
               Codec.INT.optionalFieldOf("amount", 1).forGetter(s -> s.stack.method_7947()),
               LoggedOptionalField.of("tag", Nbt.CODEC).forGetter(s -> s.stack.method_7985() ? Optional.of(new Nbt(s.stack.method_7969())) : Optional.empty()),
               TextComponent.CODEC.optionalFieldOf("name").forGetter(s -> Optional.empty()),
               Codec.BOOL.optionalFieldOf("unbreakable", false).forGetter(s -> false)
            )
            .apply(i, ItemStackData::build)
      ),
      Map.of("id", "item", "count", "amount")
   );
   public static final Codec<ItemStackData> CODEC = MAP_CODEC.codec();

   public static boolean hasData(class_1799 template) {
      return template.method_7985();
   }

   public static boolean sameData(class_1799 a, class_1799 b) {
      return class_1799.method_31577(a, b);
   }

   private static ItemStackData build(class_1792 item, Integer amount, Optional<Nbt> tag, Optional<class_2561> name, Boolean unbreakable) {
      class_1799 stack = new class_1799(item, amount);
      tag.ifPresent(n -> stack.method_7980(n.tag()));
      if (Boolean.TRUE.equals(unbreakable)) {
         class_2487 root = stack.method_7948();
         root.method_10556("Unbreakable", true);
         root.method_10569("HideFlags", root.method_10550("HideFlags") | 4);
      }

      name.ifPresent(component -> stack.method_7977(ItemNames.upright(component)));
      return new ItemStackData(stack);
   }
}
