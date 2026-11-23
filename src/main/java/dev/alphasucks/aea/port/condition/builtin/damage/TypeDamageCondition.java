package dev.alphasucks.aea.port.condition.builtin.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.data.IdOrTag;
import net.minecraft.class_7924;
import net.minecraft.class_8110;

public final class TypeDamageCondition implements ConditionType<DamageCtx, TypeDamageCondition.Cfg> {
   @Override
   public MapCodec<TypeDamageCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdOrTag.codec(class_7924.field_42534).fieldOf("damage_type").forGetter(TypeDamageCondition.Cfg::damageType))
            .apply(i, TypeDamageCondition.Cfg::new)
      );
   }

   public boolean test(TypeDamageCondition.Cfg cfg, DamageCtx ctx) {
      return cfg.damageType.matches(ctx.source().method_48793());
   }

   public record Cfg(IdOrTag<class_8110> damageType) {
   }
}
