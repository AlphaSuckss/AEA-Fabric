package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class InvisibilityPower extends PowerType<InvisibilityPower.Config> {
   private static final Predicate<InvisibilityPower.Config> ARMOR = InvisibilityPower.Config::renderArmor;
   private static final Predicate<InvisibilityPower.Config> OUTLINE = InvisibilityPower.Config::renderOutline;
   private static final Predicate<InvisibilityPower.Config> HELD_ITEMS = InvisibilityPower.Config::renderHeldItems;
   private static final Predicate<InvisibilityPower.Config> UNCONDITIONAL = cfg -> cfg.bientityCondition().isEmpty();
   private static volatile int cachedGeneration = -1;
   private static volatile boolean cachedInUse;

   @Override
   public MapCodec<InvisibilityPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("render_armor", false).forGetter(InvisibilityPower.Config::renderArmor),
               Codec.BOOL.optionalFieldOf("render_outline", false).forGetter(InvisibilityPower.Config::renderOutline),
               Codec.BOOL.optionalFieldOf("render_held_items", true).forGetter(InvisibilityPower.Config::renderHeldItems),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(InvisibilityPower.Config::bientityCondition)
            )
            .apply(i, InvisibilityPower.Config::new)
      );
   }

   public static boolean hidesArmor(@Nullable class_1297 entity) {
      return suppresses(entity, ARMOR);
   }

   public static boolean hidesOutline(@Nullable class_1297 entity) {
      return suppresses(entity, OUTLINE);
   }

   public static boolean hidesHeldItems(@Nullable class_1297 entity) {
      return suppresses(entity, HELD_ITEMS);
   }

   public static boolean inUse() {
      int generation = ApoliPowers.generation();
      if (generation != cachedGeneration) {
         cachedInUse = ApoliPowers.anyOfType(ApoliIds.INVISIBILITY);
         cachedGeneration = generation;
      }

      return cachedInUse;
   }

   public static boolean hidesUnconditionally(@Nullable class_1297 entity) {
      return inUse() && PowerLookup.anyActive(entity, ApoliIds.INVISIBILITY, InvisibilityPower.Config.class, UNCONDITIONAL);
   }

   private static boolean suppresses(@Nullable class_1297 entity, Predicate<InvisibilityPower.Config> renders) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.INVISIBILITY);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         int i = 0;

         while (true) {
            if (i >= powers.size()) {
               return false;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof InvisibilityPower.Config cfg && !renders.test(cfg)) {
                  if (!power.condition().isPresent()) {
                     break;
                  }

                  if (ctx == null) {
                     ctx = EntityCtx.of(entity, entity.method_37908());
                  }

                  if (((EntityCondition)power.condition().get()).test(ctx)) {
                     break;
                  }
               }
            }

            i++;
         }

         return true;
      } else {
         return false;
      }
   }

   public record Config(boolean renderArmor, boolean renderOutline, boolean renderHeldItems, Optional<BiEntityCondition> bientityCondition) {
   }
}
