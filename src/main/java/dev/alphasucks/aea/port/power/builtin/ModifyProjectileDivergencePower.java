package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1676;

public final class ModifyProjectileDivergencePower extends PowerType<ModifyProjectileDivergencePower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.MODIFY_PROJECTILE_DIVERGENCE);

   @Override
   public MapCodec<ModifyProjectileDivergencePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyProjectileDivergencePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyProjectileDivergencePower.Config::modifiers),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ModifyProjectileDivergencePower.Config::bientityCondition)
            )
            .apply(i, (modifier, modifiers, condition) -> new ModifyProjectileDivergencePower.Config(modifier, modifiers, condition))
      );
   }

   public static float modify(class_1676 projectile, float divergence) {
      if (!IN_USE.inUse()) {
         return divergence;
      } else {
         class_1297 owner = projectile.method_24921();
         if (owner != null && PowerLookup.holdsType(owner, ApoliIds.MODIFY_PROJECTILE_DIVERGENCE)) {
            BiEntityCtx ctx = BiEntityCtx.of(owner, projectile, owner.method_37908());
            List<AttributeModifier> mods = new ArrayList(2);
            PowerLookup.forEach(owner, ApoliIds.MODIFY_PROJECTILE_DIVERGENCE, ModifyProjectileDivergencePower.Config.class, cfg -> {
               if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
                  mods.addAll(cfg.combined());
               }
            });
            return mods.isEmpty() ? divergence : Math.max(0.0F, AttributeModifierHelper.apply(divergence, AttributeModifierHelper.ensureSorted(mods), owner));
         } else {
            return divergence;
         }
      }
   }

   public record Config(
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      Optional<BiEntityCondition> bientityCondition,
      List<AttributeModifier> combined
   ) {
      public Config(Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers, Optional<BiEntityCondition> bientityCondition) {
         this(modifier, modifiers, bientityCondition, AttributeModifierHelper.flatten(modifier, modifiers));
      }
   }
}
