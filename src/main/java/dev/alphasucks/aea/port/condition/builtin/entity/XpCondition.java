package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_1657;
import net.minecraft.class_3542;

public final class XpCondition implements ConditionType<EntityCtx, XpCondition.Cfg> {
   private final XpCondition.Unit defaultUnit;

   public XpCondition(XpCondition.Unit defaultUnit) {
      this.defaultUnit = defaultUnit;
   }

   public XpCondition() {
      this(XpCondition.Unit.LEVELS);
   }

   @Override
   public MapCodec<XpCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               XpCondition.Unit.CODEC.optionalFieldOf("unit", this.defaultUnit).forGetter(XpCondition.Cfg::unit),
               Comparison.CODEC.fieldOf("comparison").forGetter(XpCondition.Cfg::comparison),
               Codec.INT.fieldOf("compare_to").forGetter(XpCondition.Cfg::compareTo)
            )
            .apply(i, XpCondition.Cfg::new)
      );
   }

   public boolean test(XpCondition.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_1657 player) {
         int val = cfg.unit == XpCondition.Unit.LEVELS ? player.field_7520 : player.field_7495;
         return cfg.comparison.compare(val, cfg.compareTo);
      } else {
         return false;
      }
   }

   public record Cfg(XpCondition.Unit unit, Comparison comparison, int compareTo) {
   }

   public enum Unit implements class_3542 {
      LEVELS("levels"),
      POINTS("points");

      public static final Codec<XpCondition.Unit> CODEC = class_3542.method_28140(XpCondition.Unit::values);
      private final String name;

      Unit(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
