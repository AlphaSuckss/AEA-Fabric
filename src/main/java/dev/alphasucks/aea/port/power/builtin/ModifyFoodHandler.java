package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyFoodHandler {
   private ModifyFoodHandler() {
   }

   @Nullable
   private static PowerContainer candidates(@Nullable class_1309 entity, class_1799 stack) {
      if (entity != null && !stack.method_7960()) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            return container.powersOfType(ApoliIds.MODIFY_FOOD).isEmpty() ? null : container;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static void forEachApplicable(class_1309 entity, class_1799 stack, PowerContainer container, Consumer<ModifyFoodPower.Config> consumer) {
      List<class_2960> powers = container.powersOfType(ApoliIds.MODIFY_FOOD);
      class_1937 level = entity.method_37908();
      EntityCtx entityCtx = null;
      ItemCtx itemCtx = null;

      for (int i = 0; i < powers.size(); i++) {
         class_2960 powerId = (class_2960)powers.get(i);
         if (!container.isSuppressed(powerId)) {
            Power power = ApoliPowers.get(powerId);
            if (power != null && power.config() instanceof ModifyFoodPower.Config cfg) {
               if (power.condition().isPresent()) {
                  if (entityCtx == null) {
                     entityCtx = EntityCtx.of(entity, level);
                  }

                  if (!((EntityCondition)power.condition().get()).test(entityCtx)) {
                     continue;
                  }
               }

               if (cfg.itemCondition().isPresent()) {
                  if (itemCtx == null) {
                     itemCtx = new ItemCtx(stack, level, entity);
                  }

                  if (!((ItemCondition)cfg.itemCondition().get()).test(itemCtx)) {
                     continue;
                  }
               }

               consumer.accept(cfg);
            }
         }
      }
   }

   @Nullable
   public static ModifyFoodHandler.Values values(class_1309 entity, class_1799 stack, int nutrition, float saturation) {
      PowerContainer container = candidates(entity, stack);
      if (container == null) {
         return null;
      }

      ModifyFoodHandler.Accumulator acc = new ModifyFoodHandler.Accumulator(nutrition, saturation);
      forEachApplicable(entity, stack, container, cfg -> {
         acc.nutrition = apply(cfg.foodModifiers(), acc.nutrition, entity, container);
         acc.saturation = apply(cfg.saturationModifiers(), acc.saturation, entity, container);
         if (cfg.alwaysEdible()) {
            acc.canAlwaysEat = true;
         }

         if (cfg.preventEffects()) {
            acc.preventEffects = true;
         }

         cfg.replaceStack().ifPresent(data -> acc.replaceStack = data.stack().method_7972());
         acc.touched = true;
      });
      return !acc.touched
         ? null
         : new ModifyFoodHandler.Values(Math.max(0, (int)acc.nutrition), (float)acc.saturation, acc.canAlwaysEat, acc.preventEffects, acc.replaceStack);
   }

   public static double eatTicks(@Nullable class_1309 entity, class_1799 stack, double base) {
      PowerContainer container = candidates(entity, stack);
      if (container == null) {
         return base;
      }

      double[] ticks = new double[]{base};
      forEachApplicable(entity, stack, container, cfg -> ticks[0] = apply(cfg.eatTicksModifiers(), ticks[0], entity, container));
      return ticks[0];
   }

   public static boolean alwaysEdible(@Nullable class_1309 entity, class_1799 stack) {
      PowerContainer container = candidates(entity, stack);
      if (container == null) {
         return false;
      }

      boolean[] found = new boolean[]{false};
      forEachApplicable(entity, stack, container, cfg -> {
         if (cfg.alwaysEdible()) {
            found[0] = true;
         }
      });
      return found[0];
   }

   public static boolean preventsEffects(@Nullable class_1309 entity, class_1799 stack) {
      PowerContainer container = candidates(entity, stack);
      if (container == null) {
         return false;
      }

      boolean[] found = new boolean[]{false};
      forEachApplicable(entity, stack, container, cfg -> {
         if (cfg.preventEffects()) {
            found[0] = true;
         }
      });
      return found[0];
   }

   @Nullable
   public static class_1799 replacement(@Nullable ModifyFoodHandler.Eaten eaten) {
      if (eaten == null) {
         return null;
      }

      class_1799 result = null;
      List<ModifyFoodPower.Config> configs = eaten.configs();

      for (int i = 0; i < configs.size(); i++) {
         if (((ModifyFoodPower.Config)configs.get(i)).replaceStack().isPresent()) {
            result = ((ItemStackData)((ModifyFoodPower.Config)configs.get(i)).replaceStack().get()).stack().method_7972();
         }
      }

      return result;
   }

   @Nullable
   public static ModifyFoodHandler.Eaten beforeEat(class_1309 entity, class_1799 stack) {
      PowerContainer container = candidates(entity, stack);
      if (container == null) {
         return null;
      }

      List<ModifyFoodPower.Config> configs = new ArrayList(2);
      forEachApplicable(entity, stack, container, cfg -> {
         if (cfg.itemAction().isPresent() || cfg.entityAction().isPresent() || cfg.replaceStack().isPresent()) {
            configs.add(cfg);
         }
      });
      return configs.isEmpty() ? null : new ModifyFoodHandler.Eaten(configs, stack.method_46651(1));
   }

   public static void afterEat(class_1309 entity, class_1799 stack) {
      afterEat(entity, stack, beforeEat(entity, stack));
   }

   public static void afterEat(class_1309 entity, class_1799 stack, @Nullable ModifyFoodHandler.Eaten eaten) {
      if (eaten != null) {
         class_1937 level = entity.method_37908();
         if (!level.method_8608()) {
            class_1799 eatenStack = stack.method_7960() ? eaten.stack() : stack;
            List<ModifyFoodPower.Config> configs = eaten.configs();

            for (int i = 0; i < configs.size(); i++) {
               ModifyFoodPower.Config cfg = (ModifyFoodPower.Config)configs.get(i);
               cfg.itemAction().ifPresent(action -> action.run(new ItemCtx(eatenStack, level, entity)));
               cfg.entityAction().ifPresent(action -> action.run(EntityCtx.of(entity, level)));
            }
         }
      }
   }

   private static double apply(Optional<List<AttributeModifier>> modifiers, double value, @Nullable class_1309 entity, @Nullable PowerContainer container) {
      if (modifiers.isEmpty()) {
         return value;
      }

      List<AttributeModifier> list = (List<AttributeModifier>)modifiers.get();

      for (int i = 0; i < list.size(); i++) {
         value = ((AttributeModifier)list.get(i)).applyToValue(value, entity, container);
      }

      return value;
   }

   private static final class Accumulator {
      double nutrition;
      double saturation;
      boolean canAlwaysEat;
      boolean preventEffects;
      @Nullable
      class_1799 replaceStack;
      boolean touched;

      Accumulator(int nutrition, float saturation) {
         this.nutrition = nutrition;
         this.saturation = saturation;
      }
   }

   public record Eaten(List<ModifyFoodPower.Config> configs, class_1799 stack) {
   }

   public record Values(int nutrition, float saturation, boolean canAlwaysEat, boolean preventEffects, @Nullable class_1799 replaceStack) {
   }
}
