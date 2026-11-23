package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import net.minecraft.class_3542;

public enum PlayerModelType implements class_3542 {
   WIDE("wide"),
   SLIM("slim");

   public static final Codec<PlayerModelType> CODEC = Codec.STRING.comapFlatMap(PlayerModelType::byName, PlayerModelType::method_15434);
   private final String name;

   PlayerModelType(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public static DataResult<PlayerModelType> byName(String raw) {
      return switch (raw.toLowerCase(Locale.ROOT)) {
         case "wide", "default", "classic", "steve" -> DataResult.success(WIDE);
         case "slim", "alex" -> DataResult.success(SLIM);
         default -> DataResult.error(() -> "Unknown player model type '" + raw + "' (expected wide or slim)");
      };
   }
}
