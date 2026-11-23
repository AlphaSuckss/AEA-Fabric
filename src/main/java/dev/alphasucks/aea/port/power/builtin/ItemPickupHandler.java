package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PreventItemPickupPower.Config;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ItemPickupHandler {
   private static final Comparator<ItemPickupHandler.Match<?>> BY_PRIORITY = Comparator.comparingInt((ItemPickupHandler.Match<?> match) -> match.priority()).reversed();

   private ItemPickupHandler() {
   }

   public static boolean watches(@Nullable class_1297 holder) {
      if (holder == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(holder);
      return container != null && !container.isEmpty()
         ? !container.powersOfType(ApoliIds.PREVENT_ITEM_PICKUP).isEmpty() || !container.powersOfType(ApoliIds.ACTION_ON_ITEM_PICKUP).isEmpty()
         : false;
   }

   public static boolean hasRoom(class_1657 player, class_1799 stack) {
      if (player.method_31549().field_7477) {
         return true;
      }

      class_1661 inventory = player.method_31548();
      return inventory.method_7390(stack) >= 0 || inventory.method_7376() >= 0;
   }

   public static boolean prevents(class_1542 item, class_1297 holder) {
      class_1937 level = holder.method_37908();
      class_1309 living = holder instanceof class_1309 le ? le : null;
      class_1297 thrower = item.method_24921();
      class_1799 stack = item.method_6983();
      List<ItemPickupHandler.Match<Config>> matches = new ArrayList(2);
      PowerLookup.forEachEntry(holder, ApoliIds.PREVENT_ITEM_PICKUP, Config.class, (id, cfgx) -> {
         if (applies(cfgx.itemCondition(), cfgx.bientityCondition(), stack, level, living, thrower, holder)) {
            matches.add(new ItemPickupHandler.Match<>(id, cfgx, cfgx.priority()));
         }
      });
      if (matches.isEmpty()) {
         return false;
      }

      if (matches.size() > 1) {
         matches.sort(BY_PRIORITY);
      }

      for (int i = 0; i < matches.size(); i++) {
         ItemPickupHandler.Match<Config> match = (ItemPickupHandler.Match<Config>)matches.get(i);
         Config cfg = match.config();
         boolean attributed = PowerCause.push(holder, match.powerId());

         try {
            if (cfg.itemAction().isPresent()) {
               ((ItemAction)cfg.itemAction().get()).run(new ItemCtx(item.method_6983(), level, living, item::method_6979));
            }

            if (cfg.bientityActionThrower().isPresent()) {
               ((BiEntityAction)cfg.bientityActionThrower().get()).run(BiEntityCtx.of(thrower, holder, level));
            }

            if (cfg.bientityActionItem().isPresent()) {
               ((BiEntityAction)cfg.bientityActionItem().get()).run(BiEntityCtx.of(holder, item, level));
            }
         } finally {
            if (attributed) {
               PowerCause.pop();
            }
         }
      }

      return true;
   }

   @Nullable
   public static ItemPickupHandler.Pending before(class_1542 item, class_1297 holder, boolean runItemActions) {
      class_1937 level = holder.method_37908();
      class_1309 living = holder instanceof class_1309 le ? le : null;
      class_1297 thrower = item.method_24921();
      class_1799 stack = item.method_6983();
      List<ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config>> matches = new ArrayList(2);
      PowerLookup.forEachEntry(holder, ApoliIds.ACTION_ON_ITEM_PICKUP, dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config.class, (id, cfg) -> {
         if (applies(cfg.itemCondition(), cfg.bientityCondition(), stack, level, living, thrower, holder)) {
            matches.add(new ItemPickupHandler.Match<>(id, cfg, cfg.priority()));
         }
      });
      if (matches.isEmpty()) {
         return null;
      }

      if (matches.size() > 1) {
         matches.sort(BY_PRIORITY);
      }

      if (runItemActions) {
         for (int i = 0; i < matches.size(); i++) {
            ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config> match = (ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config>)matches.get(
               i
            );
            if (!match.config().itemAction().isEmpty()) {
               boolean attributed = PowerCause.push(holder, match.powerId());

               try {
                  ((ItemAction)match.config().itemAction().get()).run(new ItemCtx(item.method_6983(), level, living, item::method_6979));
               } finally {
                  if (attributed) {
                     PowerCause.pop();
                  }
               }
            }
         }
      }

      return new ItemPickupHandler.Pending(item, thrower, matches, item.method_6983().method_7947());
   }

   public static void after(ItemPickupHandler.Pending pending, class_1297 holder) {
      class_1937 level = holder.method_37908();
      List<ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config>> matches = pending.actions();

      for (int i = 0; i < matches.size(); i++) {
         ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config> match = (ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config>)matches.get(
            i
         );
         if (!match.config().bientityAction().isEmpty()) {
            boolean attributed = PowerCause.push(holder, match.powerId());

            try {
               ((BiEntityAction)match.config().bientityAction().get()).run(BiEntityCtx.of(pending.thrower(), holder, level));
            } finally {
               if (attributed) {
                  PowerCause.pop();
               }
            }
         }
      }
   }

   private static boolean applies(
      Optional<ItemCondition> itemCondition,
      Optional<BiEntityCondition> bientityCondition,
      class_1799 stack,
      class_1937 level,
      @Nullable class_1309 living,
      @Nullable class_1297 thrower,
      class_1297 holder
   ) {
      return itemCondition.isPresent() && !((ItemCondition)itemCondition.get()).test(new ItemCtx(stack, level, living))
         ? false
         : bientityCondition.isEmpty() || ((BiEntityCondition)bientityCondition.get()).test(BiEntityCtx.of(thrower, holder, level));
   }

   public record Match<C>(class_2960 powerId, C config, int priority) {
   }

   public record Pending(
      class_1542 item,
      @Nullable class_1297 thrower,
      List<ItemPickupHandler.Match<dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower.Config>> actions,
      int count
   ) {
      public boolean taken() {
         return this.item.method_31481() || this.item.method_6983().method_7947() < this.count;
      }
   }
}
