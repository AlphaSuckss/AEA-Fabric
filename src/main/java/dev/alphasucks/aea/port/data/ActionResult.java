package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_1269;
import net.minecraft.class_3542;

public enum ActionResult implements class_3542 {
   SUCCESS("success", class_1269.field_5812),
   CONSUME("consume", class_1269.field_21466),
   CONSUME_PARTIAL("consume_partial", class_1269.field_33562),
   PASS("pass", class_1269.field_5811),
   FAIL("fail", class_1269.field_5814);

   public static final Codec<ActionResult> CODEC = class_3542.method_28140(ActionResult::values);
   private final String name;
   private final class_1269 vanilla;

   ActionResult(String name, class_1269 vanilla) {
      this.name = name;
      this.vanilla = vanilla;
   }

   public class_1269 vanilla() {
      return this.vanilla;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
