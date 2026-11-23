package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.data.expr.ExprContext;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;

public final class ActionOnKeyPressPower extends PowerType<ActionOnKeyPressPower.Config> {
   private static final int SLOT_HELD_TICKS = ExprContext.slot("held_ticks");
   private final Map<ActionOnKeyPressPower.CooldownKey, Integer> cooldowns = new HashMap();
   private final Map<ActionOnKeyPressPower.CooldownKey, Long> pressedAt = new HashMap();

   @Override
   public MapCodec<ActionOnKeyPressPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               EntityAction.CODEC.fieldOf("entity_action").forGetter(ActionOnKeyPressPower.Config::entityAction),
               LoggedOptionalField.of("entity_action_released", EntityAction.CODEC).forGetter(ActionOnKeyPressPower.Config::entityActionReleased),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(1.0)).forGetter(ActionOnKeyPressPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnKeyPressPower.Config::hudRender),
               Key.CODEC.optionalFieldOf("key", Key.DEFAULT_PRIMARY).forGetter(ActionOnKeyPressPower.Config::key)
            )
            .apply(i, ActionOnKeyPressPower.Config::new)
      );
   }

   public void tick(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder) {
      if (!this.cooldowns.isEmpty()) {
         ActionOnKeyPressPower.CooldownKey key = new ActionOnKeyPressPower.CooldownKey(holder.rawOwner().method_5667(), powerId);
         Integer remaining = (Integer)this.cooldowns.get(key);
         if (remaining != null) {
            if (remaining <= 1) {
               this.cooldowns.remove(key);
            } else {
               this.cooldowns.put(key, remaining - 1);
            }
         }
      }
   }

   public void tickStored(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder) {
      this.tick(powerId, cfg, holder);
   }

   public void onRemoved(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.allPowers().contains(powerId)) {
         ActionOnKeyPressPower.CooldownKey key = new ActionOnKeyPressPower.CooldownKey(holder.rawOwner().method_5667(), powerId);
         this.cooldowns.remove(key);
         this.pressedAt.remove(key);
      }
   }

   public boolean tryActivate(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908() instanceof class_3218 level) {
         ActionOnKeyPressPower.CooldownKey var8 = new ActionOnKeyPressPower.CooldownKey(owner.method_5667(), powerId);
         if ((Integer)this.cooldowns.getOrDefault(var8, 0) > 0) {
            return false;
         }

         cfg.entityAction.run(new EntityCtx(owner, level));
         int ticks = PowerResources.cooldownTicks(cfg.cooldown, holder);
         if (ticks > 0) {
            this.cooldowns.put(var8, ticks);
         }

         if (cfg.releases()) {
            this.pressedAt.putIfAbsent(var8, level.method_8510());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean release(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder, boolean run) {
      if (this.pressedAt.isEmpty()) {
         return false;
      } else {
         class_1297 owner = holder.rawOwner();
         if (!(owner.method_37908() instanceof class_3218 level)) {
            return false;
         } else {
            Long pressed = (Long)this.pressedAt.remove(new ActionOnKeyPressPower.CooldownKey(owner.method_5667(), powerId));
            if (pressed != null && run && !cfg.entityActionReleased.isEmpty()) {
               double previous = ExprContext.push(SLOT_HELD_TICKS, Math.max(0L, level.method_8510() - pressed));
               boolean attributed = PowerCause.push(owner, powerId);

               try {
                  ((EntityAction)cfg.entityActionReleased.get()).run(new EntityCtx(owner, level));
               } finally {
                  if (attributed) {
                     PowerCause.pop();
                  }

                  ExprContext.pop(SLOT_HELD_TICKS, previous);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   public List<class_2960> pressedPowers(UUID entity) {
      if (this.pressedAt.isEmpty()) {
         return List.of();
      }

      List<class_2960> out = null;

      for (ActionOnKeyPressPower.CooldownKey key : this.pressedAt.keySet()) {
         if (key.entity().equals(entity)) {
            if (out == null) {
               out = new ArrayList(2);
            }

            out.add(key.powerId());
         }
      }

      return out == null ? List.of() : out;
   }

   public void forget(UUID entity) {
      if (!this.pressedAt.isEmpty()) {
         this.pressedAt.keySet().removeIf(key -> key.entity().equals(entity));
      }
   }

   public int getCooldown(class_1309 owner, class_2960 powerId) {
      return (Integer)this.cooldowns.getOrDefault(new ActionOnKeyPressPower.CooldownKey(owner.method_5667(), powerId), 0);
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      return owner.method_37908().method_8608()
         ? OptionalInt.of(PowerResources.clientCooldown(holder, powerId))
         : OptionalInt.of((Integer)this.cooldowns.getOrDefault(new ActionOnKeyPressPower.CooldownKey(owner.method_5667(), powerId), 0));
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder, int value) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.empty();
      }

      int clamped = Math.max(0, Math.min(value, Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0)));
      ActionOnKeyPressPower.CooldownKey key = new ActionOnKeyPressPower.CooldownKey(owner.method_5667(), powerId);
      if (clamped <= 0) {
         this.cooldowns.remove(key);
      } else {
         this.cooldowns.put(key, clamped);
      }

      if (owner instanceof class_3222 player) {
         ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, clamped));
      }

      return OptionalInt.of(clamped);
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnKeyPressPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0) : 0);
   }

   public record Config(EntityAction entityAction, Optional<EntityAction> entityActionReleased, Expression cooldown, HudRender hudRender, Key key)
      implements HudRendered {
      public boolean releases() {
         return this.entityActionReleased.isPresent();
      }
   }

   private record CooldownKey(UUID entity, class_2960 powerId) {
   }
}
