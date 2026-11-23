package dev.alphasucks.aea.port.keybind;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record Keybind(class_2960 id, String key, String category, Optional<class_2561> name) {
   public static final String DEFAULT_CATEGORY = "key.categories.apoli";
   public static final Codec<Keybind> CODEC_NO_ID = RecordCodecBuilder.create(
      i -> i.group(
            Codec.STRING.fieldOf("key").forGetter(Keybind::key),
            Codec.STRING.optionalFieldOf("category", "key.categories.apoli").forGetter(Keybind::category),
            TextComponent.CODEC.optionalFieldOf("name").forGetter(Keybind::name)
         )
         .apply(i, (k, c, n) -> new Keybind(null, k, c, n))
   );

   public Keybind withId(class_2960 id) {
      return new Keybind(id, this.key, this.category, this.name);
   }

   public String translationKey() {
      return "key." + this.id.method_12836() + "." + this.id.method_12832().replace('/', '.');
   }

   public static String encodeName(class_2561 component) {
      String plain = component.getString();
      return class_2561.method_43470(plain).equals(component)
         ? plain
         : (String)TextComponent.CODEC.encodeStart(JsonOps.INSTANCE, component).result().map(JsonElement::toString).orElse(plain);
   }

   public static class_2561 decodeName(String encoded) {
      if (encoded.isEmpty()) {
         return class_2561.method_43473();
      }

      char first = encoded.charAt(0);
      if (first == '{' || first == '[' || first == '"') {
         try {
            Optional<class_2561> parsed = TextComponent.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(encoded)).result();
            if (parsed.isPresent()) {
               return (class_2561)parsed.get();
            }
         } catch (RuntimeException var3) {
         }
      }

      return class_2561.method_43470(encoded);
   }
}
