package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.ContainerType;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.network.payload.PowerInventoryS2C;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import net.minecraft.class_1262;
import net.minecraft.class_1277;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;

public final class InventoryPower extends PowerType<InventoryPower.Config> {
   private static final class_2561 DEFAULT_TITLE = class_2561.method_43471("container.inventory");
   private static final Map<UUID, Map<class_2960, class_1277>> OPEN_CONTAINERS = new HashMap();
   private static BiFunction<class_1309, class_2960, class_2487> CLIENT_LOOKUP = (holder, powerId) -> null;

   @Override
   public MapCodec<InventoryPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TextComponent.CODEC.optionalFieldOf("title", DEFAULT_TITLE).forGetter(InventoryPower.Config::title),
               ContainerType.CODEC.optionalFieldOf("container_type", ContainerType.DROPPER).forGetter(InventoryPower.Config::containerType),
               Codec.BOOL.optionalFieldOf("drop_on_death", false).forGetter(InventoryPower.Config::dropOnDeath),
               LoggedOptionalField.strict("drop_on_death_filter", ItemCondition.CODEC).forGetter(InventoryPower.Config::dropOnDeathFilter),
               Codec.BOOL.optionalFieldOf("recoverable", true).forGetter(InventoryPower.Config::recoverable),
               Key.CODEC.optionalFieldOf("key", Key.DEFAULT_PRIMARY).forGetter(InventoryPower.Config::key)
            )
            .apply(i, InventoryPower.Config::new)
      );
   }

   private static class_1277 liveContainer(UUID owner, class_2960 powerId) {
      Map<class_2960, class_1277> byPower = (Map<class_2960, class_1277>)OPEN_CONTAINERS.get(owner);
      return byPower == null ? null : (class_1277)byPower.get(powerId);
   }

   private static void releaseContainer(UUID owner, class_2960 powerId) {
      Map<class_2960, class_1277> byPower = (Map<class_2960, class_1277>)OPEN_CONTAINERS.get(owner);
      if (byPower != null) {
         byPower.remove(powerId);
         if (byPower.isEmpty()) {
            OPEN_CONTAINERS.remove(owner);
         }
      }
   }

   public static void onPlayerLeave(UUID owner) {
      OPEN_CONTAINERS.remove(owner);
   }

   public void open(class_2960 powerId, InventoryPower.Config cfg, class_3222 player, PowerContainerImpl container) {
      final UUID owner = player.method_5667();
      class_1277 inv = new class_1277(cfg.containerType().slots()) {
         @Override
         public void method_5432(class_1657 closing) {
            InventoryPower.releaseContainer(owner, powerId);
            super.method_5432(closing);
         }
      };
      fill(inv, container.getAuxNbt(powerId));
      inv.method_5489(changed -> container.setAuxNbt(powerId, save(inv)));
      ((Map)OPEN_CONTAINERS.computeIfAbsent(owner, k -> new HashMap(2))).put(powerId, inv);
      player.method_17355(cfg.containerType().menuProvider(cfg.title(), inv));
   }

   public void onRemoved(class_2960 powerId, InventoryPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.allPowers().contains(powerId)) {
         if (holder instanceof PowerContainerImpl impl) {
            class_2487 stored = impl.getAuxNbt(powerId);
            if (stored != null && cfg.recoverable() && holder.owner() instanceof class_3222 player) {
               class_1277 inv = load(stored, cfg.containerType().slots());

               for (int slot = 0; slot < inv.method_5439(); slot++) {
                  class_1799 stack = inv.method_5438(slot);
                  if (!stack.method_7960()) {
                     player.method_31548().method_7398(stack);
                  }
               }
            }

            impl.removeAux(powerId);
         }
      }
   }

   public void dropOnDeath(class_2960 powerId, InventoryPower.Config cfg, class_1309 dead, PowerContainerImpl container, class_3218 level) {
      List<InventoryPower.DeathDrop> drops = new ArrayList();
      collectDeathDrops(powerId, cfg, dead, container, level, true, drops);

      for (int i = 0; i < drops.size(); i++) {
         dead.method_5775(((InventoryPower.DeathDrop)drops.get(i)).stack());
      }
   }

   public static List<InventoryPower.DeathDrop> deathDrops(class_1309 dead, boolean take) {
      if (dead.method_37908() instanceof class_3218 level) {
         if (PowerContainer.of(dead) instanceof PowerContainerImpl impl && !impl.isEmpty()) {
            List<InventoryPower.DeathDrop> drops = null;

            for (class_2960 powerId : impl.allPowers()) {
               if (!impl.isSuppressed(powerId)) {
                  Power loaded = ApoliPowers.get(powerId);
                  if (loaded != null && loaded.config() instanceof InventoryPower.Config cfg && cfg.dropOnDeath()) {
                     if (drops == null) {
                        drops = new ArrayList(4);
                     }

                     collectDeathDrops(powerId, cfg, dead, impl, level, take, drops);
                  }
               }
            }

            return drops == null ? List.of() : drops;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static void collectDeathDrops(
      class_2960 powerId,
      InventoryPower.Config cfg,
      class_1309 dead,
      PowerContainerImpl container,
      class_3218 level,
      boolean take,
      List<InventoryPower.DeathDrop> out
   ) {
      class_2487 stored = container.getAuxNbt(powerId);
      if (stored != null) {
         class_1277 live = liveContainer(dead.method_5667(), powerId);
         class_1277 inv = live != null ? live : load(stored, cfg.containerType().slots());
         boolean changed = false;

         for (int slot = 0; slot < inv.method_5439(); slot++) {
            class_1799 stack = inv.method_5438(slot);
            if (!stack.method_7960()
               && (!cfg.dropOnDeathFilter().isPresent() || ((ItemCondition)cfg.dropOnDeathFilter().get()).test(new ItemCtx(stack, level, dead)))) {
               if (!take) {
                  out.add(new InventoryPower.DeathDrop(powerId, slot, stack.method_7972()));
               } else {
                  out.add(new InventoryPower.DeathDrop(powerId, slot, stack));
                  inv.method_5447(slot, class_1799.field_8037);
                  changed = true;
               }
            }
         }

         if (changed && live == null) {
            container.setAuxNbt(powerId, save(inv));
         }
      }
   }

   public static class_1799 restore(class_1309 holder, class_2960 powerId, int slot, class_1799 stack) {
      if (stack.method_7960()) {
         return class_1799.field_8037;
      }

      if (holder.method_37908() instanceof class_3218 level
         && PowerContainer.of(holder) instanceof PowerContainerImpl impl
         && impl.allPowers().contains(powerId)) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null && loaded.config() instanceof InventoryPower.Config cfg) {
            class_1277 live = liveContainer(holder.method_5667(), powerId);
            class_1277 inv = live != null ? live : load(impl.getAuxNbt(powerId), cfg.containerType().slots());
            if (slot >= 0 && slot < inv.method_5439() && inv.method_5438(slot).method_7960()) {
               inv.method_5447(slot, stack);
               if (live == null) {
                  impl.setAuxNbt(powerId, save(inv));
               }

               return class_1799.field_8037;
            } else {
               return stack;
            }
         } else {
            return stack;
         }
      } else {
         return stack;
      }
   }

   public static void syncAll(class_3222 player) {
      if (PowerContainer.of(player) instanceof PowerContainerImpl impl) {
         for (class_2960 powerId : impl.allPowers()) {
            class_2487 stored = impl.getAuxNbt(powerId);
            if (stored != null) {
               Power loaded = ApoliPowers.get(powerId);
               if (loaded != null && loaded.config() instanceof InventoryPower.Config) {
                  ApoliNetwork.sendPowerInventory(player, new PowerInventoryS2C(powerId, stored));
               }
            }
         }
      }
   }

   public static void setClientLookup(BiFunction<class_1309, class_2960, class_2487> lookup) {
      CLIENT_LOOKUP = lookup;
   }

   private static class_1277 clientContainer(class_1309 holder, class_2960 powerId) {
      if (!holder.method_37908().method_8608()) {
         return null;
      } else {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null && loaded.config() instanceof InventoryPower.Config cfg) {
            class_2487 stored = (class_2487)CLIENT_LOOKUP.apply(holder, powerId);
            return stored == null ? null : load(stored, cfg.containerType().slots());
         } else {
            return null;
         }
      }
   }

   public static class_1277 openContainer(class_1309 holder, class_2960 powerId) {
      if (holder.method_37908().method_8608()) {
         return clientContainer(holder, powerId);
      }

      if (PowerContainer.of(holder) instanceof PowerContainerImpl impl) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null && loaded.config() instanceof InventoryPower.Config cfg) {
            class_1277 live = liveContainer(holder.method_5667(), powerId);
            return live != null ? live : load(impl.getAuxNbt(powerId), cfg.containerType().slots());
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void saveContainer(class_1309 holder, class_2960 powerId, class_1277 container) {
      if (!holder.method_37908().method_8608() && PowerContainer.of(holder) instanceof PowerContainerImpl impl) {
         impl.setAuxNbt(powerId, save(container));
         if (container == liveContainer(holder.method_5667(), powerId)) {
            container.method_5431();
         }
      }
   }

   private static class_1277 load(class_2487 stored, int slots) {
      class_1277 inv = new class_1277(slots);
      fill(inv, stored);
      return inv;
   }

   private static void fill(class_1277 inv, class_2487 stored) {
      if (stored != null) {
         int slots = inv.method_5439();
         class_2371<class_1799> items = class_2371.method_10213(slots, class_1799.field_8037);
         class_1262.method_5429(stored, items);

         for (int i = 0; i < slots; i++) {
            inv.method_5447(i, items.get(i));
         }
      }
   }

   private static class_2487 save(class_1277 inv) {
      class_2371<class_1799> items = class_2371.method_10213(inv.method_5439(), class_1799.field_8037);

      for (int i = 0; i < inv.method_5439(); i++) {
         items.set(i, inv.method_5438(i));
      }

      class_2487 tag = new class_2487();
      class_1262.method_5426(tag, items);
      return tag;
   }

   public record Config(
      class_2561 title, ContainerType containerType, boolean dropOnDeath, Optional<ItemCondition> dropOnDeathFilter, boolean recoverable, Key key
   ) {
   }

   public record DeathDrop(class_2960 power, int slot, class_1799 stack) {
      public static final Codec<InventoryPower.DeathDrop> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               class_2960.field_25139.fieldOf("power").forGetter(InventoryPower.DeathDrop::power),
               Codec.INT.fieldOf("slot").forGetter(InventoryPower.DeathDrop::slot),
               class_1799.field_24671.fieldOf("stack").forGetter(InventoryPower.DeathDrop::stack)
            )
            .apply(i, InventoryPower.DeathDrop::new)
      );
   }
}
