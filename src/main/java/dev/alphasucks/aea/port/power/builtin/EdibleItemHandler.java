package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Pair;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.FoodComponent;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.ModifyFoodHandler.Values;
import dev.alphasucks.aea.port.sound.Sounds;
import java.util.List;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_174;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_3468;
import net.minecraft.class_5712;
import org.jetbrains.annotations.Nullable;

public final class EdibleItemHandler {
   private static final ThreadLocal<EdibleItemHandler.Slot> ACTIVE_SLOT = ThreadLocal.withInitial(EdibleItemHandler.Slot::new);

   private EdibleItemHandler() {
   }

   @Nullable
   public static EdibleItemPower.Config find(@Nullable class_1309 entity, class_1799 stack) {
      if (entity != null && !stack.method_7960()) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(ApoliIds.EDIBLE_ITEM);
            if (powers.isEmpty()) {
               return null;
            }

            class_1937 level = entity.method_37908();
            EntityCtx entityCtx = null;
            ItemCtx itemCtx = null;
            for (int i = 0; i < powers.size(); i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (container.isSuppressed(powerId)) {
                  continue;
               }

               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof EdibleItemPower.Config cfg) {
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

                  arm(entity, stack, cfg);
                  return cfg;
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean canConsume(class_1657 player, EdibleItemPower.Config cfg, class_1799 stack) {
      return player.method_7332(cfg.foodComponent().alwaysEdible() || ModifyFoodHandler.alwaysEdible(player, stack));
   }

   public static int useDuration(class_1309 entity, EdibleItemPower.Config cfg, class_1799 stack) {
      double ticks = cfg.foodComponent().eatDurationTicks();
      ticks = ModifyFoodHandler.eatTicks(entity, stack, ticks);
      if (cfg.consumingTimeModifier().isEmpty() && cfg.consumingTimeModifiers().isEmpty()) {
         return Math.max(1, (int)Math.round(ticks));
      }

      PowerContainer container = PowerContainer.of(entity);
      if (cfg.consumingTimeModifier().isPresent()) {
         ticks = ((AttributeModifier)cfg.consumingTimeModifier().get()).applyToValue(ticks, entity, container);
      }

      if (cfg.consumingTimeModifiers().isPresent()) {
         List<AttributeModifier> modifiers = (List<AttributeModifier>)cfg.consumingTimeModifiers().get();

         for (int i = 0; i < modifiers.size(); i++) {
            ticks = ((AttributeModifier)modifiers.get(i)).applyToValue(ticks, entity, container);
         }
      }

      return Math.max(1, (int)Math.round(ticks));
   }

   @Nullable
   public static class_1839 animationFor(class_1799 stack) {
      EdibleItemHandler.Slot slot = (EdibleItemHandler.Slot)ACTIVE_SLOT.get();
      return slot.config != null && slot.item == stack.method_7909() ? slot.config.consumeAnimation().vanilla() : null;
   }

   public static int armedDuration(class_1799 stack) {
      EdibleItemHandler.Slot slot = (EdibleItemHandler.Slot)ACTIVE_SLOT.get();
      return slot.config != null && slot.item == stack.method_7909() ? slot.duration : -1;
   }

   public static class_1799 finish(class_1937 level, class_1309 entity, class_1799 stack, EdibleItemPower.Config cfg) {
      disarm(stack);
      boolean server = !level.method_8608();
      FoodComponent food = cfg.foodComponent();
      playConsumeSound(level, entity, cfg);
      Values modified = ModifyFoodHandler.values(entity, stack, food.hunger(), food.saturation());
      if (entity instanceof class_1657 player) {
         if (modified != null) {
            player.method_7344().method_7585(modified.nutrition(), modified.saturation());
         } else {
            player.method_7344().method_7585(food.hunger(), food.saturation());
         }

         player.method_7259(class_3468.field_15372.method_14956(stack.method_7909()));
         if (player instanceof class_3222 serverPlayer) {
            class_174.field_1198.method_8821(serverPlayer, stack);
         }
      }

      if (server) {
         List<Pair<class_1293, Float>> effects = modified != null && modified.preventEffects() ? List.of() : food.allEffectsWithProbability(entity);

         for (int i = 0; i < effects.size(); i++) {
            Pair<class_1293, Float> effect = (Pair<class_1293, Float>)effects.get(i);
            if (level.method_8409().method_43057() < (Float)effect.getSecond()) {
               entity.method_6092(new class_1293((class_1293)effect.getFirst()));
            }
         }

         cfg.itemAction().ifPresent(action -> action.run(new ItemCtx(stack, level, entity)));
         cfg.entityAction().ifPresent(action -> action.run(EntityCtx.of(entity, level)));
         ModifyFoodHandler.afterEat(entity, stack);
      }

      if (!(entity instanceof class_1657 player && player.method_31549().field_7477)) {
         stack.method_7934(1);
      }

      entity.method_32876(class_5712.field_28735);
      if (cfg.resultStack().isEmpty()) {
         return stack;
      }

      class_1799 result = ((ItemStackData)cfg.resultStack().get()).stack().method_7972();
      if (result.method_7960()) {
         return stack;
      }

      if (server) {
         cfg.resultItemAction().ifPresent(action -> action.run(new ItemCtx(result, level, entity)));
      }

      if (stack.method_7960()) {
         return result;
      }

      if (server) {
         giveResult(entity, result);
      }

      return stack;
   }

   private static void giveResult(class_1309 entity, class_1799 result) {
      if (entity instanceof class_1657 player) {
         if (!player.method_31548().method_7394(result)) {
            player.method_7328(result, false);
         }
      } else {
         entity.method_5775(result);
      }
   }

   private static void playConsumeSound(class_1937 level, class_1309 entity, EdibleItemPower.Config cfg) {
      class_3414 sound = Sounds.event(cfg.consumeSound());
      level.method_43128(
         null,
         entity.method_23317(),
         entity.method_23318(),
         entity.method_23321(),
         sound,
         class_3419.field_15254,
         1.0F,
         1.0F + (level.method_8409().method_43057() - level.method_8409().method_43057()) * 0.4F
      );
   }

   private static void arm(class_1309 entity, class_1799 stack, EdibleItemPower.Config cfg) {
      EdibleItemHandler.Slot slot = (EdibleItemHandler.Slot)ACTIVE_SLOT.get();
      slot.item = stack.method_7909();
      slot.config = cfg;
      slot.duration = useDuration(entity, cfg, stack);
   }

   private static void disarm(class_1799 stack) {
      EdibleItemHandler.Slot slot = (EdibleItemHandler.Slot)ACTIVE_SLOT.get();
      if (slot.item == stack.method_7909()) {
         slot.item = null;
         slot.config = null;
         slot.duration = 0;
      }
   }

   private static final class Slot {
      @Nullable
      private class_1792 item;
      @Nullable
      private EdibleItemPower.Config config;
      private int duration;
   }
}
