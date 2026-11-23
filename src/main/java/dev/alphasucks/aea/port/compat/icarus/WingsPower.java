package dev.alphasucks.aea.port.compat.icarus;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1324;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class WingsPower extends PowerType<WingsPower.Config> {
   private static final class_2960 MAX_STAMINA = new class_2960("icarus", "max_stamina");
   private static final Codec<class_1799> WINGS_TYPE_CODEC = Codec.either(
         class_7923.field_41178.method_39673().xmap(class_1799::new, class_1799::method_7909),
         ItemStackData.CODEC.xmap(ItemStackData::stack, ItemStackData::new)
      )
      .xmap(either -> (class_1799)either.map(stack -> stack, stack -> stack), stack -> Either.right(stack));

   @Override
   public MapCodec<WingsPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               WINGS_TYPE_CODEC.fieldOf("wings_type").forGetter(WingsPower.Config::wingsType),
               Codec.BOOL.optionalFieldOf("armor_slows").forGetter(WingsPower.Config::armorSlows),
               Codec.DOUBLE
                  .optionalFieldOf("max_slowed_multiplier")
                  .xmap(WingsPower::toOptionalDouble, WingsPower::fromOptionalDouble)
                  .forGetter(WingsPower.Config::maxSlowedMultiplier),
               Codec.DOUBLE
                  .optionalFieldOf("wings_speed")
                  .xmap(WingsPower::toOptionalDouble, WingsPower::fromOptionalDouble)
                  .forGetter(WingsPower.Config::wingsSpeed),
               Codec.DOUBLE
                  .optionalFieldOf("exhaustion_amount")
                  .xmap(WingsPower::toOptionalDouble, WingsPower::fromOptionalDouble)
                  .forGetter(WingsPower.Config::exhaustionAmount),
               Codec.INT
                  .optionalFieldOf("max_height_above_world")
                  .xmap(WingsPower::toOptionalInt, WingsPower::fromOptionalInt)
                  .forGetter(WingsPower.Config::maxHeightAboveWorld),
               Codec.DOUBLE.optionalFieldOf("stamina").xmap(WingsPower::toOptionalDouble, WingsPower::fromOptionalDouble).forGetter(WingsPower.Config::stamina)
            )
            .apply(instance, WingsPower.Config::new)
      );
   }

   private static OptionalDouble toOptionalDouble(Optional<Double> opt) {
      return (OptionalDouble)opt.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
   }

   private static Optional<Double> fromOptionalDouble(OptionalDouble od) {
      return od.isPresent() ? Optional.of(od.getAsDouble()) : Optional.empty();
   }

   private static OptionalInt toOptionalInt(Optional<Integer> opt) {
      return (OptionalInt)opt.map(OptionalInt::of).orElseGet(OptionalInt::empty);
   }

   private static Optional<Integer> fromOptionalInt(OptionalInt oi) {
      return oi.isPresent() ? Optional.of(oi.getAsInt()) : Optional.empty();
   }

   public void onAdded(class_2960 powerId, WingsPower.Config cfg, PowerContainer holder, class_2960 source) {
      IcarusFieldHooks.install();
      class_1309 owner = holder.owner();
      applyStamina(owner, cfg);
      if (owner != null && conditionPasses(powerId, owner)) {
         WingsAccess.refresh(owner, cfg);
      }
   }

   public void onRemoved(class_2960 powerId, WingsPower.Config cfg, PowerContainer holder, class_2960 source) {
      class_1309 owner = holder.owner();
      if (owner != null) {
         resetStamina(owner, cfg);
         WingsAccess.invalidate(owner);
      }
   }

   public void tick(class_2960 powerId, WingsPower.Config cfg, PowerContainer holder) {
      IcarusFieldHooks.install();
      class_1309 owner = holder.owner();
      if (owner != null) {
         if (conditionPasses(powerId, owner)) {
            WingsAccess.refresh(owner, cfg);
         } else {
            WingsAccess.invalidate(owner);
         }
      }
   }

   private static boolean conditionPasses(class_2960 powerId, class_1309 owner) {
      Power power = ApoliPowers.get(powerId);
      return power != null && !power.condition().isEmpty() ? ((EntityCondition)power.condition().get()).test(EntityCtx.of(owner, owner.method_37908())) : true;
   }

   private static void applyStamina(@Nullable class_1309 owner, WingsPower.Config cfg) {
      if (owner != null && !cfg.stamina().isEmpty()) {
         class_1324 instance = maxStaminaInstance(owner);
         if (instance != null) {
            instance.method_6192(cfg.stamina().getAsDouble());
         }
      }
   }

   private static void resetStamina(@Nullable class_1309 owner, WingsPower.Config cfg) {
      if (owner != null && !cfg.stamina().isEmpty()) {
         class_1324 instance = maxStaminaInstance(owner);
         if (instance != null) {
            instance.method_6192(defaultMaxStamina(instance));
         }
      }
   }

   @Nullable
   private static class_1324 maxStaminaInstance(class_1309 owner) {
      class_1320 attribute = class_7923.field_41190.method_10223(MAX_STAMINA);
      return attribute == null ? null : owner.method_5996(attribute);
   }

   private static double defaultMaxStamina(class_1324 instance) {
      return instance.method_6198().method_6169();
   }

   public record Config(
      class_1799 wingsType,
      Optional<Boolean> armorSlows,
      OptionalDouble maxSlowedMultiplier,
      OptionalDouble wingsSpeed,
      OptionalDouble exhaustionAmount,
      OptionalInt maxHeightAboveWorld,
      OptionalDouble stamina
   ) {
   }
}
