package dev.alphasucks.aea.port.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.ColorCodecs;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4081;

public record CustomEffect(
   class_2960 id, List<class_2960> powers, class_243 color, Optional<class_2960> icon, int loadingPriority, class_4081 mobEffectCategory, Optional<String> name
) {
   public static final Codec<class_4081> CATEGORY_CODEC = Codec.STRING.comapFlatMap(name -> {
      return switch (name) {
         case "beneficial", "BENEFICIAL" -> DataResult.success(class_4081.field_18271);
         case "harmful", "HARMFUL" -> DataResult.success(class_4081.field_18272);
         case "neutral", "NEUTRAL" -> DataResult.success(class_4081.field_18273);
         default -> DataResult.error(() -> "Unknown effect type '" + name + "' (expected beneficial, harmful or neutral, upper case accepted)");
      };
   }, category -> category.name().toLowerCase(Locale.ROOT));

   public static Codec<CustomEffect> codec(class_2960 id) {
      return RecordCodecBuilder.create(
         instance -> instance.group(
               IdCodecs.ID.listOf().fieldOf("powers").forGetter(CustomEffect::powers),
               Codec.DOUBLE.optionalFieldOf("r", 0.0).forGetter(effect -> effect.color.field_1352),
               Codec.DOUBLE.optionalFieldOf("g", 0.0).forGetter(effect -> effect.color.field_1351),
               Codec.DOUBLE.optionalFieldOf("b", 0.0).forGetter(effect -> effect.color.field_1350),
               IdCodecs.ID.optionalFieldOf("icon").forGetter(CustomEffect::icon),
               Codec.INT.optionalFieldOf("loading_priority", 0).forGetter(CustomEffect::loadingPriority),
               CATEGORY_CODEC.fieldOf("type").forGetter(CustomEffect::mobEffectCategory),
               Codec.STRING.optionalFieldOf("name").forGetter(CustomEffect::name)
            )
            .apply(
               instance,
               (powers, r, g, b, icon, priority, category, name) -> new CustomEffect(id, powers, new class_243(r, g, b), icon, priority, category, name)
            )
      );
   }

   public int colorInt() {
      return ColorCodecs.pack((float)this.color.field_1352, (float)this.color.field_1351, (float)this.color.field_1350, 0.0F);
   }
}
