package dev.alphasucks.aea.port.skill;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum SkillFrame implements class_3542 {
   TASK("task"),
   GOAL("goal"),
   CHALLENGE("challenge");

   public static final Codec<SkillFrame> CODEC = class_3542.method_28140(SkillFrame::values);
   public static final SkillFrame[] VALUES = values();
   private final String name;

   SkillFrame(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
