package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ActionOnDeathPower.Config;
import java.util.List;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class DeathHandler {
   private DeathHandler() {
   }

   public static void onDeath(class_1309 dead, class_1282 source) {
      if (dead.method_37908() instanceof class_3218 level) {
         class_1309 killer = source.method_5529() instanceof class_1309 living ? living : null;
         if (PowerContainer.of(dead) instanceof PowerContainerImpl impl) {
            fireDeathPowers(dead, killer, source, level, impl);
         }

         if (killer != null) {
            fireActionOnKill(killer, dead, source, level);
         }
      }
   }

   private static void fireDeathPowers(class_1309 dead, @Nullable class_1309 killer, class_1282 source, class_3218 level, PowerContainerImpl impl) {
      for (class_2960 powerId : impl.allPowers()) {
         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null && !impl.isSuppressed(powerId)) {
            PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
            if (type instanceof ActionOnDeathPower aod && loaded.config() instanceof Config cfg) {
               if (!loaded.condition().isPresent() || ((EntityCondition)loaded.condition().get()).test(new EntityCtx(dead, level))) {
                  aod.onDeath(cfg, dead, killer, source, level);
               }
            } else if (type instanceof InventoryPower inv
               && loaded.config() instanceof dev.alphasucks.aea.port.power.builtin.InventoryPower.Config cfg
               && cfg.dropOnDeath()) {
               inv.dropOnDeath(powerId, cfg, dead, impl, level);
            } else if (type instanceof PowerStoragePower
               && loaded.config() instanceof dev.alphasucks.aea.port.power.builtin.PowerStoragePower.Config cfg
               && cfg.dropOnDeath()) {
               PowerStoragePower.clear(impl, powerId);
            }
         }
      }
   }

   private static void fireActionOnKill(class_1309 killer, class_1309 victim, class_1282 source, class_3218 level) {
      if (PowerContainer.of(killer) instanceof PowerContainerImpl impl) {
         List<class_2960> powers = impl.powersOfType(ApoliIds.ACTION_ON_KILL);
         if (!powers.isEmpty()) {
            EntityCtx killerCtx = null;

            for (int i = 0; i < powers.size(); i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (!impl.isSuppressed(powerId)) {
                  Power loaded = ApoliPowers.get(powerId);
                  if (loaded != null
                     && loaded.config() instanceof dev.alphasucks.aea.port.power.builtin.ActionOnKillPower.Config cfg
                     && PowerTypeRegistry.get(loaded.typeId()) instanceof ActionOnKillPower aok) {
                     if (loaded.condition().isPresent()) {
                        if (killerCtx == null) {
                           killerCtx = new EntityCtx(killer, level);
                        }

                        if (!((EntityCondition)loaded.condition().get()).test(killerCtx)) {
                           continue;
                        }
                     }

                     aok.onKill(powerId, cfg, killer, victim, source, level, impl);
                  }
               }
            }
         }
      }
   }
}
