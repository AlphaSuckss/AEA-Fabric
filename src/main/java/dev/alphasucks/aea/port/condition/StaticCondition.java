package dev.alphasucks.aea.port.condition;

import com.mojang.serialization.Codec;
import dev.alphasucks.aea.port.codec.DispatchedTypeCodec;
import dev.alphasucks.aea.port.codec.DispatchedTypeCodec.CodecLookup.Resolution;
import dev.alphasucks.aea.port.condition.context.StaticCtx;
import net.minecraft.class_2960;

public record StaticCondition(class_2960 typeId, Object config, boolean inverted) {
   public static final Codec<StaticCondition> CODEC = DispatchedTypeCodec.createInvertible("static_condition", id -> {
      class_2960 canonical = ConditionTypes.STATIC.resolveId(id);
      ConditionType<StaticCtx, ?> t = ConditionTypes.STATIC.get(canonical);
      return t == null ? null : new Resolution(canonical, t.codec());
   }, StaticCondition::new, StaticCondition::typeId, StaticCondition::config, StaticCondition::inverted);

   public StaticCondition(class_2960 typeId, Object config) {
      this(typeId, config, false);
   }

   public StaticCondition {
      typeId = ConditionTypes.STATIC.resolveId(typeId);
   }

   public boolean test() {
      return this.test(StaticCtx.INSTANCE);
   }

   public boolean test(StaticCtx ctx) {
      ConditionType<StaticCtx, ?> type = ConditionTypes.STATIC.get(this.typeId);
      if (type == null) {
         return true;
      }

      boolean result = ((ConditionType<StaticCtx, Object>)type).test(this.config, ctx);
      return this.inverted != result;
   }
}
