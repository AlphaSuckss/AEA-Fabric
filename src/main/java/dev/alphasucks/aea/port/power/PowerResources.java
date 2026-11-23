package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.advancement.ApoliCriteria;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.builtin.ModifyResourceChangePower;
import dev.alphasucks.aea.port.power.builtin.ResourcePower;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class PowerResources {
   private static PowerResources.ClientCooldownLookup clientCooldowns = (owner, powerId) -> 0;

   private PowerResources() {
   }

   public static void setClientCooldownLookup(PowerResources.ClientCooldownLookup lookup) {
      clientCooldowns = lookup;
   }

   public static int clientCooldown(PowerContainer holder, class_2960 powerId) {
      return clientCooldowns.remaining(holder.rawOwner(), powerId);
   }

   public static int cooldownTicks(Expression cooldown, @Nullable PowerContainer holder) {
      return cooldown == null ? 0 : cooldown.evalIntWith(holder == null ? null : holder.rawOwner(), holder, 0.0);
   }

   public static OptionalInt read(@Nullable PowerContainer holder, class_2960 powerId) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null) {
            PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
            if (type != null) {
               OptionalInt typed = invokeRead(type, powerId, loaded.config(), holder);
               if (typed.isPresent()) {
                  return typed;
               }
            }
         }

         return holder.getAuxInt(powerId);
      } else {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt write(@Nullable PowerContainer holder, class_2960 powerId, int value) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         PowerType<?> type = loaded == null ? null : PowerTypeRegistry.get(loaded.typeId());
         if (type == null) {
            return apaceWrite(holder, powerId, value);
         }

         int target = value;
         if (ModifyResourceChangePower.inUse()) {
            OptionalInt current = invokeRead(type, powerId, loaded.config(), holder);
            int before = current.orElseGet(() -> holder.getAuxIntOr(powerId, value));
            target = ModifyResourceChangePower.adjust(holder, powerId, before, value);
         }

         OptionalInt written = invokeWrite(type, powerId, loaded.config(), holder, target);
         if (written.isPresent()) {
            ApoliCriteria.resourceChanged(holder.rawOwner(), powerId, written.getAsInt());
         }

         return written;
      } else {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt readAt(@Nullable PowerContainer holder, class_2960 powerId, int slot) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded == null) {
            return slot == 0 ? holder.getAuxInt(powerId) : OptionalInt.empty();
         } else {
            PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
            if (type == null) {
               return slot == 0 ? holder.getAuxInt(powerId) : OptionalInt.empty();
            } else {
               OptionalInt typed = invokeReadAt(type, powerId, loaded.config(), holder, slot);
               if (typed.isPresent()) {
                  return typed;
               } else {
                  return slot == 0 ? holder.getAuxInt(powerId) : OptionalInt.empty();
               }
            }
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt writeAt(@Nullable PowerContainer holder, class_2960 powerId, int slot, int value) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         PowerType<?> type = loaded == null ? null : PowerTypeRegistry.get(loaded.typeId());
         if (type == null) {
            return slot == 0 ? apaceWrite(holder, powerId, value) : OptionalInt.empty();
         }

         int target = value;
         if (ModifyResourceChangePower.inUse()) {
            OptionalInt current = invokeReadAt(type, powerId, loaded.config(), holder, slot);
            int before = current.orElseGet(() -> holder.getAuxIntOr(powerId, value));
            target = ModifyResourceChangePower.adjust(holder, powerId, before, value);
         }

         OptionalInt written = invokeWriteAt(type, powerId, loaded.config(), holder, slot, target);
         if (written.isPresent()) {
            ApoliCriteria.resourceChanged(holder.rawOwner(), powerId, written.getAsInt());
         }

         return written;
      } else {
         return OptionalInt.empty();
      }
   }

   public static int size(@Nullable PowerContainer holder, class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return 0;
      }

      PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
      return type == null ? 0 : invokeSize(type, powerId, loaded.config(), holder);
   }

   public static int indexOf(@Nullable PowerContainer holder, class_2960 powerId, int value) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded == null) {
            return holder.getAuxIntOr(powerId, value + 1) == value ? 0 : -1;
         }

         PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
         return type == null ? -1 : invokeIndexOf(type, powerId, loaded.config(), holder, value);
      } else {
         return -1;
      }
   }

   public static OptionalInt readDeadline(PowerContainer holder, class_2960 powerId) {
      if (DevMode.isEnabled(holder.rawOwner())) {
         return OptionalInt.of(0);
      }

      OptionalInt stored = holder.getAuxInt(powerId);
      if (stored.isEmpty()) {
         return OptionalInt.of(0);
      }

      int now = (int)holder.rawOwner().method_37908().method_8510();
      return OptionalInt.of(Math.max(0, stored.getAsInt() - now));
   }

   public static OptionalInt writeDeadline(PowerContainer holder, class_2960 powerId, int remaining, int cooldown) {
      if (holder instanceof PowerContainerImpl impl) {
         int clamped = Math.max(0, Math.min(remaining, Math.max(cooldown, 0)));
         int now = (int)holder.rawOwner().method_37908().method_8510();
         impl.setAuxInt(powerId, now + clamped);
         return OptionalInt.of(clamped);
      } else {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt bound(@Nullable PowerContainer holder, class_2960 powerId, boolean max) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return apaceBound(holder, powerId, max);
      } else {
         PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
         if (type == null) {
            return apaceBound(holder, powerId, max);
         } else {
            return type instanceof ResourcePower ? ResourcePower.boundOf(holder, powerId, max) : invokeBound(type, powerId, loaded.config(), holder, max);
         }
      }
   }

   public static boolean triggerCooldown(@Nullable PowerContainer holder, class_2960 powerId) {
      if (holder != null && holder.hasPower(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded == null) {
            return false;
         }

         PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
         if (type != null && type.isCooldown()) {
            if (invokeRead(type, powerId, loaded.config(), holder).orElse(0) > 0) {
               return false;
            }

            OptionalInt length = invokeBound(type, powerId, loaded.config(), holder, true);
            return !length.isEmpty() && length.getAsInt() > 0 ? invokeWrite(type, powerId, loaded.config(), holder, length.getAsInt()).isPresent() : false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isResource(class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return false;
      } else {
         PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
         if (type == null) {
            return false;
         } else {
            return type instanceof ResourcePower ? true : invokeBound(type, powerId, loaded.config(), null, true).isPresent();
         }
      }
   }

   private static OptionalInt invokeRead(PowerType type, class_2960 powerId, Object cfg, PowerContainer holder) {
      return type.readResource(powerId, cfg, holder);
   }

   private static OptionalInt invokeWrite(PowerType type, class_2960 powerId, Object cfg, PowerContainer holder, int value) {
      return type.writeResource(powerId, cfg, holder, value);
   }

   private static OptionalInt invokeReadAt(PowerType type, class_2960 powerId, Object cfg, PowerContainer holder, int slot) {
      return type.readResourceAt(powerId, cfg, holder, slot);
   }

   private static OptionalInt invokeWriteAt(PowerType type, class_2960 powerId, Object cfg, PowerContainer holder, int slot, int value) {
      return type.writeResourceAt(powerId, cfg, holder, slot, value);
   }

   private static int invokeIndexOf(PowerType type, class_2960 powerId, Object cfg, PowerContainer holder, int value) {
      return type.resourceIndexOf(powerId, cfg, holder, value);
   }

   private static int invokeSize(PowerType type, class_2960 powerId, Object cfg, @Nullable PowerContainer holder) {
      return type.resourceSize(powerId, cfg, holder);
   }

   private static OptionalInt invokeBound(PowerType type, class_2960 powerId, Object cfg, @Nullable PowerContainer holder, boolean max) {
      return type.resourceBound(powerId, cfg, holder, max);
   }

   public interface ClientCooldownLookup {
      int remaining(class_1297 var1, class_2960 var2);
   }

   // AEA: fallback for powers owned by Apoli 2.9.2 itself (apoli:resource, apoli:cooldown, ...).
   private static OptionalInt apaceWrite(PowerContainer holder, class_2960 powerId, int value) {
      if (!(holder instanceof PowerContainerImpl impl)) {
         return OptionalInt.empty();
      }
      OptionalInt before = impl.getAuxInt(powerId);
      if (before.isEmpty()) {
         return OptionalInt.empty();
      }
      int target = value;
      if (ModifyResourceChangePower.inUse()) {
         target = ModifyResourceChangePower.adjust(holder, powerId, before.getAsInt(), value);
      }
      impl.setAuxInt(powerId, target);
      OptionalInt written = impl.getAuxInt(powerId);
      if (written.isPresent()) {
         ApoliCriteria.resourceChanged(holder.rawOwner(), powerId, written.getAsInt());
      }
      return written;
   }

   private static OptionalInt apaceBound(@Nullable PowerContainer holder, class_2960 powerId, boolean max) {
      if (holder == null) {
         return OptionalInt.empty();
      }
      try {
         class_1297 owner = holder.rawOwner();
         io.github.apace100.apoli.component.PowerHolderComponent comp = io.github.apace100.apoli.component.PowerHolderComponent.KEY.getNullable(owner);
         if (comp == null) {
            return OptionalInt.empty();
         }
         io.github.apace100.apoli.power.PowerType<?> pt = io.github.apace100.apoli.power.PowerTypeRegistry.get(powerId);
         io.github.apace100.apoli.power.Power p = comp.getPower(pt);
         if (p instanceof io.github.apace100.apoli.power.VariableIntPower v) {
            return OptionalInt.of(max ? v.getMax() : v.getMin());
         }
         if (p instanceof io.github.apace100.apoli.power.CooldownPower c) {
            return OptionalInt.of(max ? c.cooldownDuration : 0);
         }
      } catch (RuntimeException ignored) {
      }
      return OptionalInt.empty();
   }
}
