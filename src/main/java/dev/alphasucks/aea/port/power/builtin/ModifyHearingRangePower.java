package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
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
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyHearingRangePower extends PowerType<ModifyHearingRangePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_hearing_range");
   private static volatile int cachedGeneration = -1;
   private static volatile boolean cachedInUse;

   @Override
   public MapCodec<ModifyHearingRangePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyHearingRangePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyHearingRangePower.Config::modifiers),
               Codec.BOOL.optionalFieldOf("sounds", true).forGetter(ModifyHearingRangePower.Config::sounds),
               Codec.BOOL.optionalFieldOf("voice", true).forGetter(ModifyHearingRangePower.Config::voice),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ModifyHearingRangePower.Config::bientityCondition)
            )
            .apply(i, ModifyHearingRangePower.Config::new)
      );
   }

   public static boolean inUse() {
      int generation = ApoliPowers.generation();
      if (generation != cachedGeneration) {
         cachedInUse = ApoliPowers.anyOfType(CANONICAL);
         cachedGeneration = generation;
      }

      return cachedInUse;
   }

   @Override
   public boolean resolvesForClient() {
      return true;
   }

   public static double resolvedSoundRange(@Nullable class_1297 listener, double base, Predicate<class_2960> active) {
      if (listener == null) {
         return base;
      }

      PowerContainer container = PowerContainer.of(listener);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return base;
         }

         double range = base;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (active.test(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyHearingRangePower.Config cfg && cfg.sounds() && !cfg.bientityCondition().isPresent()) {
                  List<AttributeModifier> mods = cfg.flattened();
                  if (!mods.isEmpty()) {
                     range = AttributeModifierHelper.apply(range, mods, listener, container);
                  }
               }
            }
         }

         return range;
      } else {
         return base;
      }
   }

   public static double soundRange(@Nullable class_1297 listener, double base) {
      if (listener == null) {
         return base;
      }

      PowerContainer container = PowerContainer.of(listener);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return base;
         }

         EntityCtx ctx = null;
         double range = base;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyHearingRangePower.Config cfg && cfg.sounds() && !cfg.bientityCondition().isPresent()) {
                  List<AttributeModifier> mods = cfg.flattened();
                  if (!mods.isEmpty()) {
                     if (power.condition().isPresent()) {
                        if (ctx == null) {
                           ctx = new EntityCtx(listener, listener.method_37908());
                        }

                        if (!((EntityCondition)power.condition().get()).test(ctx)) {
                           continue;
                        }
                     }

                     range = AttributeModifierHelper.apply(range, mods, listener, container);
                  }
               }
            }
         }

         return range;
      } else {
         return base;
      }
   }

   public static boolean hearsDifferently(@Nullable class_1297 listener) {
      if (listener == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(listener);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyHearingRangePower.Config cfg && cfg.voice() && !cfg.flattened().isEmpty()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static double @Nullable [] voiceRanges(@Nullable class_1297 listener, @Nullable class_1297 speaker, double normalBase, double whisperBase) {
      if (listener == null) {
         return null;
      }

      PowerContainer container = PowerContainer.of(listener);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return null;
         }

         EntityCtx ctx = null;
         double normal = normalBase;
         double whisper = whisperBase;
         boolean any = false;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyHearingRangePower.Config cfg && cfg.voice()) {
                  List<AttributeModifier> mods = cfg.flattened();
                  if (!mods.isEmpty()
                     && (
                        !cfg.bientityCondition().isPresent()
                           || speaker != null
                              && ((BiEntityCondition)cfg.bientityCondition().get()).test(BiEntityCtx.of(listener, speaker, listener.method_37908()))
                     )) {
                     if (power.condition().isPresent()) {
                        if (ctx == null) {
                           ctx = new EntityCtx(listener, listener.method_37908());
                        }

                        if (!((EntityCondition)power.condition().get()).test(ctx)) {
                           continue;
                        }
                     }

                     normal = AttributeModifierHelper.apply(normal, mods, listener, container);
                     whisper = AttributeModifierHelper.apply(whisper, mods, listener, container);
                     any = true;
                  }
               }
            }
         }

         return !any ? null : new double[]{normal, whisper};
      } else {
         return null;
      }
   }

   public record Config(
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      boolean sounds,
      boolean voice,
      Optional<BiEntityCondition> bientityCondition,
      List<AttributeModifier> flattened
   ) {
      Config(
         Optional<AttributeModifier> modifier,
         Optional<List<AttributeModifier>> modifiers,
         boolean sounds,
         boolean voice,
         Optional<BiEntityCondition> bientityCondition
      ) {
         this(modifier, modifiers, sounds, voice, bientityCondition, AttributeModifierHelper.flatten(modifier, modifiers));
      }
   }
}
