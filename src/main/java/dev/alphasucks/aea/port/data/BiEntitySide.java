package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import net.minecraft.class_1297;
import net.minecraft.class_3542;

public enum BiEntitySide implements class_3542 {
   ACTOR("actor"),
   TARGET("target");

   public static final Codec<BiEntitySide> CODEC = class_3542.method_28140(BiEntitySide::values);
   private final String name;

   BiEntitySide(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public BiEntitySide opposite() {
      return this == ACTOR ? TARGET : ACTOR;
   }

   public class_1297 of(BiEntityCtx ctx) {
      return this == ACTOR ? ctx.actor() : ctx.target();
   }
}
