package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.data.expr.ExprContext;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ModifyCursorSpeedPower.Multipliers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class ActionOnMouseMovementPower extends PowerType<ActionOnMouseMovementPower.Config> {
   private static final int SLOT_MOUSE_X = ExprContext.slot("mouse_x");
   private static final int SLOT_MOUSE_Y = ExprContext.slot("mouse_y");
   private final Map<ActionOnMouseMovementPower.StateKey, Integer> cooldowns = new HashMap();

   @Override
   public MapCodec<ActionOnMouseMovementPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               EntityAction.CODEC.fieldOf("entity_action").forGetter(ActionOnMouseMovementPower.Config::entityAction),
               Codec.BOOL.optionalFieldOf("up", false).forGetter(ActionOnMouseMovementPower.Config::up),
               Codec.BOOL.optionalFieldOf("down", false).forGetter(ActionOnMouseMovementPower.Config::down),
               Codec.BOOL.optionalFieldOf("left", false).forGetter(ActionOnMouseMovementPower.Config::left),
               Codec.BOOL.optionalFieldOf("right", false).forGetter(ActionOnMouseMovementPower.Config::right),
               Codec.FLOAT.optionalFieldOf("threshold", 0.0F).forGetter(ActionOnMouseMovementPower.Config::threshold),
               Codec.BOOL.optionalFieldOf("ignore_modify_cursor_speed", false).forGetter(ActionOnMouseMovementPower.Config::ignoreModifyCursorSpeed),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(0.0)).forGetter(ActionOnMouseMovementPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnMouseMovementPower.Config::hudRender)
            )
            .apply(i, ActionOnMouseMovementPower.Config::new)
      );
   }

   public void tick(class_2960 powerId, ActionOnMouseMovementPower.Config cfg, PowerContainer holder) {
      ActionOnMouseMovementPower.StateKey key = new ActionOnMouseMovementPower.StateKey(holder.rawOwner().method_5667(), powerId);
      Integer left = (Integer)this.cooldowns.get(key);
      if (left != null && left > 0) {
         this.cooldowns.put(key, left - 1);
      }
   }

   public void onRemoved(class_2960 powerId, ActionOnMouseMovementPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.hasPower(powerId)) {
         this.cooldowns.remove(new ActionOnMouseMovementPower.StateKey(holder.rawOwner().method_5667(), powerId));
      }
   }

   public void forget(UUID entity) {
      this.cooldowns.keySet().removeIf(key -> key.entity().equals(entity));
   }

   public static boolean anyWatching(@Nullable class_1297 entity) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.ACTION_ON_MOUSE_MOVEMENT);

         for (int i = 0; i < powers.size(); i++) {
            if (!container.isSuppressed((class_2960)powers.get(i))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void moved(@Nullable class_1297 entity, float rawYaw, float rawPitch) {
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.powersOfType(ApoliIds.ACTION_ON_MOUSE_MOVEMENT).isEmpty()) {
            if (PowerTypeRegistry.get(ApoliIds.ACTION_ON_MOUSE_MOVEMENT) instanceof ActionOnMouseMovementPower type) {
               Multipliers var11 = ModifyCursorSpeedPower.compute(entity);
               float scaledYaw = (float)(rawYaw * var11.x());
               float scaledPitch = (float)(rawPitch * var11.y());
               EntityCtx ctx = new EntityCtx(entity, level);
               PowerLookup.forEachEntry(
                  entity,
                  ApoliIds.ACTION_ON_MOUSE_MOVEMENT,
                  ActionOnMouseMovementPower.Config.class,
                  (powerId, cfg) -> type.feed(
                     entity,
                     container,
                     powerId,
                     cfg,
                     ctx,
                     cfg.ignoreModifyCursorSpeed() ? rawYaw : scaledYaw,
                     cfg.ignoreModifyCursorSpeed() ? rawPitch : scaledPitch
                  )
               );
            }
         }
      }
   }

   private void feed(
      class_1297 entity, PowerContainer container, class_2960 powerId, ActionOnMouseMovementPower.Config cfg, EntityCtx ctx, float yaw, float pitch
   ) {
      if (cfg.accepts(yaw, pitch)) {
         if (!(cfg.magnitude(yaw, pitch) < cfg.threshold())) {
            ActionOnMouseMovementPower.StateKey key = new ActionOnMouseMovementPower.StateKey(entity.method_5667(), powerId);
            Integer remaining = (Integer)this.cooldowns.get(key);
            if (remaining == null || remaining <= 0) {
               double previousX = ExprContext.push(SLOT_MOUSE_X, yaw);
               double previousY = ExprContext.push(SLOT_MOUSE_Y, pitch);

               try {
                  cfg.entityAction().run(ctx);
               } finally {
                  ExprContext.pop(SLOT_MOUSE_Y, previousY);
                  ExprContext.pop(SLOT_MOUSE_X, previousX);
               }

               int ticks = Math.max(PowerResources.cooldownTicks(cfg.cooldown(), container), 0);
               this.cooldowns.put(key, ticks);
               if (entity instanceof class_3222 player) {
                  ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, ticks));
               }
            }
         }
      }
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnMouseMovementPower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.of(PowerResources.clientCooldown(holder, powerId));
      }

      Integer left = (Integer)this.cooldowns.get(new ActionOnMouseMovementPower.StateKey(owner.method_5667(), powerId));
      return OptionalInt.of(left == null ? 0 : Math.max(0, left));
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnMouseMovementPower.Config cfg, PowerContainer holder, int value) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.empty();
      }

      int clamped = Math.max(0, Math.min(value, Math.max(PowerResources.cooldownTicks(cfg.cooldown(), holder), 0)));
      this.cooldowns.put(new ActionOnMouseMovementPower.StateKey(owner.method_5667(), powerId), clamped);
      if (owner instanceof class_3222 player) {
         ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, clamped));
      }

      return OptionalInt.of(clamped);
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnMouseMovementPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown(), holder), 0) : 0);
   }

   public record Config(
      EntityAction entityAction,
      boolean up,
      boolean down,
      boolean left,
      boolean right,
      float threshold,
      boolean ignoreModifyCursorSpeed,
      Expression cooldown,
      HudRender hudRender
   ) implements HudRendered {
      public boolean anyDirection() {
         return !this.up && !this.down && !this.left && !this.right;
      }

      public boolean accepts(float yaw, float pitch) {
         if (this.anyDirection()) {
            return true;
         } else if (this.right && yaw > 0.0F) {
            return true;
         } else if (this.left && yaw < 0.0F) {
            return true;
         } else {
            return this.down && pitch > 0.0F ? true : this.up && pitch < 0.0F;
         }
      }

      public float magnitude(float yaw, float pitch) {
         if (this.anyDirection()) {
            return Math.max(Math.abs(yaw), Math.abs(pitch));
         }

         float best = 0.0F;
         if (this.right && yaw > 0.0F) {
            best = Math.max(best, yaw);
         }

         if (this.left && yaw < 0.0F) {
            best = Math.max(best, -yaw);
         }

         if (this.down && pitch > 0.0F) {
            best = Math.max(best, pitch);
         }

         if (this.up && pitch < 0.0F) {
            best = Math.max(best, -pitch);
         }

         return best;
      }
   }

   private record StateKey(UUID entity, class_2960 powerId) {
   }
}
