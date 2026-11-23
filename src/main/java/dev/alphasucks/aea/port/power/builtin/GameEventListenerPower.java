package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2806;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_3542;
import net.minecraft.class_4076;
import net.minecraft.class_5698;
import net.minecraft.class_5709;
import net.minecraft.class_5712;
import net.minecraft.class_5714;
import net.minecraft.class_5715;
import net.minecraft.class_5716;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_5712.class_7397;
import org.jetbrains.annotations.Nullable;

public final class GameEventListenerPower extends PowerType<GameEventListenerPower.Config> {
   @Override
   public MapCodec<GameEventListenerPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               GameEventListenerPower.TriggerOrder.CODEC
                  .optionalFieldOf("trigger_order", GameEventListenerPower.TriggerOrder.UNSPECIFIED)
                  .forGetter(GameEventListenerPower.Config::triggerOrder),
               Codec.BOOL.optionalFieldOf("entity", true).forGetter(GameEventListenerPower.Config::entity),
               Codec.BOOL.optionalFieldOf("block", true).forGetter(GameEventListenerPower.Config::block),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(GameEventListenerPower.Config::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(GameEventListenerPower.Config::bientityCondition),
               LoggedOptionalField.of("block_action", BlockAction.CODEC).forGetter(GameEventListenerPower.Config::blockAction),
               LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(GameEventListenerPower.Config::blockCondition),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(1.0)).forGetter(GameEventListenerPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(GameEventListenerPower.Config::hudRender),
               IdCodecs.ID.optionalFieldOf("event").forGetter(GameEventListenerPower.Config::event),
               Codec.list(IdCodecs.ID).optionalFieldOf("events").forGetter(GameEventListenerPower.Config::events),
               IdCodecs.TAG.optionalFieldOf("event_tag").forGetter(GameEventListenerPower.Config::eventTag),
               Codec.BOOL.optionalFieldOf("show_particle", true).forGetter(GameEventListenerPower.Config::showParticle),
               Codec.INT.optionalFieldOf("range", 16).forGetter(GameEventListenerPower.Config::range)
            )
            .apply(i, GameEventListenerPower.Config::new)
      );
   }

   public void onAdded(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder.rawOwner() instanceof class_1309 entity) {
         if (entity.method_37908() instanceof class_3218 serverLevel) {
            GameEventListenerPower.State state = GameEventListenerPower.State.getOrCreate(entity, powerId, cfg);
            state.add(serverLevel);
         }
      }
   }

   public void onRemoved(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder.rawOwner() instanceof class_1309 entity) {
         if (entity.method_37908() instanceof class_3218 serverLevel) {
            GameEventListenerPower.State state = GameEventListenerPower.State.get(entity, powerId);
            if (state != null) {
               state.remove(serverLevel);
               GameEventListenerPower.State.remove(entity, powerId);
            }
         }
      }
   }

   public void onSuppressed(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder) {
      this.onRemoved(powerId, cfg, holder, powerId);
   }

   public void onUnsuppressed(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder) {
      this.onAdded(powerId, cfg, holder, powerId);
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder) {
      return PowerResources.readDeadline(holder, powerId);
   }

   public OptionalInt writeResource(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder, int value) {
      return PowerResources.writeDeadline(holder, powerId, value, PowerResources.cooldownTicks(cfg.cooldown(), holder));
   }

   public OptionalInt resourceBound(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown(), holder), 0) : 0);
   }

   public void tick(class_2960 powerId, GameEventListenerPower.Config cfg, PowerContainer holder) {
      if (holder.rawOwner() instanceof class_1309 entity) {
         class_1937 var8 = entity.method_37908();
         GameEventListenerPower.State state = GameEventListenerPower.State.get(entity, powerId);
         if (state == null) {
            if (!(var8 instanceof class_3218 serverLevel)) {
               return;
            }

            state = GameEventListenerPower.State.getOrCreate(entity, powerId, cfg);
            state.add(serverLevel);
         }

         state.tick(var8);
      }
   }

   public record Config(
      GameEventListenerPower.TriggerOrder triggerOrder,
      boolean entity,
      boolean block,
      Optional<BiEntityAction> bientityAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<BlockAction> blockAction,
      Optional<BlockCondition> blockCondition,
      Expression cooldown,
      HudRender hudRender,
      Optional<class_2960> event,
      Optional<List<class_2960>> events,
      Optional<class_2960> eventTag,
      boolean showParticle,
      int range
   ) implements HudRendered {
   }

   public static final class State {
      private final class_5715<class_5714> eventHandler;
      private final class_2960 powerId;
      private final GameEventListenerPower.Config cfg;
      private final class_1297 entity;
      @Nullable
      private final class_6862<class_5712> resolvedTag;
      private boolean registered;

      State(GameEventListenerPower.Config cfg, class_1297 entity, class_2960 powerId) {
         this.cfg = cfg;
         this.entity = entity;
         this.powerId = powerId;
         this.resolvedTag = cfg.eventTag().isPresent() ? class_6862.method_40092(class_7924.field_41273, (class_2960)cfg.eventTag().get()) : null;
         this.eventHandler = new class_5715<>(new class_5714() {
            @Override
            public class_5716 method_32946() {
               return new class_5709(entity, entity.method_18381(entity.method_18376()));
            }

            @Override
            public int method_32948() {
               return Math.max(cfg.range(), 1);
            }

            @Override
            public boolean method_32947(class_3218 level, class_5712 gameEvent, class_7397 context, class_243 pos) {
               return State.this.handle(level, pos, gameEvent, context);
            }
         });
      }

      private boolean handle(class_3218 level, class_243 pos, class_5712 gameEvent, class_7397 context) {
         if (!this.onCooldown(level)) {
            return false;
         }

         if (!this.cfg.entity() && context.comp_713() != null) {
            return false;
         }

         if (!this.cfg.block() && context.comp_713() == null) {
            return false;
         }

         if (this.cfg.event().isPresent()) {
            class_5712 configured = class_7923.field_41171.method_10223((class_2960)this.cfg.event().get());
            if (gameEvent != configured) {
               return false;
            }
         }

         if (this.cfg.events().isPresent() && !((List)this.cfg.events().get()).isEmpty()) {
            boolean matches = ((List<class_2960>)this.cfg.events().get()).stream().anyMatch(id -> class_7923.field_41171.method_10223(id) == gameEvent);
            if (!matches) {
               return false;
            }
         }

         if (this.resolvedTag != null && !gameEvent.method_40156(this.resolvedTag)) {
            return false;
         }

         class_1297 src = context.comp_713();
         if (src != null) {
            if (src.method_7325()) {
               return false;
            }

            if (src.method_21749() && gameEvent.method_40156(class_5698.field_28091)) {
               return false;
            }

            if (src.method_33189()) {
               return false;
            }
         }

         if (context.comp_714() != null && context.comp_714().method_26164(class_3481.field_38835)) {
            return false;
         }

         if (this.cfg.bientityCondition().isPresent()
            && context.comp_713() instanceof class_1309 actor
            && !((BiEntityCondition)this.cfg.bientityCondition().get()).test(new BiEntityCtx(actor, (class_1309)this.entity, level))) {
            return false;
         }

         if (this.cfg.blockCondition().isPresent() && context.comp_713() == null) {
            class_2338 blockPos = class_2338.method_49638(pos);
            if (!((BlockCondition)this.cfg.blockCondition().get()).test(new BlockCtx(blockPos, level.method_8320(blockPos), level))) {
               return false;
            }
         }

         this.setCooldown(level);
         class_1297 actionSource = context.comp_713();
         if (actionSource instanceof class_1309 actor && this.entity instanceof class_1309 target) {
            this.cfg.bientityAction().ifPresent(a -> a.run(new BiEntityCtx(actor, target, level)));
         }

         this.cfg
            .blockAction()
            .ifPresent(a -> a.run(new BlockCtx(class_2338.method_49638(pos), level.method_8320(class_2338.method_49638(pos)), level, actionSource)));
         return true;
      }

      void add(class_3218 level) {
         if (!this.registered && this.chunkReady(level)) {
            this.eventHandler.method_42335(level);
            this.registered = true;
         }
      }

      void remove(class_3218 level) {
         if (this.registered) {
            this.eventHandler.method_32949(level);
            this.registered = false;
         }
      }

      void tick(class_1937 level) {
         if (level instanceof class_3218 serverLevel) {
            if (!this.registered) {
               this.add(serverLevel);
            } else {
               this.eventHandler.method_32952(serverLevel);
            }
         }
      }

      private boolean chunkReady(class_3218 level) {
         return level.method_8402(
               class_4076.method_18675(this.entity.method_31477()), class_4076.method_18675(this.entity.method_31479()), class_2806.field_12803, false
            )
            != null;
      }

      public class_5715<class_5714> getEventHandler() {
         return this.eventHandler;
      }

      private boolean onCooldown(class_3218 level) {
         PowerContainer container = PowerContainer.of(this.entity);
         return container == null ? true : container.getAuxInt(this.powerId).orElse(0) <= level.method_8510();
      }

      private void setCooldown(class_3218 level) {
         if (PowerContainer.of(this.entity) instanceof PowerContainerImpl impl) {
            impl.setAuxInt(this.powerId, (int)(level.method_8510() + PowerResources.cooldownTicks(this.cfg.cooldown(), impl)));
         }
      }

      public static GameEventListenerPower.State getOrCreate(class_1297 entity, class_2960 powerId, GameEventListenerPower.Config cfg) {
         Map<class_2960, GameEventListenerPower.State> map = EntityGameEventListenerAccess.getMap(entity);
         if (map == null) {
            map = new HashMap();
            EntityGameEventListenerAccess.setMap(entity, map);
         }

         return (GameEventListenerPower.State)map.computeIfAbsent(powerId, id -> new GameEventListenerPower.State(cfg, entity, id));
      }

      @Nullable
      public static GameEventListenerPower.State get(class_1297 entity, class_2960 powerId) {
         Map<class_2960, GameEventListenerPower.State> map = EntityGameEventListenerAccess.getMap(entity);
         return map != null ? (GameEventListenerPower.State)map.get(powerId) : null;
      }

      @Nullable
      public static GameEventListenerPower.State remove(class_1297 entity, class_2960 powerId) {
         Map<class_2960, GameEventListenerPower.State> map = EntityGameEventListenerAccess.getMap(entity);
         if (map == null) {
            return null;
         }

         GameEventListenerPower.State removed = (GameEventListenerPower.State)map.remove(powerId);
         if (map.isEmpty()) {
            EntityGameEventListenerAccess.setMap(entity, null);
         }

         return removed;
      }
   }

   public enum TriggerOrder implements class_3542 {
      BY_DISTANCE("by_distance"),
      UNSPECIFIED("unspecified");

      public static final Codec<GameEventListenerPower.TriggerOrder> CODEC = class_3542.method_28140(GameEventListenerPower.TriggerOrder::values);
      private final String name;

      TriggerOrder(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
