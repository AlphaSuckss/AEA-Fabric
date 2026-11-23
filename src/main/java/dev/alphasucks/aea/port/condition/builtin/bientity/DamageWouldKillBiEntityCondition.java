package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
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

public final class DamageWouldKillBiEntityCondition implements ConditionType<BiEntityCtx, DamageWouldKillBiEntityCondition.Cfg> {
   @Override
   public MapCodec<DamageWouldKillBiEntityCondition.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Expression.FLOAT_OR_EXPR.fieldOf("amount").forGetter(DamageWouldKillBiEntityCondition.Cfg::amount),
                  IdCodecs.ID.fieldOf("damage_type").forGetter(DamageWouldKillBiEntityCondition.Cfg::damageType),
                  AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(DamageWouldKillBiEntityCondition.Cfg::modifier),
                  AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(DamageWouldKillBiEntityCondition.Cfg::modifiers)
               )
               .apply(i, DamageWouldKillBiEntityCondition.Cfg::new)
         ),
         Map.of("damage", "amount")
      );
   }

   public boolean test(DamageWouldKillBiEntityCondition.Cfg cfg, BiEntityCtx ctx) {
      class_1309 target = ctx.livingTarget();
      if (target == null) {
         return false;
      }

      class_1282 source = DamagePrediction.source(ctx.level(), cfg.damageType, ctx.actor());
      if (source == null) {
         return false;
      }

      float base = (float)cfg.amount.eval(ctx.actor());
      List<AttributeModifier> mods = AttributeModifierHelper.flatten(cfg.modifier, cfg.modifiers);
      float amount = mods.isEmpty() ? base : Math.max(0.0F, AttributeModifierHelper.apply(base, mods, target));
      return DamagePrediction.wouldKill(target, source, amount);
   }

   public record Cfg(Expression amount, class_2960 damageType, Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers) {
   }
}
