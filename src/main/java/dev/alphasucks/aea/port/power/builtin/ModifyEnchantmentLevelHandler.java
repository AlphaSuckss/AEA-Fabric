package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.mixin.enchantment.EnchantmentSlotsAccessor;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_2499;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class ModifyEnchantmentLevelHandler {
   private static final class_1304[] SLOTS = class_1304.values();
   private static final ModifyEnchantmentLevelHandler.Owner[] NONE = new ModifyEnchantmentLevelHandler.Owner[0];
   private static ModifyEnchantmentLevelHandler.Owner[] server = NONE;
   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner client;
   private static class_1309[] scan = new class_1309[4];
   private static int scanned;
   @Nullable
   private static class_1799 scopeStack;
   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner scopeOwner;
   @Nullable
   private static Thread scopeThread;

   private ModifyEnchantmentLevelHandler() {
   }

   public static void beginScan() {
      scanned = 0;
   }

   public static void scan(class_1297 entity, PowerContainer container) {
      if (entity instanceof class_1309 living) {
         if (!container.powersOfType(ApoliIds.MODIFY_ENCHANTMENT_LEVEL).isEmpty()) {
            if (scanned == scan.length) {
               scan = (class_1309[])Arrays.copyOf(scan, scanned * 2);
            }

            scan[scanned++] = living;
         }
      }
   }

   public static void endScan() {
      ModifyEnchantmentLevelHandler.Owner[] current = server;
      boolean same = current.length == scanned;

      for (int i = 0; same && i < scanned; i++) {
         same = current[i].entity == scan[i];
      }

      if (!same) {
         ModifyEnchantmentLevelHandler.Owner[] next = scanned == 0 ? NONE : new ModifyEnchantmentLevelHandler.Owner[scanned];

         for (int i = 0; i < scanned; i++) {
            ModifyEnchantmentLevelHandler.Owner kept = find(current, scan[i]);
            next[i] = kept != null ? kept : new ModifyEnchantmentLevelHandler.Owner(scan[i]);
         }

         server = next;
      }

      Arrays.fill(scan, 0, scanned, null);
      endToolScope();
   }

   public static void clearServer() {
      server = NONE;
      endToolScope();
   }

   public static void clientTick(@Nullable class_1309 player) {
      if (player == null) {
         client = null;
      } else {
         PowerContainer container = PowerContainer.of(player);
         if (container == null || container.powersOfType(ApoliIds.MODIFY_ENCHANTMENT_LEVEL).isEmpty()) {
            client = null;
         } else if (client == null || client.entity != player) {
            client = new ModifyEnchantmentLevelHandler.Owner(player);
         }
      }
   }

   public static boolean inactive() {
      return server.length == 0 && client == null;
   }

   public static boolean isHolder(class_1309 entity) {
      return !inactive() && stateOf(entity) != null;
   }

   public static int level(class_1799 stack, class_1887 enchantment, int base) {
      if (inactive()) {
         return base;
      }

      ModifyEnchantmentLevelHandler.Owner owner = resolve(stack);
      return owner == null ? base : level(owner, stack, enchantment, base);
   }

   public static class_2499 enchantmentTags(class_1799 stack, class_2499 base) {
      if (inactive()) {
         return base;
      }

      ModifyEnchantmentLevelHandler.Owner owner = resolve(stack);
      return owner == null ? base : tags(owner, stack, base);
   }

   public static int emptySlotLevel(class_1309 living, class_1887 enchantment, int base) {
      if (inactive()) {
         return base;
      }

      ModifyEnchantmentLevelHandler.Owner owner = stateOf(living);
      if (owner == null) {
         return base;
      }

      for (class_1304 slot : ((EnchantmentSlotsAccessor)enchantment).apoli$getSlots()) {
         if (living.method_6118(slot).method_7960()) {
            return Math.max(base, level(owner, class_1799.field_8037, enchantment, 0));
         }
      }

      return base;
   }

   public static float emptyDamageBonus(class_1309 living, class_1310 mobType) {
      if (inactive()) {
         return 0.0F;
      }

      ModifyEnchantmentLevelHandler.Owner owner = stateOf(living);
      if (owner != null && !owner.busy && refresh(owner)) {
         float bonus = 0.0F;

         for (int i = 0; i < owner.resolved.size(); i++) {
            class_1887 enchantment = (class_1887)owner.resolved.get(i);
            if (enchantment != null && !seenBefore(owner, i, enchantment)) {
               int level = level(owner, class_1799.field_8037, enchantment, 0);
               if (level > 0) {
                  bonus += enchantment.method_8196(level, mobType);
               }
            }
         }

         return bonus;
      } else {
         return 0.0F;
      }
   }

   public static int levelInContext(@Nullable class_1309 context, class_1799 stack, class_1887 enchantment, int gameplayLevel) {
      if (!inactive() && resolve(stack) == null) {
         ModifyEnchantmentLevelHandler.Owner owner = context == null ? null : stateOf(context);
         return owner == null ? gameplayLevel : level(owner, stack, enchantment, gameplayLevel);
      } else {
         return gameplayLevel;
      }
   }

   public static Map<class_1887, Integer> enchantmentsInContext(@Nullable class_1309 context, class_1799 stack, Map<class_1887, Integer> raw) {
      if (inactive()) {
         return raw;
      }

      ModifyEnchantmentLevelHandler.Owner owner = resolve(stack);
      if (owner == null && context != null) {
         owner = stateOf(context);
      }

      if (owner != null && !owner.busy && refresh(owner)) {
         Map<class_1887, Integer> out = null;

         for (int i = 0; i < owner.resolved.size(); i++) {
            class_1887 enchantment = (class_1887)owner.resolved.get(i);
            if (enchantment != null && !seenBefore(owner, i, enchantment)) {
               int before = (Integer)raw.getOrDefault(enchantment, 0);
               int after = level(owner, stack, enchantment, before);
               if (after != before) {
                  if (out == null) {
                     out = new LinkedHashMap(raw);
                  }

                  if (after > 0) {
                     out.put(enchantment, after);
                  } else {
                     out.remove(enchantment);
                  }
               }
            }
         }

         return out == null ? raw : out;
      } else {
         return raw;
      }
   }

   public static int rawLevel(class_1799 stack, class_1887 enchantment) {
      if (stack.method_7960()) {
         return 0;
      }

      class_2960 id = class_7923.field_41176.method_10221(enchantment);
      class_2499 list = stack.method_7921();
      int index = id == null ? -1 : indexOf(list, id);
      return index < 0 ? 0 : class_1890.method_37424(list.method_10602(index));
   }

   public static boolean matchesSlot(class_1887 enchantment, class_1304 slot) {
      for (class_1304 candidate : ((EnchantmentSlotsAccessor)enchantment).apoli$getSlots()) {
         if (candidate == slot) {
            return true;
         }
      }

      return false;
   }

   private static class_2499 tags(ModifyEnchantmentLevelHandler.Owner owner, class_1799 stack, class_2499 base) {
      if (!owner.busy && refresh(owner)) {
         class_2499 out = null;

         for (int i = 0; i < owner.resolved.size(); i++) {
            class_1887 enchantment = (class_1887)owner.resolved.get(i);
            if (enchantment != null && !seenBefore(owner, i, enchantment)) {
               class_2960 id = ((ModifyEnchantmentLevelPower.Config)owner.active.get(i)).enchantment();
               class_2499 list = out == null ? base : out;
               int index = indexOf(list, id);
               int before = index < 0 ? 0 : class_1890.method_37424(list.method_10602(index));
               int after = level(owner, stack, enchantment, before);
               if (after != before) {
                  if (out == null) {
                     out = base.method_10612();
                  }

                  if (after <= 0) {
                     if (index >= 0) {
                        out.method_10536(index);
                     }
                  } else if (index >= 0) {
                     class_1890.method_37425(out.method_10602(index), after);
                  } else {
                     out.add(class_1890.method_37426(id, after));
                  }
               }
            }
         }

         return out == null ? base : out;
      } else {
         return base;
      }
   }

   private static int indexOf(class_2499 list, class_2960 id) {
      for (int i = 0; i < list.size(); i++) {
         if (id.equals(class_1890.method_37427(list.method_10602(i)))) {
            return i;
         }
      }

      return -1;
   }

   private static int level(ModifyEnchantmentLevelHandler.Owner owner, class_1799 stack, class_1887 enchantment, int base) {
      if (!owner.busy && refresh(owner)) {
         owner.busy = true;

         try {
            List<AttributeModifier> mods = null;

            for (int i = 0; i < owner.active.size(); i++) {
               ModifyEnchantmentLevelPower.Config cfg = (ModifyEnchantmentLevelPower.Config)owner.active.get(i);
               if (!cfg.allModifiers().isEmpty() && owner.resolved.get(i) == enchantment && cfg.appliesTo(stack, owner.entity)) {
                  if (mods == null) {
                     mods = cfg.allModifiers();
                  } else {
                     if (mods != owner.combined) {
                        owner.combined.clear();
                        owner.combined.addAll(mods);
                        mods = owner.combined;
                     }

                     owner.combined.addAll(cfg.allModifiers());
                  }
               }
            }

            return mods == null ? base : class_3532.method_15340((int)Math.round(AttributeModifierHelper.apply(base, mods, owner.entity)), 0, 255);
         } finally {
            owner.busy = false;
         }
      } else {
         return base;
      }
   }

   private static boolean refresh(ModifyEnchantmentLevelHandler.Owner owner) {
      long now = owner.entity.method_37908().method_8510();
      if (owner.tick != now) {
         owner.tick = now;
         owner.active.clear();
         owner.resolved.clear();
         owner.busy = true;

         try {
            PowerLookup.collect(owner.entity, ApoliIds.MODIFY_ENCHANTMENT_LEVEL, ModifyEnchantmentLevelPower.Config.class, owner.active);

            for (int i = 0; i < owner.active.size(); i++) {
               owner.resolved
                  .add((class_1887)class_7923.field_41176.method_17966(((ModifyEnchantmentLevelPower.Config)owner.active.get(i)).enchantment()).orElse(null));
            }
         } finally {
            owner.busy = false;
         }
      }

      return !owner.active.isEmpty();
   }

   private static boolean seenBefore(ModifyEnchantmentLevelHandler.Owner owner, int index, class_1887 enchantment) {
      for (int j = 0; j < index; j++) {
         if (owner.resolved.get(j) == enchantment) {
            return true;
         }
      }

      return false;
   }

   public static boolean beginToolScope(@Nullable class_1297 entity, class_1799 tool) {
      if (!inactive() && scopeThread == null && entity instanceof class_1309 living) {
         ModifyEnchantmentLevelHandler.Owner owner = stateOf(living);
         if (owner == null) {
            return false;
         }

         scopeStack = tool;
         scopeOwner = owner;
         scopeThread = Thread.currentThread();
         return true;
      } else {
         return false;
      }
   }

   public static void endToolScope() {
      scopeStack = null;
      scopeOwner = null;
      scopeThread = null;
   }

   public static boolean inToolScope(class_1799 stack) {
      return scopeStack == stack && scopeThread == Thread.currentThread();
   }

   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner resolve(class_1799 stack) {
      ModifyEnchantmentLevelHandler.Owner owner = ownerOf(stack);
      if (owner == null && inToolScope(stack)) {
         owner = scopeOwner;
      }

      return owner;
   }

   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner ownerOf(class_1799 stack) {
      if (stack.method_7960()) {
         return null;
      }

      ModifyEnchantmentLevelHandler.Owner[] owners = server;

      for (ModifyEnchantmentLevelHandler.Owner owner : owners) {
         if (wears(owner.entity, stack)) {
            return owner;
         }
      }

      ModifyEnchantmentLevelHandler.Owner local = client;
      return local != null && wears(local.entity, stack) ? local : null;
   }

   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner stateOf(class_1309 entity) {
      ModifyEnchantmentLevelHandler.Owner[] owners = server;

      for (ModifyEnchantmentLevelHandler.Owner owner : owners) {
         if (owner.entity == entity) {
            return owner;
         }
      }

      ModifyEnchantmentLevelHandler.Owner local = client;
      return local != null && local.entity == entity ? local : null;
   }

   @Nullable
   private static ModifyEnchantmentLevelHandler.Owner find(ModifyEnchantmentLevelHandler.Owner[] owners, class_1309 entity) {
      for (ModifyEnchantmentLevelHandler.Owner owner : owners) {
         if (owner.entity == entity) {
            return owner;
         }
      }

      return null;
   }

   private static boolean wears(class_1309 entity, class_1799 stack) {
      if (entity.method_31481()) {
         return false;
      }

      for (class_1304 slot : SLOTS) {
         if (entity.method_6118(slot) == stack) {
            return true;
         }
      }

      return false;
   }

   private static final class Owner {
      final class_1309 entity;
      final List<ModifyEnchantmentLevelPower.Config> active = new ArrayList(2);
      final List<class_1887> resolved = new ArrayList(2);
      final List<AttributeModifier> combined = new ArrayList(4);
      long tick = Long.MIN_VALUE;
      boolean busy;

      Owner(class_1309 entity) {
         this.entity = entity;
      }
   }
}
