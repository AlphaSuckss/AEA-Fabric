package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypeCodec;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ScalePower extends PowerType<ScalePower.Config> {
   @Override
   public MapCodec<ScalePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ScaleTypeCodec.LIST_OR_SINGLE.optionalFieldOf("scale_types", List.of(ScaleTypes.BASE)).forGetter(ScalePower.Config::types),
               Expression.DOUBLE_OR_EXPR.optionalFieldOf("scale").forGetter(ScalePower.Config::scale),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ScalePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ScalePower.Config::modifiers)
            )
            .apply(i, ScalePower.Config::new)
      );
   }

   public void onAdded(class_2960 powerId, ScalePower.Config cfg, PowerContainer holder, class_2960 source) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         Scales.invalidate(owner);
      }
   }

   public void onRemoved(class_2960 powerId, ScalePower.Config cfg, PowerContainer holder, class_2960 source) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         Scales.invalidate(owner);
      }
   }

   public void onSuppressed(class_2960 powerId, ScalePower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         Scales.invalidate(owner);
      }
   }

   public void onUnsuppressed(class_2960 powerId, ScalePower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         Scales.invalidate(owner);
      }
   }

   public void tick(class_2960 powerId, ScalePower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         Scales.refreshDimensions(owner, Scales.stateOrCreate(owner));
      }
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public static boolean hasAny(class_1297 entity) {
      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() && !container.powersOfType(ApoliIds.SCALE).isEmpty();
   }

   public static boolean applyFactors(class_1297 entity, float[] out) {
      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.SCALE);
         if (powers.isEmpty()) {
            return false;
         }

         class_1937 level = entity.method_37908();
         EntityCtx ctx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ScalePower.Config cfg) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, level);
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  float factor = factorOf(cfg, entity, container);
                  if (factor != 1.0F) {
                     List<ScaleType> types = cfg.types();
                     int t = 0;

                     for (int tn = types.size(); t < tn; t++) {
                        out[((ScaleType)types.get(t)).index()] *= factor;
                     }
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static float factorOf(ScalePower.Config cfg, class_1297 entity, @Nullable PowerContainer container) {
      double factor = 1.0;
      if (cfg.scale().isPresent()) {
         factor = ((Expression)cfg.scale().get()).evalWith(entity, container, 1.0);
      }

      List<AttributeModifier> mods = cfg.allModifiers();
      if (!mods.isEmpty()) {
         factor = AttributeModifierHelper.apply(factor, mods, entity, container);
      }

      return ScaleState.clamp((float)factor);
   }

   public record Config(List<ScaleType> types, Optional<Expression> scale, Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers) {
      public List<AttributeModifier> allModifiers() {
         return AttributeModifierHelper.flatten(this.modifier, this.modifiers);
      }
   }
}
