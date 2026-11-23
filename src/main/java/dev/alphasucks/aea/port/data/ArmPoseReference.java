package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum ArmPoseReference implements class_3542 {
   EMPTY("empty"),
   ITEM("item"),
   BLOCK("block"),
   BOW_AND_ARROW("bow_and_arrow"),
   THROW_SPEAR("throw_spear"),
   CROSSBOW_CHARGE("crossbow_charge"),
   CROSSBOW_HOLD("crossbow_hold"),
   SPYGLASS("spyglass"),
   TOOT_HORN("toot_horn"),
   BRUSH("brush");

   public static final Codec<ArmPoseReference> CODEC = class_3542.method_28140(ArmPoseReference::values);
   private final String name;

   ArmPoseReference(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
