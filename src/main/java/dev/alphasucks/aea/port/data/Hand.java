package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_3542;

public enum Hand implements class_3542 {
   MAIN_HAND("main_hand", class_1268.field_5808),
   OFF_HAND("off_hand", class_1268.field_5810);

   public static final Codec<Hand> CODEC = Codec.STRING.comapFlatMap(Hand::byName, Hand::method_15434);
   public static final Codec<List<Hand>> LIST_CODEC = Codec.list(CODEC);
   public static final List<Hand> BOTH = List.of(OFF_HAND, MAIN_HAND);
   private final String name;
   private final class_1268 vanilla;

   Hand(String name, class_1268 vanilla) {
      this.name = name;
      this.vanilla = vanilla;
   }

   public class_1268 vanilla() {
      return this.vanilla;
   }

   public static Hand of(class_1268 vanilla) {
      return vanilla == class_1268.field_5808 ? MAIN_HAND : OFF_HAND;
   }

   public static DataResult<Hand> byName(String name) {
      return switch (name) {
         case "main_hand", "mainhand", "MAIN_HAND", "MAINHAND" -> DataResult.success(MAIN_HAND);
         case "off_hand", "offhand", "OFF_HAND", "OFFHAND" -> DataResult.success(OFF_HAND);
         default -> DataResult.error(() -> "Unknown hand: '" + name + "' (expected main_hand/mainhand or off_hand/offhand, upper case accepted)");
      };
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
