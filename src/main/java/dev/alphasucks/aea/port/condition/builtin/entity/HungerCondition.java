package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_1657;
import net.minecraft.class_3542;

public final class HungerCondition implements ConditionType<EntityCtx, HungerCondition.Cfg> {
   private final HungerCondition.Kind defaultKind;

   public HungerCondition(HungerCondition.Kind defaultKind) {
      this.defaultKind = defaultKind;
   }

   public HungerCondition() {
      this(HungerCondition.Kind.FOOD);
   }

   @Override
   public MapCodec<HungerCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               HungerCondition.Kind.CODEC.optionalFieldOf("kind", this.defaultKind).forGetter(HungerCondition.Cfg::kind),
               Comparison.CODEC.fieldOf("comparison").forGetter(HungerCondition.Cfg::comparison),
               Codec.FLOAT.fieldOf("compare_to").forGetter(HungerCondition.Cfg::compareTo)
            )
            .apply(i, HungerCondition.Cfg::new)
      );
   }

   public boolean test(HungerCondition.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_1657 player) {
         float val = cfg.kind == HungerCondition.Kind.FOOD ? player.method_7344().method_7586() : player.method_7344().method_7589();
         return cfg.comparison.compare(val, cfg.compareTo);
      } else {
         return false;
      }
   }

   public record Cfg(HungerCondition.Kind kind, Comparison comparison, float compareTo) {
   }

   public enum Kind implements class_3542 {
      FOOD("food"),
      SATURATION("saturation");

      public static final Codec<HungerCondition.Kind> CODEC = class_3542.method_28140(HungerCondition.Kind::values);
      private final String name;

      Kind(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
