package dev.alphasucks.aea.port.keybind;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.network.payload.ForceKeyS2C;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerKeys;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import dev.alphasucks.aea.port.power.builtin.MultiplePower;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PreventKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.TogglePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower.Config;
import dev.alphasucks.aea.port.power.builtin.MultiplePower.Cfg;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class KeyDispatch {
   private KeyDispatch() {
   }

   public static void force(class_1297 entity, String key, int duration, boolean release) {
      if (entity != null && entity.method_37908() instanceof class_3218) {
         if (key != null && !key.isEmpty()) {
            if (release || !PreventKeyPressPower.blocksForcedKeys(entity, key)) {
               if (entity instanceof class_3222 player) {
                  ApoliNetwork.sendForceKey(player, new ForceKeyS2C(key, Math.max(1, duration), release));
               }

               if (release) {
                  HeldKeys.release(entity.method_5667(), key);
                  if (!(entity instanceof class_3222)) {
                     releaseKey(entity, key);
                  }
               } else {
                  HeldKeys.force(entity.method_5667(), key, Math.max(1, duration));
                  if (!(entity instanceof class_3222)) {
                     press(entity, key);
                  }
               }
            }
         }
      }
   }

   public static void releaseExpired(MinecraftServer server, UUID entityId, String key) {
      ActionOnKeyPressPower active = keyPressType();
      if (active != null && !active.pressedPowers(entityId).isEmpty()) {
         class_1297 entity = EntitySetPower.resolveEntity(server, entityId);
         if (entity == null) {
            active.forget(entityId);
         } else if (!(entity instanceof class_3222)) {
            releaseKey(entity, key);
         }
      }
   }

   public static void releaseKey(class_1297 entity, String key) {
      ActionOnKeyPressPower active = keyPressType();
      if (active != null) {
         List<class_2960> pressed = active.pressedPowers(entity.method_5667());

         for (int i = 0; i < pressed.size(); i++) {
            Power loaded = ApoliPowers.get((class_2960)pressed.get(i));
            if (loaded != null && loaded.config() instanceof Config cfg && cfg.key().key().equals(key)) {
               releasePower(entity, (class_2960)pressed.get(i));
            }
         }
      }
   }

   public static boolean releasePower(class_1297 entity, class_2960 powerId) {
      if (entity != null && entity.method_37908() instanceof class_3218) {
         ActionOnKeyPressPower active = keyPressType();
         if (active == null) {
            return false;
         }

         Power loaded = ApoliPowers.get(powerId);
         if (loaded != null && loaded.config() instanceof Config cfg) {
            PowerContainer container = PowerContainer.of(entity);
            if (container == null) {
               return false;
            }

            boolean usable = container.hasPower(powerId) && !container.isSuppressed(powerId);
            return active.release(powerId, cfg, container, usable);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   private static ActionOnKeyPressPower keyPressType() {
      return PowerTypeRegistry.get(ApoliIds.ACTION_ON_KEY_PRESS) instanceof ActionOnKeyPressPower active ? active : null;
   }

   public static void tickForcedNonPlayer(class_1297 entity) {
      if (!(entity instanceof class_3222)) {
         Set<String> forced = HeldKeys.forcedKeys(entity.method_5667());
         if (!forced.isEmpty()) {
            for (String key : forced) {
               dispatch(entity, key, true);
            }
         }
      }
   }

   public static int press(class_1297 entity, String key) {
      return dispatch(entity, key, false);
   }

   public static boolean blocked(class_1297 entity, String key) {
      if (!PreventKeyPressPower.any(entity)) {
         return false;
      } else {
         return HeldKeys.forcedHeld(entity.method_5667(), key) ? PreventKeyPressPower.blocksForcedKeys(entity, key) : PreventKeyPressPower.blocks(entity, key);
      }
   }

   public static boolean pressPower(class_1297 entity, class_2960 powerId, @Nullable String key) {
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         PowerContainer container = PowerContainer.of(entity);
         if (container == null) {
            return false;
         }

         if (container.isSuppressed(powerId)) {
            return false;
         }

         Power loaded = ApoliPowers.get(powerId);
         if (loaded == null) {
            return false;
         }

         if (key != null && blocked(entity, key)) {
            return false;
         }

         EntityCtx ctx = new EntityCtx(entity, level);
         if (loaded.condition().isPresent() && !((EntityCondition)loaded.condition().get()).test(ctx)) {
            return false;
         }

         class_3222 player = entity instanceof class_3222 sp ? sp : null;
         if (!activate(container, player, powerId, loaded, null, false)) {
            return false;
         }

         releasePower(entity, powerId);
         if (loaded.config() instanceof Cfg multiple) {
            List<class_2960> subs = multiple.subPowerIds();

            for (int i = 0; i < subs.size(); i++) {
               releasePower(entity, (class_2960)subs.get(i));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static int dispatch(class_1297 entity, String key, boolean continuousOnly) {
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> candidates = PowerKeys.heldPowersUsingKey(container, key);
            if (candidates.isEmpty()) {
               return 0;
            }

            if (blocked(entity, key)) {
               return 0;
            }

            EntityCtx ctx = new EntityCtx(entity, level);
            class_3222 player = entity instanceof class_3222 sp ? sp : null;
            int fired = 0;

            for (int i = 0; i < candidates.size(); i++) {
               class_2960 id = (class_2960)candidates.get(i);
               if (!container.isSuppressed(id) && !PowerStoragePower.keyMuted(container, id)) {
                  Power loaded = ApoliPowers.get(id);
                  if (loaded != null
                     && (!loaded.condition().isPresent() || ((EntityCondition)loaded.condition().get()).test(ctx))
                     && activate(container, player, id, loaded, key, continuousOnly)) {
                     fired++;
                  }
               }
            }

            return fired;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static boolean activate(
      PowerContainer container, @Nullable class_3222 player, class_2960 id, Power loaded, @Nullable String key, boolean continuousOnly
   ) {
      boolean attributed = PowerCause.push(container.rawOwner(), id);

      try {
         return activateTyped(container, player, id, loaded, key, continuousOnly);
      } finally {
         if (attributed) {
            PowerCause.pop();
         }
      }
   }

   private static boolean activateTyped(
      PowerContainer container, @Nullable class_3222 player, class_2960 id, Power loaded, @Nullable String key, boolean continuousOnly
   ) {
      PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
      Object cfg = loaded.config();
      if (type instanceof MultiplePower && cfg instanceof Cfg multiple) {
         return activateSubPowers(container, player, multiple, key, continuousOnly);
      } else if (type instanceof ActionOnKeyPressPower active && cfg instanceof Config c) {
         if (key != null && !c.key().key().equals(key)) {
            return false;
         }

         if (continuousOnly && !c.key().continuous()) {
            return false;
         }

         if (!active.tryActivate(id, c, container)) {
            return false;
         }

         if (player != null) {
            ApoliNetwork.sendActivated(player, new PowerActivatedS2C(id, PowerResources.cooldownTicks(c.cooldown(), container)));
         }

         return true;
      } else if (type instanceof FireProjectilePower fire && cfg instanceof dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config c) {
         if (key != null && c.params().key().map(Key::key).filter(key::equals).isEmpty()) {
            return false;
         }

         if (continuousOnly && !(Boolean)c.params().key().map(Key::continuous).orElse(false)) {
            return false;
         }

         if (!fire.tryActivate(id, c, container)) {
            return false;
         }

         if (player != null) {
            ApoliNetwork.sendActivated(player, new PowerActivatedS2C(id, PowerResources.cooldownTicks(c.params().cooldown(), container)));
         }

         return true;
      } else if (type instanceof TogglePower && cfg instanceof dev.alphasucks.aea.port.power.builtin.TogglePower.Config c) {
         if (continuousOnly) {
            return false;
         } else {
            return key != null && !c.key().key().equals(key) ? false : TogglePower.toggleByKey(container, id, c);
         }
      } else if (!(type instanceof InventoryPower inventory && cfg instanceof dev.alphasucks.aea.port.power.builtin.InventoryPower.Config c)) {
         return false;
      } else if (continuousOnly) {
         return false;
      } else if (!(player != null && container instanceof PowerContainerImpl impl)) {
         return false;
      } else {
         if (key != null && !c.key().key().equals(key)) {
            return false;
         }

         inventory.open(id, c, player, impl);
         return true;
      }
   }

   private static boolean activateSubPowers(PowerContainer container, @Nullable class_3222 player, Cfg cfg, @Nullable String key, boolean continuousOnly) {
      List<class_2960> subs = cfg.subPowerIds();
      if (subs.isEmpty()) {
         return false;
      } else {
         class_1297 owner = container.rawOwner();
         if (!(owner.method_37908() instanceof class_3218 level)) {
            return false;
         } else {
            EntityCtx var13 = null;
            boolean fired = false;

            for (int i = 0; i < subs.size(); i++) {
               class_2960 subId = (class_2960)subs.get(i);
               if (!container.isSuppressed(subId)) {
                  Power sub = ApoliPowers.get(subId);
                  if (sub != null && !(PowerTypeRegistry.get(sub.typeId()) instanceof MultiplePower)) {
                     if (sub.condition().isPresent()) {
                        if (var13 == null) {
                           var13 = new EntityCtx(owner, level);
                        }

                        if (!((EntityCondition)sub.condition().get()).test(var13)) {
                           continue;
                        }
                     }

                     if (activate(container, player, subId, sub, key, continuousOnly)) {
                        fired = true;
                     }
                  }
               }
            }

            return fired;
         }
      }
   }
}
