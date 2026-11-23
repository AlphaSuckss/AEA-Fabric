package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RefundSkillC2S(class_2960 skill) {
   public static final class_2960 CHANNEL = Apoli.internal("refund_skill");

   public void write(class_2540 buf) {
      buf.method_10812(this.skill);
   }

   public static RefundSkillC2S read(class_2540 buf) {
      return new RefundSkillC2S(buf.method_10810());
   }
}
