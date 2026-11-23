package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class TogglePower extends PowerType<TogglePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("toggle");

   @Override
   public MapCodec<TogglePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("active_by_default", true).forGetter(TogglePower.Config::activeByDefault),
               Key.CODEC.optionalFieldOf("key", Key.DEFAULT_PRIMARY).forGetter(TogglePower.Config::key),
               Codec.BOOL.optionalFieldOf("retain_state", true).forGetter(TogglePower.Config::retainState),
               LoggedOptionalField.strict("turn_on_condition", EntityCondition.CODEC).forGetter(TogglePower.Config::turnOnCondition),
               LoggedOptionalField.strict("turn_off_condition", EntityCondition.CODEC).forGetter(TogglePower.Config::turnOffCondition)
            )
            .apply(i, TogglePower.Config::new)
      );
   }

   public void onAdded(class_2960 powerId, TogglePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!impl.getAuxInt(powerId).isPresent()) {
            impl.setAuxInt(powerId, cfg.activeByDefault ? 1 : 0);
         }
      }
   }

   public void onRemoved(class_2960 powerId, TogglePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            impl.removeAux(powerId);
         }
      }
   }

   public void tick(class_2960 powerId, TogglePower.Config cfg, PowerContainer holder) {
      if (!cfg.retainState) {
         if (holder instanceof PowerContainerImpl impl) {
            if (impl.getAuxIntOr(powerId, 0) != 0) {
               class_1309 owner = holder.owner();
               if (owner != null && owner.method_37908() instanceof class_3218 level) {
                  Power loaded = ApoliPowers.get(powerId);
                  if (loaded != null && !loaded.condition().isEmpty()) {
                     if (!((EntityCondition)loaded.condition().get()).test(new EntityCtx(owner, level))) {
                        impl.setAuxInt(powerId, 0);
                     }
                  }
               }
            }
         }
      }
   }

   public static void toggle(PowerContainer holder, class_2960 powerId) {
      if (holder instanceof PowerContainerImpl impl) {
         int cur = impl.getAuxInt(powerId).orElse(0);
         impl.setAuxInt(powerId, cur == 0 ? 1 : 0);
      }
   }

   public static boolean toggleByKey(PowerContainer holder, class_2960 powerId, TogglePower.Config cfg) {
      if (holder instanceof PowerContainerImpl impl) {
         int cur = impl.getAuxInt(powerId).orElse(0);
         Optional<EntityCondition> gate = cur == 0 ? cfg.turnOnCondition() : cfg.turnOffCondition();
         if (gate.isPresent()) {
            class_1297 owner = holder.rawOwner();
            if (owner == null || !(owner.method_37908() instanceof class_3218 level)) {
               return false;
            }

            if (!((EntityCondition)gate.get()).test(EntityCtx.of(owner, level))) {
               return false;
            }
         }

         impl.setAuxInt(powerId, cur == 0 ? 1 : 0);
         return true;
      } else {
         return false;
      }
   }

   public static boolean isActive(class_1297 entity, class_2960 powerId) {
      PowerContainer holder = PowerContainer.of(entity);
      if (holder == null) {
         return false;
      }

      Power power = ApoliPowers.get(powerId);
      if (power == null) {
         return false;
      }

      if (!CANONICAL.equals(PowerTypeRegistry.resolveId(power.typeId()))) {
         return false;
      }

      OptionalInt v = holder.getAuxInt(powerId);
      return v.isPresent() && v.getAsInt() != 0;
   }

   public record Config(
      boolean activeByDefault, Key key, boolean retainState, Optional<EntityCondition> turnOnCondition, Optional<EntityCondition> turnOffCondition
   ) {
   }
}
