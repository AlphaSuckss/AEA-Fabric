package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.data.ScrollDirection;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class ActionOnScrollWheelPower extends PowerType<ActionOnScrollWheelPower.Config> {
   private final Map<ActionOnScrollWheelPower.StateKey, ActionOnScrollWheelPower.Progress> progress = new HashMap();

   @Override
   public MapCodec<ActionOnScrollWheelPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               EntityAction.CODEC.fieldOf("entity_action").forGetter(ActionOnScrollWheelPower.Config::entityAction),
               ScrollDirection.CODEC.optionalFieldOf("direction", ScrollDirection.ANY).forGetter(ActionOnScrollWheelPower.Config::direction),
               Codec.INT.optionalFieldOf("scroll_amount", 1).forGetter(ActionOnScrollWheelPower.Config::scrollAmount),
               Codec.INT.optionalFieldOf("within_ticks", 10).forGetter(ActionOnScrollWheelPower.Config::withinTicks),
               Codec.BOOL.optionalFieldOf("reset_on_direction_change", true).forGetter(ActionOnScrollWheelPower.Config::resetOnDirectionChange),
               Codec.BOOL.optionalFieldOf("prevent_hotbar_change", false).forGetter(ActionOnScrollWheelPower.Config::preventHotbarChange),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(0.0)).forGetter(ActionOnScrollWheelPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnScrollWheelPower.Config::hudRender)
            )
            .apply(i, ActionOnScrollWheelPower.Config::new)
      );
   }

   public void tick(class_2960 powerId, ActionOnScrollWheelPower.Config cfg, PowerContainer holder) {
      ActionOnScrollWheelPower.Progress state = (ActionOnScrollWheelPower.Progress)this.progress
         .get(new ActionOnScrollWheelPower.StateKey(holder.rawOwner().method_5667(), powerId));
      if (state != null) {
         if (state.cooldown > 0) {
            state.cooldown--;
         }

         if (state.notches > 0) {
            if (cfg.withinTicks > 0) {
               state.idle++;
               if (state.idle >= cfg.withinTicks) {
                  state.notches = 0;
                  state.idle = 0;
                  state.last = null;
               }
            }
         }
      }
   }

   public void onRemoved(class_2960 powerId, ActionOnScrollWheelPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.hasPower(powerId)) {
         this.progress.remove(new ActionOnScrollWheelPower.StateKey(holder.rawOwner().method_5667(), powerId));
      }
   }

   public void forget(UUID entity) {
      this.progress.keySet().removeIf(key -> key.entity().equals(entity));
   }

   public static boolean anyAccepting(@Nullable class_1297 entity, ScrollDirection notch) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.ACTION_ON_SCROLL_WHEEL);

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ActionOnScrollWheelPower.Config cfg && cfg.direction.accepts(notch)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static int scroll(@Nullable class_1297 entity, ScrollDirection notch, int notches) {
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         PowerContainer container = PowerContainer.of(entity);
         if (container == null || container.isEmpty()) {
            return 0;
         }

         if (container.powersOfType(ApoliIds.ACTION_ON_SCROLL_WHEEL).isEmpty()) {
            return 0;
         }

         ActionOnScrollWheelPower type = PowerTypeRegistry.get(ApoliIds.ACTION_ON_SCROLL_WHEEL) instanceof ActionOnScrollWheelPower t ? t : null;
         if (type == null) {
            return 0;
         }

         EntityCtx ctx = new EntityCtx(entity, level);
         int[] fired = new int[]{0};
         BiConsumer<class_2960, ActionOnScrollWheelPower.Config> feeder = (powerId, cfg) -> {
            if (type.feed(entity, container, powerId, cfg, ctx, notch, notches)) {
               fired[0]++;
            }
         };
         PowerLookup.forEachEntry(entity, ApoliIds.ACTION_ON_SCROLL_WHEEL, ActionOnScrollWheelPower.Config.class, feeder);
         return fired[0];
      } else {
         return 0;
      }
   }

   private boolean feed(
      class_1297 entity, PowerContainer container, class_2960 powerId, ActionOnScrollWheelPower.Config cfg, EntityCtx ctx, ScrollDirection notch, int notches
   ) {
      if (!cfg.direction.accepts(notch)) {
         return false;
      }

      ActionOnScrollWheelPower.StateKey key = new ActionOnScrollWheelPower.StateKey(entity.method_5667(), powerId);
      ActionOnScrollWheelPower.Progress state = (ActionOnScrollWheelPower.Progress)this.progress
         .computeIfAbsent(key, k -> new ActionOnScrollWheelPower.Progress());
      if (state.cooldown > 0) {
         return false;
      }

      if (cfg.resetOnDirectionChange && state.last != null && state.last != notch) {
         state.notches = 0;
      }

      state.last = notch;
      state.idle = 0;
      state.notches = state.notches + Math.max(1, notches);
      int needed = Math.max(1, cfg.scrollAmount);
      if (state.notches < needed) {
         return false;
      }

      state.notches = 0;
      state.last = null;
      cfg.entityAction.run(ctx);
      int ticks = Math.max(PowerResources.cooldownTicks(cfg.cooldown, container), 0);
      state.cooldown = ticks;
      if (entity instanceof class_3222 player) {
         ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, ticks));
      }

      return true;
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnScrollWheelPower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.of(PowerResources.clientCooldown(holder, powerId));
      }

      ActionOnScrollWheelPower.Progress state = (ActionOnScrollWheelPower.Progress)this.progress
         .get(new ActionOnScrollWheelPower.StateKey(owner.method_5667(), powerId));
      return OptionalInt.of(state == null ? 0 : Math.max(0, state.cooldown));
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnScrollWheelPower.Config cfg, PowerContainer holder, int value) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.empty();
      }

      int clamped = Math.max(0, Math.min(value, Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0)));
      ((ActionOnScrollWheelPower.Progress)this.progress
            .computeIfAbsent(new ActionOnScrollWheelPower.StateKey(owner.method_5667(), powerId), k -> new ActionOnScrollWheelPower.Progress()))
         .cooldown = clamped;
      if (owner instanceof class_3222 player) {
         ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, clamped));
      }

      return OptionalInt.of(clamped);
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnScrollWheelPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0) : 0);
   }

   public record Config(
      EntityAction entityAction,
      ScrollDirection direction,
      int scrollAmount,
      int withinTicks,
      boolean resetOnDirectionChange,
      boolean preventHotbarChange,
      Expression cooldown,
      HudRender hudRender
   ) implements HudRendered {
   }

   private static final class Progress {
      int notches;
      int idle;
      int cooldown;
      ScrollDirection last;
   }

   private record StateKey(UUID entity, class_2960 powerId) {
   }
}
