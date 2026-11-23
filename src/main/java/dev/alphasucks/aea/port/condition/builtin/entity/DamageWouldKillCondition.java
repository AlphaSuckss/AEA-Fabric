package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.data.DamagePrediction;
import dev.alphasucks.aea.port.data.Expression;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_2960;

public final class DamageWouldKillCondition implements ConditionType<EntityCtx, DamageWouldKillCondition.Cfg> {
   @Override
   public MapCodec<DamageWouldKillCondition.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Expression.FLOAT_OR_EXPR.fieldOf("amount").forGetter(DamageWouldKillCondition.Cfg::amount),
                  IdCodecs.ID.fieldOf("damage_type").forGetter(DamageWouldKillCondition.Cfg::damageType),
                  AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(DamageWouldKillCondition.Cfg::modifier),
                  AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(DamageWouldKillCondition.Cfg::modifiers)
               )
               .apply(i, DamageWouldKillCondition.Cfg::new)
         ),
         Map.of("damage", "amount")
      );
   }

   public boolean test(DamageWouldKillCondition.Cfg cfg, EntityCtx ctx) {
      class_1309 target = ctx.living();
      if (target == null) {
         return false;
      }

      class_1282 source = DamagePrediction.source(ctx.level(), cfg.damageType, null);
      if (source == null) {
         return false;
      }

      float base = (float)cfg.amount.eval(target);
      List<AttributeModifier> mods = AttributeModifierHelper.flatten(cfg.modifier, cfg.modifiers);
      float amount = mods.isEmpty() ? base : Math.max(0.0F, AttributeModifierHelper.apply(base, mods, target));
      return DamagePrediction.wouldKill(target, source, amount);
   }

   public record Cfg(Expression amount, class_2960 damageType, Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers) {
   }
}
