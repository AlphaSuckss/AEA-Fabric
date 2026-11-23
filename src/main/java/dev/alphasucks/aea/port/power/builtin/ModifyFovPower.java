package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyFovPower extends PowerType<ModifyFovPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_fov");

   @Override
   public MapCodec<ModifyFovPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyFovPower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyFovPower.Config::modifiers),
               Codec.BOOL.optionalFieldOf("affected_by_fov_effect_scale", true).forGetter(ModifyFovPower.Config::affectedByFovEffectScale)
            )
            .apply(i, ModifyFovPower.Config::new)
      );
   }

   @Override
   public boolean resolvesForClient() {
      return true;
   }

   public static float apply(@Nullable class_1297 holder, float value, Predicate<class_2960> active) {
      if (holder == null) {
         return value;
      }

      PowerContainer container = PowerContainer.of(holder);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         class_1309 living = holder instanceof class_1309 le ? le : null;
         boolean transferred = living != null && AttributeModifyTransferPower.FOV.appliesTo(container);
         if (powers.isEmpty() && !transferred) {
            return value;
         }

         double result = value;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (active.test(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyFovPower.Config cfg && !cfg.flattened().isEmpty()) {
                  result = AttributeModifierHelper.apply(result, cfg.flattened(), holder, container);
               }
            }
         }

         if (transferred) {
            List<AttributeModifier> transfers = AttributeModifyTransferPower.FOV.merge(living, container, List.of());
            if (!transfers.isEmpty()) {
               result = AttributeModifierHelper.apply(result, transfers, holder, container);
            }
         }

         return (float)result;
      } else {
         return value;
      }
   }

   public static boolean ignoresEffectScale(@Nullable class_1297 holder, Predicate<class_2960> active) {
      if (holder == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(holder);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (active.test(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyFovPower.Config cfg && !cfg.affectedByFovEffectScale()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public record Config(
      Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers, boolean affectedByFovEffectScale, List<AttributeModifier> flattened
   ) {
      Config(Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers, boolean affectedByFovEffectScale) {
         this(modifier, modifiers, affectedByFovEffectScale, AttributeModifierHelper.flatten(modifier, modifiers));
      }
   }
}
