package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.FluidCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeUsage;
import dev.alphasucks.aea.port.power.PowerTypeUsage.Handle;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_3486;
import net.minecraft.class_3610;

public final class IgnoreFluidPower extends PowerType<IgnoreFluidPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("ignore_fluid");
   public static final Handle HELD = PowerTypeUsage.handle(ApoliIds.IGNORE_FLUID);

   @Override
   public MapCodec<IgnoreFluidPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.strict("fluid_condition", FluidCondition.CODEC).forGetter(IgnoreFluidPower.Config::fluidCondition))
            .apply(i, IgnoreFluidPower.Config::new)
      );
   }

   public static boolean mayIgnore(class_1297 entity) {
      return entity.method_37908().method_8608() ? entity.method_5787() : HELD.isHeld();
   }

   public static boolean holds(class_1297 entity) {
      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() && !container.powersOfType(ApoliIds.IGNORE_FLUID).isEmpty();
   }

   public static boolean ignores(class_1297 entity, class_3610 fluid, class_2338 pos) {
      if (fluid.method_15769()) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.IGNORE_FLUID);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         FluidCtx fluidCtx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof IgnoreFluidPower.Config cfg) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (fluidCtx == null) {
                     fluidCtx = new FluidCtx(fluid, pos.method_10062(), entity.method_37908());
                  }

                  if (cfg.matches(fluid, fluidCtx)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public record Config(Optional<FluidCondition> fluidCondition) {
      boolean matches(class_3610 fluid, FluidCtx ctx) {
         return this.fluidCondition.isPresent() ? ((FluidCondition)this.fluidCondition.get()).test(ctx) : fluid.method_15767(class_3486.field_15517);
      }
   }
}
